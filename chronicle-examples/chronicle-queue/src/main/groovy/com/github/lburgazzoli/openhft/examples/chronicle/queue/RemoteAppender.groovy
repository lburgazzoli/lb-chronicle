package com.github.lburgazzoli.openhft.examples.chronicle.queue
import net.openhft.chronicle.ChronicleQueueBuilder

class RemoteAppender {
    public static void main(String[] args) throws Exception {
        final def path  = args.length == 1 ? args[0] : './data'
        final int count = 100

        Thread reader = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Create a new Chronicle-Queue source
                    def source = ChronicleQueueBuilder.vanilla(path)
                        .source()
                        .bindAddress("localhost", 1234)
                        .build();

                    source.clear()
                    def reader = source.createTailer();
                    for (int i=0; i<count; ) {
                        if (reader.nextIndex()) {
                            println("received > " + reader.readInt())
                            reader.finish()

                            i++
                        } else {
                            Thread.sleep(250)
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        reader.start()
        Thread.sleep(1000L)

        // Create a new Chronicle-Queue source - Remote Appender
        def chronicle = ChronicleQueueBuilder.remoteAppender()
            .connectAddress("localhost", 1234)
            .build();

        def appender = chronicle.createAppender()
        (0..<count).each {
            appender.startExcerpt()
            appender.writeInt(it)
            appender.finish()
        }

        reader.join()
    }
}
