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
import java.io.Writer;
import java.util.List;

public class Report {

    private static final String SIMULATION_TEMPLATE = "simulation.mustache";
    private static final String TREND_TEMPLATE = "trend.mustache";
    private static final String INDEX = "index.html";
    private final List<SimulationContext> stats;
    private File outputDirectory;
    private Writer writer;

    public Report(List<SimulationContext> stats) {
        this.stats = stats;
    }

    public Report setOutputDirectory(File output) {
        this.outputDirectory = output;
        return this;
    }

    public Report setWriter(Writer writer) {
        this.writer = writer;
        return this;
    }

    public String create() throws IOException {
        if (stats.size() == 1) {
            createSimulationReport();
        } else {
            createTrendReport();
        }
        return getReportPath().getAbsolutePath();
    }

    public void createSimulationReport() throws IOException {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(SIMULATION_TEMPLATE);
        mustache.execute(getWriter(), stats.get(0)).flush();
    }

    public void createTrendReport() throws IOException {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(TREND_TEMPLATE);
        mustache.execute(getWriter(), new TrendContext(stats)).flush();
    }

    public Writer getWriter() throws IOException {
        if (writer == null) {
            File index = getReportPath();
            writer = new FileWriter(index);
        }
        return writer;
    }

    public File getReportPath() {
        return new File(outputDirectory, INDEX);
    }
}
