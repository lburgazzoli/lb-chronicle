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
import net.openhft.chronicle.tools.ChronicleTools;
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

    private static final class ChronicleCopy implements Runnable {
        private final Logger logger;
        private final int nbMessages;
        private final CountDownLatch latch;
        private final ExcerptTailer sourceExcerpt;
        private final ExcerptAppender destinationExcerpt;

        public ChronicleCopy(String name, int nbMessages, Chronicle source, Chronicle destination, CountDownLatch latch) throws IOException {
            this.logger = LoggerFactory.getLogger(ChronicleCopy.class.getName() + "@" + name);
            this.nbMessages = nbMessages;
            this.latch = latch;
            this.sourceExcerpt = source.createTailer();
            this.destinationExcerpt = destination.createAppender();
        }

        @Override
        public void run() {
            try {
                logger.info("Starting ChronicleCopy");

                long start = System.currentTimeMillis();
                Msg msg = new Msg();

                for(int i=0; i< nbMessages && !Thread.interrupted();) {
                    if(sourceExcerpt.nextIndex()) {
                        sourceExcerpt.readInstance(Msg.class, msg);
                        sourceExcerpt.finish();

                        destinationExcerpt.startExcerpt();
                        destinationExcerpt.writeInstance(Msg.class, msg);
                        destinationExcerpt.finish();

                        if((i % 100) == 0) {
                            logger.info(".. {}", i);
                        }

                        i++;
                    }
                }

                long end = System.currentTimeMillis();
                logger.info("Done {}s", (end - start) / 1000);

                sourceExcerpt.close();
                destinationExcerpt.close();

                latch.countDown();
                latch.await(60, TimeUnit.SECONDS);


                sourceExcerpt.chronicle().close();
                destinationExcerpt.chronicle().close();
            } catch(Exception e) {
                logger.warn("", e);
            }
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
        private final ExecutorService executorService;

        public P1(@NotNull String basePath, int nbMessages, ExecutorService executorService, CountDownLatch latch) {
            this.basePath = basePath;
            this.nbMessages = nbMessages;
            this.latch = latch;
            this.executorService = executorService;
        }

        @Override
        public void run() {
            try {
                LOGGER.info("Starting P1");

                Chronicle p1out = new IndexedChronicle(basePath + "/queue-p1-out");
                Chronicle p1in = new IndexedChronicle(basePath + "/queue-p1-in");

                ExcerptAppender writer = p1out.createAppender();
                ExcerptTailer reader = p1in.createTailer();

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

                    if((i % 100) == 0) {
                        LOGGER.info(".. {}", i);
                    }
                }

                long end = System.currentTimeMillis();
                LOGGER.info("Done {}s", (end - start) / 1000);

                writer.close();
                reader.close();

                latch.countDown();
                latch.await(60, TimeUnit.SECONDS);

                p1out.close();
                p1in.close();
            } catch(Exception e) {
                LOGGER.warn("", e);
            }
        }
    }


    private static final class P2 implements Runnable {
        private static final Logger LOGGER = LoggerFactory.getLogger(P2.class);

        private final String basePath;
        private final int nbMessages;
        private final CountDownLatch latch;
        private final ExecutorService executorService;

        public P2(@NotNull String basePath, int nbMessages, ExecutorService executorService, CountDownLatch latch) {
            this.basePath = basePath;
            this.nbMessages = nbMessages;
            this.latch = latch;
            this.executorService = executorService;
        }

        @Override
        public void run() {
            try {
                LOGGER.info("Starting P2");

                Chronicle p1out = new IndexedChronicle(basePath + "/queue-p1-out");
                Chronicle p1in = new IndexedChronicle(basePath + "/queue-p1-in");

                Chronicle publisher = new ChronicleSource(
                    new IndexedChronicle(basePath + "/queue-p2-source"),
                    10001);
                Chronicle subscriber = new ChronicleSink(
                    "localhost",
                    10002);

                executorService.submit(new ChronicleCopy("sub-to-p1", nbMessages, subscriber , p1in, latch));
                executorService.submit(new ChronicleCopy("p1-to-pub", nbMessages, p1out, publisher, latch));

                latch.countDown();
                LOGGER.info("Done");
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
        private final ExecutorService executorService;

        public P3(@NotNull String basePath, int nbMessages, ExecutorService executorService, CountDownLatch latch) {
            this.basePath = basePath;
            this.nbMessages = nbMessages;
            this.latch = latch;
            this.executorService = executorService;
        }

        @Override
        public void run() {
            try {
                LOGGER.info("Starting P3");

                Chronicle subscriber = new ChronicleSink(
                    new IndexedChronicle(basePath + "/queue-p3-in"),
                    "localhost",
                    10001);

                Chronicle publisher = new ChronicleSource(
                    new IndexedChronicle(basePath + "/queue-p3-out"),
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

                        if((i % 100) == 0) {
                            LOGGER.info(".. {}", i);
                        }

                        i++;
                    }
                }

                reader.close();
                writer.close();

                long end = System.currentTimeMillis();
                LOGGER.info("Done {}s", (end - start) / 1000);

                latch.countDown();
                latch.await(60, TimeUnit.SECONDS);

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
        final CountDownLatch latch = new CountDownLatch(5);

        ChronicleTools.warmup();
        IOTools.deleteDir(basePath);

        ExecutorService svc = Executors.newCachedThreadPool();
        svc.execute(new P3(basePath, nbMessages, svc, latch));
        svc.execute(new P1(basePath, nbMessages, svc, latch));
        svc.execute(new P2(basePath, nbMessages, svc, latch));

        latch.await(2, TimeUnit.MINUTES);

        svc.shutdown();
        svc.awaitTermination(30, TimeUnit.SECONDS);
    }
}
