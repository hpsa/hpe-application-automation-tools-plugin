package com.hpe.application.automation.tools.pc;

import com.thoughtworks.xstream.XStream;

/**
 * Created by bemh on 6/1/2017.
 */
public class PcTestSet {

    String TestSetName;
    String TestSetComment;
    int TestSetParentId;
    int TestSetID;

    public static PcTestSet xmlToObject(String xml){
        XStream xstream = new XStream();
        xstream.alias("TestSetID" , PcRunResult.class);
        return (PcTestSet)xstream.fromXML(xml);
    }


    public String getTestSetName(){
        return TestSetName;
    }
    public String getTestSetComment(){
        return TestSetComment;
    }

    public int getTestSetParentId(){
        return TestSetParentId;
    }

    public int getTestSetID(){
        return TestSetID;
    }

}
