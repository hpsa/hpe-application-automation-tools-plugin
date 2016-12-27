package com.hp.mqm.atrf.core.configuration;

import com.hp.mqm.atrf.core.rest.RestConnector;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;


public class CliParser {

    static final Logger logger = LogManager.getLogger();

    private static final String CMD_LINE_SYNTAX = "java -jar alm-test-result-fetcher.jar [OPTIONS]... \n";
    private static final String HEADER = "HPE ALM Test Result Collection Tool";
    private static final String FOOTER = "";
    private static final String VERSION = "1.0";

    public static final String DEFAULT_CONF_FILE = "conf.xml";
    public static final String DEFAULT_OUTPUT_FILE = "output.xml";

    public static final String HELP_OPTION = "h";
    public static final String HELP_OPTION_LONG = "help";
    public static final String VERSION_OPTION = "v";
    public static final String VERSION_OPTION_LONG = "version";
    public static final String OUTPUT_FILE_OPTION = "o";
    public static final String OUTPUT_FILE_OPTION_LONG = "output-file";
    public static final String CONFIG_FILE_OPTION = "c";
    public static final String CONFIG_FILE_OPTION_LONG = "config-file";
    public static final String PASSWORD_ALM_OPTION = "pa";
    public static final String PASSWORD_ALM_OPTION_LONG = "password-alm";
    public static final String PASSWORD_ALM_FILE_OPTION = "paf";
    public static final String PASSWORD_ALM_FILE_OPTION_LONG = "password-alm-file";
    public static final String PASSWORD_OCTANE_OPTION = "po";
    public static final String PASSWORD_OCTANE_OPTION_LONG = "password-oct";
    public static final String PASSWORD_OCTANE_FILE_OPTION = "pof";
    public static final String PASSWORD_OCTANE_FILE_OPTION_LONG = "password-oct-file";
    public static final String RUN_FILTER_ID_OPTION = "rfid";
    public static final String RUN_FILTER_ID_OPTION_LONG = "run-filter-id";
    public static final String RUN_FILTER_DATE_OPTION = "rfd";
    public static final String RUN_FILTER_DATE_OPTION_LONG = "run-filter-date";
    public static final String RUN_FILTER_LIMIT_OPTION = "rfl";
    public static final String RUN_FILTER_LIMIT_OPTION_LONG = "run-filter-limit";

    private Options options = new Options();
    private LinkedList<String> argsWithSingleOccurrence = new LinkedList<>();


    public CliParser() {
        options.addOption(Option.builder(HELP_OPTION).longOpt(HELP_OPTION_LONG).desc("show this help").build());
        options.addOption(Option.builder(VERSION_OPTION).longOpt(VERSION_OPTION_LONG).desc("show version of this tool").build());

        options.addOption(Option.builder(OUTPUT_FILE_OPTION).longOpt(OUTPUT_FILE_OPTION_LONG).desc("write output to file instead of pushing it to the server. File path is optional. Default file name is '" + DEFAULT_OUTPUT_FILE + "'").hasArg().argName("FILE").optionalArg(true).build());
        options.addOption(Option.builder(CONFIG_FILE_OPTION).longOpt(CONFIG_FILE_OPTION_LONG).desc("configuration file location. Default configuration file name is '" + DEFAULT_CONF_FILE + "'").hasArg().argName("FILE").build());

        OptionGroup passAlmGroup = new OptionGroup();
        passAlmGroup.addOption(Option.builder(PASSWORD_ALM_OPTION).longOpt(PASSWORD_ALM_OPTION_LONG).desc("password for alm user").hasArg().argName("PASSWORD").build());
        passAlmGroup.addOption(Option.builder(PASSWORD_ALM_FILE_OPTION).longOpt(PASSWORD_ALM_FILE_OPTION_LONG).desc("location of file with password for alm user").hasArg().argName("FILE").build());
        options.addOptionGroup(passAlmGroup);

        OptionGroup passOctaneGroup = new OptionGroup();
        passOctaneGroup.addOption(Option.builder(PASSWORD_OCTANE_OPTION).longOpt(PASSWORD_OCTANE_OPTION_LONG).desc("password for octane user").hasArg().argName("PASSWORD").optionalArg(true).build());
        passOctaneGroup.addOption(Option.builder(PASSWORD_OCTANE_FILE_OPTION).longOpt(PASSWORD_OCTANE_FILE_OPTION_LONG).desc("location of file with password for octane user").hasArg().argName("FILE").build());
        options.addOptionGroup(passOctaneGroup);

        options.addOption(Option.builder(RUN_FILTER_ID_OPTION).longOpt(RUN_FILTER_ID_OPTION_LONG).desc("start run fetching from id").hasArg().argName("ID").build());
        options.addOption(Option.builder(RUN_FILTER_DATE_OPTION).longOpt(RUN_FILTER_DATE_OPTION_LONG).desc("start run fetching from date").hasArg().argName("YYYY-MM-DD").build());

        options.addOption(Option.builder(RUN_FILTER_LIMIT_OPTION).longOpt(RUN_FILTER_LIMIT_OPTION_LONG).desc("limit number of fetched runs from ALM side").hasArg().argName("NUMBER").build());

        argsWithSingleOccurrence.addAll(Arrays.asList(OUTPUT_FILE_OPTION, CONFIG_FILE_OPTION, PASSWORD_ALM_OPTION, PASSWORD_ALM_FILE_OPTION, PASSWORD_OCTANE_OPTION,
                PASSWORD_OCTANE_FILE_OPTION, RUN_FILTER_ID_OPTION, RUN_FILTER_DATE_OPTION, RUN_FILTER_LIMIT_OPTION));

    }

    public FetchConfiguration parse(String[] args) {
        FetchConfiguration configuration = null;
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            //help
            if (cmd.hasOption(HELP_OPTION)) {
                printHelp();
                System.exit(ReturnCode.SUCCESS.getReturnCode());
            }

            //version
            if (cmd.hasOption(VERSION_OPTION)) {
                printVersion();
                System.exit(ReturnCode.SUCCESS.getReturnCode());
            }
            validateArguments(cmd);


            // load config
            String configFile = null;
            if (cmd.hasOption(CONFIG_FILE_OPTION)) {
                configFile = cmd.getOptionValue(CONFIG_FILE_OPTION);
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
            if (cmd.hasOption(OUTPUT_FILE_OPTION)) {
                String outputFilePath = cmd.getOptionValue(OUTPUT_FILE_OPTION);
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
            if (cmd.hasOption(PASSWORD_ALM_OPTION)) {
                configuration.setAlmPassword(cmd.getOptionValue(PASSWORD_ALM_OPTION));
            } else if (cmd.hasOption(PASSWORD_ALM_FILE_OPTION)) {
                try {
                    configuration.setAlmPassword(FileUtils.readFileToString(new File(cmd.getOptionValue(PASSWORD_ALM_FILE_OPTION))));
                } catch (IOException e) {
                    logger.error("Can not read the ALM password file: " + cmd.getOptionValue(PASSWORD_ALM_FILE_OPTION));
                    System.exit(ReturnCode.FAILURE.getReturnCode());
                }
            }

            //load octane password
            if (cmd.hasOption(PASSWORD_OCTANE_OPTION)) {
                configuration.setOctanePassword(cmd.getOptionValue(PASSWORD_OCTANE_OPTION));
            } else if (cmd.hasOption(PASSWORD_OCTANE_FILE_OPTION)) {
                try {
                    configuration.setOctanePassword(FileUtils.readFileToString(new File(cmd.getOptionValue(PASSWORD_OCTANE_FILE_OPTION))));
                } catch (IOException e) {
                    logger.error("Can not read the Octane password file: " + cmd.getOptionValue(PASSWORD_OCTANE_FILE_OPTION));
                    System.exit(ReturnCode.FAILURE.getReturnCode());
                }
            }

            //run filter options
            if (cmd.hasOption(RUN_FILTER_ID_OPTION)) {
                configuration.setAlmRunFilterStartFromId(cmd.getOptionValue(RUN_FILTER_ID_OPTION));
            }
            if (cmd.hasOption(RUN_FILTER_DATE_OPTION)) {
                configuration.setAlmRunFilterStartFromDate(cmd.getOptionValue(RUN_FILTER_DATE_OPTION));
            }
            if (cmd.hasOption(RUN_FILTER_LIMIT_OPTION)) {
                configuration.setRunFilterFetchLimit(cmd.getOptionValue(RUN_FILTER_LIMIT_OPTION));
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
