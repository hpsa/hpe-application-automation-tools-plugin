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

package com.hp.octane.plugins.jenkins.actions;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.poi.poifs.filesystem.*;
import org.apache.poi.util.StringUtil;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMFactory;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.SAXHandlerFactory;
import org.jdom2.input.sax.XMLReaders;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class Converts UFT's Paramters info stored in Resource.mtr file into JSON string
 */
public class UFTParameterFactory {
    private static POIFSFileSystem poiFS = null;
    private static String xmlData = "";
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(UFTParameterFactory.class.getName());


    public static String convertResourceMtrAsJSON(InputStream  resourceMtrInputStream) throws IOException {

//        poiFS = new POIFSFileSystem(new FileInputStream(new File("C:\\Users\\kashbi\\Desktop\\very-public-repository-master\\CalculatorPlusParams\\Action0\\Resource.mtr")));
        //todo Check is exists
        poiFS = new POIFSFileSystem(resourceMtrInputStream);
        DirectoryNode root = poiFS.getRoot();

        for (Entry entry : root) {
            String name = entry.getName();
            if (name.equals("ComponentInfo")) {
                if (entry instanceof DirectoryEntry) {
                    System.out.println(entry);
                } else if (entry instanceof DocumentEntry) {
                    byte[] content = new byte[((DocumentEntry) entry).getSize()];
                    poiFS.createDocumentInputStream("ComponentInfo").read(content);
                    String fromUnicodeLE = StringUtil.getFromUnicodeLE(content);
                    xmlData = fromUnicodeLE.substring(fromUnicodeLE.indexOf("<")).replaceAll("\u0000", "");
//                    System.out.println(xmlData);
                }
            }
        }
        try {
            SAXBuilder saxBuilder = new SAXBuilder(XMLReaders.NONVALIDATING, (SAXHandlerFactory) null, (JDOMFactory) null);
            Document document = null;
            document = saxBuilder.build(new StringReader(xmlData));
//            System.out.println("Root element :" + document.getRootElement().getName());
            Element classElement = document.getRootElement();
            List<Element> studentList = classElement.getChildren();
//            System.out.println("----------------------------");
            ObjectMapper mapper = new ObjectMapper();
            ArrayList<UFTParameter> uftParameters = new ArrayList<UFTParameter>();
            UFTParameter uftParameter = new UFTParameter();
            for (int temp = 0; temp < studentList.size(); temp++) {
                Element tag = studentList.get(temp);
//                System.out.println("\nCurrent Element :"
//                        + tag.getName());
                if (tag.getName().equalsIgnoreCase("ArgumentsCollection")) {
                    List<Element> children = tag.getChildren();
                    for (int i = 0; i < children.size(); i++) {
                        Element element = children.get(i);
                        List<Element> elements = element.getChildren();

                        for (int j = 0; j < elements.size(); j++) {

                            Element element1 = elements.get(j);
                            switch (element1.getName()) {
                                case "ArgName":
                                    uftParameter.setArgName(element1.getValue());
                                    break;
                                case "ArgDirection":
                                    uftParameter.setArgDirection(Integer.parseInt(element1.getValue()));
                                    break;
                                case "ArgDefaultValue":
                                    uftParameter.setArgDefaultValue(element1.getValue());
                                    break;
                                case "ArgType":
                                    uftParameter.setArgType(element1.getValue());
                                    break;
                                case "ArgIsExternal":
                                    uftParameter.setArgIsExternal(Integer.parseInt(element1.getValue()));
                                    break;
                            }
                        }
                        uftParameters.add(uftParameter);
                    }
                    String jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(uftParameters);
                    return jsonInString;
                }
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
        return null;
    }


}



