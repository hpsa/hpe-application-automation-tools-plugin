/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2021 Micro Focus or one of its affiliates.
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

import com.microfocus.application.automation.tools.model.EnumDescription;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.kohsuke.stapler.DataBoundConstructor;
import net.minidev.json.parser.ParseException;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class SpecifyParametersModel extends AbstractDescribableImpl<SpecifyParametersModel> {

    private final static EnumDescription STRING_TYPE = new EnumDescription("String", "String");
    private final static EnumDescription NUMBER_TYPE = new EnumDescription("Number", "Number");
    private final static EnumDescription DATE_TYPE = new EnumDescription("Date", "Date");
    private final static EnumDescription BOOL_TYPE = new EnumDescription("Boolean", "Boolean");
    private final static EnumDescription ANY_TYPE = new EnumDescription("Any", "Any");
    public final static List<EnumDescription> paramTypes = Arrays.asList(STRING_TYPE, NUMBER_TYPE, DATE_TYPE, BOOL_TYPE, ANY_TYPE);

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

    public void addProperties(Properties props, String searchStr) throws ParseException {
        JSONArray testParameters = (JSONArray) JSONValue.parseStrict(parameterJson);

        int pidx = 1;
        while (props.getProperty(searchStr + pidx) != null) {
            final int currPidx = pidx;

            List<Object> relevant = testParameters.stream().filter(elem -> Integer.parseInt((String) (((JSONObject) elem).get("index"))) == currPidx).collect(Collectors.toList());

            for (int i = 0; i < relevant.size(); ++i) {
                JSONObject curr = ((JSONObject) relevant.get(i));

                props.setProperty(String.format("Param%d_Name_%d", currPidx, i + 1), curr.get("name").toString());
                props.setProperty(String.format("Param%d_Value_%d", currPidx, i + 1), curr.get("value").toString());
                props.setProperty(String.format("Param%d_Type_%d", currPidx, i + 1), curr.get("type").toString());
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

        public List<EnumDescription> getParamTypes() {
            return paramTypes;
        }
    }

}
