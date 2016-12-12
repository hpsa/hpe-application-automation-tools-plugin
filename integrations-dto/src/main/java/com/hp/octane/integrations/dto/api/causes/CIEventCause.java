package com.hp.octane.integrations.dto.api.causes;

import com.hp.octane.integrations.dto.DTOBase;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 20/10/14
 * Time: 17:01
 * To change this template use File | Settings | File Templates.
 */

public interface CIEventCause extends DTOBase {

	CIEventCauseType getType();

	CIEventCause setType(CIEventCauseType type);

	String getUser();

	CIEventCause setUser(String user);

	String getProject();

	CIEventCause setProject(String ciJobRefId);

	String getBuildCiId();

	CIEventCause setBuildCiId(String buildCiId);

	List<CIEventCause> getCauses();

	CIEventCause setCauses(List<CIEventCause> causes);
}
