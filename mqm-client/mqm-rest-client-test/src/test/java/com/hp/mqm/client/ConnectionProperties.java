package com.hp.mqm.client;

public class ConnectionProperties {

	public static String getLocation() {
		return getStringValue("mqm.location", "http://localhost:8080/qcbin");
	}

	public static String getSharedSpace() {
		return getStringValue("mqm.sharedSpace", "DEFAULT");
	}

	public static String getUsername() {
		return getStringValue("mqm.user", "admin");
	}

	public static String getPassword() {
		return getStringValue("mqm.password", "changeit");
	}

	public static String getProxyHost() {
		return getStringValue("mqm.proxyHost", null);
	}

	public static Integer getProxyPort() {
		return getIntValue("mqm.proxyPort", null);
	}

	public static String getProxyUsername() {
		return getStringValue("mqm.proxyUsername", null);
	}

	public static String getProxyPassword() {
		return getStringValue("mqm.proxyPassword", null);
	}

	private static Integer getIntValue(String propName, Integer defaultValue) {
		String value = System.getProperty(propName);
		return value != null ? Integer.valueOf(value) : defaultValue;
	}

	private static String getStringValue(String propName, String defaultValue) {
		String value = System.getProperty(propName);
		return value != null ? value : defaultValue;
	}
}
