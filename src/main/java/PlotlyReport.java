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

    public static String generate(File outputDirectory, SimulationStat stat) throws IOException {
        Writer output;
        String ret = "stdout";
        if (outputDirectory == null) {
            output = new PrintWriter(System.out);
        } else {
            File index = new File(outputDirectory, "index.html");
            output = new FileWriter(index);
            ret = index.getAbsolutePath();
        }
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile("report.mustache");
        mustache.execute(output, stat).flush();
        return ret;
    }

    public static String generate(File outputDirectory, List<SimulationStat> stats) throws IOException {
        Writer output;
        String ret = "stdout";
        if (outputDirectory == null) {
            output = new PrintWriter(System.out);
        } else {
            File index = new File(outputDirectory, "index.html");
            output = new FileWriter(index);
            ret = index.getAbsolutePath();
        }
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile("trend.mustache");
        mustache.execute(output, new TrendContext(stats)).flush();
        return ret;
    }

}
