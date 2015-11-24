import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

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

public class TestPlotly {
    private static final String SIM_GZ = "simulation-1.log.gz";

    @Test
    public void parseCompressedSimulation() throws Exception {
        SimulationStat ret = new Parser(getRessourceFile(SIM_GZ)).parse();
        ret.computeStat();
        PlotlyReport.generate(null, ret);
    }

    private File getRessourceFile(String filename) throws FileNotFoundException {
        ClassLoader classLoader = getClass().getClassLoader();
        if (classLoader.getResource(filename) == null) {
            throw new FileNotFoundException(filename);
        }
        return new File(classLoader.getResource(filename).getFile());
    }

}