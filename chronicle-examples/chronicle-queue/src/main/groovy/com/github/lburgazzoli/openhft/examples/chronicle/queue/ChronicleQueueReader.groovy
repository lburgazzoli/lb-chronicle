package com.github.lburgazzoli.openhft.examples.chronicle.queue

import net.openhft.chronicle.ChronicleQueueBuilder
 
class ChronicleQueueReader {
    public static void main(String[] args) throws Exception {
        def chronicle = ChronicleQueueBuilder.indexed("./data").build()
 
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
