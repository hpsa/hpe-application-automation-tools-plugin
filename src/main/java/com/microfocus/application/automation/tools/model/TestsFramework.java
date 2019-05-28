/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2019 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.model;

import com.hp.octane.integrations.executor.TestsToRunConverter;
import org.apache.commons.collections.map.HashedMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TestsFramework {

	private String description;
	private String value;
	private String format;
	private String delimiter;

	public TestsFramework(String value) {
		this.value = value;
		this.description = "";
		this.format = "";
		this.delimiter = "";
	}

	public TestsFramework(String value, String description, String format, String delimiter) {
		this.value = value;
		this.description = description;
		this.format = format;
		this.delimiter = delimiter;
	}

	public String getDescription() {
		return description;
	}

	public String getValue() {
		return value;
	}

	public String getFormat() { return format; }

	public String getDelimiter() { return delimiter; }

	public Map<String, String> getProperties() {
		Map<String, String> properties = new HashMap();

		setProperty(properties, TestsToRunConverter.CONVERTER_FORMAT, this.format);
		setProperty(properties, TestsToRunConverter.CONVERTER_DELIMITER, this.delimiter);

		return properties;
	}

	private void setProperty(Map<String, String> map, String key, String value) {
		if (map.containsKey(key)) {
			map.replace(key, value);
		}
		else {
			map.put(key, value);
		}
	}
}
