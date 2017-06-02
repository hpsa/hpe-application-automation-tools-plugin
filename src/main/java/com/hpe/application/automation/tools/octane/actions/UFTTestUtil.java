/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.actions;

import hudson.FilePath;
import org.apache.poi.poifs.filesystem.*;
import org.apache.poi.util.StringUtil;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

/**
 * Utility for extracting information from UFT test located on FS
 */
public class UFTTestUtil {

    /**
     * Extract test description from UFT GUI test.
     * Note : UFT API test doesn't contain description
     * @param dirPath path of UFT test
     * @return test description
     */
    public static String getTestDescription(FilePath dirPath) {
        String desc;

        try {
            if(!dirPath.exists()){
                return  null;
            }

            FilePath tspTestFile = new FilePath(dirPath, "Test.tsp");
            InputStream is = new FileInputStream(tspTestFile.getRemote());
            String xmlContent = decodeXmlContent(is);

            SAXBuilder saxBuilder = new SAXBuilder(XMLReaders.NONVALIDATING, null, null);
            Document document = saxBuilder.build(new StringReader(xmlContent));
            Element rootElement = document.getRootElement();
            Element descElement = rootElement.getChild("Description");
            desc = descElement.getValue();
        } catch (Exception e) {
            return null;
        }

        return desc;
    }

    public static String decodeXmlContent(InputStream stream) throws IOException {
        POIFSFileSystem poiFS = new POIFSFileSystem(stream);
        DirectoryNode root = poiFS.getRoot();
        String xmlData = "";

        for (Entry entry : root) {
            String name = entry.getName();
            if ("ComponentInfo".equals(name)) {
                if (entry instanceof DirectoryEntry) {
                    System.out.println(entry);
                } else if (entry instanceof DocumentEntry) {
                    byte[] content = new byte[((DocumentEntry) entry).getSize()];
                    poiFS.createDocumentInputStream("ComponentInfo").read(content);
                    String fromUnicodeLE = StringUtil.getFromUnicodeLE(content);
                    xmlData = fromUnicodeLE.substring(fromUnicodeLE.indexOf('<')).replaceAll("\u0000", "");
                }
            }
        }
        return xmlData;
    }
}
