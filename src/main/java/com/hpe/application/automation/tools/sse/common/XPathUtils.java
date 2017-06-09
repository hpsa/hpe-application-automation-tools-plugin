package com.hpe.application.automation.tools.sse.common;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import com.hpe.application.automation.tools.common.SSEException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */

public class XPathUtils {
    
    public static List<Map<String, String>> toEntities(String xml) {
        
        Document document = getDocument(xml);
        
        List<Map<String, String>> ret = new ArrayList<Map<String, String>>();
        NodeList entities = document.getElementsByTagName("Entity");
        for (int i = 0; i < entities.getLength(); i++) {
            Map<String, String> currEntity = new HashMap<String, String>();
            NodeList fields = ((Element) entities.item(i)).getElementsByTagName("Field");
            for (int j = 0; j < fields.getLength(); j++) {
                Node item = fields.item(j);
                currEntity.put(item.getAttributes().item(0).getNodeValue(), getFieldValue(item));
            }
            ret.add(currEntity);
        }
        
        return ret;
    }
    
    public static String getAttributeValue(String xml, String attrName) {
        
        NodeList nodes = getChildNodes(xml, "Entity/Fields/Field");
        String ret = StringUtils.EMPTY_STRING;
        for (int i = 0; i < nodes.getLength(); i++) {
            Node currNode = nodes.item(i);
            String attr;
            try {
                attr = getNecessaryAttribute(currNode, "Name");
            } catch (Throwable cause) {
                throw new SSEException(cause);
            }
            if (attr.equals(attrName)) {
                ret = getFieldValue(currNode);
                break;
            }
        }
        
        return ret;
    }
    
    private static String getFieldValue(Node node) {
        
        String ret = null;
        Node child = node.getFirstChild();
        if (child != null) {
            Node child2 = child.getFirstChild();
            if (child2 != null) {
                ret = child2.getNodeValue();
            }
        }
        
        return ret;
    }
    
    private static NodeList getChildNodes(String xml, String xpath) {
        
        NodeList ret = null;
        try {
            Document document = getDocument(xml);
            XPathFactory factory = XPathFactory.newInstance();
            XPathExpression expression = factory.newXPath().compile(xpath);
            ret = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
        } catch (Throwable cause) {
            throw new SSEException(cause);
        }
        
        return ret;
    }
    
    private static String getNecessaryAttribute(Node node, String attributeName) {
        
        if (!node.hasAttributes()) {
            return null;
        }
        Node attr = node.getAttributes().getNamedItem(attributeName);
        if (attr == null) {
            throw new SSEException(String.format(
                    "Error parsing XML, missing mandatory attribute '%s'",
                    attributeName));
        }
        String ret = attr.getNodeValue();
        if (StringUtils.isNullOrEmpty(ret)) {
            throw new SSEException(String.format(
                    "Error parsing XML, mandatory attribute '%s' cannot be empty", //$NON-NLS-1$
                    attributeName));
        }
        
        return ret;
    }
    
    private static Document getDocument(String xml) {
        
        Document ret = null;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource inputSource = new InputSource();
            inputSource.setCharacterStream(new StringReader(xml));
            ret = builder.parse(inputSource);
        } catch (Throwable cause) {
            throw new SSEException(cause);
        }
        
        return ret;
    }
}
