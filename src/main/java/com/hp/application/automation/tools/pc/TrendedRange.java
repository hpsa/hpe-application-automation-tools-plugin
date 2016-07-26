package com.hp.application.automation.tools.pc;


public class TrendedRange {

    @SuppressWarnings("unused")
    private String xmlns = PcRestProxy.PC_API_XMLNS;
    private TimeInterval startTime;
    private TimeInterval endTime;

    public TrendedRange(TimeInterval startTime, TimeInterval endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public TimeInterval getStartTime() {
        return startTime;
    }

    public TimeInterval getEndTime() {
        return endTime;
    }
}
