/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.configuration;

import hudson.util.Secret;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;

@SuppressWarnings({"squid:S1312","squid:S00122"})
final public class ServerConfiguration {
	private static final Logger logger = LogManager.getLogger(ServerConfiguration.class);

	public String location;
	public String sharedSpace;
	public String username;
	public Secret password;
	public String impersonatedUser;

	public ServerConfiguration(String location, String sharedSpace, String username, Secret password, String impersonatedUser) {
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

	public boolean isValid() {
		boolean result = false;
		if (location != null && !location.isEmpty() &&
				sharedSpace != null && !sharedSpace.isEmpty()) {
			try {
				URL tmp = new URL(location);
				logger.debug(String.format("location: %s",tmp.toString()));
				result = true;
			} catch (MalformedURLException mue) {
				logger.error("configuration with malformed URL supplied", mue);
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return "{ url: " + location +
				", sharedSpace: " + sharedSpace +
				", username: " + username + " }";
	}
}
