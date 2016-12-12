package com.hp.octane.integrations.dto.impl.general;

import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.DTOInternalProviderBase;
import com.hp.octane.integrations.dto.api.general.CIJobsList;
import com.hp.octane.integrations.dto.api.general.CIPluginInfo;
import com.hp.octane.integrations.dto.api.general.CIPluginSDKInfo;
import com.hp.octane.integrations.dto.api.general.CIProviderSummaryInfo;
import com.hp.octane.integrations.dto.api.general.CIServerInfo;

/**
 * Created by gullery on 10/02/2016.
 *
 * General purposes DTOs definitions, mostly configuration related ones
 */

public final class DTOGeneralProvider extends DTOInternalProviderBase {

	public DTOGeneralProvider() {
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
