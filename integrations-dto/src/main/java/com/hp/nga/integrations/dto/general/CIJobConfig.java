package com.hp.nga.integrations.dto.general;

import com.hp.nga.integrations.dto.DTOBase;
import com.hp.nga.integrations.dto.parameters.ParameterConfig;

/**
 * Created by lazara on 08/02/2016.
 */

public interface CIJobConfig extends DTOBase {

	CIJobConfig setName(String value);

	String getName();

	CIJobConfig setCiId(String ciId);

	String getCiId();

	CIJobConfig setParameters(ParameterConfig[] parameters);

	ParameterConfig[] getParameters();
}
