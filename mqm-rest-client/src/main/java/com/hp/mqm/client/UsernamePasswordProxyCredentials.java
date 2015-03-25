package com.hp.mqm.client;

public class UsernamePasswordProxyCredentials implements ProxyCredentials {

    private String username;
    private String password;

    public UsernamePasswordProxyCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
