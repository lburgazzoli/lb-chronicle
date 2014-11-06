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
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.IndexedChronicle;
import net.openhft.chronicle.tcp.ChronicleSink;
import net.openhft.chronicle.tcp.ChronicleSource;
import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.BytesMarshallable;
import net.openhft.lang.model.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class IpcExampleMain {

    // *************************************************************************
    //
    // *************************************************************************

    private static class Msg implements BytesMarshallable {
        public long longField;
        public int intField;

        public Msg() {
            this(0,0);
        }

        public Msg(long longField, int intField) {
            this.longField = longField;
            this.intField = intField;
        }

        public long longField() {
            return this.longField;
        }

        public Msg longField(long longField) {
            this.longField = longField;
            return this;
        }

        public int intField() {
            return this.intField;
        }

        public Msg intField(int intField) {
            this.intField = intField;
            return this;
        }

        @Override
        public void readMarshallable(@NotNull Bytes in) throws IllegalStateException {
            longField = in.readLong();
            intField  = in.readInt();
        }

        @Override
        public void writeMarshallable(@NotNull Bytes out) {
            out.writeLong(longField);
            out.writeInt(intField);
        }

        @Override
        public String toString() {
            return "{" +
                " long=" + longField +
                " int="  + intField  +
            " }";
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    private static final class P1 implements Runnable {
        private static final Logger LOGGER = LoggerFactory.getLogger(P1.class);

        private final String basePath;
        private final int nbMessages;

        public P1(@NotNull String basePath, int nbMessages) {
            this.basePath = basePath;
            this.nbMessages = nbMessages;
        }

        @Override
        public void run() {
            try {
                Chronicle out = new IndexedChronicle(basePath + "/queue-out");
                Chronicle in  = new IndexedChronicle(basePath + "/queue-in");

                ExcerptAppender writer = out.createAppender();
                ExcerptTailer reader = in.createTailer();

                final Msg msg = new Msg();
                for(int i=0;i<nbMessages;i++) {
                    writer.writeInstance(
                        Msg.class,
                        msg.longField(1000 + i).intField(i)
                    );

                    while (true) {
                        if (reader.nextIndex()) {
                            reader.readInstance(Msg.class, msg);
                            reader.finish();

                            LOGGER.info("received {}", msg);
                            break;
                        }
                    }
                }

                writer.close();
                reader.close();

                out.close();
                in.close();
            } catch(IOException e) {
                LOGGER.warn("", e);
            }
        }
    }

    private static final class P2 implements Runnable {
        private static final Logger LOGGER = LoggerFactory.getLogger(P2.class);

        private final String basePath;
        private final int nbMessages;

        public P2(@NotNull String basePath, int nbMessages) {
            this.basePath = basePath;
            this.nbMessages = nbMessages;
        }

        @Override
        public void run() {
            try {
                Chronicle publisher = new ChronicleSource(
                    new IndexedChronicle(basePath + "/queue-out"),
                    10001);

                Chronicle subscriber = new ChronicleSink(
                    new IndexedChronicle(basePath + "/queue-in"),
                    "localhost",
                    10002);

                ExcerptTailer reader = subscriber.createTailer();
                for(int i=0; i<nbMessages; ) {
                    if(reader.nextIndex()) {
                        LOGGER.info("Got a message");
                        i++;

                        reader.finish();
                    }
                }

                reader.close();
                subscriber.close();
                publisher.close();
            } catch(IOException e) {
                LOGGER.warn("", e);
            }
        }
    }

    private static final class P3 implements Runnable {
        private static final Logger LOGGER = LoggerFactory.getLogger(P3.class);

        private final String basePath;
        private final int nbMessages;

        public P3(@NotNull String basePath, int nbMessages) {
            this.basePath = basePath;
            this.nbMessages = nbMessages;
        }

        @Override
        public void run() {
            try {
                Chronicle subscriber = new ChronicleSink(
                    new IndexedChronicle(basePath + "/queue-in-p3"),
                    "localhost",
                    10001);

                Chronicle publisher = new ChronicleSource(
                    new IndexedChronicle(basePath + "/queue-in-p3"),
                    10002);

                ExcerptTailer reader = subscriber.createTailer();
                for(int i=0; i<nbMessages; ) {
                    if(reader.nextIndex()) {
                        LOGGER.info("Got a message");
                        i++;

                        reader.finish();
                    }
                }

                reader.close();
                subscriber.close();
                publisher.close();
            } catch(IOException e) {
                LOGGER.warn("", e);
            }
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static void main(final String[] args) throws Exception {
        final Logger logger     = LoggerFactory.getLogger(IpcExampleMain.class);
        final String basePath   = "data/chronicle-ipc"; //System.getProperty("java.io.tmpdir") + "/chronicle-ipc";
        final int    nbMessages = 10;

        new File(basePath).delete();

        ExecutorService svc = Executors.newFixedThreadPool(3);
        //svc.execute(new P3(basePath, nbMessages));
        //svc.execute(new P2(basePath, nbMessages));
        svc.execute(new P1(basePath, nbMessages));

        svc.shutdownNow();
        svc.awaitTermination(1, TimeUnit.MINUTES);
    }
}
