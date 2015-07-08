package com.github.lburgazzoli.openhft.examples.chronicle.queue;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class SelectorGarbage {
    public static void main(String[] args) throws Exception {
        AtomicBoolean run = new AtomicBoolean(true);

        String dataPath = System.getProperty("data.path");
        int iterations = Integer.getInteger("iterations", 10000);

        System.out.println("data.path  " + dataPath);
        System.out.println("iterations " + iterations);

        Chronicle chronicle = ChronicleQueueBuilder.vanilla(dataPath != null ? dataPath : "./data", "gow").build();
        
        Chronicle source = ChronicleQueueBuilder.source(chronicle)
            .selectorSpinLoopCount(-1)
            .bindAddress("localhost", 9876)
            .build();

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
                        int value = tailer.readInt();
                        if((value > 0) && (value % 1000 == 0)) {
                            System.out.println("> " + value);
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
        for(int i=0; i<iterations; i++) {
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
