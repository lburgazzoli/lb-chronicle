package com.github.lburgazzoli.openhft.examples.chronicle.queue;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.affinity.AffinityLock;
import net.openhft.affinity.Affinity;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChronicleProfile {
    public static final String DATA_PATH  = System.getProperty("data.path", "./data");
    public static final int    ITERATIONS = Integer.getInteger("iterations", 10000);

    public static void main(String[] args) throws Exception {
        System.out.println("data.path  " + DATA_PATH);
        System.out.println("iterations " + ITERATIONS);

        Chronicle chronicle = ChronicleQueueBuilder.vanilla(DATA_PATH, "source")
            .source()
            .bindAddress("localhost", 9876)
            .build();

        Thread.sleep(1000);

        Thread th1 = new Thread(new StatelessReader(), "stateless");
        Thread th2 = new Thread(new StatefulReader() , "stateful");

        th1.start();
        th2.start();

        AffinityLock lock = null;
        try {
            lock = Affinity.acquireLock();

            final ExcerptAppender appender = chronicle.createAppender();
            for(int i=0; i<ITERATIONS; i++) {
                appender.startExcerpt(4);
                appender.writeInt(i);
                appender.finish();
            }

            appender.close();
        } finally {
            if(lock != null) {
                lock.release();
            }
        }

        System.out.println("Finished writing data");

        th1.join();
        th2.join();

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
            AffinityLock lock = null;

            try {
                lock = Affinity.acquireLock();

                final String threadName = Thread.currentThread().getName();
                final Chronicle reader = createChronicle();
                final ExcerptTailer tailer = reader.createTailer();

                for(int i=0; i< ITERATIONS; ) {
                    if(tailer.nextIndex()) {
                        int value = tailer.readInt();
                        if((value > 0) && (value % 100000 == 0)) {
                            System.out.println(threadName + " > " + value);
                        }

                        tailer.finish();
                        i++;
                    } else {
                        Thread.sleep(10);
                    }
                }

                tailer.close();
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(lock != null) {
                    lock.release();
                }
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
                    DATA_PATH,
                    Thread.currentThread().getName())
                .sink()
                .connectAddress("localhost", 9876)
                .build();
        }
    }
}
