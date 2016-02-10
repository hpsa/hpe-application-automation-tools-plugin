package com.hp.nga.integrations.dto.general;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by gullery on 08/02/2016.
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(@JsonSubTypes.Type(value = PluginInfoImpl.class, name = "PluginInfoImpl"))
public interface PluginInfo {

	String getVersion();

	PluginInfo setVersion(String version);
}
