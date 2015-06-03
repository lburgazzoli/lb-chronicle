package com.github.lburgazzoli.openhft.examples.chronicle.queue

import net.openhft.chronicle.ChronicleQueueBuilder
 
class ChronicleQueueWriter {
    public static void main(String[] args) throws Exception {
        def chronicle = ChronicleQueueBuilder.indexed("./data").build()
 
        chronicle.clear()
 
        def appender = chronicle.createAppender();
        appender.startExcerpt(8)
        appender.writeLong(1)
        appender.finish() 
        appender.startExcerpt(8)
        appender.writeLong(1)
        appender.finish() 
        appender.close()

        chronicle.close()
    }
}
