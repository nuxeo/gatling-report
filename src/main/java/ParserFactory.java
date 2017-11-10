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

import net.quux00.simplecsv.CsvParser;
import net.quux00.simplecsv.CsvParserBuilder;
import net.quux00.simplecsv.CsvReader;
import net.quux00.simplecsv.CsvReaderBuilder;

import java.io.*;
import java.util.List;

public class ParserFactory {

    private static final int VERSION2 = 2;
    private static final int VERSION3 = 3;

    public static SimulationParser getParser(File file, Float apdexT) throws IOException {
        return getVersionSpecificParser(file, apdexT);
    }

    public static SimulationParser getParser(File file) throws IOException {
        return getVersionSpecificParser(file, null);
    }

    private static SimulationParser getVersionSpecificParser(File file, Float apdexT) throws IOException {
        CsvParser p = new CsvParserBuilder().trimWhitespace(true).allowUnbalancedQuotes(true).separator('\t').build();
        CsvReader reader = new CsvReaderBuilder(Utils.getReaderFor(file)).csvParser(p).build();

        List<String> header = reader.readNext();
        int version = getVersion(header);
        if (version == VERSION3) {
            return new SimulationParserV3(file, apdexT);
        } else {
            return new SimulationParserV2(file, apdexT);
        }
    }

    private static int getVersion(List<String> line) {
        if (line.size() > 2) {
            String v = line.get(5);
            if (v.startsWith("2.")) {
                return VERSION2;
            } else {
                return VERSION3;
            }
        } else {
            return -1;
        }
    }


}
