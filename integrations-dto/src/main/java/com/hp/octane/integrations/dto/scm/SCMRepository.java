package com.hp.octane.integrations.dto.scm;

import com.hp.octane.integrations.dto.DTOBase;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 13/10/14
 * Time: 09:46
 * To change this template use File | Settings | File Templates.
 */

public interface SCMRepository extends DTOBase {

	SCMType getType();

	SCMRepository setType(SCMType type);

	String getUrl();

	SCMRepository setUrl(String url);

	String getBranch();

	SCMRepository setBranch(String branch);
}
