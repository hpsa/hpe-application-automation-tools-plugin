/*
 * © Copyright 2013 EntIT Software LLC
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright (c) 2018 Micro Focus Company, L.P.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ___________________________________________________________________
 *
 */
package com.hpe.application.automation.tools.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import javax.xml.bind.ValidationException;
import java.util.*;

/**
 * Represents the environment that Parallel Runner uses to execute a test.
 */
public class ParallelRunnerEnvironmentModel extends AbstractDescribableImpl<ParallelRunnerEnvironmentModel> {
    private static class EnvironmentValidationException extends Exception {
        public EnvironmentValidationException(String message) {
            super(message);
        }
    }

    private String environment;
    private String environmentType; // 'Web', 'Mobile'

    @DataBoundConstructor
    public ParallelRunnerEnvironmentModel(String environment,String environmentType) {
        this.environment = environment;
        this.environmentType = environmentType;
    }

    /**
     * Returns the parallel runner environment string.
     * @return the environment string
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * Set the parallel runner environment string.
     * @param environment the parallel runner environment
     */
    @DataBoundSetter
    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    /**
     * Set the environment type
     * @param environmentType the environment type
     */
    @DataBoundSetter
    public void setEnvironmentType(String environmentType) { this.environmentType = environmentType; }

    /**
     * Returns the environment type
     * @return the environment type
     */
    public String getEnvironmentType() { return environmentType; }

    @Extension
    public static class DescriptorImpl extends Descriptor<ParallelRunnerEnvironmentModel> {
        private static final String DeviceIdKey = "deviceid";
        private static final String OsTypeKey = "ostype";
        private static final String OsVersionKey = "osversion";
        private static final String ManufacturerAndModelKey = "manufacturerandmodel";
        private static final String BrowserKey = "browser";

        // the list of browsers that ParallelRunner supports
        private static final List<String> SupportedBrowsers = new ArrayList<String>() {
            {
                add("IE");
                add("IE64");
                add("CHROME");
                add("FIREFOX");
                add("FIREFOX64");
            }
        };

        private static final List<String> KnownProperties = new ArrayList<String>() {
            {
                add(DeviceIdKey);
                add(OsTypeKey);
                add(OsVersionKey);
                add(ManufacturerAndModelKey);
                add(BrowserKey);
            }
        };

        /**
         * Instantiates a new Descriptor.
         */
        public DescriptorImpl() { load(); }


        /**
         * Returns the display name.
         */
        @Nonnull
        public String getDisplayName() {return "Parallel Runner Environment model.";}

        /**
         * Return the key value pairs from an environment string.
         * @param environment the environment string
         * @return a map containing the key value pairs
         */
        private Map<String,String> getEnvironmentKeyValuePairs(String environment) {
            // environment string example: osType : Android,osVersion : 4.4.2,manufacturerAndModel : "samsung GT-I9515"
            // or : browser : Chrome
            Map<String,String> pairs = new HashMap<>();

            String [] tokens = environment.toLowerCase().split(",");

            for (String token : tokens) {
                String [] keyValue = token.split(":");

                // invalid key/value pair
                if(keyValue.length < 2) {
                    continue;
                }

                // we will treat the first value as the key
                String key = keyValue[0].trim();

                if(key.isEmpty())
                    continue;

                // the rest of the list represents the value
                String value = StringUtils.
                        join(Arrays.asList(keyValue).subList(1, keyValue.length),"")
                        .trim();

                // we will always use the last provided key
                pairs.put(key,value);
            }

            // as result we will have
            // { osType : Android}
            // { osVersion : 4.4.2 }
            // { manufacturerAndModel : "samsung GT-I9515" }

            return pairs;
        }

        /**
         * Checks if the provided browser is supported by ParallelRunner.
         * @param browserName the name of the browser
         * @return true if the browser is supported, false otherwise
         */
        private boolean isSupportedBrowser(String browserName) {
            for(String browser : SupportedBrowsers) {
                if(browser.equalsIgnoreCase(browserName)){
                    return true;
                }
            }

            return false;
        }

        /**
         * Check if the given property is known(valid).
         * @param property the property
         * @return true if the property is known, false otherwise
         */
        private boolean isKnownProperty(String property) {
            for(String knownProperty : KnownProperties) {
                if(knownProperty.equalsIgnoreCase(property)){
                    return true;
                }
            }

            return false;
        }

        private void checkKeyValueValidity(String key,Map<String,String> pairs,String message) throws EnvironmentValidationException {
            // if the key is present the value must be present too
            if(pairs.containsKey(key) && pairs.get(key).trim().isEmpty()) {
                throw new EnvironmentValidationException(message);
            }
        }

        /**
         * Validate the environment string.
         * @param environment the environment string
         */
        private void validateEnvironmentString(String environment) throws EnvironmentValidationException {
            Map<String,String> pairs = this.getEnvironmentKeyValuePairs(environment);

            // check if any property was provided
            if(pairs.isEmpty()) {
                throw new EnvironmentValidationException("No property provided. Enter properties using the following syntax: <name>:<value>");
            }

            // check if there are any invalid property names
            for(String key : pairs.keySet()) {
                if(!isKnownProperty(key)) {
                    throw new EnvironmentValidationException("Invalid property name: " + key);
                }
            }

            checkKeyValueValidity(DeviceIdKey,pairs,"DeviceId value must not be empty!");
            checkKeyValueValidity(OsVersionKey,pairs,"OsVersion value must not be empty!");
            checkKeyValueValidity(OsTypeKey,pairs,"OsType value must not be empty!");
            checkKeyValueValidity(ManufacturerAndModelKey,pairs,"ManufacturerAndModel value must not be empty!");

            // if it's a browser environment
            if(pairs.containsKey(BrowserKey)) {
                String browserName = pairs.get(BrowserKey).trim();

                // check if the value is present
                if(browserName.isEmpty()) {
                    throw new EnvironmentValidationException("Browser value must not be empty!");
                }

                // check if ParallelRunner supports the given browser
                if(!isSupportedBrowser(browserName)) {
                    throw new EnvironmentValidationException(String.format("Invalid browser. %s is not supported!", browserName.toUpperCase()));
                }
            }
        }

        /**
         * Sanity check for the environment string.
         * @param value the environment string
         * @return the result of the sanity check
         */
        @SuppressWarnings("unused")
        public FormValidation doCheckEnvironment(@QueryParameter String value) {
            if(StringUtils.isEmpty(value)) {
                return FormValidation.ok();
            }

            try{
                this.validateEnvironmentString(value);
            }catch (EnvironmentValidationException e) {
                return FormValidation.error(e.getMessage());
            }

            return FormValidation.ok();
        }
    }
}
