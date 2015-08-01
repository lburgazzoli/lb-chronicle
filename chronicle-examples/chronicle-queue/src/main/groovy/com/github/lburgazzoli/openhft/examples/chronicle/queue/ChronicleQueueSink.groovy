package com.github.lburgazzoli.openhft.examples.chronicle.queue

import net.openhft.chronicle.ChronicleQueueBuilder
import org.slf4j.LoggerFactory

class ChronicleQueueSink {
    public static void main(String[] args) throws Exception {
        def log       = LoggerFactory.getLogger(ChronicleQueueSink.class)
        def path      = args.length == 1 ? args[0] : './data'
        def chronicle = ChronicleQueueBuilder.remoteTailer().connectAddress('localhost',9876).build()
 
        def tailer = chronicle.createTailer()
        for(int count=0; count < 1000; ) {
            boolean hasIndex = tailer.nextIndex()
            log.info "HasIndex: ${hasIndex}"

            if(hasIndex) {
                tailer.finish();
            } else {
                Thread.sleep(250)
            }
        }
 
        tailer.close()
        chronicle.close()
        chronicle.clear()
    }
}
