package com.hp.nga.integrations.dto.snapshots.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hp.nga.integrations.dto.DTOBase;
import com.hp.nga.integrations.dto.DTOFactoryInternalAbstract;
import com.hp.nga.integrations.dto.snapshots.SnapshotNode;
import com.hp.nga.integrations.dto.snapshots.SnapshotPhase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gullery on 10/02/2016.
 */

public final class DTOFactorySnapshots extends DTOFactoryInternalAbstract {
	private final Map<Class, Class> dtoPairs = new HashMap<Class, Class>();

	private DTOFactorySnapshots() {
	}

	@Override
	protected Class[] getXMLAbleClasses() {
		return new Class[0];
	}

	public static void ensureInit(Map<Class<? extends DTOBase>, DTOFactoryInternalAbstract> registry, ObjectMapper objectMapper) {
		registry.put(SnapshotNode.class, INSTANCE_HOLDER.instance);
		registry.put(SnapshotPhase.class, INSTANCE_HOLDER.instance);

		INSTANCE_HOLDER.instance.dtoPairs.put(SnapshotNode.class, SnapshotNodeImpl.class);
		INSTANCE_HOLDER.instance.dtoPairs.put(SnapshotPhase.class, SnapshotPhaseImpl.class);

		SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
		resolver.addMapping(SnapshotNode.class, SnapshotNodeImpl.class);
		resolver.addMapping(SnapshotPhase.class, SnapshotPhaseImpl.class);
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
		private static final DTOFactorySnapshots instance = new DTOFactorySnapshots();
	}
}
