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

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class App implements Runnable {
    private final static Logger log = Logger.getLogger(App.class);
    private static final String USAGE = "java -jar gatling-report.jar simulation.log [simulation.log.2 ...] " +
            "outputDirectory";
    private final String[] args;

    public App(String[] args) {
        this.args = args;
    }

    @Override
    public void run() {
        List<SimulationStat> stats = getSimulationStats();
        renderStats(stats, getOutputDirectory());
    }

    private List<SimulationStat> getSimulationStats() {
        List<File> files = getSimulationFiles();
        List<SimulationStat> stats = new ArrayList<>(files.size());
        for (File file : files) {
            log.info("Parsing " + file.getAbsolutePath());
            try {
                stats.add(new Parser(file).parse());
            } catch (IOException e) {
                log.error("Invalid file: " + file.getAbsolutePath(), e);
            }
        }
        return stats;
    }

    private void renderStats(List<SimulationStat> stats, File outputDirectory) {
        if (outputDirectory == null) {
            renderStatsAsCSV(stats);
        } else {
            renderPlotyReport(outputDirectory, stats);
        }
    }

    private void renderPlotyReport(File outputDirectory, List<SimulationStat> stats) {
        outputDirectory.mkdirs();
        if (stats.size() == 1) {
            renderPlotySingleReport(outputDirectory, stats.get(0));
        } else {
            renderPlotyTrendReport(outputDirectory, stats);
        }
    }

    private void renderPlotyTrendReport(File outputDirectory, List<SimulationStat> stats) {
        try {
            for (SimulationStat stat : stats) {
                stat.computeStat();
            }
            String reportPath = PlotlyReport.generate(outputDirectory, stats);
            log.info("Report generated: " + reportPath);
        } catch (IOException e) {
            log.error("Can not generate report", e);
        }
    }

    private void renderPlotySingleReport(File outputDirectory, SimulationStat stat) {
        try {
            stat.computeStat();
            String reportPath = PlotlyReport.generate(outputDirectory, stat);
            log.info("Report generated: " + reportPath);
        } catch (IOException e) {
            log.error("Can not generate report", e);
        }
    }

    private void renderStatsAsCSV(List<SimulationStat> stats) {
        System.out.println(Stat.header());
        for (SimulationStat stat : stats) {
            stat.computeStat();
            System.out.println(stat);
        }
    }

    private void displayHelpAndExit() {
        System.err.println(USAGE);
        System.exit(-1);
    }

    private List<File> getSimulationFiles() {
        List<File> ret = new ArrayList<>();
        if (args.length < 1) {
            displayHelpAndExit();
        }
        for (int i = 0; i < args.length - 1; i++) {
            File file = new File(args[i]);
            ret.add(new File(args[i]));
        }
        File file = new File(args[args.length - 1]);
        if (file.exists() && file.isFile()) {
            ret.add(file);
        }
        return ret;
    }

    private File getOutputDirectory() {
        File ret = new File(args[args.length - 1]);
        if (!ret.isFile()) {
            return ret;
        }
        return null;
    }

    public static void main(String args[]) {
        (new Thread(new App(args))).start();
    }
}
