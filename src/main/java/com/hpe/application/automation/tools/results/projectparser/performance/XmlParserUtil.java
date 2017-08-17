package com.hpe.application.automation.tools.results.projectparser.performance;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.annotation.Nullable;

/**
 * Created by kazaky on 17/10/2016.
 */
public class XmlParserUtil {

  @Nullable
  public static Node getNode(String tagName, NodeList nodes) {
    for (int x = 0; x < nodes.getLength(); x++) {
      Node node = nodes.item(x);
      if (node.getNodeName().equalsIgnoreCase(tagName)) {
        return node;
      }
    }
    return null;
  }

  public static String getNodeValue(Node node) {
    NodeList childNodes = node.getChildNodes();
    for (int x = 0; x < childNodes.getLength(); x++) {
      Node data = childNodes.item(x);
      if (data.getNodeType() == Node.TEXT_NODE)
        return data.getNodeValue();
    }
    return "";
  }

  public static String getNodeValue(String tagName, NodeList nodes) {
    for (int x = 0; x < nodes.getLength(); x++) {
      Node node = nodes.item(x);
      if (node.getNodeName().equalsIgnoreCase(tagName)) {
        NodeList childNodes = node.getChildNodes();
        for (int y = 0; y < childNodes.getLength(); y++) {
          Node data = childNodes.item(y);
          if (data.getNodeType() == Node.TEXT_NODE)
            return data.getNodeValue();
        }
      }
    }
    return "";
  }

  public static String getNodeAttr(String attrName, Node node) {
    NamedNodeMap attrs = node.getAttributes();
    for (int y = 0; y < attrs.getLength(); y++) {
      Node attr = attrs.item(y);
      if (attr.getNodeName().equalsIgnoreCase(attrName)) {
        return attr.getNodeValue();
      }
    }
    return "";
  }

  public static String getNodeAttr(String tagName, String attrName, NodeList nodes) {
    for (int x = 0; x < nodes.getLength(); x++) {
      Node node = nodes.item(x);
      if (node.getNodeName().equalsIgnoreCase(tagName)) {
        NodeList childNodes = node.getChildNodes();
        for (int y = 0; y < childNodes.getLength(); y++) {
          Node data = childNodes.item(y);
          if ((data.getNodeType() == Node.ATTRIBUTE_NODE) && data.getNodeName().equalsIgnoreCase(attrName)) {
              return data.getNodeValue();
            }
          }
        }
      }
    return "";
  }
}
