package com.hp.octane.plugins.jenkins.configuration;

import com.hp.indi.bdi.client.BdiConstants;
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
        if (jsonObject == null || !jsonObject.containsKey(BdiConstants.HOST_PARAM)
                || !jsonObject.containsKey(BdiConstants.PORT_PARAM) || !jsonObject.containsKey(BdiConstants.TENANT_PARAM)) {
            return null;
        }

        String host = jsonObject.getString(BdiConstants.HOST_PARAM);
        String port = jsonObject.getString(BdiConstants.PORT_PARAM);
        String tenant = jsonObject.getString(BdiConstants.TENANT_PARAM);

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
