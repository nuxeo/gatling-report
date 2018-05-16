/*
 * (C) Copyright 2018 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     bdelbosc
 */
package org.nuxeo.tools.gatling.report;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import net.quux00.simplecsv.CsvParser;
import net.quux00.simplecsv.CsvParserBuilder;
import net.quux00.simplecsv.CsvReader;

/**
 * A CsvReader that skip assertions lines
 *
 * @since 3.0
 */
public class SimulationReader extends CsvReader {

    protected static final String ASSERTION = "assertion";

    public SimulationReader(File file) throws IOException {
        this(Utils.getReaderFor(file), 0,
                new CsvParserBuilder().trimWhitespace(true).allowUnbalancedQuotes(true).separator('\t').build());
    }

    public SimulationReader(Reader reader, int line, CsvParser csvParser) {
        super(reader, line, csvParser);
    }

    @Override
    public List<String> readNext() throws IOException {
        List<String> ret = super.readNext();
        if (ret != null && !ret.isEmpty() && ret.get(0).toLowerCase().startsWith(ASSERTION)) {
            return readNext();
        }
        return ret;
    }
}
