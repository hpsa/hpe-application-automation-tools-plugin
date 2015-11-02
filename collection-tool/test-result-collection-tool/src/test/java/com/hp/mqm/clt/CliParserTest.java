package com.hp.mqm.clt;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.List;

public class CliParserTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    Options options;

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException {
        CliParser cliParser = new CliParser();
        Field optionsFiled = cliParser.getClass().getDeclaredField("options");
        optionsFiled.setAccessible(true);
        options = (Options) optionsFiled.get(cliParser);
    }

    @Test
    public void testHelp() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        CliParser parser = new CliParser();
        Method help = parser.getClass().getDeclaredMethod("printHelp");
        help.setAccessible(true);
        help.invoke(parser);
    }

    @Test
    public void testVersion() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        CliParser parser = new CliParser();
        Method help = parser.getClass().getDeclaredMethod("printVersion");
        help.setAccessible(true);
        help.invoke(parser);
    }

    @Test
    public void testArgs_missingArgument() throws ParseException {
        CommandLineParser parser = new DefaultParser();
        try {
            parser.parse(options, new String[]{"-i", "-d"});
            Assert.fail();
        } catch (MissingArgumentException e) {
            Assert.assertEquals("Missing argument for option: d", e.getMessage());
        }
    }

    @Test
    public void testArgs_invalidOption() throws ParseException {
        CommandLineParser parser = new DefaultParser();
        try {
            parser.parse(options, new String[]{"-i", "-xyz"});
            Assert.fail();
        } catch (UnrecognizedOptionException e) {
            Assert.assertEquals("Unrecognized option: -xyz", e.getMessage());
        }
    }

    @Test
    public void testArgs_invalidIntegerValue() throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = parser.parse(options, new String[]{"-i", "-r", "invalidIntegerValue"});
        Assert.assertTrue(commandLine.hasOption("r"));
        try {
            commandLine.getParsedOptionValue("r");
            Assert.fail();
        } catch (ParseException e){
            Assert.assertEquals("For input string: \"invalidIntegerValue\"", e.getMessage());
        }
    }

    @Test
    public void testArgs_invalidInternalCombination() throws NoSuchMethodException, ParseException, InvocationTargetException, IllegalAccessException {
        CliParser cliParser = new CliParser();
        Method argsValidation = cliParser.getClass().getDeclaredMethod("areCmdArgsValid", CommandLine.class);
        argsValidation.setAccessible(true);
        CommandLineParser parser = new DefaultParser();

        CommandLine cmdArgs = parser.parse(options, new String[]{ "-i", "-w", "1002", "publicApi.xml"});
        Boolean result = (Boolean) argsValidation.invoke(cliParser, cmdArgs);
        Assert.assertTrue(result);

        cmdArgs = parser.parse(options, new String[]{"-i", "-b", "1002", "publicApi.xml"});
        result = (Boolean) argsValidation.invoke(cliParser, cmdArgs);
        Assert.assertFalse(result);
    }

    @Test
         public void testArgs_duplicates() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ParseException, IOException {
        CliParser cliParser = new CliParser();
        Method argsValidation = cliParser.getClass().getDeclaredMethod("areCmdArgsValid", CommandLine.class);
        argsValidation.setAccessible(true);
        CommandLineParser parser = new DefaultParser();

        CommandLine cmdArgs = parser.parse(options, new String[]{ "-r", "1", "test.xml"});
        Boolean result = (Boolean) argsValidation.invoke(cliParser, cmdArgs);
        Assert.assertTrue(result);

        cmdArgs = parser.parse(options, new String[]{"-r", "1", "-r", "2", "test.xml"});
        result = (Boolean) argsValidation.invoke(cliParser, cmdArgs);
        Assert.assertFalse(result);

        cmdArgs = parser.parse(options, new String[]{});
        result = (Boolean) argsValidation.invoke(cliParser, cmdArgs);
        Assert.assertFalse(result);

        File outputFolder = temporaryFolder.newFolder();
        cmdArgs = parser.parse(options, new String[]{"--output-file",
                outputFolder.getPath() + File.separator + "testResults.xml", "JUnit1.xml", "JUnit2.xml"});
        result = (Boolean) argsValidation.invoke(cliParser, cmdArgs);
        Assert.assertFalse(result);

        cmdArgs = parser.parse(options, new String[]{"--output-file",
                outputFolder.getPath() + File.separator + "testResults.xml", "JUnit.xml"});
        result = (Boolean) argsValidation.invoke(cliParser, cmdArgs);
        Assert.assertTrue(result);
    }

    @Test
    public void testArgs_inputFiles() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ParseException, URISyntaxException, IOException {
        CliParser cliParser = new CliParser();
        Method inputFilesValidation = cliParser.getClass().getDeclaredMethod("addInputFilesToSettings", CommandLine.class, Settings.class);
        inputFilesValidation.setAccessible(true);
        CommandLineParser parser = new DefaultParser();

        CommandLine cmdArgs = parser.parse(options, new String[]{"test.xml"});
        Settings settings = new Settings();
        Boolean result = (Boolean) inputFilesValidation.invoke(cliParser, cmdArgs, settings);
        Assert.assertFalse(result);
        Assert.assertNull(settings.getInputXmlFileNames());

        cmdArgs = parser.parse(options, new String[]{getClass().getResource("JUnit-minimalAccepted.xml").toURI().getPath(),
                getClass().getResource("JUnit-missingTestName.xml").toURI().getPath()});
        result = (Boolean) inputFilesValidation.invoke(cliParser, cmdArgs, settings);
        Assert.assertTrue(result);
        List<String> fileNames = settings.getInputXmlFileNames();
        Assert.assertNotNull(fileNames);
        Assert.assertEquals(2, fileNames.size());
        Assert.assertTrue(fileNames.get(0).contains("JUnit-minimalAccepted.xml"));
        Assert.assertTrue(fileNames.get(1).contains("JUnit-missingTestName.xml"));
    }

    @Test
    public void testArgs_tagFormat() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ParseException {
        CliParser cliParser = new CliParser();
        Method argsValidation = cliParser.getClass().getDeclaredMethod("areCmdArgsValid", CommandLine.class);
        argsValidation.setAccessible(true);
        CommandLineParser parser = new DefaultParser();

        CommandLine cmdArgs = parser.parse(options, new String[]{"-t", "OS:Linux", "test.xml"});
        Boolean result = (Boolean) argsValidation.invoke(cliParser, cmdArgs);
        Assert.assertTrue(result);

        cmdArgs = parser.parse(options, new String[]{"-t", "OS:", "test.xml"});
        result = (Boolean) argsValidation.invoke(cliParser, cmdArgs);
        Assert.assertFalse(result);

        cmdArgs = parser.parse(options, new String[]{"-f", "OS::Linux", "test.xml"});
        result = (Boolean) argsValidation.invoke(cliParser, cmdArgs);
        Assert.assertFalse(result);

        cmdArgs = parser.parse(options, new String[]{"-f", ":", "test.xml"});
        result = (Boolean) argsValidation.invoke(cliParser, cmdArgs);
        Assert.assertFalse(result);
    }


    @Test
    public void testArgs_passwordFile() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ParseException, URISyntaxException {
        CliParser cliParser = new CliParser();
        Method argsValidation = cliParser.getClass().getDeclaredMethod("areCmdArgsValid", CommandLine.class);
        argsValidation.setAccessible(true);
        CommandLineParser parser = new DefaultParser();

        CommandLine cmdArgs = parser.parse(options, new String[]{"--password-file",
                getClass().getResource("testPasswordFile").toURI().getPath(),
                getClass().getResource("JUnit-minimalAccepted.xml").toURI().getPath()});
        Boolean result = (Boolean) argsValidation.invoke(cliParser, cmdArgs);
        Assert.assertTrue(result);

        cmdArgs = parser.parse(options, new String[]{"--password-file",
                "invalidPasswordFile",
                getClass().getResource("JUnit-minimalAccepted.xml").toURI().getPath()});
        result = (Boolean) argsValidation.invoke(cliParser, cmdArgs);
        Assert.assertFalse(result);
    }

    @Test
    public void testArgs_settingsValidation() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ParseException {
        CliParser cliParser = new CliParser();
        Method settingsValidation = cliParser.getClass().getDeclaredMethod("areSettingsValid", Settings.class);
        settingsValidation.setAccessible(true);
        Settings settings = new Settings();

        Boolean result = (Boolean) settingsValidation.invoke(cliParser, settings);
        Assert.assertFalse(result);

        settings.setServer("http://test.hp.com:8080");
        result = (Boolean) settingsValidation.invoke(cliParser, settings);
        Assert.assertFalse(result);

        settings.setSharedspace(1001);
        result = (Boolean) settingsValidation.invoke(cliParser, settings);
        Assert.assertFalse(result);

        settings.setWorkspace(1002);
        result = (Boolean) settingsValidation.invoke(cliParser, settings);
        Assert.assertTrue(result);
    }

    @Test
    public void testParser_outputFile() throws IOException, URISyntaxException {
        CliParser cliParser = new CliParser();
        File outputFile = new File(temporaryFolder.newFolder(), "testResults.xml");
        Settings settings = cliParser.parse(new String[]{"--started", "123456", "-a", "2",
                "--output-file", outputFile.getPath(),
                "-c", getClass().getResource("test.properties").toURI().getPath(),
                getClass().getResource("JUnit-minimalAccepted.xml").toURI().getPath()});
        Assert.assertEquals(Long.valueOf(123456), settings.getStarted());
        Assert.assertTrue(outputFile.canWrite());
        Assert.assertEquals("http://localhost:8080/qcbin", settings.getServer());
        Assert.assertEquals(Integer.valueOf(1001), settings.getSharedspace());
        Assert.assertEquals(Integer.valueOf(1002), settings.getWorkspace());
        Assert.assertEquals("admin", settings.getUser());
    }
}
