package com.hp.octane.integrations.dto.api.general;

import com.hp.octane.integrations.dto.DTOBase;

/**
 * Created by gullery on 03/01/2016.
 * <p>
 * CI Server info descriptor
 */

public interface CIServerInfo extends DTOBase {

	CIServerTypes getType();

	CIServerInfo setType(CIServerTypes type);

	String getVersion();

	CIServerInfo setVersion(String version);

	String getUrl();

	CIServerInfo setUrl(String url);

	String getInstanceId();

	CIServerInfo setInstanceId(String instanceId);

	Long getInstanceIdFrom();

	CIServerInfo setInstanceIdFrom(Long instanceIdFrom);

	Long getSendingTime();

	CIServerInfo setSendingTime(Long sendingTime);
}
