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

public class SimulationContext {
    private final Float apdexT;
    String filePath;
    String simulationName;
    RequestStat simStat;
    Map<String, RequestStat> reqStats;
    List<String> scripts = new ArrayList<>();

    private static final String ALL_REQUESTS = "_all";
    private long start;

    public SimulationContext(String filePath, Float apdexT) {
        this.filePath = filePath;
        this.simStat = new RequestStat(filePath, ALL_REQUESTS, 0, apdexT);
        reqStats = new HashMap<>();
        this.apdexT = apdexT;
    }

    public List<RequestStat> getRequests() {
        List<RequestStat> ret = new ArrayList<>(reqStats.values());
        Collections.sort(ret, (a, b) -> (int) (1000 * (a.avg - b.avg)));
        return ret;
    }

    public void addRequest(String requestName, long start, long end, boolean success) {
        RequestStat request = reqStats.get(requestName);
        if (request == null) {
            request = new RequestStat(simulationName, requestName, this.start, apdexT);
            reqStats.put(requestName, request);
        }
        request.add(start, end, success);
        simStat.add(start, end, success);
    }

    public void computeStat() {
        simStat.computeStat();
        reqStats.values().forEach(request -> request.computeStat(simStat.duration));
    }

    public void setSimulationName(String name) {
        this.simulationName = name;
        simStat.setScenario(name);
    }

    public void setStart(long start) {
        this.start = start;
        simStat.setStart(start);
    }

    public SimulationContext setScripts(List<String> scripts) {
        this.scripts = scripts;
        return this;
    }

    @Override
    public String toString() {
        return simStat.toString() + "\n" + getRequests().stream().map(RequestStat::toString)
                .collect(Collectors.joining("\n"));
    }
}
