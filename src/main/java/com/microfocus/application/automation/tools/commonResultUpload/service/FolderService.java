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
import java.util.StringTokenizer;

public class FolderService {

    private static final String FOLDER_SEPERATOR = "\\";
    private RestService restService;

    public FolderService(RestService restService) {
        this.restService = restService;
    }

    public Map<String, String> createOrFindPath(String prefix, String parentId, String path) {
        List<Map<String, String>> folders = new ArrayList<Map<String, String>>();
        StringTokenizer tokenizer = new StringTokenizer(path, FOLDER_SEPERATOR);
        while (tokenizer.hasMoreTokens()) {
            String itemString = tokenizer.nextToken();
            Map<String, String> folder = createFolder(prefix, parentId, itemString);
            if (folder != null) {
                folders.add(folder);
                parentId = folder.get("id");
            }
        }
        if (folders.size() > 0) {
            return folders.get(folders.size() - 1);
        } else  {
            return null;
        }
    }

    public Map<String, String> createFolder(String prefix, String parentId, String folderName) {
        Map<String, String> existsFolder = checkFolderExits(prefix, parentId, folderName);
        if (existsFolder == null) {
            existsFolder = new HashMap<>();
            existsFolder.put(AlmCommonProperties.PARENT_ID, parentId);
            existsFolder.put(AlmCommonProperties.NAME, folderName);
            return restService.create(prefix, existsFolder);
        } else {
            return existsFolder;
        }
    }

    public Map<String, String> checkFolderExits(String prefix, String parentId, String folderName) {
        String query = String.format("fields=id,name&query={parent-id[%s];name[%s]}",
                parentId,
                AlmRestTool.getEncodedString(folderName));
        List<Map<String, String>> entities = restService.get(null, prefix, query);
        if (entities.size() > 0) {
            return entities.get(0);
        } else {
            return null;
        }
    }

    public List<Map<String, String>> getSubFolders(String prefix, String parentFolderId) {
        String query = String.format("fields=id,name&query={parent-id[%s]}", parentFolderId);
        return restService.get(null, prefix, query);
    }

    public Map<String, String> findEntityInFolder(
            Map<String, String> testFolder,
            Map<String, String> test,
            String entityPrefix,
            String folderPrefix,
            String[] queryFields) {
        List<Map<String, String>> foundTests = new ArrayList<>();

        // Make criteria fields.
        test.put(CriteriaTranslator.CRITERIA_PREFIX + AlmCommonProperties.PARENT_ID,
                testFolder.get(AlmCommonProperties.ID));
        test.put(CriteriaTranslator.CRITERIA_PREFIX + AlmCommonProperties.NAME,
                test.get(AlmCommonProperties.NAME));

        findEntityInFolderAndSub(foundTests, test, entityPrefix, folderPrefix, queryFields);
        return foundTests.size() > 0 ? foundTests.get(0) : null;
    }

    private void findEntityInFolderAndSub(List<Map<String, String>> foundTests,
                                          Map<String, String> test, String entityPrefix,
                                          String folderPrefix, String[] queryFields) {
        List<Map<String, String>> existTests = restService.get(null,
                entityPrefix, CriteriaTranslator.getCriteriaString(queryFields, test));

        if (existTests == null || existTests.size() == 0) {
            // Not in current folder, find in sub folders
            List<Map<String, String>> subFolders =
                    getSubFolders(folderPrefix, test.get(AlmCommonProperties.PARENT_ID));

            if (subFolders != null) {
                for (Map<String, String> subfolder : subFolders) {
                    // Make criteria fields.
                    test.put(CriteriaTranslator.CRITERIA_PREFIX + AlmCommonProperties.PARENT_ID,
                            subfolder.get(AlmCommonProperties.ID));
                    test.put(CriteriaTranslator.CRITERIA_PREFIX + AlmCommonProperties.NAME,
                            test.get(AlmCommonProperties.NAME));
                    findEntityInFolderAndSub(foundTests, test, entityPrefix, folderPrefix, queryFields);
                }
            }
        } else {
            foundTests.addAll(existTests);
        }
    }
}
