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

import au.com.bytecode.opencsv.CSVReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.zip.GZIPInputStream;

import static java.lang.Math.max;

public class SimulationParser {

    private static final String OK = "OK";
    private static final String REQUEST = "REQUEST";
    private static final String RUN = "RUN";
    private static final String USER = "USER";
    private static final String START = "START";
    private static final String END = "END";
    private static final String GZ = "gz";
    private final File file;
    private final Float apdexT;
    private final boolean normalised;

    public SimulationParser(File file, Float apdexT, boolean normalised) {
        this.file = file;
        this.apdexT = apdexT;
        this.normalised = normalised;
    }

    public SimulationParser(File file) {
        this.file = file;
        this.apdexT = null;
        this.normalised = false;
    }

    public SimulationContext parse() throws IOException {
        SimulationContext ret = new SimulationContext(file.getAbsolutePath(), apdexT);
        CSVReader reader = new CSVReader(getReaderFor(file), '\t');
        String[] line;
        String name;
        String scenario;
        long start, end;
        long firstReq = 0;
        boolean firstReqSet = false;
        boolean success;
        ArrayList <Request> requests = new ArrayList<Request>();
        while ((line = reader.readNext()) != null) {
            if (line.length <= 2) {
                invalidFile();
            }

            scenario = line[0];
            switch (line[2]) {
                case RUN:
                    String version = line[5];
                    if (!version.startsWith("2.")) {
                        return invalidFile();
                    }
                    ret.setSimulationName(line[1]);
                    ret.setStart(Long.parseLong(line[3]));
                    break;
                case REQUEST:
                	start = Long.parseLong(line[6]);
                	if (!firstReqSet && normalised) {
                		firstReq = start;
                		firstReqSet = true;
                	}
                	else if (normalised && start < firstReq) {
                    	firstReq = start;
                    }
                    name = line[4];
                    end = Long.parseLong(line[8]);
                    success = OK.equals(line[9]);
                    requests.add(new Request(name, scenario, start,end, success));
                    break;
                case USER:
                    switch (line[3]) {
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
        //sort
        Collections.sort(requests, new Comparator<Request>() {
            @Override
            public int compare(Request request, Request request2)
            {

                return  Long.compare(request.getStart(), request2.getStart());
            }
        });
        
        for (Request req : requests) {
        	if (normalised) {
        		ret.addRequest(req.getScenario(), req.getName(), req.getStart() - firstReq, req.getEnd() - firstReq, req.getSuccess());
        	} else {
        		ret.addRequest(req.getScenario(), req.getName(), req.getStart(), req.getEnd(), req.getSuccess());        		
        	}
        }
        ret.computeStat(normalised);
        System.out.println("SimContext: " + ret.toString());
        return ret;
    }

    private SimulationContext invalidFile() {
        throw new IllegalArgumentException(String.format("Invalid simulation file: %s expecting " +
                "Gatling 2.x format", file.getAbsolutePath()));
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }

    private Reader getReaderFor(File file) throws IOException {
        if (GZ.equals(getFileExtension(file))) {
            InputStream fileStream = new FileInputStream(file);
            InputStream gzipStream = new GZIPInputStream(fileStream);
            return new InputStreamReader(gzipStream, "UTF-8");
        }
        return new FileReader(file);
    }

}
