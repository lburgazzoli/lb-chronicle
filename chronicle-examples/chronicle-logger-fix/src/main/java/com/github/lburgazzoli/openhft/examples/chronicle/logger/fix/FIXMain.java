package com.github.lburgazzoli.openhft.examples.chronicle.logger.fix;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FIXMain {
    public static void main(String[] args) throws Exception {
        Random random = new Random();
        ExecutorService svc = Executors.newFixedThreadPool(10);
        for(int i=0; i<2; i++) {
            Thread.sleep(random.nextInt(5000));
            svc.submit(new FIXGatewayProcessor(i));
        }

        svc.shutdown();
        svc.awaitTermination(1, TimeUnit.HOURS);
    }
}
