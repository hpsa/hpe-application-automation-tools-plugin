package com.hp.mqm.opb.service.logging;

/**
 * Created with IntelliJ IDEA.
 * User: menashee
 * Date: 23/02/14
 * Time: 13:39
 * Container for both user & detailed task loggers
 */
public interface ContextLoggers {

    public ContextDetailedLogger getDetailedLogger();

    public ContextUserLogger getUserLogger();

    public ContextProgressReporter getContextProgressReporter();

    public void setContextProgressReporter(ContextProgressReporter contextProgressReporter);
    
    public void markToRemoveLog();
    
    public boolean isMarkToRemoveLog();
}
