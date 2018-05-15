
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

import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SimulationContext {
    String filePath;

    String simulationName;

    String scenarioName;

    RequestStat simStat;

    Map<String, RequestStat> reqStats = new HashMap<>();

    List<String> scripts = new ArrayList<>();

    int maxUsers;

    private final Float apdexT;

    private static final String ALL_REQUESTS = "_all";

    private long start;

    class CountMax {
        int current = 0, maximum = 0;

        public void incr() {
            current += 1;
            maximum = max(current, maximum);
        }

        public void decr() {
            current -= 1;
        }

        public int getMax() {
            return maximum;
        }
    }

    Map<String, CountMax> users = new HashMap<>();

    public SimulationContext(String filePath, Float apdexT) {
        this.filePath = filePath;
        this.simStat = new RequestStat(ALL_REQUESTS, ALL_REQUESTS, ALL_REQUESTS, 0, apdexT);
        this.apdexT = apdexT;
    }

    public List<RequestStat> getRequests() {
        List<RequestStat> ret = new ArrayList<>(reqStats.values());
        Collections.sort(ret, (a, b) -> (int) (1000 * (a.avg - b.avg)));
        return ret;
    }

    public void addRequest(String scenario, String requestName, long start, long end, boolean success) {
        RequestStat request = reqStats.get(requestName);
        if (request == null) {
            request = new RequestStat(simulationName, scenario, requestName, this.start, apdexT);
            reqStats.put(requestName, request);
        }
        request.add(start, end, success);
        simStat.add(start, end, success);
    }

    public void computeStat() {
        maxUsers = users.values().stream().mapToInt(CountMax::getMax).sum();
        simStat.computeStat(maxUsers);
        reqStats.values()
                .forEach(request -> request.computeStat(simStat.duration, users.get(request.scenario).maximum));

    }

    public void setSimulationName(String name) {
        this.simulationName = name;
        simStat.setSimulationName(name);
    }

    public void setScenarioName(String name) {
        this.scenarioName = name;
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
        return simStat.toString() + "\n"
                + getRequests().stream().map(RequestStat::toString).collect(Collectors.joining("\n"));
    }

    public SimulationContext setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
        return this;
    }

    public void addUser(String scenario) {
        CountMax count = users.get(scenario);
        if (count == null) {
            count = new CountMax();
            users.put(scenario, count);
        }
        count.incr();
    }

    public void endUser(String scenario) {
        CountMax count = users.get(scenario);
        if (count != null) {
            count.decr();
        }
    }

}
