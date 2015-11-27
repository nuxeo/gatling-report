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
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Report {

    private static final String SIMULATION_TEMPLATE = "simulation.mustache";
    private static final String TREND_TEMPLATE = "trend.mustache";
    private static final String DIFF_TEMPLATE = "diff.mustache";
    private static final String INDEX = "index.html";
    private static final String DEFAULT_SCRIPT = "plotly-latest.min.js";
    private static final String DEFAULT_CDN_SCRIPT = "https://cdn.plot.ly/plotly-latest.min.js";

    private final List<SimulationContext> stats;
    private File outputDirectory;
    private Writer writer;
    private List<String> scripts = new ArrayList<>();
    private boolean includeJs = false;
    private String template;

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

    public Report addScript(String script) {
        scripts.add(script);
        return this;
    }

    public Report includeJs(boolean value) {
        includeJs = value;
        return this;
    }

    public Report setTemplate(String template) {
        this.template = template;
        return this;
    }

    public String create() throws IOException {
        int nbSimulation = stats.size();
        switch (nbSimulation) {
            case 1:
                createSimulationReport();
                break;
            case 2:
                createDiffReport();
                break;
            default:
                createTrendReport();
        }
        return getReportPath().getAbsolutePath();
    }

    public void createSimulationReport() throws IOException {
        Mustache mustache = getMustache();
        mustache.execute(getWriter(), stats.get(0).setScripts(getScripts())).flush();
    }

    private Mustache getMustache() throws FileNotFoundException {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache;
        if (template == null) {
            mustache = mf.compile(getDefaultTemplate());
        } else {
            mustache = mf.compile(new FileReader(new File(template)), template);
        }
        return mustache;
    }

    public void createTrendReport() throws IOException {
        Mustache mustache = getMustache();
        mustache.execute(getWriter(), new TrendContext(stats).setScripts(getScripts())).flush();
    }

    public void createDiffReport() throws IOException {
        Mustache mustache = getMustache();
        mustache.execute(getWriter(), new DiffContext(stats).setScripts(getScripts())).flush();
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

    public List<String> getScripts() {
        if (scripts.isEmpty()) {
            scripts.add(getOrCreateDefaultScript());
        }
        return scripts;
    }

    public String getOrCreateDefaultScript() {
        if (outputDirectory == null || !includeJs) {
            return DEFAULT_CDN_SCRIPT;
        }
        URL src = getClass().getResource(DEFAULT_SCRIPT);
        try {
            FileUtils.copyURLToFile(src, new File(outputDirectory, DEFAULT_SCRIPT));
        } catch (IOException e) {
            throw new IllegalArgumentException("Can not copy script: " + src, e);
        }
        return DEFAULT_SCRIPT;
    }

    public String getDefaultTemplate() {
        int nbSimulation = stats.size();
        switch (nbSimulation) {
            case 1:
                return SIMULATION_TEMPLATE;
            case 2:
                return DIFF_TEMPLATE;
            default:
                return template = TREND_TEMPLATE;
        }
    }
}
