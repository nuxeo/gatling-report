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

import com.beust.jcommander.JCommander;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class App implements Runnable {
    private final static Logger log = Logger.getLogger(App.class);
    private static final String PROGRAM_NAME = "java -jar gatling-report.jar";
    private final Options options;
    private List<SimulationContext> stats;

    public App(String[] args) {
        options = new Options();
        JCommander command = new JCommander(options, args);
        command.setProgramName(PROGRAM_NAME);
        if (options.help) {
            command.usage();
            System.exit(0);
        }
    }

    @Override
    public void run() {
        parseSimulationFiles();
        render();
    }

    private void parseSimulationFiles() {
        List<File> files = new ArrayList<>(options.simulations.size());
        stats = new ArrayList<>(files.size());
        options.simulations.forEach(simulation -> parseSimulationFile(new File(simulation)));
    }

    private void parseSimulationFile(File file) {
        log.info("Parsing " + file.getAbsolutePath());
        try {
            stats.add(new SimulationParser(file, options.apdexT, options.normalised).parse());
        } catch (IOException e) {
            log.error("Invalid file: " + file.getAbsolutePath(), e);
        }
    }

    private void render() {
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

    private void renderAsReport() throws IOException {
        File dir = new File(options.outputDirectory);
        if (!dir.mkdirs()) {
            if (!options.force) {
                log.error("Abort, report directory already exists, use -f to override.");
                System.exit(-2);
            }
            log.warn("Overriding existing report directory" + options.outputDirectory);
        }
        String reportPath = new Report(stats, options.normalised)
                .setOutputDirectory(dir)
                .includeJs(options.includeJs)
                .setTemplate(options.template)
                .includeGraphite(options.graphiteUrl, options.user, options.password, options.getZoneId())
                .yamlReport(options.yaml)
                .withMap(options.map)
                .setFilename(options.outputName)
                .create();
        log.info("Report generated: " + reportPath);
    }

    private void renderAsCsv() {
        System.out.println(RequestStat.header());
        stats.forEach(System.out::println);
    }

    public static void main(String args[]) {
        (new Thread(new App(args))).start();
    }
}
