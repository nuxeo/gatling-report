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
    private final Options options;
    private final JCommander command;

    public App(String[] args) {
        options = new Options();
        command = new JCommander(options, args);
        command.setProgramName("java -jar gatling-report.jar");
        if (options.help) {
            command.usage();
            System.exit(0);
        }
    }

    @Override
    public void run() {
        List<SimulationContext> stats = getSimulationStats();
        render(stats, options.outputDirectory);
    }

    private List<SimulationContext> getSimulationStats() {
        List<File> files = new ArrayList<>(options.simulations.size());
        List<SimulationContext> stats = new ArrayList<>(files.size());
        options.simulations.forEach(simulation -> files.add(new File(simulation)));
        for (File file : files) {
            log.info("Parsing " + file.getAbsolutePath());
            try {
                stats.add(new SimulationParser(file).parse());
            } catch (IOException e) {
                log.error("Invalid file: " + file.getAbsolutePath(), e);
            }
        }
        return stats;
    }

    private void render(List<SimulationContext> stats, String outputDirectory) {
        if (outputDirectory == null) {
            renderAsCsv(stats);
        } else {
            try {
                renderAsReport(stats, outputDirectory);
            } catch (IOException e) {
                log.error("Can not generate report", e);
            }
        }
    }

    private void renderAsReport(List<SimulationContext> stats, String outputDirectory) throws IOException {
        File dir = new File(outputDirectory);
        if (!dir.mkdirs()) {
            if (!options.force) {
                log.error("Abort, report direcotry already exists, use -f to override.");
                System.exit(-2);
            }
            log.warn("Overriding existing report directory" + outputDirectory);
        }
        String reportPath = new Report(stats).setOutputDirectory(dir).includeJs(options.includeJs).create();
        log.info("Report generated: " + reportPath);
    }

    private void renderAsCsv(List<SimulationContext> stats) {
        System.out.println(RequestStat.header());
        stats.forEach(System.out::println);
    }

    public static void main(String args[]) {
        (new Thread(new App(args))).start();
    }
}
