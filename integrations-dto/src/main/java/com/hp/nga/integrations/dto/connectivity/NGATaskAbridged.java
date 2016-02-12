package com.hp.nga.integrations.dto.connectivity;

import com.hp.nga.integrations.dto.DTOBase;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by gullery on 08/01/2016.
 */

public interface NGATaskAbridged extends DTOBase, Serializable {

	String getId();

	NGATaskAbridged setId(String id);

	String getServiceId();

	NGATaskAbridged setServiceId(String serviceId);

	String getUrl();

	NGATaskAbridged setUrl(String url);

	NGAHttpMethod getMethod();

	NGATaskAbridged setMethod(NGAHttpMethod method);

	Map<String, String> getHeaders();

	NGATaskAbridged setHeaders(Map<String, String> headers);

	String getBody();

	NGATaskAbridged setBody(String body);
}
