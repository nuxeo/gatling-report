/*
 * (C) Copyright 2015-2026 Nuxeo (http://nuxeo.com/) and others.
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

import java.io.IOException;

/**
 * Common contract for parsing a Gatling {@code simulation.log} file (whatever its underlying format) into a
 * {@link SimulationContext}.
 *
 * @since 6.4
 */
public interface Parser {

    SimulationContext parse() throws IOException;

}
