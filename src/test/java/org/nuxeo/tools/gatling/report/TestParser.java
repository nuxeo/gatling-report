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
package org.nuxeo.tools.gatling.report;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Test;

public class TestParser {
    private static final String SIM_SMALL_V2_1 = "simulation-small.log";

    private static final String SIM_V2_3 = "simulation-v2.3.log";

    private static final String SIM_SMALL_V3 = "simulation-small-v3.log";

    private static final String SIM_GZ = "simulation-1.log.gz";

    @Test
    public void parseSimpleSimulationVersion21() throws Exception {
        SimulationContext ret = ParserFactory.getParser(getRessourceFile(SIM_SMALL_V2_1)).parse();
        // System.out.println(ret);
        Assert.assertEquals("sim80reindexall", ret.getSimulationName());
        Assert.assertEquals(2, ret.getSimStat().getCount());
        Assert.assertTrue(ret.toString().contains("_all"));
    }

    @Test
    public void parseCompressedSimulation() throws Exception {
        SimulationContext ret = ParserFactory.getParser(getRessourceFile(SIM_GZ)).parse();
        // System.out.println(ret);
        Assert.assertEquals("sim50bench", ret.getSimulationName());
        Assert.assertEquals(2464, ret.getSimStat().getCount());
        Assert.assertTrue(ret.toString().contains("_all"));
    }

    @Test
    public void parseSimulationVersion23() throws Exception {
        SimulationContext ret = ParserFactory.getParser(getRessourceFile(SIM_V2_3)).parse();
        // System.out.println(ret);
        Assert.assertEquals("sim20createdocuments", ret.getSimulationName());
        Assert.assertEquals(1000, ret.getSimStat().getCount());
        Assert.assertTrue(ret.toString().contains("_all"));
    }

    @Test
    public void parseSimpleSimulationVersion3() throws Exception {
        SimulationContext ret = ParserFactory.getParser(getRessourceFile(SIM_SMALL_V3)).parse();
        // System.out.println(ret);
        Assert.assertEquals("sim80reindexall", ret.getSimulationName());
        Assert.assertEquals(2, ret.getSimStat().getCount());
        Assert.assertTrue(ret.toString().contains("_all"));
    }

    private File getRessourceFile(String filename) throws FileNotFoundException {
        ClassLoader classLoader = getClass().getClassLoader();
        if (classLoader.getResource(filename) == null) {
            throw new FileNotFoundException(filename);
        }
        return new File(classLoader.getResource(filename).getFile());
    }

}
