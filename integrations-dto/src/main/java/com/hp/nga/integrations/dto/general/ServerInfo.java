package com.hp.nga.integrations.dto.general;

/**
 * Created by gullery on 03/01/2016.
 */

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
