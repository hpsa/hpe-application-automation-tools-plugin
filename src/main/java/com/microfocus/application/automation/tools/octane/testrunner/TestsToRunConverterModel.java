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

package com.microfocus.application.automation.tools.octane.testrunner;

import com.hp.octane.integrations.executor.TestsToRunFramework;
import com.microfocus.application.automation.tools.model.TestsFramework;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*
 * Test model for storing of available frameworks for converting
 */
public class TestsToRunConverterModel implements Serializable {

    private static final long serialVersionUID = 1L;

    public final static TestsFramework none = new TestsFramework("", "", "");

    public final static List<TestsFramework> Frameworks;

    private String framework;
    private String format;

    static {
        List<TestsFramework> temp = new ArrayList<>();
        temp.add(none);
        for (TestsToRunFramework fr : TestsToRunFramework.values()) {
            temp.add(new TestsFramework(fr.value(), fr.getDesc(), fr.getFormat()));
        }
        Frameworks = temp;
    }

    @DataBoundConstructor
    public TestsToRunConverterModel(String framework, String format) {
        this.framework = framework;
        this.format = format;
    }

    public TestsFramework getFramework() {
        return new TestsFramework(framework, "", format);
    }

}



