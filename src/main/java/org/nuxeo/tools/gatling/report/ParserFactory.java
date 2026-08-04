/*
 * (C) Copyright 2015-2026 Nuxeo (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Kris Geusebroek
 */
package org.nuxeo.tools.gatling.report;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class ParserFactory {

    public static Parser getParser(File file, Float apdexT) throws IOException {
        return getVersionSpecificParser(file, apdexT);
    }

    public static Parser getParser(File file) throws IOException {
        return getVersionSpecificParser(file, null);
    }

    protected static Parser getVersionSpecificParser(File file, Float apdexT) throws IOException {
        if (isBinaryFormat(file)) {
            // Gatling 3.13+ simulation.log format
            return new SimulationParserV4(file, apdexT);
        }
        List<String> header = getHeaderLine(file);
        if (header.size() == 6) {
            String version = header.get(5);
            if (version.startsWith("2.")) {
                return new SimulationParserV2(file, apdexT);
            }
            if (version.equals("3.0") || version.startsWith("3.0.")) {
                return new SimulationParserV3(file, apdexT);
            }
            if ("3.2".equals(version) || version.startsWith("3.2.")
                    || ("3.3".equals(version) || version.startsWith("3.3."))) {
                return new SimulationParserV32(file, apdexT);
            }
            if (version.equals("3.4") || version.startsWith("3.4.")) {
                return new SimulationParserV34(file, apdexT);
            }
            // 3.5 and above
            if (version.startsWith("3.")) {
                return new SimulationParserV35(file, apdexT);
            }
        } else if (header.size() == 7) {
            String version = header.get(6);
            if (version.startsWith("2.")) {
                return new SimulationParserV23(file, apdexT);
            }
        }
        throw new IllegalArgumentException("Unknown Gatling simulation version: " + header);
    }

    protected static List<String> getHeaderLine(File file) throws IOException {
        try (SimulationReader reader = new SimulationReader(file)) {
            return reader.readNext();
        }
    }

    /**
     * Gatling 3.13+ binary format starts with a single {@code 0x00} byte (the RUN record header), whereas every
     * previous text format starts with the ASCII line {@code RUN\t...}.
     */
    protected static boolean isBinaryFormat(File file) throws IOException {
        try (Reader reader = Utils.getReaderFor(file)) {
            return reader.read() == 0;
        }
    }

}
