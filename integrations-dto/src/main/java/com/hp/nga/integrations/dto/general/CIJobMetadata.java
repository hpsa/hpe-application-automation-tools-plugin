package com.hp.nga.integrations.dto.general;

import com.hp.nga.integrations.dto.DTOBase;
import com.hp.nga.integrations.dto.parameters.ParameterConfig;

/**
 * Created by lazara on 08/02/2016.
 * <p>
 * CI Job metadata descriptor
 */

public interface CIJobMetadata extends DTOBase {

	CIJobMetadata setName(String value);

	String getName();

	CIJobMetadata setCiId(String ciId);

	String getCiId();

	CIJobMetadata setParameters(ParameterConfig[] parameters);

	ParameterConfig[] getParameters();
}
