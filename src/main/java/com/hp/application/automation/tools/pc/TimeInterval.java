package com.hp.application.automation.tools.pc;


public class TimeInterval {

    @SuppressWarnings("unused")
    private String xmlns = PcRestProxy.PC_API_XMLNS;
    private int days;
    private int hours;
    private int minutes;
    private int seconds;
    private boolean daysSpecified;
    private boolean hoursSpecified;
    private boolean minutesSpecified;
    private boolean secondsSpecified;

    public TimeInterval(int days, int hours, int minutes, int seconds, boolean daysSpecified, boolean hoursSpecified, boolean minutesSpecified, boolean secondsSpecified) {
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.daysSpecified = daysSpecified;
        this.hoursSpecified = hoursSpecified;
        this.minutesSpecified = minutesSpecified;
        this.secondsSpecified = secondsSpecified;
    }

    public int getDays() {
        return days;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public boolean isDaysSpecified() {
        return daysSpecified;
    }

    public boolean isHoursSpecified() {
        return hoursSpecified;
    }

    public boolean isMinutesSpecified() {
        return minutesSpecified;
    }

    public boolean isSecondsSpecified() {
        return secondsSpecified;
    }
}
