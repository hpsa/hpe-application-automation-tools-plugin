package com.hp.octane.plugins.jetbrains.teamcity.configuration;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "NGAconfig")
public class NGAConfigStructure {
	private String identity;
	private String identityFrom;
	private String uiLocation;
	private String username;
	private String secretPassword;
	private String location;
	private Long sharedSpace;

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

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Long getSharedSpace() {
		return sharedSpace;
	}

	public void setSharedSpace(Long sharedSpace) {
		this.sharedSpace = sharedSpace;
	}

	@Override
	public String toString() {
		return "Config { " +
				"identity: " + identity +
				", identityFrom: " + identityFrom +
				", uiLocation: " + uiLocation +
				", username: " + username +
				", secretPassword: " + secretPassword +
				", location: " + location +
				", sharedSpace: " + sharedSpace + '}';
	}
}
