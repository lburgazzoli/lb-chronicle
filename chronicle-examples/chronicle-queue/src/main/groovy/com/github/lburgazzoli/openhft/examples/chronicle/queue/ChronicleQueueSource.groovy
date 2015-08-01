package com.github.lburgazzoli.openhft.examples.chronicle.queue

import net.openhft.chronicle.ChronicleQueueBuilder
import org.slf4j.LoggerFactory

class ChronicleQueueSource {
    public static void main(String[] args) throws Exception {
        def log       = LoggerFactory.getLogger(ChronicleQueueSource.class)
        def path      = args.length == 1 ? args[0] : './data'
        def chronicle = ChronicleQueueBuilder.vanilla(path).source().bindAddress(9876).build()

        chronicle.clear()
 
        def appender = chronicle.createAppender();
        for(int i=0; i<10;i++) {
            log.info("Loop ${i}")
            Thread.sleep(1000)
        }

        appender.close()
        chronicle.close()
    }
}
