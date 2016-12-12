package com.hp.octane.integrations.dto.impl.snapshots;

import com.hp.octane.integrations.dto.DTOBase;
import com.hp.octane.integrations.dto.DTOInternalProviderBase;
import com.hp.octane.integrations.dto.api.snapshots.SnapshotNode;
import com.hp.octane.integrations.dto.api.snapshots.SnapshotPhase;

/**
 * Created by gullery on 10/02/2016.
 *
 * Snapshots data related DTOs definitions provider
 */

public final class DTOSnapshotsProvider extends DTOInternalProviderBase {

	public DTOSnapshotsProvider() {
		dtoPairs.put(SnapshotNode.class, SnapshotNodeImpl.class);
		dtoPairs.put(SnapshotPhase.class, SnapshotPhaseImpl.class);
	}

	protected <T extends DTOBase> T instantiateDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException {
		T result = null;
		if (dtoPairs.containsKey(targetType)) {
			result = (T) dtoPairs.get(targetType).newInstance();
		}
		return result;
	}
}
