package com.github.lburgazzoli.openhft.examples.chronicle.queue
import net.openhft.chronicle.ChronicleQueueBuilder

class RemoteAppender {
    public static void main(String[] args) throws Exception {
        Thread reader = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Create a new Chronicle-Queue source
                    def source = ChronicleQueueBuilder.indexed("/tmp/stuff/chronicle-queue")
                        .source()
                        .bindAddress("localhost", 1234)
                        .build();

                    def reader = source.createTailer();
                    while (true) {
                        while (!reader.nextIndex()) {
                            Thread.sleep(500)
                        }

                        System.out.println("r > " + reader.readLong());
                        reader.finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        reader.setDaemon(false);
        reader.start();
        Thread.sleep(1000L);

        // Create a new Chronicle-Queue source - Remote Appender
        def chronicle = ChronicleQueueBuilder.remoteAppender()
            .connectAddress("localhost", 1234)
            .build();

        def appender = chronicle.createAppender();
        appender.startExcerpt();
        appender.writeLong(new Random().nextLong());
        appender.finish();

        Thread.sleep(1000)
    }
}
