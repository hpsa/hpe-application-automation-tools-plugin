package com.hp.octane.integrations.dto.coverage.impl;

import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.DTOInternalProviderBase;
import com.hp.octane.integrations.dto.coverage.BuildCoverage;
import com.hp.octane.integrations.dto.coverage.FileCoverage;
import com.hp.octane.integrations.dto.coverage.LineCoverage;
import com.hp.octane.integrations.dto.coverage.TestCoverage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by gullery on 10/02/2016.
 *
 * Coverage related DTOs definitions provider
 */

public final class DTOCoverageProvider extends DTOInternalProviderBase {
	private final Map<Class<? extends DTOBase>, Class> dtoPairs = new HashMap<>();

	public DTOCoverageProvider() {
		dtoPairs.put(BuildCoverage.class, BuildCoverageImpl.class);
		dtoPairs.put(FileCoverage.class, FileCoverageImpl.class);
		dtoPairs.put(LineCoverage.class, LineCoverageImpl.class);
		dtoPairs.put(TestCoverage.class, TestCoverageImpl.class);
	}

	@Override
	protected void provideImplResolvingMap(SimpleAbstractTypeResolver dtoImplResolver) {
		dtoImplResolver.addMapping(BuildCoverage.class, BuildCoverageImpl.class);
		dtoImplResolver.addMapping(FileCoverage.class, FileCoverageImpl.class);
		dtoImplResolver.addMapping(LineCoverage.class, LineCoverageImpl.class);
		dtoImplResolver.addMapping(TestCoverage.class, TestCoverageImpl.class);
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
