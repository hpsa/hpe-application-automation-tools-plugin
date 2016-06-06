package com.hp.application.automation.tools.pc;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;

public class PcTrendReportMetaData {
    private static ArrayList<PcTrendedRun> Results;
    static Document dom;
    static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

//    public PcTrendReportMetaData() {
//        Results = new ArrayList<PcTrendedRun>();
//    }

    public static ArrayList<PcTrendedRun> xmlToObject(String xml)
    {
        Results = new ArrayList<PcTrendedRun>();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))));
            Element doc = dom.getDocumentElement();
            NodeList nListTrendedRun = doc.getElementsByTagName("TrendedRun");

            for (int i=0;i<nListTrendedRun.getLength();i++){
                Node nTrendedRun = nListTrendedRun.item(i);
                NodeList nListChild =  nTrendedRun.getChildNodes();
                PcTrendedRun pcTR = new PcTrendedRun();
                for (int j=0;j < nListChild.getLength();j++) {
                    if (nListChild.item(j).getNodeName().equals("RunID")){
                        pcTR.setRunID(Integer.parseInt(nListChild.item(j).getTextContent()));
                    }
                    if (nListChild.item(j).getNodeName().equals("State")){
                        pcTR.setState(nListChild.item(j).getTextContent());
                    }
                }
               Results.add(pcTR);
            }

        }catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

        return Results;
    }

}
