package com.github.lburgazzoli.openhft.examples.chronicle.logger.fix;

import net.openhft.chronicle.ChronicleQueueBuilder;

import net.openhft.chronicle.logger.tools.ChroniTool;

public class FIXLogTailer {
    public static void main(String[] args) throws Exception{
        ChroniTool.process(
            ChronicleQueueBuilder.vanilla(System.getProperty("java.io.tmpdir"), "chronicle-fix").build(),
            ChroniTool.READER_BINARY,
            true,
            true);
    }
}
