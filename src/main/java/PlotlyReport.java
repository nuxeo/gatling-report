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

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

public class PlotlyReport {

    private static final String SIMULATION_TEMPLATE = "simulation.mustache";
    private static final String TREND_TEMPLATE = "trend.mustache";
    private static final String INDEX = "index.html";

    public static String createSimulationReport(SimulationContext stat, File outputDirectory) throws IOException {
        File index = new File(outputDirectory, INDEX);
        Writer output = new FileWriter(index);
        generate(stat, output);
        return index.getAbsolutePath();
    }

    protected static void generate(SimulationContext stat, Writer output) throws IOException {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(SIMULATION_TEMPLATE);
        mustache.execute(output, stat).flush();
    }

    public static String createTrendReport(List<SimulationContext> stats, File outputDirectory) throws IOException {
            File index = new File(outputDirectory, INDEX);
            Writer output = new FileWriter(index);
        generate(stats, output);
        return index.getAbsolutePath();
    }

    protected static void generate(List<SimulationContext> stats, Writer output) throws IOException {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(TREND_TEMPLATE);
        mustache.execute(output, new TrendContext(stats)).flush();
    }

}
