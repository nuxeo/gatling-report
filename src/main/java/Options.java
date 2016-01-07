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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Lists;

import java.time.ZoneId;
import java.util.List;

public class Options {
    @Parameter(required = true, description = "SIMULATION.LOG...")
    public List<String> simulations = Lists.newArrayList();

    @Parameter(names = {"--output-dir", "-o"},
            description = "Create a report in this directory, if not specified output CSV stats to stdout.")
    public String outputDirectory;

    @Parameter(names = {"--yaml", "-y"}, description = "Create a YAML report instead of an HTML report.")
    public boolean yaml = false;

    @Parameter(names = {"--force", "-f"}, description = "Override an existing report.")
    public boolean force = false;

    @Parameter(names = {"--include-js", "-i"},
            description = "Include Plotly js in the report, otherwhise use the CDN version.")
    public boolean includeJs = false;

    @Parameter(names = {"--template", "-t"},
            description = "Use a custom mustache template to generate the report.")
    public String template;

    @Parameter(names = {"--graphite", "-g"},
            description = "Download graphite dashboard images.")
    public String graphiteUrl;

    @Parameter(names = {"--user", "-u"},
            description = "Graphite basic authentication user.")
    public String user;

    @Parameter(names = {"--password", "-p"},
            description = "Graphite basic authentication password.")
    public String password;

    @Parameter(names = {"--timezone"},
            description = "Graphite time zone if different from Gatling, ex: Europe/Paris")
    private String timeZoneString;

    public ZoneId getZoneId() {
        if (timeZoneString == null) {
            return null;
        }
        return ZoneId.of(timeZoneString);
    }

    @Parameter(names = {"--apdex-threshold", "-T"},
            description = "Apdex thresold, the response time in second above which the request switch from satisfying" +
                    " to tolerable.")
    public Float apdexT = 1.5f;

    @Parameter(names = {"--help", "-h"}, description = "Display this message.", help = true)
    public boolean help;
}