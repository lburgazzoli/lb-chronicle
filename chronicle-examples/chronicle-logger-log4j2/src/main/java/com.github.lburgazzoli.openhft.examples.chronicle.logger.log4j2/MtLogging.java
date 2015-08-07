package com.github.lburgazzoli.openhft.examples.chronicle.logger.log4j2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class MtLogging {
    public static void main(final String[] args) throws Exception {
        Thread th1 = new Thread(() -> {
            final String name = "thread-1";
            final Logger log = LogManager.getLogger(name);
            for (int m = 0; m < 5; m++) {
                System.out.println("write " + name + " : " + m);
                log.info("message " + m);
            }

            System.out.println("done " + name);
        });

        Thread th2 = new Thread(() -> {
            final String name = "thread-2";
            final Logger log = LogManager.getLogger(name);
            for (int m = 0; m < 5; m++) {
                System.out.println("write " + name + " : " + m);
                log.info("message " + m);
            }

            System.out.println("done " + name);
        });

        th1.start();
        th2.start();
        th1.join();
        th2.join();
    }
}
