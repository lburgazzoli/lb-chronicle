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

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.logger.tools.ChroniTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;

public class SimpleExample {
    public static void main(final String[] args) throws IOException {
        final String path = System.getProperty("java.io.tmpdir") + "/logger-logback";
        final Logger log  = LoggerFactory.getLogger(SimpleExample.class);

        Throwable th1 = new IOException("io-exception-1");
        Throwable th2 = new IOException("io-exception-2", new EOFException("eof-execption"));

        log.info("message-1", th1);
        log.info("message-2", th2);

        Chronicle chronicle = ChronicleQueueBuilder.vanilla(path).build();
        ChroniTool.process(chronicle, ChroniTool.READER_BINARY, false, false);
        chronicle.close();
        chronicle.clear();
    }
}
