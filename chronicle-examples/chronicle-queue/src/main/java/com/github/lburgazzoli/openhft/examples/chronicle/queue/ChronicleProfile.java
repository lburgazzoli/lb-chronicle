package com.github.lburgazzoli.openhft.examples.chronicle.queue;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChronicleProfile {
    private static AtomicBoolean RUN = new AtomicBoolean(true);

    public static void main(String[] args) throws Exception {
        AtomicBoolean run = new AtomicBoolean(true);

        String dataPath = System.getProperty("data.path", "./data");
        int iterations = Integer.getInteger("iterations", 10000);

        System.out.println("data.path  " + dataPath);
        System.out.println("iterations " + iterations);

        Chronicle chronicle = ChronicleQueueBuilder.vanilla(dataPath, "source")
            .source()
            .bindAddress("localhost", 9876)
            .build();

        Thread.sleep(1000);

        Thread th1 = new Thread(new StatelessReader(), "stateless");
        Thread th2 = new Thread(new StatefulReader() , "stateful");

        th1.start();
        th2.start();

        Random random = new Random(System.currentTimeMillis());
        ExcerptAppender appender = chronicle.createAppender();
        for(int i=0; i<iterations; i++) {
            appender.startExcerpt(4);
            appender.writeInt(i);
            appender.finish();

            Thread.sleep(random.nextInt(250));
        }

        run.set(false);
        th1.join();
        th2.join();

        appender.close();
        chronicle.close();
        chronicle.clear();
    }

    // *************************************************************************
    //
    // *************************************************************************

    private static abstract class Reader implements Runnable {

        protected abstract Chronicle createChronicle() throws IOException;

        @Override
        public void run() {
            try {
                final String threadName = Thread.currentThread().getName();
                final Chronicle reader = createChronicle();
                final ExcerptTailer tailer = reader.createTailer();

                while(RUN.get()) {
                    if(tailer.nextIndex()) {
                        int value = tailer.readInt();
                        if((value > 0) && (value % 1000 == 0)) {
                            System.out.println(threadName + " > " + value);
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
        }
    }

    private static final class StatelessReader extends Reader {
        @Override
        protected Chronicle createChronicle() throws IOException {
            return ChronicleQueueBuilder.remoteTailer()
                .connectAddress("localhost", 9876)
                .build();
        }
    }

    private static final class StatefulReader extends Reader {
        @Override
        protected Chronicle createChronicle() throws IOException {
            return ChronicleQueueBuilder.vanilla(
                System.getProperty("data.path", "./data"),
                Thread.currentThread().getName())
                .sink()
                .connectAddress("localhost", 9876)
                .build();
        }
    }
}
