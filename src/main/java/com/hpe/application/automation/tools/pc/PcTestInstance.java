package com.hpe.application.automation.tools.pc;

import com.thoughtworks.xstream.XStream;

/**
 * Created by bemh on 6/1/2017.
 */
public class PcTestInstance {
    public int TestID;
    public int TestSetID;
    public int TestInstanceID;

    public static PcTestInstance xmlToObject(String xml)
    {
        XStream xstream = new XStream();
        xstream.alias("TestInstanceID" , PcRunResult.class);
        return (PcTestInstance)xstream.fromXML(xml);
    }

    public int getInstanceId() {
        return TestInstanceID;
    }

    public int getTestId(){
        return TestID;
    }

    public int getTestSetId(){
        return TestSetID;
    }
}
