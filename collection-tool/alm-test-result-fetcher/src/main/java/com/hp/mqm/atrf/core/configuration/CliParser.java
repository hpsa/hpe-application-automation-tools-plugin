package com.hp.mqm.atrf.core.configuration;

import com.hp.mqm.atrf.core.rest.RestConnector;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;


public class CliParser {

    static final Logger logger = LogManager.getLogger();

    private static final String CMD_LINE_SYNTAX = "java -jar alm-test-result-fetcher.jar [OPTIONS]... \n";
    private static final String HEADER = "HPE ALM2Octane Test Result Fetcher";
    private static final String FOOTER = "";
    private static final String VERSION = "1.0";

    public static final String DEFAULT_CONF_FILE = "conf.xml";
    public static final String DEFAULT_OUTPUT_FILE = "output.xml";

    private Options options = new Options();
    private LinkedList<String> argsWithSingleOccurrence = new LinkedList<>();


    public CliParser() {
        options.addOption(Option.builder("h").longOpt("help").desc("show this help").build());
        options.addOption(Option.builder("v").longOpt("version").desc("show version of this tool").build());

        options.addOption(Option.builder("o").longOpt("output-file").desc("write output to file instead of pushing it to the server").optionalArg(true).argName("FILE").build());
        options.addOption(Option.builder("c").longOpt("config-file").desc("configuration file location").hasArg().argName("FILE").build());

        OptionGroup passAlmGroup = new OptionGroup();
        passAlmGroup.addOption(Option.builder("pa").longOpt("password-alm").desc("password for alm user").hasArg().argName("PASSWORD").build());
        passAlmGroup.addOption(Option.builder("paf").longOpt("password-alm-file").desc("location of file with password for alm user").hasArg().argName("FILE").build());
        options.addOptionGroup(passAlmGroup);

        OptionGroup passOctaneGroup = new OptionGroup();
        passOctaneGroup.addOption(Option.builder("po").longOpt("password-oct").desc("password for octane user").hasArg().argName("PASSWORD").optionalArg(true).build());
        passOctaneGroup.addOption(Option.builder("pof").longOpt("password-oct-file").desc("location of file with password for octane user").hasArg().argName("FILE").build());
        options.addOptionGroup(passOctaneGroup);


        argsWithSingleOccurrence.addAll(Arrays.asList("o", "c", "pa", "paf", "po", "pof"));

    }

    public FetchConfiguration parse(String[] args) {
        FetchConfiguration configuration = null;
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            //help
            if (cmd.hasOption("h")) {
                printHelp();
                System.exit(ReturnCode.SUCCESS.getReturnCode());
            }

            //version
            if (cmd.hasOption("v")) {
                printVersion();
                System.exit(ReturnCode.SUCCESS.getReturnCode());
            }
            validateArguments(cmd);


            // load config
            String configFile = null;
            if (cmd.hasOption("c")) {
                configFile = cmd.getOptionValue("c");
            }
            if (StringUtils.isEmpty(configFile)) {
                configFile = DEFAULT_CONF_FILE;
            }
            try {
                File f = new File(configFile);
                logger.info("Configuration is loading from : " + f.getAbsolutePath());
                configuration = FetchConfiguration.loadPropertiesFromFile(configFile);
            } catch (Exception e) {
                logger.error("Failed to load configuration file : " + e.getMessage());
                System.exit(ReturnCode.FAILURE.getReturnCode());
            }

            //load output file
            if (cmd.hasOption("o")) {
                String outputFilePath = cmd.getOptionValue("o");
                if (StringUtils.isEmpty(outputFilePath)) {
                    outputFilePath = DEFAULT_OUTPUT_FILE;
                }
                configuration.setOutputFile(outputFilePath);
                File outputFile = new File(outputFilePath);
                if (!outputFile.exists()) {
                    boolean canCreate = true;
                    try {
                        if (!outputFile.createNewFile()) {
                            canCreate = false;
                        }
                    } catch (IOException e) {
                        canCreate = false;
                    }
                    if (!canCreate) {
                        logger.error("Can not create the output file: " + outputFile.getAbsolutePath());
                        System.exit(ReturnCode.FAILURE.getReturnCode());
                    }
                }
                if (!outputFile.canWrite()) {
                    logger.error("Can not write to the output file: " + outputFile.getAbsolutePath());
                    System.exit(ReturnCode.FAILURE.getReturnCode());
                }
                logger.info("Output results to file  : " + outputFile.getAbsolutePath());
            }

            //load alm password
            if (cmd.hasOption("pa")) {
                configuration.setAlmPassword(cmd.getOptionValue("pa"));
            } else if (cmd.hasOption("paf")) {
                try {
                    configuration.setAlmPassword(FileUtils.readFileToString(new File(cmd.getOptionValue("paf"))));
                } catch (IOException e) {
                    logger.error("Can not read the ALM password file: " + cmd.getOptionValue("paf"));
                    System.exit(ReturnCode.FAILURE.getReturnCode());
                }
            }

            //load octane password
            if (cmd.hasOption("po")) {
                configuration.setOctanePassword(cmd.getOptionValue("p"));
            } else if (cmd.hasOption("pof")) {
                try {
                    configuration.setOctanePassword(FileUtils.readFileToString(new File(cmd.getOptionValue("pof"))));
                } catch (IOException e) {
                    logger.error("Can not read the Octane password file: " + cmd.getOptionValue("pof"));
                    System.exit(ReturnCode.FAILURE.getReturnCode());
                }
            }

            try {
                configuration.validateProperties();
            } catch (Exception e) {
                logger.error("Failed to parse configuration file : " + e.getMessage());
                System.exit(ReturnCode.FAILURE.getReturnCode());
            }

            initProxyIfDefined(configuration);
            configuration.logProperties();

        } catch (Exception e) {
            logger.error(e.getMessage());
            System.exit(ReturnCode.FAILURE.getReturnCode());
        }
        return configuration;
    }

    private void validateArguments(CommandLine cmd) {
        //validation before loading other args
        for (String arg : argsWithSingleOccurrence) {
            if (cmd.getOptionProperties(arg).size() > 1) {
                logger.error("Only single occurrence is allowed for argument: '" + arg + "'");
                System.exit(ReturnCode.FAILURE.getReturnCode());
            }
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

    private void initProxyIfDefined(FetchConfiguration configuration) {
        if (StringUtils.isNotEmpty(configuration.getProxyHost()) && StringUtils.isNotEmpty(configuration.getProxyPort())) {
            try {
                logger.info("Setting proxy " + configuration.getProxyHost() + ":" + configuration.getProxyPort());
                int port = Integer.parseInt(configuration.getProxyPort());
                RestConnector.setProxy(configuration.getProxyHost(), port);
            } catch (Exception e) {
                logger.error("Failed to set proxy : " + e.getMessage());
                System.exit(ReturnCode.FAILURE.getReturnCode());
            }
        }
    }

}
