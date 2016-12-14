package com.hp.octane.integrations.dto.general.impl;

import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.DTOInternalProviderBase;
import com.hp.octane.integrations.dto.general.CIJobsList;
import com.hp.octane.integrations.dto.general.CIPluginInfo;
import com.hp.octane.integrations.dto.general.CIPluginSDKInfo;
import com.hp.octane.integrations.dto.general.CIProviderSummaryInfo;
import com.hp.octane.integrations.dto.general.CIServerInfo;

/**
 * Created by gullery on 10/02/2016.
 *
 * General purposes DTOs definitions, mostly configuration related ones
 */

public final class DTOGeneralProvider extends DTOInternalProviderBase {

	public DTOGeneralProvider(DTOFactory.DTOConfiguration configuration) {
		dtoPairs.put(CIPluginInfo.class, CIPluginInfoImpl.class);
		dtoPairs.put(CIServerInfo.class, CIServerInfoImpl.class);
		dtoPairs.put(CIPluginSDKInfo.class, CIPluginSDKInfoImpl.class);
		dtoPairs.put(CIProviderSummaryInfo.class, CIProviderSummaryInfoImpl.class);
		dtoPairs.put(CIJobsList.class, CIJobsListImpl.class);
	}

	protected <T extends DTOBase> T instantiateDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException {
		T result = null;
		if (dtoPairs.containsKey(targetType)) {
			result = (T) dtoPairs.get(targetType).newInstance();
		}
		return result;
	}
}
