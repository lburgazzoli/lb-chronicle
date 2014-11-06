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
import net.openhft.lang.io.IOTools;
import net.openhft.lang.io.serialization.BytesMarshallable;
import net.openhft.lang.model.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class IpcExample1 {

    // *************************************************************************
    //
    // *************************************************************************

    private static class Msg implements BytesMarshallable {
        public int index;
        public String data;

        public Msg() {
            this(0, "");
        }

        public Msg(int index, String data) {
            this.index = index;
            this.data = data;
        }

        public int index() {
            return this.index;
        }

        public Msg index(int index) {
            this.index = index;
            return this;
        }

        public String data() {
            return this.data;
        }

        public Msg data(String data) {
            this.data = data;
            return this;
        }

        @Override
        public void readMarshallable(@NotNull Bytes in) throws IllegalStateException {
            this.index = in.readInt();
            this.data  = in.readUTFΔ();
        }

        @Override
        public void writeMarshallable(@NotNull Bytes out) {
            out.writeInt(this.index);
            out.writeUTFΔ(this.data);
        }

        @Override
        public String toString() {
            return "Msg {" +
                " index="   + this.index +
                ", data="   + this.data  +
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
        private final CountDownLatch latch;

        public P1(@NotNull String basePath, int nbMessages, CountDownLatch latch) {
            this.basePath = basePath;
            this.nbMessages = nbMessages;
            this.latch = latch;
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

                ExcerptAppender writer = publisher.createAppender();
                ExcerptTailer reader = subscriber.createTailer();

                long start = System.currentTimeMillis();
                Msg msg = new Msg();

                for(int i=0; i<nbMessages && !Thread.interrupted(); i++) {
                    msg.index(i);
                    msg.data("request-" + i);

                    writer.startExcerpt();
                    writer.writeInstance(Msg.class, msg);
                    writer.finish();

                    while (!Thread.interrupted()) {
                        if (reader.nextIndex()) {
                            reader.readInstance(Msg.class, msg);
                            reader.finish();

                            if(msg.index() != i) {
                                LOGGER.warn("index : expected {}, got {}", i, msg.index());
                            }
                            if(!msg.data().equals("reply-" + i)) {
                                LOGGER.warn("data  : expected {}, got {}", "reply-" + i, msg.data());
                            }

                            break;
                        }
                    }

                    if((i % 1000) == 0) {
                        LOGGER.info(".. {}", i);
                    }
                }

                long end = System.currentTimeMillis();
                LOGGER.info("Done {}s", (end - start) / 1000);

                writer.close();
                reader.close();

                this.latch.countDown();
                this.latch.await(30, TimeUnit.SECONDS);

                publisher.close();
                subscriber.close();
            } catch(Exception e) {
                LOGGER.warn("", e);
            }
        }
    }

    private static final class P3 implements Runnable {
        private static final Logger LOGGER = LoggerFactory.getLogger(P3.class);

        private final String basePath;
        private final int nbMessages;
        private final CountDownLatch latch;

        public P3(@NotNull String basePath, int nbMessages, CountDownLatch latch) {
            this.basePath = basePath;
            this.nbMessages = nbMessages;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                Chronicle subscriber = new ChronicleSink(
                    new IndexedChronicle(basePath + "/queue-in-p3"),
                    "localhost",
                    10001);

                Chronicle publisher = new ChronicleSource(
                    new IndexedChronicle(basePath + "/queue-out-p3"),
                    10002);

                ExcerptTailer reader = subscriber.createTailer();
                ExcerptAppender writer = publisher.createAppender();

                long start = System.currentTimeMillis();
                Msg msg = new Msg();

                for(int i=0; i<nbMessages && !Thread.interrupted(); ) {
                    if(reader.nextIndex()) {
                        reader.readInstance(Msg.class, msg);
                        reader.finish();

                        if(msg.index() != i) {
                            LOGGER.warn("index : expected {}, got {}", i, msg.index());
                        }
                        if(!msg.data().equals("request-" + i)) {
                            LOGGER.warn("data  : expected {}, got {}", "req-" + i, msg.data());
                        }

                        msg.index(i);
                        msg.data("reply-" + i);

                        writer.startExcerpt();
                        writer.writeInstance(Msg.class, msg);
                        writer.finish();

                        if((i % 1000) == 0) {
                            LOGGER.info(".. {}", i);
                        }

                        i++;
                    }
                }

                reader.close();
                writer.close();

                long end = System.currentTimeMillis();
                LOGGER.info("Done {}s", (end - start) / 1000);

                this.latch.countDown();
                this.latch.await(30, TimeUnit.SECONDS);

                subscriber.close();
                publisher.close();
            } catch(Exception e) {
                LOGGER.warn("", e);
            }
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static void main(final String[] args) throws Exception {
        final String basePath = System.getProperty("java.io.tmpdir") + "/chronicle-ipc";
        final int nbMessages = 5000;
        final CountDownLatch latch = new CountDownLatch(2);

        IOTools.deleteDir(basePath);

        ExecutorService svc = Executors.newFixedThreadPool(3);
        svc.execute(new P3(basePath, nbMessages, latch));
        svc.execute(new P1(basePath, nbMessages, latch));

        latch.await(5, TimeUnit.MINUTES);

        svc.shutdown();
        svc.awaitTermination(30, TimeUnit.SECONDS);
    }
}
