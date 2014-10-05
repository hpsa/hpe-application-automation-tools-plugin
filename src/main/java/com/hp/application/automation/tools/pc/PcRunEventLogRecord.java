package com.hp.application.automation.tools.pc;

import com.thoughtworks.xstream.XStream;

public class PcRunEventLogRecord {

    private int    ID;

    private String Type;

    private String Time;

    private String Name;

    private String Description;

    private String Responsible;
    
    public static PcRunEventLogRecord xmlToObject(String xml)
    {         
      XStream xstream = new XStream();
      xstream.alias("Record" , PcRunEventLogRecord.class);
      return (PcRunEventLogRecord)xstream.fromXML(xml); 
    }

    public int getID() {
        return ID;
    }

    public String getType() {
        return Type;
    }

    public String getTime() {
        return Time;
    }

    public String getName() {
        return Name;
    }

    public String getDescription() {
        return Description;
    }

    public String getResponsible() {
        return Responsible;
    }

}
