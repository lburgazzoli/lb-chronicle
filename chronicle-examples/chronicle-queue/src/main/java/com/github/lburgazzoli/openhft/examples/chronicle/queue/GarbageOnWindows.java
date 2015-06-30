package com.github.lburgazzoli.openhft.examples.chronicle.queue;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class GarbageOnWindows {
    public static void main(String[] args) throws Exception {
        AtomicBoolean run = new AtomicBoolean(true);

        Chronicle chronicle = ChronicleQueueBuilder.vanilla(args[0], "gow").build();
        Chronicle source = ChronicleQueueBuilder.source(chronicle).bindAddress("localhost", 9876).build();

        Thread.sleep(1000);

        Thread readerTh = new Thread(() -> {
            try {

                Chronicle reader = ChronicleQueueBuilder
                    .remoteTailer()
                    .connectAddress("localhost", 9876)
                    .readSpinCount(1)
                    .build();

                ExcerptTailer tailer = reader.createTailer();
                while(run.get()) {
                    if(tailer.nextIndex()) {
                        if(tailer.readInt() % 100 == 0) {
                            System.out.println(".");
                        }

                        tailer.finish();
                    } else {
                        Thread.sleep(10);
                    }
                }

                tailer.close();
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        readerTh.start();

        Random random = new Random(System.currentTimeMillis());
        ExcerptAppender appender = chronicle.createAppender();
        for(int i=0;i<1000000;i++) {
            appender.startExcerpt(4);
            appender.writeInt(i);
            appender.finish();

            Thread.sleep(random.nextInt(250));
        }

        run.set(false);
        readerTh.join();

        appender.close();
        chronicle.close();
        chronicle.clear();
    }
}
