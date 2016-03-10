package com.hp.nga.integrations.dto.general.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hp.nga.integrations.dto.DTOBase;
import com.hp.nga.integrations.dto.DTOFactoryInternalBase;
import com.hp.nga.integrations.dto.general.CIPluginSDKInfo;
import com.hp.nga.integrations.dto.general.CIProviderSummaryInfo;
import com.hp.nga.integrations.dto.general.CIJobMetadata;
import com.hp.nga.integrations.dto.general.CIJobsList;
import com.hp.nga.integrations.dto.general.CIPluginInfo;
import com.hp.nga.integrations.dto.general.CIServerInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gullery on 10/02/2016.
 */

public final class DTOFactoryGeneral extends DTOFactoryInternalBase {
	private final Map<Class, Class> dtoPairs = new HashMap<Class, Class>();

	private DTOFactoryGeneral() {
	}

	@Override
	protected Class[] getXMLAbleClasses() {
		return new Class[0];
	}

	public static void ensureInit(Map<Class<? extends DTOBase>, DTOFactoryInternalBase> registry, ObjectMapper objectMapper) {
		registry.put(CIPluginInfo.class, INSTANCE_HOLDER.instance);
		registry.put(CIServerInfo.class, INSTANCE_HOLDER.instance);
		registry.put(CIPluginSDKInfo.class, INSTANCE_HOLDER.instance);
		registry.put(CIProviderSummaryInfo.class, INSTANCE_HOLDER.instance);
		registry.put(CIJobMetadata.class, INSTANCE_HOLDER.instance);
		registry.put(CIJobsList.class, INSTANCE_HOLDER.instance);

		INSTANCE_HOLDER.instance.dtoPairs.put(CIPluginInfo.class, CIPluginInfoImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(CIServerInfo.class, CIServerInfoImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(CIPluginSDKInfo.class, CIPluginSDKInfoImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(CIProviderSummaryInfo.class, CIProviderSummaryInfoImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(CIJobMetadata.class, CIJobMetadataImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(CIJobsList.class, CIJobsListImpl.class);

		SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
		resolver.addMapping(CIPluginInfo.class, CIPluginInfoImpl.class);
		resolver.addMapping(CIServerInfo.class, CIServerInfoImpl.class);
		resolver.addMapping(CIPluginSDKInfo.class, CIPluginSDKInfoImpl.class);
		resolver.addMapping(CIProviderSummaryInfo.class, CIProviderSummaryInfoImpl.class);
		resolver.addMapping(CIJobMetadata.class, CIJobMetadataImpl.class);
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
