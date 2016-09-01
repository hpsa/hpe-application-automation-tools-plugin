package ngaclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Utility class which translates a set of Jenkins test results in on NGA style test result file.
 * This class is using a SAX parser to parse the XML files, and tries to be as much as possible a tolerant reader.
 * @author Romulus Pa&#351;ca
 */
public class JenkinsToNga extends DefaultHandler {
    private static final String TYPE = "type";
    private static final String TIME = "time";
    private static final String MESSAGE = "message";
    private static final String CLASSNAME = "classname";
    private static final String NAME = "name";
    private static final String FAILURE = "failure";
    private static final String TESTCASE = "testcase";
    private static final String TESTSUITE = "testsuite";

    private final PrintWriter printOut;
    private final ArrayDeque<String> tagsStack = new ArrayDeque<>();
    private final long buildStartTime;

    private String crtModule;
    private TestCaseInfo crtTestCase;

    private JenkinsToNga(final long buildStartTime, final PrintWriter printOut) {
        this.buildStartTime = buildStartTime;
        this.printOut = printOut;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException {
        this.tagsStack.addLast(qName);
        if (qName.equals(TESTSUITE)) {
            this.crtModule = atts.getValue(NAME);
        } else if (qName.equals(TESTCASE)) {
            this.crtTestCase = new TestCaseInfo();
            this.crtTestCase.name = atts.getValue(NAME);
            final String fullClassName = atts.getValue(CLASSNAME);
            this.crtTestCase.className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            this.crtTestCase.packageName = fullClassName.substring(0, fullClassName.lastIndexOf("."));
            this.crtTestCase.duration = Math.round(Double.parseDouble(atts.getValue(TIME)));
        } else if (qName.equals(FAILURE)) {
            if (this.crtTestCase != null) {
                this.crtTestCase.failure = new FailureInfo();
                this.crtTestCase.failure.message = atts.getValue(MESSAGE);
                this.crtTestCase.failure.failureType = atts.getValue(TYPE);
            }
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        if (qName.equals(TESTCASE)) {
            this.crtTestCase.toXml(this.crtModule, this.buildStartTime, this.printOut);
            this.crtTestCase = null;
        }
        this.tagsStack.removeLast();
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        final String crtTag = this.tagsStack.peekLast();
        if (crtTag.equals(FAILURE)) {
            if (this.crtTestCase.failure.stackTrace == null) {
                this.crtTestCase.failure.stackTrace = new String(ch, start, length);
            } else {
                this.crtTestCase.failure.stackTrace += new String(ch, start, length);
            }
        }
    }

    private static class FailureInfo {
        String failureType;
        String message;
        String stackTrace;
    }

    private static class TestCaseInfo {
        String name;
        String className;
        String packageName;
        long duration;
        FailureInfo failure;

        void toXml(final String module, final long buildStartTime, final PrintWriter out) {
            // write it out
            if (this.failure == null) {
                out.println(String.format(
                        "<test_run module=\"%s\" package=\"%s\" class=\"%s\" name=\"%s\" duration=\"%d\" status=\"Passed\" started=\"%d\" />",
                        module, this.packageName, this.className, this.name, this.duration, buildStartTime));

            } else {
                out.println(String.format(
                        "<test_run module=\"%s\" package=\"%s\" class=\"%s\" name=\"%s\" duration=\"%d\" status=\"Failed\" started=\"%d\" >",
                        module, this.packageName, this.className, this.name, this.duration, buildStartTime));
                out.println(String.format("<error type=\"%s\"  message=\"%s\" >",
                        StringEscapeUtils.escapeXml10(this.failure.failureType),
                        StringEscapeUtils.escapeXml10(this.failure.message)));
                out.println("<![CDATA[" + this.failure.stackTrace + "]]>");
                out.println("</error>");
                out.println("</test_run>");
            }
        }
    }

    private static String getBuildName(final BuildInfo buildInfo) {
        final String buildName = buildInfo.getBuildName();
        return buildName != null ? buildName : buildInfo.getBuildCiId();
    }

    private static String getBuildJobName(final BuildInfo buildInfo) {
        final String jobName = buildInfo.getJob().getName();
        return jobName != null ? jobName : buildInfo.getJob().getId();
    }

    /**
     * Use this method to read and parse a set of jenkins test results file from
     * a zip archive and write them on stream using the NGa XML format
     */
    public static void transform(final BuildInfo buildInfo, final ZipInputStream jenkinsIn,
            final OutputStream ngaOutFile) throws IOException, SAXException, ParserConfigurationException {
        final PrintWriter printOut = new PrintWriter(ngaOutFile);
        printOut.println("<?xml version='1.0' encoding='UTF-8'?>");
        printOut.println("<test_result>");
        printOut.println(String.format(
                "<build server_id=\"%s\" job_id=\"%s\" job_name=\"%s\" build_id=\"%s\" build_name=\"%s\" />",
                buildInfo.getServerCiId(), buildInfo.getJob().getId(), getBuildJobName(buildInfo),
                buildInfo.getBuildCiId(), getBuildName(buildInfo)));
        printOut.println("<test_runs>");
        ZipEntry entry;
        final int readBufferSize = 4096;
        while ((entry = jenkinsIn.getNextEntry()) != null) {
            if (!entry.isDirectory() && entry.getName().endsWith(".xml")) {
                final File tmp = File.createTempFile("nga-tmp", ".tmp");
                final FileOutputStream fout = new FileOutputStream(tmp);
                final byte data[] = new byte[readBufferSize];
                int count;
                while ((count = jenkinsIn.read(data, 0, readBufferSize)) != -1) {
                    fout.write(data, 0, count);
                }
                fout.close();
                final FileInputStream fin = new FileInputStream(tmp);
                transform(fin, buildInfo.getStartTime().toEpochMilli(), printOut);
                fin.close();
                tmp.delete();
            }
        }
        printOut.println("</test_runs>");
        printOut.println("</test_result>");
        printOut.flush();
    }

    private static void transform(final InputStream jenkinsFile, final long buildStartTime,
            final PrintWriter ngaOutStream) throws IOException, SAXException, ParserConfigurationException {
        final SAXParserFactory spf = SAXParserFactory.newInstance();
        final SAXParser saxParser = spf.newSAXParser();
        final XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setContentHandler(new JenkinsToNga(buildStartTime, ngaOutStream));
        xmlReader.parse(new InputSource(jenkinsFile));
    }

}
