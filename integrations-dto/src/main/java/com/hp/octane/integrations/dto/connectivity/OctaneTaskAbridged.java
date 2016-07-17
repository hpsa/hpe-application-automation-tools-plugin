package com.hp.octane.integrations.dto.connectivity;

import com.hp.octane.integrations.dto.DTOBase;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by gullery on 08/01/2016.
 * <p>
 * Task container descriptor, as to be used in abridged tasking in NGA
 */

public interface OctaneTaskAbridged extends DTOBase, Serializable {

	String getId();

	OctaneTaskAbridged setId(String id);

	String getServiceId();

	OctaneTaskAbridged setServiceId(String serviceId);

	String getUrl();

	OctaneTaskAbridged setUrl(String url);

	HttpMethod getMethod();

	OctaneTaskAbridged setMethod(HttpMethod method);

	Map<String, String> getHeaders();

	OctaneTaskAbridged setHeaders(Map<String, String> headers);

	String getBody();

	OctaneTaskAbridged setBody(String body);
}
