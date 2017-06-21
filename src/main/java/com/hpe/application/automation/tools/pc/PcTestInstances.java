package com.hpe.application.automation.tools.pc;

import com.thoughtworks.xstream.XStream;

import java.util.ArrayList;

/**
 * Created by bemh on 6/1/2017.
 */
public class PcTestInstances {


    private ArrayList<PcTestInstance> TestInstancesList;

    public PcTestInstances() {
        TestInstancesList = new ArrayList<PcTestInstance>();
    }

    public static PcTestInstances xmlToObject(String xml)
    {
        XStream xstream = new XStream();
        xstream.alias("TestInstance" , PcTestInstance.class);
        xstream.alias("TestInstances" , PcTestInstances.class);
        xstream.addImplicitCollection(PcTestInstances.class, "TestInstancesList");
        xstream.setClassLoader(PcTestInstances.class.getClassLoader());
        return (PcTestInstances)xstream.fromXML(xml);
    }

    public ArrayList<PcTestInstance> getTestInstancesList() {
        return TestInstancesList;
    }
}
