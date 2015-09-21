package com.hp.mqm.clt;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Pattern;

public class CliParser {

    private static final String CMD_LINE_SYNTAX = "push-result [OPTIONS]... FILE [FILE]...";
    private static final String HEADER = "HP Lifecycle Management Test Result Collection Tool";
    private static final String FOOTER = "";
    private static final String VERSION = "1.0";

    private Options options = new Options();
    private LinkedList<String> argsWithSingleOccurrence = new LinkedList<String>();

    public CliParser() {
        options.addOption("h", "help", false, "show this help");
        options.addOption("v", "version", false, "show version");

        options.addOption("i", "internal", false, "internal test result public API xml format");
        options.addOption(Option.builder("o").longOpt("output-file").desc("output to file").hasArg().argName("FILE").build());
        options.addOption(Option.builder("c").longOpt("config-file").desc("configuration file").hasArg().argName("FILE").build());

        options.addOption(Option.builder("e").longOpt("server").desc("server").hasArg().argName("URL").build());
        options.addOption(Option.builder("d").longOpt("shared-space").desc("shared space").hasArg().argName("ID").type(Integer.class).build());
        options.addOption(Option.builder("w").longOpt("workspace").desc("workspace").hasArg().argName("ID").type(Integer.class).build());

        options.addOption(Option.builder("u").longOpt("user").desc("username").hasArg().argName("USERNAME").build());
        OptionGroup passGroup = new OptionGroup();
        passGroup.addOption(Option.builder("p").longOpt("password").desc("password").hasArg().argName("PASSWORD").optionalArg(true).build());
        passGroup.addOption(Option.builder().longOpt("password-file").desc("file with password").hasArg().argName("FILE").build());
        options.addOptionGroup(passGroup);

        options.addOption(Option.builder("t").longOpt("tag").desc("tag").hasArg().argName("VALUE").build());
        options.addOption(Option.builder("f").longOpt("field").desc("field tag").hasArg().argName("VALUE").build());

        options.addOption(Option.builder("r").longOpt("release").desc("release").hasArg().argName("ID").type(Integer.class).build());
        options.addOption(Option.builder("a").longOpt("product-area").desc("product area").hasArg().argName("ID").type(Integer.class).build());
        options.addOption(Option.builder("q").longOpt("requirement").desc("requirement").hasArg().argName("ID").type(Integer.class).build());

        options.addOption(Option.builder("b").longOpt("build-server").desc("build server identity").hasArg().argName("UUID").build());
        options.addOption(Option.builder("j").longOpt("build-job").desc("build job sid").hasArg().argName("SID").build());
        options.addOption(Option.builder("n").longOpt("build-number").desc("build number").hasArg().argName("NUMBER").build());
        options.addOption(Option.builder("s").longOpt("build-status").desc("build status").hasArg().argName("STATUS").build());

        argsWithSingleOccurrence.addAll(Arrays.asList("c", "e", "d", "o", "w", "p", "password-file", "r", "a", "q", "b", "j", "n", "s"));
    }

    public void parse(String[] args) {
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
                // error message was printed inside the method (has arg context)
                System.exit(ReturnCode.FAILURE.getReturnCode());
            }

            // load config
            String filename = null;
            if (cmd.hasOption("c")) {
                filename = cmd.getOptionValue("c");
            }
            Settings settings = new Settings();
            try {
                settings.load(filename);
            } catch (IOException e) {
                System.out.println("Can not read from properties file " + filename);
                System.exit(ReturnCode.FAILURE.getReturnCode());
            } catch (URISyntaxException e) {
                System.out.println("Can not convert file to URI " + filename);
                System.exit(ReturnCode.FAILURE.getReturnCode());
            }
            if (cmd.hasOption("i")) {
                settings.setInternal(true);
            }

            if (cmd.hasOption("e")) {
                settings.setServer(cmd.getOptionValue("e"));
            }

            if (cmd.hasOption("d")) {
                settings.setSharedspace((Integer) cmd.getParsedOptionValue("d"));
            }

            if (cmd.hasOption("w")) {
                settings.setWorkspace((Integer) cmd.getParsedOptionValue("w"));
            }

            if (cmd.hasOption("p")) {
                if (cmd.getOptionValue("p") != null) {
                    settings.setPassword(cmd.getOptionValue("p"));
                } else {
                    settings.setPassword(new String(System.console().readPassword()));
                }
            }

            if (cmd.hasOption("password-file")) {
                settings.setPasswordFile(cmd.getOptionValue("password-file"));
            }

            if (cmd.hasOption("t")) {
                settings.setTags(Arrays.asList(cmd.getOptionValues("t")));
            }

            if (cmd.hasOption("f")) {
                settings.setFields(Arrays.asList(cmd.getOptionValues("f")));
            }

            if (cmd.hasOption("r")) {
                settings.setRelease((Integer) cmd.getParsedOptionValue("r"));
            }

            if (cmd.hasOption("a")) {
                settings.setProductArea((Integer) cmd.getParsedOptionValue("a"));
            }

            if (cmd.hasOption("q")) {
                settings.setRequirement((Integer) cmd.getParsedOptionValue("q"));
            }

            if (cmd.hasOption("b")) {
                settings.setBuildServer(cmd.getOptionValue("b"));
            }

            if (cmd.hasOption("j")) {
                settings.setBuildJob(cmd.getOptionValue("j"));
            }

            if (cmd.hasOption("n")) {
                settings.setBuildNumber(cmd.getOptionValue("n"));
            }

            if (cmd.hasOption("s")) {
                settings.setBuildStatus(cmd.getOptionValue("s"));
            }

            settings.setFileNames(cmd.getArgList());

            if(!areSettingsValid(settings)) {
                // error message was printed inside the method (has settings context)
                System.exit(ReturnCode.FAILURE.getReturnCode());
            }

        } catch (ParseException e) {
            printHelp();
            System.exit(ReturnCode.FAILURE.getReturnCode());
        }
    }

    private boolean areCmdArgsValid(CommandLine cmd) {
        if (cmd.getArgList().size() < 2) {
            System.out.println("At least one XML file must be specified");
            return false;
        }

        for(String arg : argsWithSingleOccurrence) {
            if (cmd.getOptionProperties(arg).size() > 1) {
                System.out.println("Invalid multiple occurrence of argument: " + arg);
                return false;
            }
        }

        if (!isTagFormatValid(cmd, "t") || !isTagFormatValid(cmd, "f")) {
            return false;
        }

        return true;
    }

    private boolean isTagFormatValid(CommandLine cmd, String option) {
        String[] tags = cmd.getOptionValues(option);
        if (tags == null) {
            return true;
        }
        Pattern pattern = Pattern.compile("^\\w+:\\w+$");
        for(String tag : tags) {
            if (!pattern.matcher(tag).matches()) {
                System.out.println("Tag and field tag arguments must be in TYPE:VALUE format: " + tag);
                return false;
            }
        }
        return true;
    }

    private boolean areSettingsValid(Settings settings) {
        if (settings.getServer() == null) {
            System.out.println("Mandatory setting 'server' was not specified in the CLI arguments or configuration file");
            return false;
        }

        if (settings.getSharedspace() == null) {
            System.out.println("Mandatory setting 'sharedspace' was not specified in the CLI arguments or configuration file");
            return false;
        }

        if (settings.getWorkspace() == null) {
            System.out.println("Mandatory setting 'workspace' was not specified in the CLI arguments or configuration file");
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
