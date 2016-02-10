package com.hp.nga.integrations.dto.general;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by gullery on 03/01/2016.
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(@JsonSubTypes.Type(value = ServerInfoImpl.class, name = "ServerInfoImpl"))
public interface ServerInfo {

	CIServerTypes getType();

	ServerInfo setType(CIServerTypes type);

	String getVersion();

	ServerInfo setVersion(String version);

	String getUrl();

	ServerInfo setUrl(String url);

	String getInstanceId();

	ServerInfo setInstanceId(String instanceId);

	Long getInstanceIdFrom();

	ServerInfo setInstanceIdFrom(Long instanceIdFrom);

	Long getSendingTime();

	ServerInfo setSendingTime(Long sendingTime);
}
