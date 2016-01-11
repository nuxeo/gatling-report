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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Report {
    private static final String YAML = "yaml/";
    private static final String HTML = "html/";
    private static final String SIMULATION_TEMPLATE = "simulation.mustache";
    private static final String TREND_TEMPLATE = "trend.mustache";
    private static final String DIFF_TEMPLATE = "diff.mustache";
    private static final String INDEX = "index.html";
    private static final String YAML_INDEX = "data.yml";
    private static final String DEFAULT_SCRIPT = "plotly-latest.min.js";
    private static final String DEFAULT_CDN_SCRIPT = "https://cdn.plot.ly/plotly-latest.min.js";

    private final List<SimulationContext> stats;
    private File outputDirectory;
    private Writer writer;
    private List<String> scripts = new ArrayList<>();
    private boolean includeJs = false;
    private String template;
    private String graphiteUrl, user, password;
    private Graphite graphite;
    private ZoneId zoneId;
    private boolean yaml = false;
    private List<String> map;

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
        if (graphiteUrl != null) {
            stats.forEach(stats -> stats.simStat.graphite = new Graphite(graphiteUrl, user, password, stats,
                    outputDirectory, zoneId));
        }
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
        if (map != null && map.size() == stats.size()) {
            HashMap<String, Object> scopes = new HashMap<String, Object>();
            scopes.put("trend", new TrendContext(stats).setScripts(getScripts()));
            int i=0;
            for (String name : map) {
                scopes.put(name, stats.get(i++));
            }
            mustache.execute(getWriter(), scopes).flush();
        } else {
            mustache.execute(getWriter(), new TrendContext(stats).setScripts(getScripts())).flush();
        }
    }

    public void createDiffReport() throws IOException {
        Mustache mustache = getMustache();
        if (map != null && map.size() == stats.size()) {
            HashMap<String, Object> scopes = new HashMap<String, Object>();
            scopes.put("diff", new DiffContext(stats).setScripts(getScripts()));
            int i=0;
            for (String name : map) {
                scopes.put(name, stats.get(i++));
            }
            mustache.execute(getWriter(), scopes).flush();
        } else {
            mustache.execute(getWriter(), new DiffContext(stats).setScripts(getScripts())).flush();
        }

    }

    public Writer getWriter() throws IOException {
        if (writer == null) {
            File index = getReportPath();
            writer = new FileWriter(index);
        }
        return writer;
    }

    public File getReportPath() {
        if (yaml) {
            return new File(outputDirectory, YAML_INDEX);
        }
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
        String prefix = yaml ? YAML : HTML;
        switch (nbSimulation) {
            case 1:
                return prefix + SIMULATION_TEMPLATE;
            case 2:
                return prefix + DIFF_TEMPLATE;
            default:
                return prefix + TREND_TEMPLATE;
        }
    }

    public Report includeGraphite(String graphiteUrl, String user, String password, ZoneId zoneId) {
        this.graphiteUrl = graphiteUrl;
        this.user = user;
        this.password = password;
        this.zoneId = zoneId;
        return this;
    }

    public Report yamlReport(boolean yaml) {
        this.yaml = yaml;
        return this;
    }

    public Report withMap(List<String> map) {
        this.map = map;
        return this;
    }
}
