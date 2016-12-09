package com.hp.octane.integrations.dto.general.impl;

import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.DTOInternalProviderBase;
import com.hp.octane.integrations.dto.general.CIPluginSDKInfo;
import com.hp.octane.integrations.dto.general.CIProviderSummaryInfo;
import com.hp.octane.integrations.dto.general.CIJobsList;
import com.hp.octane.integrations.dto.general.CIPluginInfo;
import com.hp.octane.integrations.dto.general.CIServerInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by gullery on 10/02/2016.
 *
 * General purposes DTOs definitions, mostly configuration related ones
 */

public final class DTOGeneralProvider extends DTOInternalProviderBase {
	private final Map<Class<? extends DTOBase>, Class> dtoPairs = new HashMap<>();

	public DTOGeneralProvider() {
		dtoPairs.put(CIPluginInfo.class, CIPluginInfoImpl.class);
		dtoPairs.put(CIServerInfo.class, CIServerInfoImpl.class);
		dtoPairs.put(CIPluginSDKInfo.class, CIPluginSDKInfoImpl.class);
		dtoPairs.put(CIProviderSummaryInfo.class, CIProviderSummaryInfoImpl.class);
		dtoPairs.put(CIJobsList.class, CIJobsListImpl.class);
	}

	@Override
	protected void provideImplResolvingMap(SimpleAbstractTypeResolver dtoImplResolver) {
		dtoImplResolver.addMapping(CIPluginInfo.class, CIPluginInfoImpl.class);
		dtoImplResolver.addMapping(CIServerInfo.class, CIServerInfoImpl.class);
		dtoImplResolver.addMapping(CIPluginSDKInfo.class, CIPluginSDKInfoImpl.class);
		dtoImplResolver.addMapping(CIProviderSummaryInfo.class, CIProviderSummaryInfoImpl.class);
		dtoImplResolver.addMapping(CIJobsList.class, CIJobsListImpl.class);
	}

	@Override
	protected Set<Class<? extends DTOBase>> getJSONAbleDTOs() {
		return dtoPairs.keySet();
	}

	protected <T extends DTOBase> T instantiateDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException {
		T result = null;
		if (dtoPairs.containsKey(targetType)) {
			result = (T) dtoPairs.get(targetType).newInstance();
		}
		return result;
	}
}
