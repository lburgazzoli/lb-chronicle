package com.github.lburgazzoli.openhft.chronicle.logger.slf4j.samples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleExample {

    public static void main(final String[] args) {
        Logger log = LoggerFactory.getLogger(SimpleExample.class);
        log.info("Hello Chronicle!");
    }
}
