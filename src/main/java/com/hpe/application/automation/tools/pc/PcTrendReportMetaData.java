/*
 * © Copyright 2013 EntIT Software LLC
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright (c) 2018 Micro Focus Company, L.P.
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
 * ___________________________________________________________________
 *
 */

package com.hpe.application.automation.tools.pc;


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
