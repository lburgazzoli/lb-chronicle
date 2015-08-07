package com.github.lburgazzoli.openhft.examples.chronicle.logger.fix;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import quickfix.DefaultMessageFactory;
import quickfix.MemoryStoreFactory;
import quickfix.SLF4JLogFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class FIXGatewayProcessor implements Runnable {
    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private final Logger _logger;
    private SocketInitiator _initiator = null;
    private int _index;

    public FIXGatewayProcessor(int index) throws IOException{
        this._logger = LogManager.getLogger(FIXGatewayProcessor.class);
        this._index = index;
    }

    public void connect() {
        try {
            if (_initiator == null) {
                Random random = new Random();
                SessionID sid = new SessionID("FIX.4.4","SENDER-" + this._index, "TARGET-" +  + this._index);
                SessionSettings settings = new SessionSettings();
                settings.setString(sid,"ConnectionType","initiator");
                settings.setString(sid,"BeginString",sid.getBeginString());
                settings.setString(sid,"SenderCompID",sid.getSenderCompID());
                settings.setString(sid,"TargetCompID",sid.getTargetCompID());
                settings.setString(sid,"StartTime","00:00:00");
                settings.setString(sid,"EndTime","23:59:59");
                settings.setString(sid,"HeartBtInt","30");
                settings.setString(sid,"ReconnectInterval","" + (10 + random.nextInt(5)));
                settings.setString(sid,"SocketConnectHost","127.0.0.1");
                settings.setString(sid,"SocketConnectPort","" + (10000 + this._index));

                _initiator = new SocketInitiator(
                    new FIXApplication(settings),
                    new MemoryStoreFactory(),
                    settings,
                    new SLF4JLogFactory(settings),
                    new DefaultMessageFactory());

                _initiator.start();
                _logger.debug("FIXGatewayProcessor established");
            }
            shutdownLatch.await();
        } catch (Exception ex) {
            _logger.error("" + ex.getMessage());
        }
    }

    public void disconnect() {
        shutdownLatch.countDown();
        if (_initiator != null) {
            _initiator.stop();
            _initiator = null;
        }
    }

    @Override
    public void run() {
        try {
            connect();
            Thread.sleep(2 * 1000 * 60);
            disconnect();
        } catch(Exception e) {
            _logger.warn("" + e.getMessage());
        }
    }
}
