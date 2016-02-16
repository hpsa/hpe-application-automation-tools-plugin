package com.hp.nga.integrations.dto.coverage.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hp.nga.integrations.dto.DTOBase;
import com.hp.nga.integrations.dto.DTOFactoryInternalBase;
import com.hp.nga.integrations.dto.coverage.BuildCoverage;
import com.hp.nga.integrations.dto.coverage.FileCoverage;
import com.hp.nga.integrations.dto.coverage.LineCoverage;
import com.hp.nga.integrations.dto.coverage.TestCoverage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gullery on 10/02/2016.
 */

public final class DTOFactoryCoverage implements DTOFactoryInternalBase {
	private final Map<Class, Class> dtoPairs = new HashMap<Class, Class>();

	private DTOFactoryCoverage() {
	}

	public static void ensureInit(Map<Class<? extends DTOBase>, DTOFactoryInternalBase> registry, ObjectMapper objectMapper) {
		registry.put(BuildCoverage.class, INSTANCE_HOLDER.instance);
		registry.put(FileCoverage.class, INSTANCE_HOLDER.instance);
		registry.put(LineCoverage.class, INSTANCE_HOLDER.instance);
		registry.put(TestCoverage.class, INSTANCE_HOLDER.instance);

		INSTANCE_HOLDER.instance.dtoPairs.put(BuildCoverage.class, BuildCoverageImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(FileCoverage.class, FileCoverageImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(LineCoverage.class, LineCoverageImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(TestCoverage.class, TestCoverageImpl.class);

		SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
		resolver.addMapping(BuildCoverage.class, BuildCoverageImpl.class);
		resolver.addMapping(FileCoverage.class, FileCoverageImpl.class);
		resolver.addMapping(LineCoverage.class, LineCoverageImpl.class);
		resolver.addMapping(TestCoverage.class, TestCoverageImpl.class);
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
		private static final DTOFactoryCoverage instance = new DTOFactoryCoverage();
	}
}
