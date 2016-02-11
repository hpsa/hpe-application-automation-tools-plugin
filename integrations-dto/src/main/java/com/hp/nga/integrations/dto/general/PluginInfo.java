package com.hp.nga.integrations.dto.general;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hp.nga.integrations.dto.DTO;

/**
 * Created by gullery on 08/02/2016.
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(@JsonSubTypes.Type(value = PluginInfoImpl.class, name = "PluginInfoImpl"))
public interface PluginInfo extends DTO {

	String getVersion();

	PluginInfo setVersion(String version);
}
