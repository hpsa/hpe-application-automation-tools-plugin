package com.hp.mqm.atrf.octane.services;

/**
 * Created by berkovir on 05/12/2016.
 */
public class OctaneAuthenticationPojo {

    private String user;
    private String password;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null) {
            this.password = "";
        } else {
            this.password = password;
        }


    }
}
