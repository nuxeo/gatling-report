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

import java.util.List;

public class Options {
    @Parameter(required = true, description="SIMULATION.LOG...")
    public List<String> simulations = Lists.newArrayList();

    @Parameter(names = {"--output-dir", "-o"}, description = "Create a report, if not specified output CSV stat to " +
            "stdout")
    public String outputDirectory;

    @Parameter(names = {"--force", "-f"}, description = "Override an existing report")
    public boolean force = false;

    @Parameter(names = {"--include-js", "-i"}, description = "Include Plotly js into the report")
    public boolean includeJs = false;

    @Parameter(names = {"--help", "-h"}, help = true)
    public boolean help;
}