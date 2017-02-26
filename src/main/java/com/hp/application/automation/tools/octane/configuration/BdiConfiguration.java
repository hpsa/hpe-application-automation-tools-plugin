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

package com.hp.application.automation.tools.octane.configuration;

import net.sf.json.JSONObject;

/**
 * Created by benmeior on 12/12/2016.
 */
public class BdiConfiguration {

    private String host;
    private String port;
    private String tenantId;

    private BdiConfiguration(String host, String port, String tenantId) {
        this.host = host;
        this.port = port;
        this.tenantId = tenantId;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getTenantId() {
        return tenantId;
    }

    public boolean isFullyConfigured() {
        return host != null && port != null && tenantId != null;
    }

    public static BdiConfiguration fromJSON(JSONObject jsonObject) {
        if (jsonObject == null || !jsonObject.containsKey("host")
                || !jsonObject.containsKey("port") || !jsonObject.containsKey("tenant")) {
            return null;
        }

        String host = jsonObject.getString("host");
        String port = jsonObject.getString("port");
        String tenant = jsonObject.getString("tenant");

        return new BdiConfiguration(host, port, tenant);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BdiConfiguration that = (BdiConfiguration) o;

        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        if (port != null ? !port.equals(that.port) : that.port != null) return false;
        if (tenantId != null ? !tenantId.equals(that.tenantId) : that.tenantId != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + (port != null ? port.hashCode() : 0);
        result = 31 * result + (tenantId != null ? tenantId.hashCode() : 0);
        return result;
    }
}
