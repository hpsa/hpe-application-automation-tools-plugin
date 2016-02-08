package com.hp.nga.integrations.dto.general;

/**
 * Created by gullery on 03/01/2016.
 */

public interface IServerInfo {

	CIServerTypes getType();

	IServerInfo setType(CIServerTypes type);

	String getVersion();

	IServerInfo setVersion(String version);

	String getUrl();

	IServerInfo setUrl(String url);

	String getInstanceId();

	IServerInfo setInstanceId(String instanceId);

	Long getInstanceIdFrom();

	IServerInfo setInstanceIdFrom(Long instanceIdFrom);

	Long getSendingTime();

	IServerInfo setSendingTime(Long sendingTime);
}
