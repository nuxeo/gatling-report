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
    List<String> scripts;


    class TrendStat {
        String name;
        Integer indice;
        List<String> xvalues = new ArrayList<>();
        List<Double> yvalues = new ArrayList<>();
        List<Long> yerrors = new ArrayList<>();
        List<Double> rps = new ArrayList<>();

        public void add(RequestStat stat) {
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

    public TrendContext(List<SimulationContext> stats) {
        Set<String> names = new HashSet<>();
        List<String> requestNames = getRequestListSorted(stats.get(0));
        Collections.reverse(requestNames);
        for (String requestName : requestNames) {
            requests.add(new TrendStat());
        }
        Collections.sort(stats, (a, b) -> (int) (a.simStat.start - b.simStat.start));
        for (SimulationContext simStat : stats) {
            names.add(simStat.simulationName);
            all.add(simStat.simStat);
            for (int i = 0; i < requestNames.size(); i++) {
                String name = requestNames.get(i);
                RequestStat reqStat = simStat.reqStats.get(name);
                requests.get(i).add(reqStat);
            }
        }
        scenario = String.join(" ", names);
    }

    public TrendContext setScripts(List<String> scripts) {
        this.scripts = scripts;
        return this;
    }

    private List<String> getRequestListSorted(SimulationContext stat) {
        return stat.getRequests().stream().map(s -> s.request).collect(Collectors.toList());
    }

}
