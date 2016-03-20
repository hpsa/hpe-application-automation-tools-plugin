package com.hp.nga.integrations.dto.scm.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hp.nga.integrations.dto.DTOBase;
import com.hp.nga.integrations.dto.DTOInternalProviderBase;
import com.hp.nga.integrations.dto.scm.SCMCommit;
import com.hp.nga.integrations.dto.scm.SCMData;
import com.hp.nga.integrations.dto.scm.SCMRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gullery on 10/02/2016.
 */

public final class DTOSCMProvider extends DTOInternalProviderBase {
	private final Map<Class, Class> dtoPairs = new HashMap<Class, Class>();

	private DTOSCMProvider() {
	}

	@Override
	protected Class[] getXMLAbleClasses() {
		return new Class[0];
	}

	public static void ensureInit(Map<Class<? extends DTOBase>, DTOInternalProviderBase> registry, ObjectMapper objectMapper) {
		registry.put(SCMCommit.class, INSTANCE_HOLDER.instance);
		registry.put(SCMRepository.class, INSTANCE_HOLDER.instance);
		registry.put(SCMData.class, INSTANCE_HOLDER.instance);

		INSTANCE_HOLDER.instance.dtoPairs.put(SCMCommit.class, SCMCommitImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(SCMRepository.class, SCMRepositoryImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(SCMData.class, SCMDataImpl.class);

		SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
		resolver.addMapping(SCMCommit.class, SCMCommitImpl.class);
		resolver.addMapping(SCMRepository.class, SCMRepositoryImpl.class);
		resolver.addMapping(SCMData.class, SCMDataImpl.class);
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
		private static final DTOSCMProvider instance = new DTOSCMProvider();
	}
}
