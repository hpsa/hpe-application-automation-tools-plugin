package com.hp.nga.integrations.dto.parameters;

import com.hp.nga.integrations.dto.DTOBase;

/**
 * Created by gullery on 19/02/2015.
 * <p/>
 * CI Parameter data object descriptor
 */

public interface CIParameter extends DTOBase {

	CIParameterType getType();

	CIParameter setType(CIParameterType type);

	String getName();

	CIParameter setName(String name);

	String getDescription();

	CIParameter setDescription(String description);

	Object[] getChoices();

	CIParameter setChoices(Object[] choices);

	Object getDefaultValue();

	CIParameter setDefaultValue(Object defaultValue);

	Object getValue();

	CIParameter setValue(Object value);
}
