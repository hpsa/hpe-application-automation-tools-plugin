package com.hp.application.automation.tools.run;

import hudson.AbortException;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.junit.Messages;
import jenkins.MasterToSlaveFileCallable;
import jenkins.security.NotReallyRoleSensitiveCallable;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.input.InputRequest;
import org.apache.tools.ant.types.FileSet;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;


import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import hudson.tasks.junit.TestResult;

/**
 * Created by kazaky on 22/03/2017.
 */
public class LrScriptResultsParser {


    public void parseScriptResult(String scriptName,
                                  Run<?,?> build, FilePath workspace, Launcher launcher,
                                  TaskListener listener)
            throws InterruptedException, IOException
    {
        final long buildTime = build.getTimestamp().getTimeInMillis();
        final long timeOnMaster = System.currentTimeMillis();
        this._scriptName = scriptName;
        this._logger = listener;
        invoke(workspace);
    }

    private  TaskListener _logger;
    private Element testSuits;

    public static  String LR_SCRIPT_RESULT_FILENAME = "Results.xml";
    private  long buildTime;
    private  String _scriptName;
    private  long nowMaster;



    public Void invoke(FilePath ws_filePath) throws IOException, InterruptedException {
        final long nowSlave = System.currentTimeMillis();
//            FilePath ws_filePath = new FilePath(channel, ws.getAbsolutePath());
        log(ws_filePath.toURI().toString());
        FilePath sourceFile = ws_filePath.child(this._scriptName).child(LR_SCRIPT_RESULT_FILENAME);
        log(sourceFile.toURI().toString());
        FilePath targetFile = ws_filePath.child(this._scriptName).child("JunitResult.xml");
        log(targetFile.toURI().toString());
        parse(sourceFile, targetFile);

//            File scriptWorkSpace = new File(ws, _scriptName);

//            FileSet fs = Util.createFileSet(scriptWorkSpace, LR_SCRIPT_RESULT_FILENAME);
//            DirectoryScanner ds = fs.getDirectoryScanner();
//
//            String[] files = ds.getIncludedFiles();
//            if (files.length > 0) {
//                String[] includedFiles = ds.getIncludedFiles();
//                File baseDir = ds.getBasedir();
//                for (String value : includedFiles) {
//                    File reportFile = new File(baseDir, value);
//
//                }
//
//            } else {
//                throw new AbortException("No test report files were found");
//            }

        return null;
    }

    private void log(Object msg) {
        _logger.error(msg.toString());
    }

    public void parse(File scriptName, File outputFile) throws FileNotFoundException {
        parse(new FilePath(scriptName.getAbsoluteFile()), new FilePath(outputFile.getAbsoluteFile()));
    }
    public void parse(FilePath scriptName, FilePath outputFile) {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        Document doc;
        Document newDoc;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(scriptName.read());

            newDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            doc.getDocumentElement().normalize();
            Node mainReportNode = doc.getElementsByTagName("Report").item(0);
            Element testSuiteElement = (Element) mainReportNode;
            NodeList actionNodes = doc.getElementsByTagName("Action");

            testSuits = newDoc.createElement("testsuites");
            parseScriptAction(newDoc, actionNodes);
            Element reportSummaryNode = (Element) doc.getElementsByTagName("Summary").item(actionNodes.getLength());
            testSuits.setAttribute("passed", reportSummaryNode.getAttribute("passed"));
            testSuits.setAttribute("failures", reportSummaryNode.getAttribute("failed"));
            int tests =
                    Integer.parseInt(reportSummaryNode.getAttribute("failed")) + Integer.parseInt(reportSummaryNode
                            .getAttribute("passed"));
            testSuits.setAttribute("tests", String.valueOf(tests));
//            String startTime = reportSummaryNode.getAttribute("sTime");
//            String startimeArr[] = startTime.split(" - ");
//            String endTime = reportSummaryNode.getAttribute("eTime");
//            String endimeArr[] = endTime.split(" - ");
//
//            SimpleDateFormat inputDateFormatter = new SimpleDateFormat();
//            TimeZone timeZone = TimeZone.getTimeZone(scriptTimeZone);
//            //TODO: valdiate timezone ain't empty
//            inputDateFormatter.setTimeZone(timeZone);
//            Locale curLocale = java.util.Locale.forLanguageTag(userCountry);
//            DateFormat df = inputDateFormatter.getDateTimeInstance(SimpleDateFormat.SHORT,
//                    SimpleDateFormat.SHORT);
//            endTime = endTime.replace(" - ","T");
            newDoc.appendChild(testSuits);
            newDoc.setXmlVersion("1.0");

            TransformerFactory tFactory =
                    TransformerFactory.newInstance();
            Transformer transformer =
                    tFactory.newTransformer();

            DOMSource source = new DOMSource(newDoc);
            StreamResult result = new StreamResult(outputFile.write());

            transformer.transform(source, result);
        } catch (SAXException e) {
            log(e);

        } catch (ParserConfigurationException e) {
            log(e);
        } catch (IOException e) {
            log(e);
        }catch (TransformerConfigurationException tce) {
            log("* Transformer Factory error");
            log(" " + tce.getMessage());

            Throwable x = tce;
            if (tce.getException() != null)
                x = tce.getException();
            log(x);
        }
        catch (TransformerException te) {
            log("* Transformation error");
            log(" " + te.getMessage());

            Throwable x = te;
            if (te.getException() != null)
                x = te.getException();
            log(x);
        } catch (InterruptedException e) {
            log(e);
        }

    }

    private void parseScriptAction(Document newDoc, NodeList actionNodes) {
        for (int i = 0; i < actionNodes.getLength(); i++) {
            Element action = (Element) actionNodes.item(i);

            NodeList actionProps = action.getElementsByTagName("AName");
            Element actionName = (Element) actionProps.item(0);
//            log("Action name: " + getCharacterDataFromElement(actionName));


            Element testSuite = newDoc.createElement("testsuite");
            testSuite.setAttribute("name", getCharacterDataFromElement(actionName));

            NodeList stepNodes = action.getElementsByTagName("Step");
            parseScriptActionStep(newDoc, testSuite, stepNodes);

            Element suiteSummaryNode = (Element) action.getElementsByTagName("Summary").item(0);
            testSuite.setAttribute("passed", suiteSummaryNode.getAttribute("passed"));
            testSuite.setAttribute("failures", suiteSummaryNode.getAttribute("failed"));
            int tests =
                    Integer.parseInt(suiteSummaryNode.getAttribute("failed")) + Integer.parseInt(suiteSummaryNode.getAttribute("passed"));
            testSuite.setAttribute("tests", String.valueOf(tests));
            if(tests > 0){
                testSuits.appendChild(testSuite);
            }
        }
    }

    private void parseScriptActionStep(Document newDoc, Element testSuite, NodeList stepNodes) {
        for (int j = 0; j < stepNodes.getLength(); j++) {
            Element step = (Element) stepNodes.item(j);

            Element objNode = (Element) step.getElementsByTagName("Obj").item(0);
//                    log("Step name: " + getStepDataFromElement(objNode));
            Element testCase = newDoc.createElement("testcase");
            testCase.setAttribute("name", getStepDataFromElement(objNode));
//            Element tickNode = (Element) step.getElementsByTagName("TimeTick").item(0);
//                    log("time: " + tickNode.getTextContent());
//            testCase.setAttribute("time", tickNode.getTextContent());
            Element nodeArgs = (Element) step.getElementsByTagName("NodeArgs").item(0);
//            log("status: " + nodeArgs.getAttribute("status"));
            String stepStatus = nodeArgs.getAttribute("status");
            if(stepStatus.equals("Passed")){
                stepStatus = "pass";
            } else {//if(stepStatus.equals("Failed")){
                stepStatus = "fail";
            }
            testCase.setAttribute("status", stepStatus);

            testSuite.appendChild(testCase);
        }
    }

    private static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }

    private static String getStepDataFromElement(Element e) {
        String stepName = getCharacterDataFromElement(e);
        stepName = stepName.replace("Url: ", "");

        return stepName;
    }


    public void set_scriptName(String _scriptName) {
        this._scriptName = _scriptName;
    }
}