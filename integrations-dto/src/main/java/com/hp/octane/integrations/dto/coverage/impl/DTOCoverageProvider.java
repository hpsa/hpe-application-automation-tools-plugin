package com.hp.octane.integrations.dto.coverage.impl;

import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.DTOInternalProviderBase;
import com.hp.octane.integrations.dto.coverage.BuildCoverage;
import com.hp.octane.integrations.dto.coverage.FileCoverage;
import com.hp.octane.integrations.dto.coverage.LineCoverage;
import com.hp.octane.integrations.dto.coverage.TestCoverage;

/**
 * Created by gullery on 10/02/2016.
 *
 * Coverage related DTOs definitions provider
 */

public final class DTOCoverageProvider extends DTOInternalProviderBase {

	public DTOCoverageProvider(DTOFactory.DTOConfiguration configuration) {
		dtoPairs.put(BuildCoverage.class, BuildCoverageImpl.class);
		dtoPairs.put(FileCoverage.class, FileCoverageImpl.class);
		dtoPairs.put(LineCoverage.class, LineCoverageImpl.class);
		dtoPairs.put(TestCoverage.class, TestCoverageImpl.class);
	}

	protected <T extends DTOBase> T instantiateDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException {
		T result = null;
		if (dtoPairs.containsKey(targetType)) {
			result = (T) dtoPairs.get(targetType).newInstance();
		}
		return result;
	}
}
