/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Benoit Delbosc
 */
package org.nuxeo.tools.gatling.report;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.beust.jcommander.JCommander;

public class App implements Runnable {
    protected static final String PROGRAM_NAME = "java -jar gatling-report.jar";

    private final static Logger log = Logger.getLogger(App.class);

    protected final Options options;

    protected List<SimulationContext> stats;

    public App(String[] args) {
        options = new Options();
        JCommander command = new JCommander(options, args);
        command.setProgramName(PROGRAM_NAME);
        if (options.help) {
            command.usage();
            System.exit(0);
        }
    }

    public static void main(String args[]) {
        (new Thread(new App(args))).start();
    }

    @Override
    public void run() {
        parseSimulationFiles();
        render();
    }

    protected void parseSimulationFiles() {
        stats = new ArrayList<>(options.simulations.size());
        options.simulations.forEach(simulation -> parseSimulationFile(new File(simulation)));
    }

    protected void parseSimulationFile(File file) {
        log.info("Parsing " + file.getAbsolutePath());
        try {
            SimulationParser parser = ParserFactory.getParser(file, options.apdexT);
            stats.add(parser.parse());
        } catch (IOException e) {
            log.error("Invalid file: " + file.getAbsolutePath(), e);
        }
    }

    protected void render() {
        if (options.outputDirectory == null) {
            renderAsCsv();
        } else {
            try {
                renderAsReport();
            } catch (IOException e) {
                log.error("Can not generate report", e);
            }
        }
    }

    protected void renderAsReport() throws IOException {
        File dir = new File(options.outputDirectory);
        if (!dir.mkdirs()) {
            if (!options.force) {
                log.error("Abort, report directory already exists, use -f to override.");
                System.exit(-2);
            }
            log.warn("Overriding existing report directory" + options.outputDirectory);
        }
        String reportPath = new Report(stats).setOutputDirectory(dir)
                                             .includeJs(options.includeJs)
                                             .setTemplate(options.template)
                                             .includeGraphite(options.graphiteUrl, options.user, options.password,
                                                     options.getZoneId())
                                             .yamlReport(options.yaml)
                                             .withMap(options.map)
                                             .setFilename(options.outputName)
                                             .create();
        log.info("Report generated: " + reportPath);
    }

    protected void renderAsCsv() {
        System.out.println(RequestStat.header());
        stats.forEach(System.out::println);
    }
}
