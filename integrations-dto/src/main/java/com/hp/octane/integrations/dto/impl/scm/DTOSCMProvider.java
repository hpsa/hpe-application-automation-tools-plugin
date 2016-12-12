package com.hp.octane.integrations.dto.impl.scm;

import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.DTOInternalProviderBase;
import com.hp.octane.integrations.dto.api.scm.SCMChange;
import com.hp.octane.integrations.dto.api.scm.SCMCommit;
import com.hp.octane.integrations.dto.api.scm.SCMData;
import com.hp.octane.integrations.dto.api.scm.SCMRepository;

/**
 * Created by gullery on 10/02/2016.
 *
 * SCM related DTOs definitions provider
 */

public final class DTOSCMProvider extends DTOInternalProviderBase {

	public DTOSCMProvider() {
		dtoPairs.put(SCMChange.class, SCMChangeImpl.class);
		dtoPairs.put(SCMCommit.class, SCMCommitImpl.class);
		dtoPairs.put(SCMRepository.class, SCMRepositoryImpl.class);
		dtoPairs.put(SCMData.class, SCMDataImpl.class);
	}

	protected <T extends DTOBase> T instantiateDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException {
		T result = null;
		if (dtoPairs.containsKey(targetType)) {
			result = (T) dtoPairs.get(targetType).newInstance();
		}
		return result;
	}
}
