package com.hp.octane.plugins.jetbrains.teamcity.utils;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;


@XmlRootElement(name = "NGAconfig")
public class Config {





    private String identity;
    private String identityFrom;
    private String uiLocation;
    private String username;
    private String secretPassword;
    private String impersonatedUser;
    private String location;
    private String sharedSpace;



    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }


    public String getIdentityFrom() {
        return identityFrom;
    }

    public void setIdentityFrom(String identityFrom) {
        this.identityFrom = identityFrom;
    }

    public long getIdentityFromAsLong() {

        return Long.valueOf(identityFrom);
    }




    public String getUiLocation() {
        return uiLocation;
    }

    public void setUiLocation(String uiLocation) {
        this.uiLocation = uiLocation;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }



    public String getSecretPassword() {
        return secretPassword;
    }

    public void setSecretPassword(String secretPassword) {
        this.secretPassword = secretPassword;
    }



    public String getImpersonatedUser() {
        return impersonatedUser;
    }

    public void setImpersonatedUser(String impersonatedUser) {
        this.impersonatedUser = impersonatedUser;
    }



    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


   // @XmlElement(name = "SharedSpace")
    public String getSharedSpace() {
        return sharedSpace;
    }

    public void setSharedSpace(String sharedSpace) {
        this.sharedSpace = sharedSpace;
    }




    @Override
    public String toString() {
        return "Config{" +
                "identity='" + identity + '\'' +
                ", identityFrom='" + identityFrom + '\'' +
                ", uiLocation='" + uiLocation + '\'' +
                ", username='" + username + '\'' +
                ", secretPassword='" + secretPassword + '\'' +
                ", impersonatedUser='" + impersonatedUser + '\'' +
                ", location='" + location + '\'' +
                ", sharedSpace='" + sharedSpace + '\'' +
                '}';
    }


    public void getParamsFromConfig(Config old_cfg)
    {
        this.identity = old_cfg.getIdentity();
        this.identityFrom = old_cfg.getIdentityFrom();
        this.uiLocation = old_cfg.getUiLocation();
        this.username  = old_cfg.getUsername();
        this.secretPassword  = old_cfg.getSecretPassword();
        this.impersonatedUser = old_cfg.getImpersonatedUser();
        this.location = old_cfg.getLocation();
        this.sharedSpace = old_cfg.getSharedSpace();
    }
}
