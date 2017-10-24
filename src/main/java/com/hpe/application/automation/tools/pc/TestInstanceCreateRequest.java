package com.hpe.application.automation.tools.pc;

import com.thoughtworks.xstream.XStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by bemh on 5/23/2017.
 */
public class TestInstanceCreateRequest {

    private String xmlns = PcRestProxy.PC_API_XMLNS;

    private int testId;
    private int testSetId;

    public TestInstanceCreateRequest(int testId, int testSetId) {
        this.testId = testId;
        this.testSetId = testSetId;
    }

    public String objectToXML() {
        XStream xstream = new XStream();
        xstream.useAttributeFor(TestInstanceCreateRequest.class, "xmlns");
        xstream.alias("TestInstance", TestInstanceCreateRequest.class);
        xstream.aliasField("TestID", TestInstanceCreateRequest.class, "testId");
        xstream.aliasField("TestSetID", TestInstanceCreateRequest.class, "testSetId");
        return xstream.toXML(this);
    }

    public int getTestInstanceIDFromResponse(String xml, String getTestInstanceID) throws IOException, SAXException, ParserConfigurationException {


        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8")));
        Document doc = builder.parse(is);
        Element element = doc.getDocumentElement();


        NodeList nListTrendedRun = doc.getElementsByTagName("TestInstanceID");
        return Integer.parseInt(nListTrendedRun.item(0).getTextContent());

    }
}
