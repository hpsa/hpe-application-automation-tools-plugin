package com.hp.nga.integrations.dto.connectivity;

import com.hp.nga.integrations.dto.DTOBase;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by gullery on 08/01/2016.
 * <p>
 * Result container descriptor, as to be used in abridged tasking in NGA
 */

public interface NGAResultAbridged extends DTOBase {

	String getId();

	NGAResultAbridged setId(String id);

	String getServiceId();

	NGAResultAbridged setServiceId(String serviceId);

	int getStatus();

	NGAResultAbridged setStatus(int status);

	Map<String, String> getHeaders();

	NGAResultAbridged setHeaders(Map<String, String> headers);

	String getBody();

	NGAResultAbridged setBody(String body);
}
