package com.hp.octane.integrations.dto.tests.impl;

import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.DTOInternalProviderBase;
import com.hp.octane.integrations.dto.tests.BuildContext;
import com.hp.octane.integrations.dto.tests.TestRunError;
import com.hp.octane.integrations.dto.tests.TestsResult;
import com.hp.octane.integrations.dto.tests.TestRun;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by gullery on 10/02/2016.
 *
 * Octane oriented tests results DTOs definitions provider
 */

public final class DTOTestsProvider extends DTOInternalProviderBase {
	private final Map<Class<? extends DTOBase>, Class> dtoPairs = new HashMap<>();

	public DTOTestsProvider() {
		dtoPairs.put(BuildContext.class, BuildContextImpl.class);
		dtoPairs.put(TestRunError.class, TestRunErrorImpl.class);
		dtoPairs.put(TestRun.class, TestRunImpl.class);
		dtoPairs.put(TestsResult.class, TestsResultImpl.class);
	}

	@Override
	protected void provideImplResolvingMap(SimpleAbstractTypeResolver dtoImplResolver) {
		dtoImplResolver.addMapping(BuildContext.class, BuildContextImpl.class);
		dtoImplResolver.addMapping(TestRunError.class, TestRunErrorImpl.class);
		dtoImplResolver.addMapping(TestRun.class, TestRunImpl.class);
		dtoImplResolver.addMapping(TestsResult.class, TestsResultImpl.class);
	}

	@Override
	protected Set<Class<? extends DTOBase>> getJSONAbleDTOs() {
		return dtoPairs.keySet();
	}

	@Override
	protected Class[] getXMLAbleDTOs() {
		return new Class[]{BuildContextImpl.class, TestRunErrorImpl.class, TestRunImpl.class, TestsResultImpl.class};
	}

	protected <T extends DTOBase> T instantiateDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException {
		T result = null;
		if (dtoPairs.containsKey(targetType)) {
			result = (T) dtoPairs.get(targetType).newInstance();
		}
		return result;
	}
}
