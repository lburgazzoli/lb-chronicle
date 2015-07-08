package com.github.lburgazzoli.openhft.examples.chronicle.queue;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.VanillaChronicle;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChronicleGarbage {
    public static void main(String[] args) throws Exception {
        AtomicBoolean run = new AtomicBoolean(true);

        String dataPath = System.getProperty("data.path","./data");
        int iterations = Integer.getInteger("iterations", 10000);
        CountDownLatch l1 = new CountDownLatch(iterations);
        CountDownLatch l2 = new CountDownLatch(iterations);

        System.out.println("data.path  " + dataPath);
        System.out.println("iterations " + iterations);

        Thread readerTh1 = new Thread(() -> {
            try {
                Chronicle reader = ChronicleQueueBuilder.vanilla(dataPath, "cgc")
                    .cycle(VanillaChronicle.Cycle.MINUTES)
                    .cycleFormat("yyyyMMdd/HHmm")
                    .build();

                ExcerptTailer tailer = reader.createTailer();
                while(run.get() && l1.getCount() > 0) {
                    if(tailer.nextIndex()) {
                        tailer.readInt();
                        tailer.finish();

                        l1.countDown();
                    }
                }

                tailer.close();
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread readerTh2 = new Thread(() -> {
            try {
                Chronicle reader = ChronicleQueueBuilder.vanilla(dataPath, "cgc")
                        .cycle(VanillaChronicle.Cycle.MINUTES)
                        .cycleFormat("yyyyMMdd/HHmm")
                        .build();

                ExcerptTailer tailer = reader.createTailer();
                while(run.get() && l2.getCount() > 0) {
                    if(tailer.nextIndex()) {
                        tailer.readInt();
                        tailer.finish();

                        l2.countDown();
                    }
                }

                tailer.close();
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        readerTh1.start();
        readerTh2.start();

        Chronicle writer = ChronicleQueueBuilder.vanilla(dataPath, "cgc")
            .cycle(VanillaChronicle.Cycle.MINUTES)
            .cycleFormat("yyyyMMdd/HHmm")
            .build();

        ExcerptAppender appender = writer.createAppender();
        for(int i=0; i<iterations; i++) {
            appender.startExcerpt(4);
            appender.writeInt(i);
            appender.finish();

            if(i % 10000 == 0) {
                Thread.sleep(250);
            }
            if(i % 1000000 == 0) {
                System.out.println(".");
            }
        }

        System.out.println("Done writing");
        l1.await(5, TimeUnit.MINUTES);
        l2.await(5, TimeUnit.MINUTES);

        run.set(false);

        readerTh1.join();
        readerTh2.join();

        appender.close();
        writer.close();
        writer.clear();
    }
}
