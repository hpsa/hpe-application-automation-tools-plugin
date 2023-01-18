/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2023 Micro Focus or one of its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.settings;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.cli.shaded.org.apache.commons.lang.RandomStringUtils;
import jenkins.model.GlobalConfiguration;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;
import java.security.SecureRandom;

@Extension
public class UFTEncryptionGlobalConfiguration extends GlobalConfiguration implements Serializable {
    // won't be displayed anywhere, a bit of a hack, but should be secure

    // seems important, if further changes needed after release
    private static final long serialVersionUID = 1L;

    private static Secret generateKey() {
        return Secret.fromString(RandomStringUtils.random(32, 0, 0, true, true, null, new SecureRandom()));
    }

    public static UFTEncryptionGlobalConfiguration getInstance() throws NullPointerException {
        UFTEncryptionGlobalConfiguration config = GlobalConfiguration.all().get(UFTEncryptionGlobalConfiguration.class);

        if (config == null) throw new NullPointerException();

        return config;
    }

    private Secret encKey;

    @DataBoundConstructor
    public UFTEncryptionGlobalConfiguration() {
        load();
    }

    @NonNull
    @Override
    public String getDisplayName() {
        return "UFT Encryption Global Configuration (Should not appear)";
    }

    /**
     * Returns in encrypted form the current encryption key, generates one if this master doesn't have one.
     * @return encrypted encryption key
     */
    public String getEncKey() {
        if (encKey == null) {
            encKey = generateKey();
            save();
        }

        return encKey.getEncryptedValue();
    }

}
