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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SimulationStat {
    String filePath;
    String simulationName;
    Stat simStat;
    Map<String, Stat> reqStats;

    private static final String ALL_REQUESTS = "_all";
    private long start;

    public List<Stat> getRequests() {
        List<Stat> ret = new ArrayList<>(reqStats.values());
        Collections.sort(ret, (a, b) -> (int) (1000 * (a.avg - b.avg)));
        return ret;
    }

    public SimulationStat(String filePath) {
        this.filePath = filePath;
        this.simStat = new Stat(filePath, ALL_REQUESTS, 0);
        reqStats = new HashMap<>();
    }

    public void addRequest(String requestName, long start, long end, boolean success) {
        Stat request = reqStats.get(requestName);
        if (request == null) {
            request = new Stat(simulationName, requestName, this.start);
            reqStats.put(requestName, request);
        }
        request.add(start, end, success);
        simStat.add(start, end, success);
    }

    public void computeStat() {
        simStat.computeStat();
        reqStats.values().forEach(request -> request.computeStat(simStat.getDuration()));
    }

    @Override
    public String toString() {
        return simStat.toString() + "\n" + getRequests().stream().map(Stat::toString)
                .collect(Collectors.joining("\n"));
    }

    public void setSimulationName(String name) {
        this.simulationName = name;
        simStat.setScenario(name);
    }

    public void setStart(long start) {
        this.start = start;
        simStat.setStart(start);
    }
}
