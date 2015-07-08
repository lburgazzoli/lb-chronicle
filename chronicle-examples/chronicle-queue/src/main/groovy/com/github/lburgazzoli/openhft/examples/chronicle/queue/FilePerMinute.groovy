package com.github.lburgazzoli.openhft.examples.chronicle.queue

import net.openhft.chronicle.Chronicle
import net.openhft.chronicle.ChronicleQueueBuilder
import net.openhft.chronicle.ExcerptAppender
import net.openhft.chronicle.ExcerptTailer

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit



class FilePerMinute {

    static final String FMT = 'yyyyMMdd'
    static final int    MIN = 24 * 60 * 60 * 1000
    static final File   TMP = new File(System.getProperty('java.io.tmpdir'), "FilePerMinute")

    // *************************************************************************
    //
    // *************************************************************************

    public static void main(String[] args) throws Exception {
        if(TMP.exists()) {
            def res = TMP.deleteDir()
            println("Delete ${TMP} : ${res}")
        }

        final def svc = Executors.newFixedThreadPool(2)
        svc.execute(createWriter(createChronicle()))
        svc.execute(createReader(createChronicle()))
        svc.awaitTermination(10, TimeUnit.MINUTES);
    }

    // *************************************************************************
    //
    // *************************************************************************

    static Runnable createReader(final Chronicle chron) {
        return  {
            ExcerptTailer tailer = null

            try {
                tailer = chron.createTailer()
                for(int i=0; i<1000;) {
                    if(tailer.nextIndex()) {
                        println("> R : ${tailer.readInt()}")
                        i++
                    } else {
                        Thread.sleep(1000)
                    }
                }
            } finally {
                tailer?.close()
            }
        } as Runnable
    }

    static def createWriter(final Chronicle chron) {
        return {
            ExcerptAppender appender = null
            try {
                appender = chron.createAppender()
                for (int i = 0; i < 1000; i++) {
                    println("> W : ${i}")

                    appender.startExcerpt(4)
                    appender.writeInt(i)
                    appender.finish()

                    Thread.sleep(1000)
                }
            } finally {
                appender?.close()
            }
        } as Runnable
    }

    static Chronicle createChronicle() throws IOException {
        return ChronicleQueueBuilder.vanilla(TMP)
            .cycleLength(MIN, false)
            .cycleFormat(FMT)
            .build();
    }
}
