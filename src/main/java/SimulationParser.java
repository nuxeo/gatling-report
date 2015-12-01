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
import java.util.zip.GZIPInputStream;

public class SimulationParser {

    private static final String OK = "OK";
    private static final String REQUEST = "REQUEST";
    private static final String RUN = "RUN";
    private static final String GZ = "gz";
    private final File file;
    private final Double apdexT;

    public SimulationParser(File file, Double apdexT) {
        this.file = file;
        this.apdexT = apdexT;
    }

    public SimulationParser(File file) {
        this.file = file;
        this.apdexT = null;
    }

    public SimulationContext parse() throws IOException {
        SimulationContext ret = new SimulationContext(file.getAbsolutePath(), apdexT);
        CSVReader reader = new CSVReader(getReaderFor(file), '\t');
        String[] line;
        String name;
        long start, end;
        boolean success;
        while ((line = reader.readNext()) != null) {
            if (line.length <= 2) {
                invalidFile();
            }
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
                    name = line[4];
                    start = Long.parseLong(line[6]);
                    end = Long.parseLong(line[8]);
                    success = OK.equals(line[9]);
                    ret.addRequest(name, start, end, success);
                    break;

            }

        }
        ret.computeStat();
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
