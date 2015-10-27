package com.hp.mqm.clt;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class CliParser {

    private static final String CMD_LINE_SYNTAX = "java -jar test-result-collection-tool.jar [OPTIONS]... FILE [FILE]...\n";
    private static final String HEADER = "HP Lifecycle Management Test Result Collection Tool";
    private static final String FOOTER = "";
    private static final String VERSION = "1.0";

    private Options options = new Options();
    private LinkedList<String> argsWithSingleOccurrence = new LinkedList<String>();
    private LinkedList<String> argsRestrictedForInternal = new LinkedList<String>();

    public CliParser() {
        options.addOption("h", "help", false, "show this help");
        options.addOption("v", "version", false, "show version");

        options.addOption("i", "internal", false, "internal test result public API xml format");
        options.addOption("e", "skip-errors", false, "skip errors on the server side");
        options.addOption(Option.builder("o").longOpt("output-file").desc("output to file").hasArg().argName("FILE").build());
        options.addOption(Option.builder("c").longOpt("config-file").desc("configuration file").hasArg().argName("FILE").build());

        options.addOption(Option.builder("s").longOpt("server").desc("server").hasArg().argName("URL").build());
        options.addOption(Option.builder("d").longOpt("shared-space").desc("shared space").hasArg().argName("ID").type(Number.class).build());
        options.addOption(Option.builder("w").longOpt("workspace").desc("workspace").hasArg().argName("ID").type(Number.class).build());

        options.addOption(Option.builder("u").longOpt("user").desc("username").hasArg().argName("USERNAME").build());
        OptionGroup passGroup = new OptionGroup();
        passGroup.addOption(Option.builder("p").longOpt("password").desc("password").hasArg().argName("PASSWORD").optionalArg(true).build());
        passGroup.addOption(Option.builder().longOpt("password-file").desc("file with password").hasArg().argName("FILE").build());
        options.addOptionGroup(passGroup);

        options.addOption(Option.builder().longOpt("proxy-host").desc("proxy host").hasArg().argName("HOSTNAME").build());
        options.addOption(Option.builder().longOpt("proxy-port").desc("proxy port").hasArg().argName("PORT").type(Number.class).build());
        options.addOption(Option.builder().longOpt("proxy-user").desc("proxy username").hasArg().argName("USERNAME").build());

        OptionGroup proxyPassGroup = new OptionGroup();
        proxyPassGroup.addOption(Option.builder().longOpt("proxy-password").desc("proxy password").hasArg().argName("PASSWORD").optionalArg(true).build());
        proxyPassGroup.addOption(Option.builder().longOpt("proxy-password-file").desc("file with proxy password").hasArg().argName("FILE").build());
        options.addOptionGroup(proxyPassGroup);

        options.addOption(Option.builder().longOpt("check-status").desc("check test result status after push").build());
        options.addOption(Option.builder().longOpt("check-status-timeout").desc("timeout for test result push status retrieval").hasArg().argName("SEC").build());

        options.addOption(Option.builder("t").longOpt("tag").desc("tag").hasArg().argName("TYPE:VALUE").build());
        options.addOption(Option.builder("f").longOpt("field").desc("field tag").hasArg().argName("TYPE:VALUE").build());

        options.addOption(Option.builder("r").longOpt("release").desc("release").hasArg().argName("ID").type(Number.class).build());
        options.addOption(Option.builder("a").longOpt("product-area").desc("product area").hasArg().argName("ID").type(Number.class).build());
        options.addOption(Option.builder("b").longOpt("backlog-item").desc("backlog-item").hasArg().argName("ID").type(Number.class).build());
        options.addOption(Option.builder().longOpt("started").desc("started time in millis").hasArg().argName("TIMESTAMP").type(Number.class).build());

        argsWithSingleOccurrence.addAll(Arrays.asList("o", "c", "s", "d", "w", "u", "p", "password-file", "r", "started", "check-status",
                "check-status-timeout", "proxy-host", "proxy-port", "proxy-user", "proxy-password", "proxy-password-file"));
        argsRestrictedForInternal.addAll(Arrays.asList("o", "t", "f", "r", "a", "b", "started"));
    }

    public Settings parse(String[] args) {
        Settings settings = new Settings();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                printHelp();
                System.exit(ReturnCode.SUCCESS.getReturnCode());
            }

            if (cmd.hasOption("v")) {
                printVersion();
                System.exit(ReturnCode.SUCCESS.getReturnCode());
            }

            if (!areCmdArgsValid(cmd)) {
                printHelp();
                System.exit(ReturnCode.FAILURE.getReturnCode());
            }

            if (!addInputFilesToSettings(cmd, settings)) {
                printHelp();
                System.exit(ReturnCode.FAILURE.getReturnCode());
            }

            // load config
            String filename = null;
            if (cmd.hasOption("c")) {
                filename = cmd.getOptionValue("c");
            }
            try {
                settings.load(filename);
                // CODE REVIEW, Johnny, 19Oct2015 - you should inform user that loading of settings failed but the
                // call of settings.load() method just returns in some cases; for example it can happen that user deletes
                // accidentally his default configuration file, now there's no user he can know that that is the cause
                // of problem
            } catch (IOException e) {
                System.out.println("Can not read from properties file " + filename);
                System.exit(ReturnCode.FAILURE.getReturnCode());
            } catch (URISyntaxException e) {
                System.out.println("Can not convert file to URI " + filename);
                System.exit(ReturnCode.FAILURE.getReturnCode());
            } catch (NumberFormatException e) {
                System.out.println("Can not convert string from properties file to integer: " + e.getMessage());
                System.exit(ReturnCode.FAILURE.getReturnCode());
            }
            if (cmd.hasOption("i")) {
                settings.setInternal(true);
            }

            if (cmd.hasOption("e")) {
                settings.setSkipErrors(true);
            }

            if (cmd.hasOption("o")) {
                settings.setOutputFile(cmd.getOptionValue("o"));
            }

            if (cmd.hasOption("s")) {
                settings.setServer(cmd.getOptionValue("s"));
            }

            if (cmd.hasOption("d")) {
                settings.setSharedspace(((Long) cmd.getParsedOptionValue("d")).intValue());
            }

            if (cmd.hasOption("w")) {
                settings.setWorkspace(((Long) cmd.getParsedOptionValue("w")).intValue());
            }

            if (cmd.hasOption("u")) {
                settings.setUser(cmd.getOptionValue("u"));
            }

            if (settings.getOutputFile() == null) {
                if (cmd.hasOption("p")) {
                    settings.setPassword(cmd.getOptionValue("p"));
                } else if (cmd.hasOption("password-file")) {
                    // CODE REVIEW, Johnny, 19Oct2015 - please handle the exceptions locally not at the bottom, the path to
                    // password file might be missing or invalid or file not readable, etc.
                    settings.setPassword(FileUtils.readFileToString(new File(cmd.getOptionValue("password-file"))));
                } else {
                    System.out.println("Please enter your password if it's required and hit enter: ");
                    settings.setPassword(new String(System.console().readPassword()));
                }
            }

            if (cmd.hasOption("proxy-host")) {
                settings.setProxyHost(cmd.getOptionValue("proxy-host"));
            }

            if (cmd.hasOption("proxy-port")) {
                settings.setProxyPort(((Long) cmd.getParsedOptionValue("proxy-port")).intValue());
            }

            if (cmd.hasOption("proxy-user")) {
                settings.setProxyUser(cmd.getOptionValue("proxy-user"));
            }

            if (settings.getOutputFile() == null && StringUtils.isNotEmpty(settings.getProxyUser())) {
                if (cmd.hasOption("proxy-password")) {
                    settings.setProxyPassword(cmd.getOptionValue("proxy-password"));
                } else if (cmd.hasOption("proxy-password-file")) {
                    settings.setProxyPassword(FileUtils.readFileToString(new File(cmd.getOptionValue("proxy-password-file"))));
                } else {
                    System.out.println("Please enter your proxy password if it's required and hit enter: ");
                    settings.setProxyPassword(new String(System.console().readPassword()));
                }
            }

            if (cmd.hasOption("t")) {
                settings.setTags(Arrays.asList(cmd.getOptionValues("t")));
            }

            if (cmd.hasOption("f")) {
                settings.setFields(Arrays.asList(cmd.getOptionValues("f")));
            }

            if (cmd.hasOption("r")) {
                settings.setRelease(((Long) cmd.getParsedOptionValue("r")).intValue());
            }

            if (cmd.hasOption("started")) {
                settings.setStarted((Long) cmd.getParsedOptionValue("started"));
            }

            if (cmd.hasOption("a")) {
                settings.setProductAreas(cmd.getOptionValues("a"));
            }

            if (cmd.hasOption("b")) {
                settings.setBacklogItems(cmd.getOptionValues("b"));
            }

            if (!areSettingsValid(settings)) {
                System.exit(ReturnCode.FAILURE.getReturnCode());
            }

        } catch (ParseException e) {
            printHelp();
            System.exit(ReturnCode.FAILURE.getReturnCode());
        } catch (IOException e) {
            // CODE REVIEW, Johnny, 19Oct2015 - handle locally not here
            System.out.println("Can not read the password file");
            System.exit(ReturnCode.FAILURE.getReturnCode());
        }
        return settings;
    }

    private boolean addInputFilesToSettings(CommandLine cmd, Settings settings) {
        List<String> argList = cmd.getArgList();
        List<String> inputFiles = new LinkedList<String>();
        for (String inputFile : argList) {
            if (!inputFiles.contains(inputFile) && new File(inputFile).canRead()) {
                // CODE REVIEW, Johnny, 19Oct2015 - report which files we are unable to read; consider failing in that case
                // user might be led into thinking that all files were handed over for processing
                inputFiles.add(inputFile);
            }
        }

        if (inputFiles.isEmpty()) {
            // CODE REVIEW, Johnny, 16Oct2015 - misleading message; at this point you do not know anything about
            // the file(s )nature - whether it is etc. - so what you can just say is that there are no valid or
            // readable files at input containing tests to push
            System.out.println("No valid XML files with tests to push");
            return false;
        }

        settings.setInputXmlFileNames(inputFiles);
        return true;
    }

    private boolean areCmdArgsValid(CommandLine cmd) {
        List<String> argList = cmd.getArgList();
        if (argList.isEmpty()) {
            System.out.println("At least one XML file must be specified as input for push");
            return false;
        }

        for (String arg : argsWithSingleOccurrence) {
            if (cmd.getOptionProperties(arg).size() > 1) {
                System.out.println("Only single occurrence is allowed for argument: '" + arg + "'");
                return false;
            }
        }

        if (cmd.hasOption("i")) {
            for (String arg : argsRestrictedForInternal) {
                if (cmd.hasOption(arg)) {
                    System.out.println("Invalid argument for internal mode: '" + arg + "'");
                    return false;
                }
            }
        }

        if (!isTagFormatValid(cmd, "t") || !isTagFormatValid(cmd, "f")) {
            return false;
        }

        String configurationFile = cmd.getOptionValue("c");
        if (configurationFile != null && !(new File(configurationFile).canRead())) {
            System.out.println("Can not read the configuration file: " + configurationFile);
            return false;
        }

        String passwordFile = cmd.getOptionValue("password-file");
        if (passwordFile != null && !(new File(passwordFile).canRead())) {
            System.out.println("Can not read the password file: " + passwordFile);
            return false;
        }

        String proxyPasswordFile = cmd.getOptionValue("proxy-password-file");
        if (proxyPasswordFile != null && !new File(proxyPasswordFile).canRead()) {
            System.out.println("Can not read the proxy password file: " + passwordFile);
            return false;
        }

        String outputFilePath = cmd.getOptionValue("o");
        if (outputFilePath != null) {
            if (argList.size() != 1) {
                System.out.println("Only single JUnit input file is allowed for output mode");
                return false;
            }
            File outputFile = new File(outputFilePath);
            if (!outputFile.exists()) {
                try {
                    if (!outputFile.createNewFile()) {
                        System.out.println("Can not create the output file: " + outputFile.getAbsolutePath());
                        return false;
                    }
                } catch (IOException e) {
                    System.out.println("Can not create the output file: " + outputFile.getAbsolutePath());
                    return false;
                }
            }
            if (!outputFile.canWrite()) {
                System.out.println("Can not write to the output file: " + outputFile.getAbsolutePath());
                return false;
            }
        }
        return true;
    }

    private boolean isTagFormatValid(CommandLine cmd, String option) {
        String[] tags = cmd.getOptionValues(option);
        if (tags == null) {
            return true;
        }
        // CODE REVIEW, Johnny, 19Oct2015 - consult with Mirek with regards to localization, this is very probably
        // good for this release, but I can imagine it will have to be relaxed once we for example start to support
        // languages like French (and all their funny characters)
        Pattern pattern = Pattern.compile("^\\w+:\\w+$");
        for (String tag : tags) {
            if (!pattern.matcher(tag).matches()) {
                System.out.println("Tag and field tag arguments must be in TYPE:VALUE format: " + tag);
                return false;
            }
        }
        return true;
    }

    private boolean areSettingsValid(Settings settings) {
        if (settings.getOutputFile() == null) {
            // Server access is required
            if (!isSettingPresent(settings.getServer(), "server")) {
                return false;
            }

            if (!isSettingPresent(settings.getSharedspace(), "sharedspace")) {
                return false;
            }

            if (!isSettingPresent(settings.getWorkspace(), "workspace")) {
                return false;
            }

            if (settings.getProxyHost() != null && settings.getProxyPort() == null) {
                System.out.println("Proxy port was not specified for proxy host: " + settings.getProxyHost());
                return false;
            }

            if (settings.getProxyPassword() != null && settings.getProxyUser() == null) {
                System.out.println("Proxy user name was not specified for proxy password");
                return false;
            }
        }
        return true;
    }

    boolean isSettingPresent(Object setting, String settingName) {
        if (setting == null) {
            System.out.println("Mandatory setting '" + settingName + "' was not specified in the CLI arguments or configuration file");
            return false;
        }
        return true;
    }


    private void printVersion() {
        System.out.println(HEADER);
        System.out.println("Version: " + VERSION);
        System.out.println(FOOTER);
    }

    private void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(CMD_LINE_SYNTAX, HEADER, options, FOOTER);
    }
}
