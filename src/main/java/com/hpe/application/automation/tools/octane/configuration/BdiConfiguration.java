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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by benmeior on 12/12/2016
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class BdiConfiguration {
	private String host;
	private String port;
	private Long tenantId;
	private boolean accessTokenFlavor;

	public String getHost() {
		return host;
	}

	public String getPort() {
		return port;
	}

	public Long getTenantId() {
		return tenantId;
	}

	public boolean isAccessTokenFlavor() {
		return accessTokenFlavor;
	}

	public boolean isFullyConfigured() {
		return host != null && !host.isEmpty() && port != null && tenantId != null;
	}

	@Override
	public String toString() {
		return "BdiConfiguration: {" +
				"host: '" + host + "'" +
				", port: '" + port + "'" +
				", tenantId: " + tenantId +
				", accessTokenFlavor: " + accessTokenFlavor + "}";
	}
}
