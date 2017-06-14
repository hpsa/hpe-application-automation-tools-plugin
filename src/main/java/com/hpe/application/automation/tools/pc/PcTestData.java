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
import java.io.IOException;
import java.io.StringReader;

/**
 * Created by bemh on 6/5/2017.
 * Partial implementation of the test xml structure
 */
public class PcTestData {




    static Document dom;
    static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();


    public static PcTest xmlToObject(String xml){

        PcTest pcTest = new PcTest();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(new InputSource(new StringReader(xml)));
            Element doc = dom.getDocumentElement();
            NodeList nListTestNodes = doc.getElementsByTagName("AutomaticTrending");
            if (nListTestNodes.getLength() >0 )
            {
                NodeList nListChild = nListTestNodes.item(0).getChildNodes();
                for (int j=0;j < nListChild.getLength();j++) {
                    if (nListChild.item(j).getNodeName().equals("ReportId")){
                        pcTest.setTrendReportId(Integer.parseInt(nListChild.item(j).getTextContent()));
                        break;
                    }
                }
            }

            nListTestNodes = doc.getElementsByTagName("ID");
            pcTest.setTestId(Integer.parseInt(nListTestNodes.item(0).getFirstChild().getNodeValue()));

            nListTestNodes = doc.getElementsByTagName("Name");
            pcTest.setTestName(nListTestNodes.item(0).getFirstChild().getNodeValue());




        }catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

        return pcTest;
    }

}
