package com.hp.mqm.opb.loopback.mock.service.logging;

import com.hp.mqm.opb.api.AgentLogger;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA. User: kachko Date: 5/9/13 Time: 2:18 PM To change this template use
 * File | Settings | File Templates.
 */
public class AgentLoggerMockImpl implements AgentLogger {
    
    private static final long serialVersionUID = -3281251832117817277L;
    private static volatile AgentLoggerMockImpl agentLogger;
    
    /**
     * gets the logger
     * 
     * @return Agent Logger
     */
    public static AgentLogger getAgentLogger() {
        if (agentLogger == null) {
            synchronized (AgentLoggerMockImpl.class) {
                if (agentLogger == null) {
                    agentLogger = new AgentLoggerMockImpl();
                }
            }
        }
        return agentLogger;
    }
    
    @Override
    public void debug(Class clazz, String message) {
        printLog(clazz, Level.ALL, message);
    }
    
    @Override
    public void info(Class clazz, String message) {
        printLog(clazz, Level.INFO, message);
    }
    
    @Override
    public void warn(Class clazz, String message) {
        printLog(clazz, Level.WARNING, message);
    }
    
    @Override
    public void error(Class clazz, String message) {
        printLog(clazz, Level.SEVERE, message);
    }
    
    private void printLog(Class clazz, Level loLevel, String message) {
        Logger l = Logger.getLogger(clazz.getName());
        l.logp(loLevel, clazz.getName(), "", message);
    }
    
    @Override
    public void error(Class clazz, String message, Throwable t) {
        errorWithThrowable(clazz, Level.SEVERE, message, t);
    }
    
    @Override
    public void fatal(Class clazz, String message) {
        printLog(clazz, Level.SEVERE, message);
    }
    
    @Override
    public void fatal(Class clazz, String message, Throwable t) {
        errorWithThrowable(clazz, Level.SEVERE, message, t);
    }
    
    private void errorWithThrowable(Class clazz, Level loLevel, String message, Throwable t) {
        Logger l = Logger.getLogger(clazz.getName());
        l.logp(loLevel, clazz.getName(), "", message, t);
    }
    
    @Override
    public boolean isDebugEnabled(Class clazz) {
        return Logger.getLogger(clazz.getName()).isLoggable(Level.ALL);
    }
    
}
