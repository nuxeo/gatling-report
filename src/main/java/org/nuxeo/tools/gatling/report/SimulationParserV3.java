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
 *     Kris Geusebroek
 */
package org.nuxeo.tools.gatling.report;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gatling 3.? simulation format
 */
public class SimulationParserV3 extends SimulationParser {

    protected final Map<String, String> userIdToScenarioMap;

    public SimulationParserV3(File file, Float apdexT) {
        super(file, apdexT);
        this.userIdToScenarioMap = new HashMap();
    }

    public SimulationParserV3(File file) {
        super(file);
        this.userIdToScenarioMap = new HashMap();
    }

    protected String getSimulationName(List<String> line) {
        return line.get(2);
    }

    protected String getSimulationStart(List<String> line) {
        return line.get(3);
    }

    protected String getScenario(List<String> line) {
        if (getType(line).equals(REQUEST)) {
            final String userId = line.get(1);
            return this.userIdToScenarioMap.get(userId);
        } else {
            return line.get(1);
        }
    }

    protected String getType(List<String> line) {
        return line.get(0);
    }

    protected String getUserType(List<String> line) {
        // In Gatling 3.0, the REQUEST line contains the userid, but no longer contains the scenario.
        // To determine a REQUEST's scenario, we need to look it up based on the user id, so save the mapping.
        // Ugly to do it here like this, but the alternative is to add new methods to SimulationParser that would be Gatling 3.0 specfic.
        saveUserIdAndScenario(line);
        return line.get(3);
    }

    protected String getRequestName(List<String> line) {
        return line.get(3);
    }

    protected Long getRequestStart(List<String> line) {
        return Long.parseLong(line.get(4));
    }

    protected Long getRequestEnd(List<String> line) {
        return Long.parseLong(line.get(5));
    }

    protected boolean getRequestSuccess(List<String> line) {
        return OK.equals(line.get(6));
    }

    private void saveUserIdAndScenario(List<String> line) {
        final String scenario = getScenario(line);
        final String userId = line.get(2);
        userIdToScenarioMap.put(userId, scenario);
    }


}
