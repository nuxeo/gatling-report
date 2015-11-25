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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrendStat {

    String scenario;
    List<String> xvalues = new ArrayList<>();
    List<Double> yvalues = new ArrayList<>();
    List<Long> yerrors = new ArrayList<>();
    List<Double> rps = new ArrayList<>();

    public TrendStat(List<SimulationStat> stats) {
        Set<String> names = new HashSet<>();
        Collections.sort(stats, (a, b) -> (int) (a.simStat.start - b.simStat.start));
        for (SimulationStat stat : stats) {
            names.add(stat.simulationName);
            xvalues.add(String.format("'%s'", stat.simStat.startDate));
            yvalues.add(stat.simStat.avg);
            yerrors.add(stat.simStat.stddev);
            rps.add(stat.simStat.rps);
        }
        scenario = String.join(" ", names);
    }

}
