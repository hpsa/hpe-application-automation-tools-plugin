package com.hpe.application.automation.tools.model;

public class TimeslotDuration {

    private int Hours;

    private int Minutes;

    public TimeslotDuration(int hours, int minutes) {

        Hours = hours + minutes / 60;
        Minutes = minutes % 60;
    }

    public TimeslotDuration(String hours, String minutes) {
        
        try{
            int m = Integer.parseInt(minutes);
            int h = Integer.parseInt(hours) + m / 60;
            if (h < 480) {
                Hours = h;
                Minutes = m % 60;
            } else {
                Hours = 480;
                Minutes = 0;                
            }
        } catch (Exception e) {
            Hours = 0;
            Minutes = 0;
        }
    }

    public TimeslotDuration(int minutes) {

        this(0, minutes);
    }

    public int getHours() {

        return Hours;
    }

    public int getMinutes() {

        return Minutes;
    }

    public int toMinutes() {
        
        return Hours * 60 + Minutes;
    }

    @Override
    public String toString() {

        return String.format("%d:%02d(h:mm)", Hours, Minutes);
    }

}
