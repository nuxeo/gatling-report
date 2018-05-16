package org.nuxeo.tools.gatling.report;
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

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.quux00.simplecsv.CsvParser;
import net.quux00.simplecsv.CsvParserBuilder;
import net.quux00.simplecsv.CsvReader;
import net.quux00.simplecsv.CsvReaderBuilder;

public abstract class SimulationParser {

    protected static final String OK = "OK";

    protected static final String REQUEST = "REQUEST";

    protected static final String RUN = "RUN";

    protected static final String USER = "USER";

    protected static final String START = "START";

    protected static final String END = "END";

    protected final File file;

    protected final Float apdexT;

    public SimulationParser(File file, Float apdexT) {
        this.file = file;
        this.apdexT = apdexT;
    }

    public SimulationParser(File file) {
        this.file = file;
        this.apdexT = null;
    }

    public SimulationContext parse() throws IOException {
        SimulationContext ret = new SimulationContext(file.getAbsolutePath(), apdexT);
        CsvParser p = new CsvParserBuilder().trimWhitespace(true).allowUnbalancedQuotes(true).separator('\t').build();
        CsvReader reader = new CsvReaderBuilder(Utils.getReaderFor(file)).csvParser(p).build();

        List<String> line;
        String name;
        String scenario;
        long start, end;
        boolean success;
        List<String> header = reader.readNext();
        checkLine(header);

        ret.setSimulationName(getSimulationName(header));
        ret.setScenarioName(getScenario(header));
        ret.setStart(Long.parseLong(getSimulationStart(header)));

        while ((line = reader.readNext()) != null) {
            scenario = getScenario(line);

            switch (getType(line)) {
            case RUN:
                break;
            case REQUEST:
                name = getRequestName(line);
                start = getRequestStart(line);
                end = getRequestEnd(line);
                success = getRequestSuccess(line);
                ret.addRequest(scenario, name, start, end, success);
                break;
            case USER:
                switch (getUserType(line)) {
                case START:
                    ret.addUser(scenario);
                    break;
                case END:
                    ret.endUser(scenario);
                    break;
                }
                break;
            }
        }
        ret.computeStat();
        return ret;
    }

    protected void checkLine(List<String> line) {
        if (line.size() <= 2) {
            invalidFile();
        }
    }

    protected abstract String getSimulationName(List<String> line);

    protected abstract String getSimulationStart(List<String> line);

    protected abstract String getScenario(List<String> line);

    protected abstract String getType(List<String> line);

    protected abstract String getUserType(List<String> line);

    protected abstract String getRequestName(List<String> line);

    protected abstract Long getRequestStart(List<String> line);

    protected abstract Long getRequestEnd(List<String> line);

    protected abstract boolean getRequestSuccess(List<String> line);

    protected SimulationContext invalidFile() {
        throw new IllegalArgumentException(String.format(
                "Invalid simulation file: %s expecting " + "Gatling 2.1, 2.3.1 or 3.x format", file.getAbsolutePath()));
    }

}
