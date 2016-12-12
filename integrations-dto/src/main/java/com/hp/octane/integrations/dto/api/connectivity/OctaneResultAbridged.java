package com.hp.octane.integrations.dto.api.connectivity;

import com.hp.octane.integrations.dto.DTOBase;

import java.util.Map;

/**
 * Created by gullery on 08/01/2016.
 * <p>
 * Result container descriptor, as to be used in abridged tasking in NGA
 */

public interface OctaneResultAbridged extends DTOBase {

	String getId();

	OctaneResultAbridged setId(String id);

	String getServiceId();

	OctaneResultAbridged setServiceId(String serviceId);

	int getStatus();

	OctaneResultAbridged setStatus(int status);

	Map<String, String> getHeaders();

	OctaneResultAbridged setHeaders(Map<String, String> headers);

	String getBody();

	OctaneResultAbridged setBody(String body);
}
