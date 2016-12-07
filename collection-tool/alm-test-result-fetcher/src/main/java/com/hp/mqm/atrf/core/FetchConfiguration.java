package com.hp.mqm.atrf.core;

import com.hp.mqm.atrf.core.rest.RestConnector;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by berkovir on 01/12/2016.
 */
public class FetchConfiguration {

    static final Logger logger = LogManager.getLogger();
    Map<String, String> properties = new HashMap<>();
    private static String DATE_FORMAT = "yyyy-MM-dd";
    private static String CONF_FILE_PREFIX = "conf=";


    public static String ALM_USER_PARAM = "conf.alm.user";
    public static String ALM_PASSWORD_PARAM = "conf.alm.password";
    public static String ALM_SERVER_URL_PARAM = "conf.alm.serverUrl";
    public static String ALM_DOMAIN_PARAM = "conf.alm.domain";
    public static String ALM_PROJECT_PARAM = "conf.alm.project";

    public static String OCTANE_PASSWORD_PARAM = "conf.octane.clientSecret";
    public static String OCTANE_USER_PARAM = "conf.octane.clientId";
    public static String OCTANE_SERVER_URL_PARAM = "conf.octane.serverUrl";
    public static String OCTANE_SHAREDSPACE_ID_PARAM = "conf.octane.sharedSpaceId";
    public static String OCTANE_WORKSPACE_ID_PARAM = "conf.octane.workspaceId";


    public static String ALM_RUN_FILTER_START_FROM_ID_PARAM = "conf.alm.runFilter.startFromId";
    public static String ALM_RUN_FILTER_START_FROM_DATE_PARAM = "conf.alm.runFilter.startFromDate";
    public static String ALM_RUN_FILTER_TEST_TYPE_PARAM = "conf.alm.runFilter.testType";
    public static String ALM_RUN_FILTER_RELATED_ENTITY_TYPE_PARAM = "conf.alm.runFilter.relatedEntity.type";
    public static String ALM_RUN_FILTER_RELATED_ENTITY_ID_PARAM = "conf.alm.runFilter.relatedEntity.id";
    public static String ALM_RUN_FILTER_CUSTOM_PARAM = "conf.alm.runFilter.custom";
    public static String SYNC_BULK_SIZE_PARAM = "conf.sync.bulkSize";
    public static String SYNC_SLEEP_BETWEEN_POSTS_PARAM = "conf.sync.sleepBetweenPosts";

    public static String PROXY_HOST_PARAM = "conf.proxy.host";
    public static String PROXY_PORT_PARAM = "conf.proxy.port";

    public Set<String> allowedParameters;
    private  Map<String,String>lowered2allowedParams;

    private static int SYNC_BULK_SIZE_DEFAULT = 1000;
    private static int SYNC_BULK_SIZE_MAX = 1000;
    private static int SYNC_BULK_SIZE_MIN = 100;


    private static int SYNC_SLEEP_BETWEEN_POSTS_DEFAULT = 5;
    private static int SYNC_SLEEP_BETWEEN_POSTS_MAX = 100;
    private static int SYNC_SLEEP_BETWEEN_POSTS_MIN = 5;

    public static String ALM_RUN_FILTER_START_FROM_ID_LAST_SENT = "LAST_SENT";

    public FetchConfiguration(){
        allowedParameters = new HashSet<>(Arrays.asList(ALM_USER_PARAM, ALM_PASSWORD_PARAM, ALM_SERVER_URL_PARAM,ALM_DOMAIN_PARAM,ALM_PROJECT_PARAM,
                OCTANE_PASSWORD_PARAM,OCTANE_USER_PARAM,OCTANE_SERVER_URL_PARAM,OCTANE_SHAREDSPACE_ID_PARAM,OCTANE_WORKSPACE_ID_PARAM,
                ALM_RUN_FILTER_START_FROM_ID_PARAM,ALM_RUN_FILTER_START_FROM_DATE_PARAM,ALM_RUN_FILTER_TEST_TYPE_PARAM,ALM_RUN_FILTER_RELATED_ENTITY_TYPE_PARAM,ALM_RUN_FILTER_RELATED_ENTITY_ID_PARAM,
                ALM_RUN_FILTER_CUSTOM_PARAM,SYNC_BULK_SIZE_PARAM,SYNC_SLEEP_BETWEEN_POSTS_PARAM,PROXY_HOST_PARAM,PROXY_PORT_PARAM));

        lowered2allowedParams = new HashMap<>();
        for(String param : allowedParameters){
            lowered2allowedParams.put(param.toLowerCase(),param);
        }
    }
    public static FetchConfiguration loadFromArguments(String[] args) {

        String pathName = tryFindConfigurationFile(args);
        FetchConfiguration configuration = loadPropertiesFromFile(pathName);
        loadPropertiesFromArgs(args, configuration);
        validateProperties(configuration);
        initProxyIfDefined(configuration);
        logProperties(configuration);


        return configuration;
    }

    private static void initProxyIfDefined(FetchConfiguration configuration) {
        if (StringUtils.isNotEmpty(configuration.getProxyHost()) && StringUtils.isNotEmpty(configuration.getProxyPort())) {
            try {
                int port = Integer.parseInt(configuration.getProxyPort());
                RestConnector.setProxy(configuration.getProxyHost(), port);
                logger.info("Proxy is set to " + configuration.getProxyHost() + ":" + configuration.getProxyPort());
            } catch (Exception e) {
                throw new RuntimeException("Failed to init proxy : " + e.getMessage());
            }
        }
    }

    private static void logProperties(FetchConfiguration configuration) {
        //put in TreeMap for sorting
        TreeMap<String, String> props = new TreeMap<>(configuration.properties);
        props.put(ALM_PASSWORD_PARAM, "*****");
        props.put(OCTANE_PASSWORD_PARAM, "*****");
        logger.info("Loaded configuration : " + (props.entrySet().toString()));
    }

    private static void validateProperties(FetchConfiguration configuration) {

        //MUST PARAMETERS
        validateMustParameter(configuration, ALM_USER_PARAM);
        validateMustParameter(configuration, ALM_PROJECT_PARAM);
        validateMustParameter(configuration, ALM_DOMAIN_PARAM);
        validateMustParameter(configuration, ALM_SERVER_URL_PARAM);

        validateMustParameter(configuration, OCTANE_USER_PARAM);
        validateMustParameter(configuration, OCTANE_WORKSPACE_ID_PARAM);
        validateMustParameter(configuration, OCTANE_SHAREDSPACE_ID_PARAM);
        validateMustParameter(configuration, OCTANE_SERVER_URL_PARAM);

        //INTEGER
        validateIntegerParameter(configuration, OCTANE_WORKSPACE_ID_PARAM);
        validateIntegerParameter(configuration, OCTANE_SHAREDSPACE_ID_PARAM);
        validateIntegerParameter(configuration, SYNC_BULK_SIZE_PARAM);
        validateIntegerParameter(configuration, SYNC_SLEEP_BETWEEN_POSTS_PARAM);
        validateIntegerParameter(configuration, PROXY_PORT_PARAM);

        //CUSTOM VALIDATIONS

        //ALM_RUN_FILTER_START_FROM_ID
        String startFromIdValue = configuration.getAlmRunFilterStartFromId();
        if (StringUtils.isNotEmpty(startFromIdValue)) {
            boolean isValid = false;
            if (ALM_RUN_FILTER_START_FROM_ID_LAST_SENT.equalsIgnoreCase(startFromIdValue)) {
                isValid = true;
            } else {
                try {
                    Integer.parseInt(startFromIdValue);
                    isValid = true;
                } catch (NumberFormatException e) {

                }
            }

            if (!isValid) {
                throw new RuntimeException(String.format("Configuration parameter '%s' can hold integer value or '%', but contains '%s'",
                        ALM_RUN_FILTER_START_FROM_ID_PARAM, ALM_RUN_FILTER_START_FROM_ID_LAST_SENT, startFromIdValue));
            }
        }

        //ALM_RUN_FILTER_START_FROM_DATE
        String startFromDateValue = configuration.getAlmRunFilterStartFromDate();
        if (StringUtils.isNotEmpty(startFromDateValue)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            try {
                Date d = dateFormat.parse(startFromDateValue);
            } catch (ParseException e) {
                throw new RuntimeException(String.format("Configuration parameter '%s' should contain date in the following format '%s'",
                        ALM_RUN_FILTER_START_FROM_DATE_PARAM, DATE_FORMAT));
            }

        }

        //ALM_RUN_FILTER_RELATED_ENTITY_ENTITY_ID_PARAM
        String relatedEntityType = configuration.getAlmRunFilterRelatedEntityType();
        String relatedEntityId = configuration.getAlmRunFilterRelatedEntityId();
        if (StringUtils.isNotEmpty(relatedEntityType) && StringUtils.isEmpty(relatedEntityId)) {
            throw new RuntimeException(String.format("Configuration contains value for parameter '%s', but missing value for parameter '%s'",
                    ALM_RUN_FILTER_RELATED_ENTITY_TYPE_PARAM, ALM_RUN_FILTER_RELATED_ENTITY_ID_PARAM));
        }
        if (StringUtils.isNotEmpty(relatedEntityId) && StringUtils.isEmpty(relatedEntityType)) {
            throw new RuntimeException(String.format("Configuration contains value for parameter '%s', but missing value for parameter '%s'",
                    ALM_RUN_FILTER_RELATED_ENTITY_ID_PARAM, ALM_RUN_FILTER_RELATED_ENTITY_TYPE_PARAM));
        }
        if (StringUtils.isNotEmpty(relatedEntityType)) {
            relatedEntityType = relatedEntityType.toLowerCase();
            List<String> allowedEntityTypes = Arrays.asList("test", "testset", "sprint", "release");
            if (!allowedEntityTypes.contains(relatedEntityType)) {
                throw new RuntimeException(String.format("Configuration contains illegal value for parameter '%s', allowed values are %s",
                        ALM_RUN_FILTER_RELATED_ENTITY_TYPE_PARAM, allowedEntityTypes.toString()));
            }
        }

        //BULK SIZE
        String bulkSizeStr = configuration.getProperty(SYNC_BULK_SIZE_PARAM);
        int bulkSize = SYNC_BULK_SIZE_DEFAULT;
        if (StringUtils.isNotEmpty(bulkSizeStr)) {
            try {
                bulkSize = Integer.parseInt(bulkSizeStr);
                if (bulkSize <= SYNC_BULK_SIZE_MIN || bulkSize >= SYNC_BULK_SIZE_MAX) {
                    bulkSize = SYNC_BULK_SIZE_DEFAULT;
                }
            } catch (Exception e) {
                bulkSize = SYNC_BULK_SIZE_DEFAULT;
            }
        }
        configuration.setProperty(SYNC_BULK_SIZE_PARAM, Integer.toString(bulkSize));


        //SLEEP
        String sleepBetweenPostsStr = configuration.getProperty(SYNC_SLEEP_BETWEEN_POSTS_PARAM);
        int sleepBetweenPosts = SYNC_SLEEP_BETWEEN_POSTS_DEFAULT;
        if (StringUtils.isNotEmpty(sleepBetweenPostsStr)) {
            try {
                sleepBetweenPosts = Integer.parseInt(bulkSizeStr);
                if (sleepBetweenPosts <= SYNC_SLEEP_BETWEEN_POSTS_MIN || sleepBetweenPosts >= SYNC_SLEEP_BETWEEN_POSTS_MAX) {
                    sleepBetweenPosts = SYNC_SLEEP_BETWEEN_POSTS_DEFAULT;
                }
            } catch (Exception e) {
                sleepBetweenPosts = SYNC_SLEEP_BETWEEN_POSTS_DEFAULT;
            }
        }
        configuration.setProperty(SYNC_SLEEP_BETWEEN_POSTS_PARAM, Integer.toString(sleepBetweenPosts));

    }

    private static void validateIntegerParameter(FetchConfiguration configuration, String key) {
        String value = configuration.getProperty(key);
        if (StringUtils.isNotEmpty(value)) {
            try {
                Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new RuntimeException(String.format("Configuration parameter '%s' must hold integer value, but contains '%s'", key, value));
            }
        }
    }

    private static void validateMustParameter(FetchConfiguration configuration, String key) {
        String value = configuration.getProperty(key);
        if (StringUtils.isEmpty(value)) {
            throw new RuntimeException(String.format("Configuration parameter '%s' is missing or empty", key));
        }
    }

    private static void loadPropertiesFromArgs(String[] args, FetchConfiguration configuration) {
        for (String arg : args) {
            if (arg.startsWith("conf.") && arg.contains("=")) {
                String[] parts = arg.split("=");
                if (parts.length == 2) {
                    String key = parts[0];
                    String value = parts[1];
                    configuration.setProperty(key, value);
                    logger.info(key + " key is taken from command line.");
                } else {
                    logger.warn(arg + " argument must contain exactly one '='. Argument is ignored");
                    throw new RuntimeException();
                }
            }
        }
    }

    private static String tryFindConfigurationFile(String[] args) {
        String path = null;

        //try to find on command line
        for (String arg : args) {
            if (arg.startsWith(CONF_FILE_PREFIX)) {
                String[] confFileParts = arg.split("=");
                path = confFileParts[1];
                File f = new File(path);
                if (f.exists() && !f.isDirectory()) {
                    logger.info("Configuration file is defined on command line");
                } else {
                    String errorMsg = "Configuration file is not found on path defined in command line.";
                    throw new RuntimeException(errorMsg);
                }
            }
        }

        if (path == null) {
            //try to take from default location
            String defaultPath = "conf.xml";
            File f = new File(defaultPath);
            if (f.exists() && !f.isDirectory()) {
                path = defaultPath;
                logger.info("Configuration file is taken from default location : " + f.getPath());
            }
        }

        if (path == null) {
            throw new RuntimeException("The configuration file path argument is missing or doesn't starts with 'conf='");

        }
        return path;
    }

    private static FetchConfiguration loadPropertiesFromFile(String pathName) {

        FetchConfiguration configuration = new FetchConfiguration();

        try {
            File inputFile = new File(pathName);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            Node root = doc.getDocumentElement();
            if (!root.getNodeName().equals("conf")) {
                throw new RuntimeException("Missing root element <conf> in file " + pathName);
            }
            parseNodes(root, root.getNodeName(), configuration);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse configuration from file " + pathName, e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Failed to parse configuration from file " + pathName, e);
        } catch (SAXException e) {
            throw new RuntimeException("Failed to parse configuration from file " + pathName, e);
        }
        return configuration;
    }


    private static void parseNodes(Node node, String prefix, FetchConfiguration configuration) {
        NodeList nList = node.getChildNodes();
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                parseNodes(nNode, prefix + "." + nNode.getNodeName(), configuration);
            } else if (nNode.getNodeType() == Node.TEXT_NODE) {
                String value = nNode.getTextContent().trim();
                if (StringUtils.isNotEmpty(value)) {
                    configuration.setProperty(prefix, value);
                }

            }
        }
    }


    public String getProperty(String key) {
        return properties.get(key);
    }

    public void setProperty(String key, String value) {
        String myKey = lowered2allowedParams.get(key.toLowerCase());
        if(!allowedParameters.contains(myKey)){
            throw new RuntimeException("Unknown parameter : " + key);
        }
        properties.put(myKey, value);
    }

    public String getAlmUser() {
        return getProperty(ALM_USER_PARAM);
    }

    public String getAlmPassword() {
        return getProperty(ALM_PASSWORD_PARAM);
    }

    public String getAlmServerUrl() {
        return getProperty(ALM_SERVER_URL_PARAM);
    }

    public String getAlmDomain() {
        return getProperty(ALM_DOMAIN_PARAM);
    }

    public String getAlmProject() {
        return getProperty(ALM_PROJECT_PARAM);
    }

    public String getOctanePassword() {
        return getProperty(OCTANE_PASSWORD_PARAM);
    }

    public String getOctaneUser() {
        return getProperty(OCTANE_USER_PARAM);
    }

    public String getOctaneServerUrl() {
        return getProperty(OCTANE_SERVER_URL_PARAM);
    }

    public String getOctaneSharedSpaceId() {
        return getProperty(OCTANE_SHAREDSPACE_ID_PARAM);
    }

    public String getOctaneWorkspaceId() {
        return getProperty(OCTANE_WORKSPACE_ID_PARAM);
    }

    public String getProxyHost() {
        return getProperty(PROXY_HOST_PARAM);
    }

    public String getProxyPort() {
        return getProperty(PROXY_PORT_PARAM);
    }

    public String getAlmRunFilterStartFromId() {
        return getProperty(ALM_RUN_FILTER_START_FROM_ID_PARAM);
    }

    public String getAlmRunFilterStartFromDate() {
        return getProperty(ALM_RUN_FILTER_START_FROM_DATE_PARAM);
    }

    public String getAlmRunFilterTestType() {
        return getProperty(ALM_RUN_FILTER_TEST_TYPE_PARAM);
    }

    public String getAlmRunFilterRelatedEntityType() {
        return getProperty(ALM_RUN_FILTER_RELATED_ENTITY_TYPE_PARAM);
    }

    public String getAlmRunFilterRelatedEntityId() {
        return getProperty(ALM_RUN_FILTER_RELATED_ENTITY_ID_PARAM);
    }

    public String getAlmRunFilterCustom() {
        return getProperty(ALM_RUN_FILTER_CUSTOM_PARAM);
    }
}
