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

public class TestsFramework {

    private String description;
    private String name;
    private String format;
    private String delimiter;

    public TestsFramework() {
        this.name = "";
        this.description = "";
        this.format = "";
        this.delimiter = "";
    }

    public TestsFramework(String name, String description, String format, String delimiter) {
        this.name = name;
        this.description = description;
        this.format = format;
        this.delimiter = delimiter;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getFormat() {
        return format;
    }

    public String getDelimiter() {
        return delimiter;
    }

}
