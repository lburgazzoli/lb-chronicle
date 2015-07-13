package com.github.lburgazzoli.openhft.examples.chronicle.queue;

import net.openhft.affinity.AffinityLock;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChronicleGarbage {
    public static void main(String[] args) throws Exception {
        AtomicBoolean run = new AtomicBoolean(true);

        final String dataPath = System.getProperty("data.path","./data");
        final int iterations = Integer.getInteger("iterations", 10000);
        final CountDownLatch l1 = new CountDownLatch(iterations);
        final CountDownLatch l2 = new CountDownLatch(iterations);

        System.out.println("data.path  " + dataPath);
        System.out.println("iterations " + iterations);

        Thread readerTh1 = new Thread(() -> {
            try {
                Chronicle reader = ChronicleQueueBuilder.vanilla(dataPath, "cgc")
                        .cycleLength(60 * 1000, false)
                        .cycleFormat("yyyyMMddHHmm")
                        .entriesPerCycle(1L << 34)
                    .build();


                long count = 0;
                long start = System.nanoTime();

                AllocationMeasure am = new AllocationMeasure();
                ExcerptTailer tailer = reader.createTailer();
                while(run.get() && l1.getCount() > 0) {
                    if(tailer.nextIndex()) {
                        tailer.readInt();
                        tailer.finish();

                        count++;
                        l1.countDown();
                    }
                }
                long end = System.nanoTime();
                am.printAllocations("READER-1");

                System.out.printf("READER-1 : Took %.0f ms to read %d elements\n", (end - start) / 1e6, count);

                tailer.close();
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "READER-1");

        Thread readerTh2 = new Thread(() -> {
            try {
                Chronicle reader = ChronicleQueueBuilder.vanilla(dataPath, "cgc")
                        .cycleLength(60 * 1000, false)
                        .cycleFormat("yyyyMMddHHmm")
                        .entriesPerCycle(1L << 34)
                        .build();

                long count = 0;
                long start = System.nanoTime();

                AllocationMeasure am = new AllocationMeasure();
                ExcerptTailer tailer = reader.createTailer();
                while(run.get() && l2.getCount() > 0) {
                    if(tailer.nextIndex()) {
                        tailer.readInt();
                        tailer.finish();

                        count++;
                        l2.countDown();
                    }
                }

                long end = System.nanoTime();
                am.printAllocations("READER-2");

                System.out.printf("READER-2 : Took %.0f ms to read %d elements\n", (end - start) / 1e6, count);

                tailer.close();
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "READER-2");

        readerTh1.start();
        readerTh2.start();

        Chronicle writer = ChronicleQueueBuilder.vanilla(dataPath, "cgc")
                .cycleLength(60 * 1000, false)
                .cycleFormat("yyyyMMddHHmm")
                .entriesPerCycle(1L << 34)
            .build();

        ExcerptAppender appender = null;
        AllocationMeasure am = null;

        long start = System.nanoTime();
        am = new AllocationMeasure();

        appender = writer.createAppender();
        for (int i = 0; i < iterations; i++) {
            appender.startExcerpt(4);
            appender.writeInt(i);
            appender.finish();
        }

        long end = System.nanoTime();
        am.printAllocations("APPENDER");

        System.out.printf("APPENDER : Took %.0f ms to write %d elements\n", (end - start) / 1e6, iterations);

        l1.await(5, TimeUnit.MINUTES);
        l2.await(5, TimeUnit.MINUTES);

        run.set(false);

        readerTh1.join();
        readerTh2.join();

        if (appender != null) {
            appender.close();
        }

        writer.close();
        writer.clear();
    }
}
