/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P.
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
 */

package com.microfocus.application.automation.tools.commonResultUpload.service;

import com.microfocus.application.automation.tools.results.service.AlmRestTool;
import com.microfocus.application.automation.tools.results.service.almentities.AlmCommonProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CriteriaTranslator {

    public static final String CRITERIA_PREFIX = "q|";

    public static String getCriteriaString(String[] fields, Map<String, String> entity) {
        List<String> tobeRemoved = new ArrayList<>();
        Map<String, String> tobeAdded = new HashMap<>();

        StringBuilder sb = new StringBuilder();
        sb.append("fields=");
        for (int i = 0; i < fields.length; i++) {
            sb.append(fields[i]);
            if (i < fields.length - 1) {
                sb.append(",");
            }
        }

        sb.append("&query={");
        for (String key : entity.keySet()) {
            if (key.startsWith(CRITERIA_PREFIX)) {
                tobeRemoved.add(key);
                String realName = key.substring(
                        key.indexOf(CRITERIA_PREFIX) + CRITERIA_PREFIX.length(),
                        key.length());
                tobeAdded.put(realName, entity.get(key));
                sb.append(realName).append("[")
                        .append(AlmRestTool.getEncodedString(entity.get(key)))
                        .append("]").append(";");
            }
        }
        // Search by name by default;
        if (tobeRemoved.size() == 0) {
            sb.append(AlmCommonProperties.NAME).append("[")
                    .append(AlmRestTool.getEncodedString(entity.get("name")))
                    .append("];");
        }
        sb.append("}");

        entity.putAll(tobeAdded);
        for (String item : tobeRemoved) {
            entity.remove(item);
        }
        return sb.toString();
    }
}
