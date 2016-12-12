package com.hp.octane.integrations.dto.api.scm;

import com.hp.octane.integrations.dto.DTOBase;

/**
 * Created by gullery on 08/12/2015.
 * SCM Change descriptor
 */

public interface SCMChange extends DTOBase {

	String getType();

	SCMChange setType(String type);

	String getFile();

	SCMChange setFile(String file);
}
