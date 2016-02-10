package com.hp.nga.integrations.dto.general;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by gullery on 03/01/2016.
 * <p>
 * Description of Plugin Status
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(@JsonSubTypes.Type(value = AggregatedInfoImpl.class, name = "AggregatedInfoImpl"))
public interface AggregatedInfo {

	ServerInfo getServer();

	AggregatedInfo setServer(ServerInfo server);

	PluginInfo getPlugin();

	AggregatedInfo setPlugin(PluginInfo plugin);
}
