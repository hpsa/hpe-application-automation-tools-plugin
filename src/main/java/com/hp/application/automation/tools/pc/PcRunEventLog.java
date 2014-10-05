package com.hp.application.automation.tools.pc;

import java.util.ArrayList;

import com.thoughtworks.xstream.XStream;

public class PcRunEventLog {
    
    private ArrayList<PcRunEventLogRecord> RecordsList;

    public PcRunEventLog() {
        RecordsList = new ArrayList<PcRunEventLogRecord>();
    }
        
    public static PcRunEventLog xmlToObject(String xml)
    {         
      XStream xstream = new XStream();
      xstream.alias("Record" , PcRunEventLogRecord.class);
      xstream.alias("EventLog" , PcRunEventLog.class);
      xstream.addImplicitCollection(PcRunEventLog.class, "RecordsList");
      xstream.setClassLoader(PcRunEventLog.class.getClassLoader());
      return (PcRunEventLog)xstream.fromXML(xml);    
    }

    public ArrayList<PcRunEventLogRecord> getRecordsList() {
        return RecordsList;
    }

}
