package com.hpe.application.automation.tools.pc;

import com.thoughtworks.xstream.XStream;

import java.util.ArrayList;

/**
 * Created by bemh on 6/1/2017.
 */
public class PcTestSets {

    private ArrayList<PcTestSet> pcTestSetsList;

    public PcTestSets(){
        pcTestSetsList = new ArrayList<PcTestSet>();
    }


    public static PcTestSets xmlToObject(String xml)
    {
        XStream xstream = new XStream();
        xstream.alias("TestSet" , PcTestSet.class);
        xstream.alias("TestSets" , PcTestSets.class);
        xstream.addImplicitCollection(PcTestSets.class, "pcTestSetsList");
        xstream.setClassLoader(PcTestSets.class.getClassLoader());
        return (PcTestSets)xstream.fromXML(xml);
    }

    public ArrayList<PcTestSet> getPcTestSetsList() {
        return pcTestSetsList;
    }
}
