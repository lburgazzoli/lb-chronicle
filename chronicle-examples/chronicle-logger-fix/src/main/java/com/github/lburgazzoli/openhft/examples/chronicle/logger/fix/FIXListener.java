package com.github.lburgazzoli.openhft.examples.chronicle.logger.fix;

import quickfix.DefaultMessageFactory;
import quickfix.MemoryStoreFactory;
import quickfix.SLF4JLogFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;

/**
 * Created by lb on 07/08/15.
 */
public class FIXListener {
    public static void main(String[] args) throws Exception {
        SessionSettings settings = new SessionSettings();

        for(int i=0;i<10; i++) {
            SessionID sid = new SessionID("FIX.4.4", "TARGET-" + i, "SENDER-" + i);

            settings.setString(sid, "ConnectionType", "acceptor");
            settings.setString(sid, "BeginString", sid.getBeginString());
            settings.setString(sid, "SenderCompID", sid.getSenderCompID());
            settings.setString(sid, "TargetCompID", sid.getTargetCompID());
            settings.setString(sid, "StartTime", "00:00:00");
            settings.setString(sid, "EndTime", "23:59:59");
            settings.setString(sid, "HeartBtInt", "30");
            settings.setString(sid, "SocketAcceptPort", "" + (10000 + i));
            settings.setString(sid, "ResetOnLogon", "Y");

        }

        SocketAcceptor acceptor = new SocketAcceptor(
                new FIXApplication(),
                new MemoryStoreFactory(),
                settings,
                new SLF4JLogFactory(settings),
                new DefaultMessageFactory());

        acceptor.start();
    }
}
