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
package com.github.lburgazzoli.openhft.examples.chronicle.queue;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.IndexedChronicle;
import net.openhft.chronicle.tcp.ChronicleSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChronoSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChronoSource.class);

    public static void main(String[] args) throws Exception {
        String basepath = System.getProperty("java.io.tmpdir") + "/ChronicleOfRiddick";
        LOGGER.info("BASEPATH: {}", basepath);

        Chronicle chronicle = new IndexedChronicle(basepath);
        chronicle.clear();

        Chronicle source = new ChronicleSource(chronicle, 9876);
        ExcerptAppender appender = source.createAppender();

        for(int i=10;i<1000;i++) {
            LOGGER.info("write {}",i);
            appender.startExcerpt(4);
            appender.writeInt(i);
            appender.finish();

            Thread.sleep(1000);
        }

        LOGGER.info("shutdown ");

        appender.close();

        source.close();
        source.clear();
    }
}
