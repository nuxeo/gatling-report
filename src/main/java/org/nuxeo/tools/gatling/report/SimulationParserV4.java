/*
 * (C) Copyright 2015-2026 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     bdelbosc
 */
package org.nuxeo.tools.gatling.report;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import io.gatling.charts.stats.LogFileParser;

/**
 * Gatling 3.13+ {@code simulation.log} binary format.
 * <p>
 * Starting with Gatling 3.13, {@code simulation.log} is no longer a tab-separated text file: it is a custom binary
 * format (see the "Requiring --add-opens..." note in
 * <a href="https://docs.gatling.io/release-notes/gatling/upgrading/3.12-to-3.13/">the 3.12 to 3.13 upgrade guide</a>,
 * which is actually about this report generation rework). There is no published spec for this format.
 * <p>
 * Rather than reverse-engineering and duplicating the binary layout by hand, this class extends Gatling's own
 * {@code io.gatling.charts.stats.LogFileParser}, an Apache-2.0 licensed class (shipped in the {@code gatling-charts}
 * artifact) that already exposes the low level read primitives Gatling itself uses to read this file (cached/
 * sanitized strings, ints, longs, ...). We only reimplement the higher level RUN/USER/REQUEST/GROUP/ERROR record
 * dispatch, based on {@code io.gatling.charts.stats.LogFileReader} /
 * {@code io.gatling.core.stats.writer.RecordHeader} in Gatling's source
 * (https://github.com/gatling/gatling/blob/main/gatling-charts/src/main/scala/io/gatling/charts/stats/LogFileReader.scala),
 * and feed the resulting events straight into a {@link SimulationContext}, exactly like the legacy text-based
 * {@link SimulationParser} implementations do.
 * <p>
 * Known limitation: unlike the old text format, individual REQUEST records in the binary format no longer carry a
 * scenario/user id (only USER records do). Gatling's own reports never needed to break down requests per scenario,
 * so this information isn't there anymore. Since every Nuxeo benchmark simulation only ever declares a single
 * {@code scenario(...)}, this parser assigns every request to the (first) declared scenario, which is correct for
 * that case but would be inaccurate for a simulation.log with more than one scenario mixing distinct requests.
 *
 * @since 6.4
 */
public class SimulationParserV4 extends LogFileParser<SimulationContext> implements Parser {

    protected static final byte RUN = 0;

    protected static final byte REQUEST = 1;

    protected static final byte USER = 2;

    protected static final byte GROUP = 3;

    protected static final byte ERROR = 4;

    protected final File file;

    protected final Float apdexT;

    /** Decompressed copy of {@link #file} when it's gzip-compressed, deleted once {@link #parse()} completes. */
    protected final File tempFile;

    protected String[] scenarios;

    protected long runStart;

    public SimulationParserV4(File file, Float apdexT) {
        this(file, apdexT, Utils.GZ.equalsIgnoreCase(Utils.getFileExtension(file)) ? decompress(file) : null);
    }

    private SimulationParserV4(File file, Float apdexT, File tempFile) {
        super(tempFile != null ? tempFile : file);
        this.file = file;
        this.apdexT = apdexT;
        this.tempFile = tempFile;
    }

    public SimulationParserV4(File file) {
        this(file, null);
    }

    /**
     * {@code LogFileParser}'s constructor always opens the given {@link File} as a raw (non-gzip) input stream, so
     * gzip-compressed logs need to be decompressed to a temporary file first.
     */
    protected static File decompress(File gzFile) {
        try {
            File tmp = File.createTempFile("gatling-report-", ".log");
            tmp.deleteOnExit();
            try (InputStream in = new GZIPInputStream(new FileInputStream(gzFile));
                    OutputStream out = new FileOutputStream(tmp)) {
                byte[] buffer = new byte[8192];
                int n;
                while ((n = in.read(buffer)) != -1) {
                    out.write(buffer, 0, n);
                }
            }
            return tmp;
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot decompress " + gzFile, e);
        }
    }

    @Override
    public SimulationContext parse() {
        SimulationContext ctx = new SimulationContext(file.getAbsolutePath(), apdexT);
        try {
            parseRunRecord(ctx);
            int count = 0;
            int header;
            while ((header = read()) != -1) {
                count++;
                try {
                    switch ((byte) header) {
                    case USER:
                        parseUserRecord(ctx);
                        break;
                    case REQUEST:
                        parseRequestRecord(ctx);
                        break;
                    case GROUP:
                        skipGroupRecord();
                        break;
                    case ERROR:
                        skipErrorRecord();
                        break;
                    default:
                        throw new IllegalArgumentException(String.format(
                                "Unsupported record header %d after %d records in %s", header, count, file));
                    }
                } catch (Exception e) {
                    if (e instanceof EOFException) {
                        // The last record can be truncated if it was still being flushed when the run ended: this
                        // is tolerated by Gatling's own reader (see FirstPassParser/SecondPassParser in
                        // gatling-charts), just stop here with whatever was read so far instead of failing the
                        // whole parsing.
                        break;
                    }
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    }
                    // LogFileParser's read primitives don't declare checked exceptions in their bytecode signature
                    // (Scala has no checked exceptions), so any other IOException reaches us as an undeclared
                    // checked exception: wrap it so this method can keep matching LogFileParser#parse()'s
                    // no-throws contract.
                    throw new IllegalStateException("Error parsing " + file + " at record " + count, e);
                }
            }
        } finally {
            close();
            if (tempFile != null) {
                tempFile.delete();
            }
        }
        ctx.computeStat();
        return ctx;
    }

    protected void parseRunRecord(SimulationContext ctx) {
        int header = read();
        if (header != RUN) {
            throw new IllegalArgumentException(
                    String.format("Invalid simulation file: %s, expecting a Gatling 3.13+ binary format", file));
        }
        // gatlingVersion, unused: no need to be strict on the exact version like Gatling's own reader is
        readString();
        String simulationClassName = readString();
        runStart = readLong();
        // runDescription, unused
        readString();
        int scenarioCount = readInt();
        scenarios = new String[scenarioCount];
        for (int i = 0; i < scenarioCount; i++) {
            scenarios[i] = readSanitizedString();
        }
        int assertionCount = readInt();
        for (int i = 0; i < assertionCount; i++) {
            // pickled Assertion bytes, unused by gatling-report
            readByteArray();
        }

        ctx.setSimulationName(simulationName(simulationClassName));
        ctx.setScenarioName(simulationClassName);
        ctx.setStart(runStart);
    }

    /**
     * The short simulation id (e.g. {@code sim20createdocuments}) used to be stored in the RUN record's
     * {@code simulationId} field in older Gatling versions. It is no longer written to the log at all since 3.13
     * (Gatling only keeps the simulation class name). Gatling itself derives that short id the same way, to name
     * result directories (<code>&lt;simulationId&gt;-&lt;timestamp&gt;</code>, e.g.
     * {@code sim20createdocuments-20260804133135206} for {@code org.nuxeo.cap.bench.Sim20CreateDocuments}): the
     * simple class name, lowercased. Reproduce that instead of relying on the log file's parent directory being
     * named that way, which only holds for genuine Gatling result folders (not e.g. a test resource file).
     */
    protected String simulationName(String simulationClassName) {
        int dot = simulationClassName.lastIndexOf('.');
        String simpleName = dot >= 0 ? simulationClassName.substring(dot + 1) : simulationClassName;
        return simpleName.toLowerCase(Locale.ROOT);
    }

    protected void parseUserRecord(SimulationContext ctx) {
        int scenarioIndex = readInt();
        boolean isStart = readBoolean();
        // timestamp, unused: SimulationContext only tracks concurrency counts, not per-event dates
        readInt();
        String scenario = scenarios.length > scenarioIndex ? scenarios[scenarioIndex] : null;
        if (isStart) {
            ctx.addUser(scenario);
        } else {
            ctx.endUser(scenario);
        }
    }

    protected void parseRequestRecord(SimulationContext ctx) {
        int groupsSize = readInt();
        for (int i = 0; i < groupsSize; i++) {
            // group path, unused: gatling-report doesn't break down stats per group
            readCachedSanitizedString();
        }
        String name = readCachedSanitizedString();
        long start = runStart + readInt();
        long end = runStart + readInt();
        boolean success = readBoolean();
        // error message, unused
        readCachedSanitizedString();

        // Requests are no longer tied to a scenario in the binary format, see the class Javadoc.
        String scenario = scenarios.length > 0 ? scenarios[0] : SimulationContext.ALL_REQUESTS;
        ctx.addRequest(scenario, name, start, end, success);
    }

    protected void skipGroupRecord() {
        int groupsSize = readInt();
        for (int i = 0; i < groupsSize; i++) {
            skipCachedString();
        }
        skipInt(); // start
        skipInt(); // end
        skipInt(); // cumulatedResponseTime
        skipByte(); // status
    }

    protected void skipErrorRecord() {
        skipCachedString(); // message
        skipInt(); // timestamp
    }

}
