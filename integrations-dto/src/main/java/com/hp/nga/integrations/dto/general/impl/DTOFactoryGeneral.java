package com.hp.nga.integrations.dto.general.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hp.nga.integrations.dto.DTOBase;
import com.hp.nga.integrations.dto.DTOFactoryInternalBase;
import com.hp.nga.integrations.dto.general.CIProviderSummaryInfo;
import com.hp.nga.integrations.dto.general.CIJobConfig;
import com.hp.nga.integrations.dto.general.CIJobsList;
import com.hp.nga.integrations.dto.general.PluginInfo;
import com.hp.nga.integrations.dto.general.ServerInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gullery on 10/02/2016.
 */

public final class DTOFactoryGeneral implements DTOFactoryInternalBase {
	private final Map<Class, Class> dtoPairs = new HashMap<Class, Class>();

	private DTOFactoryGeneral() {
	}

	public static void ensureInit(Map<Class<? extends DTOBase>, DTOFactoryInternalBase> registry, ObjectMapper objectMapper) {
		registry.put(PluginInfo.class, INSTANCE_HOLDER.instance);
		registry.put(ServerInfo.class, INSTANCE_HOLDER.instance);
		registry.put(CIProviderSummaryInfo.class, INSTANCE_HOLDER.instance);
		registry.put(CIJobConfig.class, INSTANCE_HOLDER.instance);
		registry.put(CIJobsList.class, INSTANCE_HOLDER.instance);

		INSTANCE_HOLDER.instance.dtoPairs.put(PluginInfo.class, PluginInfoImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(ServerInfo.class, ServerInfoImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(CIProviderSummaryInfo.class, CIProviderSummaryInfoImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(CIJobConfig.class, CIJobConfigImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(CIJobsList.class, CIJobsListImpl.class);

		SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
		resolver.addMapping(PluginInfo.class, PluginInfoImpl.class);
		resolver.addMapping(ServerInfo.class, ServerInfoImpl.class);
		resolver.addMapping(CIProviderSummaryInfo.class, CIProviderSummaryInfoImpl.class);
		resolver.addMapping(CIJobConfig.class, CIJobConfigImpl.class);
		resolver.addMapping(CIJobsList.class, CIJobsListImpl.class);
		SimpleModule module = new SimpleModule();
		module.setAbstractTypes(resolver);
		objectMapper.registerModule(module);
	}

	public <T> T instantiateDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException {
		T result = null;
		if (dtoPairs.containsKey(targetType)) {
			result = (T) dtoPairs.get(targetType).newInstance();
		}
		return result;
	}

	private static final class INSTANCE_HOLDER {
		private static final DTOFactoryGeneral instance = new DTOFactoryGeneral();
	}
}
