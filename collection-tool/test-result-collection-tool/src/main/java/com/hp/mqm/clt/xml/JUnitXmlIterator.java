package com.hp.mqm.clt.xml;

import com.hp.mqm.clt.tests.TestResult;
import com.hp.mqm.clt.tests.TestResultStatus;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Iterator;

public class JUnitXmlIterator extends AbstractXmlIterator<TestResult> {

    private String packageName;
    private String className;
    private String testName;
    private TestResultStatus status;
    private long duration;
    private long started;

    public JUnitXmlIterator(InputStream read) throws XMLStreamException {
        super(read);
    }

    @Override
    protected void onEvent(XMLEvent event) throws IOException {
        if (event instanceof StartElement) {
            StartElement element = (StartElement) event;
            String localName = element.getName().getLocalPart();
            if ("testsuite".equals(localName)) { // NON-NLS
                Attribute timestamp = element.getAttributeByName(new QName("timestamp")); // NON-NLS
                started = (timestamp == null) ? 0 : parseDateTime(timestamp.getValue());
            } else if ("testcase".equals(localName)) { // NON-NLS
                packageName = "";
                className = "";
                testName = "";
                status = TestResultStatus.PASSED;
                duration = 0;

                Iterator iterator = element.getAttributes();
                while (iterator.hasNext()) {
                    Attribute attribute = (Attribute) iterator.next();
                    if ("classname".equals(attribute.getName().toString())) {
                        parseClassname(attribute.getValue());
                    } else if ("name".equals(attribute.getName().toString())) {
                        testName = attribute.getValue();
                    } else if ("time".equals(attribute.getName().toString())) {
                        duration = parseTime(attribute.getValue());
                    }
                }
            } else if ("skipped".equals(localName)) { // NON-NLS
                status = TestResultStatus.SKIPPED;
            } else if ("failure".equals(localName)) { // NON-NLS
                status = TestResultStatus.FAILED;
            } else if ("error".equals(localName)) { // NON-NLS
                status = TestResultStatus.FAILED;
            }
        } else if (event instanceof EndElement) {
            EndElement element = (EndElement) event;
            String localName = element.getName().getLocalPart();

            if ("testcase".equals(localName) && StringUtils.isNotEmpty(testName)) { // NON-NLS
                addItem(new TestResult(packageName, className, testName, status, duration, started));
            }
        }
    }

    private long parseDateTime(String dateTimeString) {
        try {
            // Should be in ISO 8601 format
            return DatatypeConverter.parseDateTime(dateTimeString).getTimeInMillis();
        } catch (IllegalArgumentException e) {
            System.out.println("Unable to parse the timestamp: " + dateTimeString);
        }
        return 0;
    }

    private long parseTime(String timeString) {
        String time = timeString.replace(",","");
        try {
            float seconds = Float.parseFloat(time);
            return (long) (seconds * 1000);
        } catch (NumberFormatException e) {
            try {
                return new DecimalFormat().parse(time).longValue();
            } catch (ParseException ex) {
                System.out.println("Unable to parse test duration: " + timeString);
            }
        }
        return 0;
    }

    private void parseClassname(String fqn) {
        int p = fqn.lastIndexOf(".");
        className = fqn.substring(p + 1);
        if (p > 0) {
            packageName = fqn.substring(0, p);
        } else {
            packageName = "";
        }
    }
}
