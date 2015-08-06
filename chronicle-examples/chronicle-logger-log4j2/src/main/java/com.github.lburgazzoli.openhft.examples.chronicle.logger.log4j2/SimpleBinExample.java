/*
 * Copyright 2014 Higher Frequency Trading http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.lburgazzoli.openhft.examples.chronicle.logger.log4j2;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.logger.tools.ChroniTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleBinExample {
    public static void main(final String[] args) throws Exception {
        final String path = System.getProperty("java.io.tmpdir") + "/chronicle-log4j2-example";
        final Logger log  = LogManager.getLogger(SimpleBinExample.class);

        log.info("message-1");
        log.info("message-2");

        Chronicle chronicle = ChronicleQueueBuilder.vanilla(path).build();
        ChroniTool.process(chronicle, ChroniTool.READER_BINARY, false, false);
        chronicle.close();
        chronicle.clear();
    }
}
