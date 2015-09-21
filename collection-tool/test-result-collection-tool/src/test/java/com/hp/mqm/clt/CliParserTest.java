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
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CliParserTest {

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
            parser.parse(options, new String[]{"abc", "-i", "-b"});
            Assert.fail();
        } catch (MissingArgumentException e) {
            Assert.assertEquals("Missing argument for option: b", e.getMessage());
        }
    }

    @Test
    public void testArgs_invalidOption() throws ParseException {
        CommandLineParser parser = new DefaultParser();
        try {
            parser.parse(options, new String[]{"abc", "-i", "-xyz"});
            Assert.fail();
        } catch (UnrecognizedOptionException e) {
            Assert.assertEquals("Unrecognized option: -xyz", e.getMessage());
        }
    }

    @Test
    public void testArgs_invalidIntegerValue() throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = parser.parse(options, new String[]{"abc", "-i", "-r", "invalidIntegerValue"});
        Assert.assertTrue(commandLine.hasOption("r"));
        Assert.assertNull(commandLine.getParsedOptionValue("r"));
    }

    @Test
         public void testArgs_duplicates() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ParseException {
        CliParser cliParser = new CliParser();
        Method argsValidation = cliParser.getClass().getDeclaredMethod("areCmdArgsValid", CommandLine.class);
        argsValidation.setAccessible(true);
        CommandLineParser parser = new DefaultParser();

        CommandLine cmdArgs = parser.parse(options, new String[]{"abc",  "--password-file", "path", "test.xml"});
        Boolean result = (Boolean) argsValidation.invoke(cliParser, cmdArgs);
        Assert.assertNotNull(result);
        Assert.assertTrue(result);

        cmdArgs = parser.parse(options, new String[]{"abc", "--password-file", "path", "--password-file", "invalidArg", "test.xml"});
        result = (Boolean) argsValidation.invoke(cliParser, cmdArgs);
        Assert.assertNotNull(result);
        Assert.assertFalse(result);
    }

    @Test
    public void testArgs_missingMandatoryFileArg() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ParseException {
        CliParser cliParser = new CliParser();
        Method argsValidation = cliParser.getClass().getDeclaredMethod("areCmdArgsValid", CommandLine.class);
        argsValidation.setAccessible(true);
        CommandLineParser parser = new DefaultParser();

        CommandLine cmdArgs = parser.parse(options, new String[]{"abc", "test.xml"});
        Boolean result = (Boolean) argsValidation.invoke(cliParser, cmdArgs);
        Assert.assertNotNull(result);
        Assert.assertTrue(result);

        cmdArgs = parser.parse(options, new String[]{"abc"});
        result = (Boolean) argsValidation.invoke(cliParser, cmdArgs);
        Assert.assertNotNull(result);
        Assert.assertFalse(result);
    }

    @Test
    public void testArgs_tagFormat() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ParseException {
        CliParser cliParser = new CliParser();
        Method argsValidation = cliParser.getClass().getDeclaredMethod("areCmdArgsValid", CommandLine.class);
        argsValidation.setAccessible(true);
        CommandLineParser parser = new DefaultParser();

        CommandLine cmdArgs = parser.parse(options, new String[]{"abc", "-t", "OS:Linux", "test.xml"});
        Boolean result = (Boolean) argsValidation.invoke(cliParser, cmdArgs);
        Assert.assertNotNull(result);
        Assert.assertTrue(result);

        cmdArgs = parser.parse(options, new String[]{"abc", "-t", "OS:", "test.xml"});
        result = (Boolean) argsValidation.invoke(cliParser, cmdArgs);
        Assert.assertNotNull(result);
        Assert.assertFalse(result);

        cmdArgs = parser.parse(options, new String[]{"abc", "-f", "OS::Linux", "test.xml"});
        result = (Boolean) argsValidation.invoke(cliParser, cmdArgs);
        Assert.assertNotNull(result);
        Assert.assertFalse(result);

        cmdArgs = parser.parse(options, new String[]{"abc", "-f", ":", "test.xml"});
        result = (Boolean) argsValidation.invoke(cliParser, cmdArgs);
        Assert.assertNotNull(result);
        Assert.assertFalse(result);
    }

    @Test
    public void testArgs_settingsValidation() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ParseException {
        CliParser cliParser = new CliParser();
        Method settingsValidation = cliParser.getClass().getDeclaredMethod("areSettingsValid", Settings.class);
        settingsValidation.setAccessible(true);
        Settings settings = new Settings();

        Boolean result = (Boolean) settingsValidation.invoke(cliParser, settings);
        Assert.assertNotNull(result);
        Assert.assertFalse(result);

        settings.setServer("http://test.hp.com:8080");
        result = (Boolean) settingsValidation.invoke(cliParser, settings);
        Assert.assertNotNull(result);
        Assert.assertFalse(result);

        settings.setSharedspace(1001);
        result = (Boolean) settingsValidation.invoke(cliParser, settings);
        Assert.assertNotNull(result);
        Assert.assertFalse(result);

        settings.setWorkspace(1002);
        result = (Boolean) settingsValidation.invoke(cliParser, settings);
        Assert.assertNotNull(result);
        Assert.assertTrue(result);
    }
}
