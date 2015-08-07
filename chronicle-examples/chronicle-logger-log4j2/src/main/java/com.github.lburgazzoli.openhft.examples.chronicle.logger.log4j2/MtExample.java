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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class MtExample {
    public static void main(final String[] args) throws Exception {
        final String path = System.getProperty("java.io.tmpdir") + "/chronicle-log4j2-example";

        //LogManager.getLogger("test");

        Thread th1 = new Thread(() -> {
            final String name = "thread-1";
            final Logger log = LogManager.getLogger(name);
            for (int m = 0; m < 5; m++) {
                System.out.println("write " + name + " : " + m);
                log.info("message " + m);
            }

            System.out.println("done " + name);
        });

        Thread th2 = new Thread(() -> {
            final String name = "thread-2";
            final Logger log = LogManager.getLogger(name);
            for (int m = 0; m < 5; m++) {
                System.out.println("write " + name + " : " + m);
                log.info("message " + m);
            }

            System.out.println("done " + name);
        });

        th1.start();
        th2.start();
        th1.join();
        th2.join();


        /*
        Chronicle chronicle = ChronicleQueueBuilder.vanilla(path).build();
        ChroniTool.process(chronicle, ChroniTool.READER_BINARY, false, false);
        chronicle.close();
        chronicle.clear();
        */
    }
}
