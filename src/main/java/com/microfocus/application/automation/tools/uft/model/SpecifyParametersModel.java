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

package com.microfocus.application.automation.tools.uft.model;

import com.microfocus.application.automation.tools.EncryptionUtils;
import com.microfocus.application.automation.tools.model.EnumDescription;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Node;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class SpecifyParametersModel extends AbstractDescribableImpl<SpecifyParametersModel> {
	private final static String PWD = "Password";

    // FOR GUI
    private final static EnumDescription STRING_TYPE = new EnumDescription("String", "String");
    private final static EnumDescription NUMBER_TYPE = new EnumDescription("Number", "Number");
    private final static EnumDescription DATE_TYPE = new EnumDescription("Date", "Date");
    private final static EnumDescription BOOL_TYPE = new EnumDescription("Boolean", "Boolean");
    private final static EnumDescription ANY_TYPE = new EnumDescription("Any", "Any");
    private final static EnumDescription PWD_TYPE = new EnumDescription(PWD, PWD);

    public final static List<EnumDescription> paramTypesGUI = Arrays.asList(STRING_TYPE, NUMBER_TYPE, DATE_TYPE, BOOL_TYPE, ANY_TYPE, PWD_TYPE);

    // FOR API
    private final static EnumDescription INT_TYPE = new EnumDescription("Int", "Int");
    private final static EnumDescription FLOAT_TYPE = new EnumDescription("Float", "Float");
    private final static EnumDescription DATETIME_TYPE = new EnumDescription("DateTime", "DateTime");
    private final static EnumDescription LONG_TYPE = new EnumDescription("Long", "Long");
    private final static EnumDescription DOUBLE_TYPE = new EnumDescription("Double", "Double");
    private final static EnumDescription DECIMAL_TYPE = new EnumDescription("Decimal", "Decimal");

    public final static List<EnumDescription> paramTypesAPI = Arrays.asList(STRING_TYPE, INT_TYPE, FLOAT_TYPE, DATETIME_TYPE, BOOL_TYPE, LONG_TYPE, DOUBLE_TYPE, DECIMAL_TYPE);

    public final static Map<String, List<EnumDescription>> mapping = new HashMap<>();
    public final static int NUM_OF_TYPES = paramTypesAPI.size() + paramTypesGUI.size();

    static {
        mapping.put("GUI", paramTypesGUI);
        mapping.put("API", paramTypesAPI);
    }

    private String parameterJson;

    @DataBoundConstructor
    public SpecifyParametersModel(String parameterJson) {
        this.parameterJson = parameterJson;
    }

    public String getParameterJson() {
        return parameterJson;
    }

    public void setParameterJson(String parameterJson) {
        this.parameterJson = parameterJson;
    }

    public void addProperties(Properties props, String searchStr, Node node) throws Exception {
        JSONArray testParams = (JSONArray) JSONValue.parseStrict(parameterJson);

        int pidx = 1;
        while (props.getProperty(searchStr + pidx) != null) {
            final int currPidx = pidx;

            List<Object> relevant = testParams.stream().filter(elem -> Integer.parseInt((String) (((JSONObject) elem).get("index"))) == currPidx).collect(Collectors.toList());

            for (int i = 0; i < relevant.size(); ++i) {
                JSONObject curr = ((JSONObject) relevant.get(i));
                String name = curr.get("name").toString();
                String type = curr.get("type").toString();
                String val = curr.get("value").toString();
                if (type.equals(PWD) && StringUtils.isNotBlank(val))
                {
                    val = EncryptionUtils.encrypt(val, node);
                }

                props.setProperty(String.format("Param%d_Name_%d", currPidx, i + 1), name);
                props.setProperty(String.format("Param%d_Value_%d", currPidx, i + 1), val);
                props.setProperty(String.format("Param%d_Type_%d", currPidx, i + 1), type);
            }

            ++pidx;
        }
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<SpecifyParametersModel> {
        @Nonnull
        public String getDisplayName() {
            return "Specify test parameters model";
        }

        public List<EnumDescription> getParamTypesGUI() {
            return paramTypesGUI;
        }

        public List<EnumDescription> getParamTypesAPI() {
            return paramTypesAPI;
        }

        public Map<String, List<EnumDescription>> getMapping() {
            return mapping;
        }

        public int getNumOfTypes() {
            return NUM_OF_TYPES;
        }
    }

}
