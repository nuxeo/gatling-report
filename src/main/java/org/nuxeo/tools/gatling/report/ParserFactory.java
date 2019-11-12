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
import java.io.IOException;
import java.util.List;

public class ParserFactory {

    public static SimulationParser getParser(File file, Float apdexT) throws IOException {
        return getVersionSpecificParser(file, apdexT);
    }

    public static SimulationParser getParser(File file) throws IOException {
        return getVersionSpecificParser(file, null);
    }

    protected static SimulationParser getVersionSpecificParser(File file, Float apdexT) throws IOException {
        List<String> header = getHeaderLine(file);
        // System.out.println(header.size() + " " + header);
        if (header.size() == 6) {
            String version = header.get(5);
            if (version.matches("3\\.[2-9].*")) {
                return new SimulationParserV32(file, apdexT);
            }
            if (version.startsWith("3.0")) {
                return new SimulationParserV3(file, apdexT);
            }
            if (version.startsWith("2.")) {
                return new SimulationParserV2(file, apdexT);
            }
        } else if (header.size() == 7) {
            String version = header.get(6);
            if (version.startsWith("2.")) {
                return new SimulationParserV23(file, apdexT);
            }
        }
        throw new IllegalArgumentException("Unknown Gatling simulation version: " + header);
    }

    protected static List<String> getHeaderLine(File file) throws IOException {
        try (SimulationReader reader = new SimulationReader(file)) {
            return reader.readNext();
        }
    }

}
