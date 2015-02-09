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
package com.github.lburgazzoli.openhft.examples.chronicle.logger.logback;

import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.logger.ChronicleLogEvent;
import net.openhft.chronicle.logger.tools.ChroniTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SimpleExample {

    public static void main(final String[] args) throws IOException {
        Logger log = LoggerFactory.getLogger(SimpleExample.class);
        log.info("message", Thread.currentThread().getName() , new IOException("test"));

        ChroniTool.process(
            ChronicleQueueBuilder.vanilla(
                System.getProperty("java.io.tmpdir") + "/logger-logback").build(),
            new ChroniTool.BinaryProcessor() {
                @Override
                public void process(ChronicleLogEvent event) {
                    System.out.printf("%s|%s|%s|%s|%s - <%s><%s>\n",
                        ChroniTool.DF.format(event.getTimeStamp()),
                        event.getLevel().toString(),
                        event.getThreadName(),
                        event.getLoggerName(),
                        event.getMessage(),
                        event.getArgumentArray().toString(),
                        event.getThrowable().toString()
                    );
                }
            },
            false,
            false);
    }
}
