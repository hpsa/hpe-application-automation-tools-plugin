package com.hp.octane.plugins.jetbrains.teamcity.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "octane-config")
@XmlAccessorType(XmlAccessType.NONE)
public class OctaneConfigStructure {

	@XmlElement
	private String identity;
	@XmlElement
	private String identityFrom;
	@XmlElement
	private String uiLocation;
	@XmlElement(name = "api-key")
	private String username;
	@XmlElement(name = "secret")
	private String secretPassword;
	@XmlElement
	private String location;
	@XmlElement
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

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getSharedSpace() {
		return sharedSpace;
	}

	public void setSharedSpace(String sharedSpace) {
		this.sharedSpace = sharedSpace;
	}

	@Override
	public String toString() {
		return "OctaneConfigStructure { " +
				"identity: " + identity +
				", identityFrom: " + identityFrom +
				", uiLocation: " + uiLocation +
				", apiKey: " + username +
				", secret: " + secretPassword +
				", location: " + location +
				", sharedSpace: " + sharedSpace + '}';
	}
}
