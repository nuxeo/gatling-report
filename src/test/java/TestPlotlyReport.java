import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
 *     bdelbosc
 */

public class TestPlotlyReport {

    private static final String SIM_GZ = "simulation-1.log.gz";
    private static final List<String> SIMS_GZ = Arrays.asList("simulation-1.log.gz", "simulation-2.log.gz",
            "simulation-3.log.gz");

    @Test
    public void generateSimulationReport() throws Exception {
        SimulationContext stat = new SimulationParser(getRessourceFile(SIM_GZ)).parse();
        Writer writer = new StringWriter();
        PlotlyReport.generate(stat, writer);
        // System.out.println(writer);
        Assert.assertTrue(writer.toString().contains("DOCTYPE html"));
    }

    @Test
    public void generateTrendReport() throws Exception {
        List<SimulationContext> stats = new ArrayList<>(SIMS_GZ.size());
        SIMS_GZ.forEach(file -> stats.add(new SimulationContext(file)));
        Writer writer = new StringWriter();
        PlotlyReport.generate(stats, writer);
        // System.out.println(writer);
        Assert.assertTrue(writer.toString().contains("DOCTYPE html"));
    }

    private File getRessourceFile(String filename) throws FileNotFoundException {
        ClassLoader classLoader = getClass().getClassLoader();
        if (classLoader.getResource(filename) == null) {
            throw new FileNotFoundException(filename);
        }
        return new File(classLoader.getResource(filename).getFile());
    }

}