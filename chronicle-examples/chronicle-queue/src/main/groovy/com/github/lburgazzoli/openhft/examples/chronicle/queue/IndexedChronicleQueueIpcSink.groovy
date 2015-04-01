package com.github.lburgazzoli.openhft.examples.chronicle.queue

import net.openhft.chronicle.ChronicleQueueBuilder

//@Grab(group='net.openhft', module='chronicle', version='3.4.2')
class IndexedChronicleQueueIpcSink {
    static def main(String[] args) throws Exception {
        def tmpdir = System.properties."java.io.tmpdir";
        def chronicle = ChronicleQueueBuilder.indexed(tmpdir + "/ipc-sink")
                .sink()
                .connectAddress("localhost", 9876)
                .build()

        def tailer = chronicle.createTailer()
        while(true) {
            if(tailer.nextIndex()) {
                long v = tailer.readLong()
                System.out.println("Got " + v)

                if(v == 100) {
                    break;
                }
            } else {
                System.out.println("Wait ...")
                Thread.sleep(1000);
            }
        }

        tailer.close()
        chronicle.close()
        chronicle.clear()
    }
}
