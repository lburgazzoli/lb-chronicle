package com.github.lburgazzoli.openhft.examples.chronicle.logger.fix;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import quickfix.Application;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.fix44.MessageCracker;

public class FIXApplication extends MessageCracker implements Application {
    private final Logger _logger;

    public FIXApplication(SessionSettings settings) {
        _logger = LogManager.getLogger(FIXApplication.class);
    }

    public void onCreate(SessionID sessionID) {
        _logger.trace("SessionID " + sessionID);
    }

    public void onLogon(SessionID sessionID) {
        _logger.debug("Logged int " + sessionID);
    }

    public void onLogout(SessionID sessionID) {
        _logger.debug("Logged out " + sessionID);
    }

    public void toAdmin(Message aMessage, SessionID sessionID) {
        _logger.trace(aMessage);
    }

    public void fromAdmin(Message aMessage, SessionID sessionID) {
        _logger.trace(aMessage);
    }

    public void toApp(Message aMessage, SessionID sessionID) {
        _logger.trace(aMessage);
    }

    public void fromApp(Message aMessage, SessionID sessionID) {
        _logger.trace(aMessage);
    }
}
