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
import java.util.stream.Collectors;

public class TrendContext {

    String scenario;
    TrendStat all = new TrendStat();
    List<TrendStat> requests = new ArrayList<>();

    class TrendStat {
        String name;
        Integer indice;
        List<String> xvalues = new ArrayList<>();
        List<Double> yvalues = new ArrayList<>();
        List<Long> yerrors = new ArrayList<>();
        List<Double> rps = new ArrayList<>();

        public void add(Stat stat) {
            if (stat == null) {
                xvalues.add(null);
                yvalues.add(null);
                yerrors.add(null);
                rps.add(null);
            } else {
                name = stat.request;
                indice = stat.indice;
                xvalues.add(String.format("'%s'", stat.startDate));
                yvalues.add(stat.avg);
                yerrors.add(stat.stddev);
                rps.add(stat.rps);
            }
        }
    }

    public TrendContext(List<SimulationStat> stats) {
        Set<String> names = new HashSet<>();
        List<String> requestNames = getRequestListSorted(stats.get(0));
        for (String requestName : requestNames) {
            requests.add(new TrendStat());
        }
        Collections.sort(stats, (a, b) -> (int) (a.simStat.start - b.simStat.start));
        for (SimulationStat simStat : stats) {
            names.add(simStat.simulationName);
            all.add(simStat.simStat);
            for (int i = 0; i < requestNames.size(); i++) {
                String name = requestNames.get(i);
                Stat reqStat = simStat.reqStats.get(name);
                requests.get(i).add(reqStat);
            }
        }
        scenario = String.join(" ", names);
    }

    private List<String> getRequestListSorted(SimulationStat stat) {
        return stat.getRequests().stream().map(s -> s.request).collect(Collectors.toList());
    }

}
