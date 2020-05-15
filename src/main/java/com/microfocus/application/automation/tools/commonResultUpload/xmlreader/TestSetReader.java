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

package com.microfocus.application.automation.tools.commonResultUpload.xmlreader;

import com.microfocus.application.automation.tools.commonResultUpload.xmlreader.model.EntitiesFieldMap;
import com.microfocus.application.automation.tools.commonResultUpload.xmlreader.model.XmlResultEntity;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestSetReader {

    private XpathReader xr;
    private ValueTranslator translator;
    private EntitiesFieldMap entitiesFieldMap;
    private static final String ROOT = "root";

    public TestSetReader(String resultPath, EntitiesFieldMap entitiesFieldMap)
            throws IOException, SAXException, ParserConfigurationException {
        xr = new XpathReader(resultPath);
        translator = new ValueTranslator(xr);
        this.entitiesFieldMap = entitiesFieldMap;
    }

    public List<XmlResultEntity> readTestsets() throws XPathExpressionException {
        List<XmlResultEntity> testsets = readEntities(entitiesFieldMap.getTestset(), xr.getDoc());
        return testsets;
    }

    private List<XmlResultEntity> readEntities(Map<String, String> configMap, Node node)
            throws XPathExpressionException {
        String rootXpath = configMap.get(ROOT);
        rootXpath = rootXpath.substring(2, rootXpath.length());
        NodeList nodes = xr.getNodeListFromNode(rootXpath, node);

        List<XmlResultEntity> entities = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            XmlResultEntity entity = new XmlResultEntity();
            Node currentNode = nodes.item(i);

            Map<String, String> fieldsMap = new HashMap<>();
            for (String fieldname : configMap.keySet()) {
                String fieldValue = configMap.get(fieldname);
                if (ROOT.equals(fieldname)) {
                    continue;
                }
                fieldValue = translator.translate(fieldValue, currentNode);
                fieldsMap.put(fieldname, fieldValue);
            }
            entity.setValueMap(fieldsMap);

            Map<String, String> nextConfigMap = entitiesFieldMap.getNextConfigMap(configMap);
            if (nextConfigMap != null) {
                List<XmlResultEntity> subEntities = readEntities(nextConfigMap, currentNode);
                entity.setSubEntities(subEntities);
            }
            entities.add(entity);
        }
        return entities;
    }
}
