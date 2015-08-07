package com.github.lburgazzoli.openhft.examples.chronicle.logger.fix;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import quickfix.ApplicationAdapter;
import quickfix.Message;
import quickfix.SessionID;

public class FIXApplication extends ApplicationAdapter {
    private final Logger _logger;

    public FIXApplication() {
        _logger = LogManager.getLogger(FIXApplication.class);
    }

    public void onCreate(SessionID sessionID) {
        _logger.info("SessionID " + sessionID);
    }

    public void onLogon(SessionID sessionID) {
        _logger.info("Logged int " + sessionID);
    }

    public void onLogout(SessionID sessionID) {
        _logger.info("Logged out " + sessionID);
    }

    public void toAdmin(Message aMessage, SessionID sessionID) {
        _logger.info("toAdmin :" + aMessage.toString());
    }

    public void fromAdmin(Message aMessage, SessionID sessionID) {
        _logger.info("fromAdmin :" + aMessage.toString());
    }

    public void toApp(Message aMessage, SessionID sessionID) {
        _logger.info("toApp :" + aMessage.toString());
    }

    public void fromApp(Message aMessage, SessionID sessionID) {
        _logger.info("toAdmin :" + aMessage.toString());
    }
}
