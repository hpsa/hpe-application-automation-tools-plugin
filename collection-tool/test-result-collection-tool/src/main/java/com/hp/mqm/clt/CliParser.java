package com.hp.mqm.clt;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class CliParser {

    private static final String DEFAULT_CONFIG_FILE = "config.properties";

    private static final String CMD_LINE_SYNTAX = "push-result [OPTIONS]... [FILE]...";
    private static final String HEADER = "HP Lifecycle Management Test Result Collection Tool";
    private static final String FOOTER = "";
    private static final String VERSION = "1.0";

    private Options options = new Options();

    public CliParser() {
        options.addOption("h", "help", false, "show this help");
        options.addOption("v", "version", false, "show version");

        options.addOption("i", "internal", false, "internal test result public API xml format");
        options.addOption(Option.builder("o").longOpt("output-file").desc("output to file").hasArg().argName("FILE").build());
        options.addOption(Option.builder("c").longOpt("config-file").desc("configuration file").hasArg().argName("FILE").build());

        options.addOption(Option.builder("e").longOpt("server").desc("server").hasArg().argName("URL").build());
        options.addOption(Option.builder("d").longOpt("domain").desc("domain").hasArg().argName("DOMAIN").build());
        options.addOption(Option.builder("o").longOpt("project").desc("project").hasArg().argName("PROJECT").build());
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
    }

    public void parse(String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                printHelp();
                System.exit(0);
            }

            if (cmd.hasOption("v")) {
                printVersion();
                System.exit(0);
            }

            // load config
            String filename = DEFAULT_CONFIG_FILE;
            if (cmd.hasOption("c")) {
                filename = cmd.getOptionValue("c");
            }
            Settings settings = new Settings();
            settings.load(filename);

            if (cmd.hasOption("i")) {
                settings.setInternal(true);
            }

            if (cmd.hasOption("e")) {
                settings.setServer(cmd.getOptionValue("e"));
            }

            if (cmd.hasOption("d")) {
                settings.setDomain(cmd.getOptionValue("d"));
            }

            if (cmd.hasOption("o")) {
                settings.setProject(cmd.getOptionValue("o"));
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

        } catch (ParseException e) {
            printHelp();
            System.exit(0);
        } catch (IOException e) {
            // TODO: handle IO exception
        }
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
