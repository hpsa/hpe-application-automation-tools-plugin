// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.common.configuration;

final public class ServerConfiguration {

	public String location;
	public String sharedSpace;
	public String username;
	public String password;
	public String impersonatedUser;

	public ServerConfiguration(String location, String sharedSpace, String username, String password, String impersonatedUser) {
		this.location = location;
		this.sharedSpace = sharedSpace;
		this.username = username;
		this.password = password;
		this.impersonatedUser = impersonatedUser;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ServerConfiguration that = (ServerConfiguration) o;

		if (location != null ? !location.equals(that.location) : that.location != null) return false;
		if (sharedSpace != null ? !sharedSpace.equals(that.sharedSpace) : that.sharedSpace != null) return false;
		if (username != null ? !username.equals(that.username) : that.username != null) return false;
		if (password != null ? !password.equals(that.password) : that.password != null) return false;
		if (impersonatedUser != null ? !impersonatedUser.equals(that.impersonatedUser) : that.impersonatedUser != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = location != null ? location.hashCode() : 0;
		result = 31 * result + (sharedSpace != null ? sharedSpace.hashCode() : 0);
		result = 31 * result + (username != null ? username.hashCode() : 0);
		result = 31 * result + (password != null ? password.hashCode() : 0);
		result = 31 * result + (impersonatedUser != null ? impersonatedUser.hashCode() : 0);
		return result;
	}
}
