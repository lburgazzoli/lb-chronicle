package com.github.lburgazzoli.openhft.examples.chronicle.queue

import net.openhft.chronicle.ChronicleQueueBuilder
 
class ChronicleQueueWriter {
    public static void main(String[] args) throws Exception {
        def path      = args.length == 1 ? args[0] : './data'
        def chronicle = ChronicleQueueBuilder.indexed(path).build()

        //chronicle.clear()
 
        def appender = chronicle.createAppender();
        appender.startExcerpt(8)
        appender.writeLong(1)
        appender.finish() 
        appender.close()

        chronicle.close()
    }
}
