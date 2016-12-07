package com.hp.mqm.atrf.core.configuration;

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

    Map<String, String> properties = new HashMap<>();
    private static String DATE_FORMAT = "yyyy-MM-dd";
    static final Logger logger = LogManager.getLogger();


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

    public static String OUTPUT_FILE_PARAM = "conf.outputFile";

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
                ALM_RUN_FILTER_CUSTOM_PARAM,SYNC_BULK_SIZE_PARAM,SYNC_SLEEP_BETWEEN_POSTS_PARAM,PROXY_HOST_PARAM,PROXY_PORT_PARAM, OUTPUT_FILE_PARAM));

        lowered2allowedParams = new HashMap<>();
        for(String param : allowedParameters){
            lowered2allowedParams.put(param.toLowerCase(),param);
        }
    }

    public void logProperties() {
        //put in TreeMap for sorting
        TreeMap<String, String> props = new TreeMap<>(this.properties);
        props.remove(ALM_PASSWORD_PARAM);
        props.remove(OCTANE_PASSWORD_PARAM);
        props.remove(SYNC_BULK_SIZE_PARAM);
        props.remove(SYNC_SLEEP_BETWEEN_POSTS_PARAM);
        logger.info("Loaded configuration : " + (props.entrySet().toString()));
    }

    public void validateProperties() {

        //MUST PARAMETERS
        validateMustParameter(ALM_USER_PARAM);
        validateMustParameter(ALM_PROJECT_PARAM);
        validateMustParameter(ALM_DOMAIN_PARAM);
        validateMustParameter(ALM_SERVER_URL_PARAM);

        validateMustParameter( OCTANE_USER_PARAM);
        validateMustParameter( OCTANE_WORKSPACE_ID_PARAM);
        validateMustParameter( OCTANE_SHAREDSPACE_ID_PARAM);
        validateMustParameter( OCTANE_SERVER_URL_PARAM);

        //INTEGER
        validateIntegerParameter( OCTANE_WORKSPACE_ID_PARAM);
        validateIntegerParameter( OCTANE_SHAREDSPACE_ID_PARAM);
        validateIntegerParameter( SYNC_BULK_SIZE_PARAM);
        validateIntegerParameter( SYNC_SLEEP_BETWEEN_POSTS_PARAM);
        validateIntegerParameter( PROXY_PORT_PARAM);

        //CUSTOM VALIDATIONS
        //ALM_RUN_FILTER_START_FROM_ID
        String startFromIdValue = getAlmRunFilterStartFromId();
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
        String startFromDateValue = getAlmRunFilterStartFromDate();
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
        String relatedEntityType = getAlmRunFilterRelatedEntityType();
        String relatedEntityId = getAlmRunFilterRelatedEntityId();
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
        String bulkSizeStr = getProperty(SYNC_BULK_SIZE_PARAM);
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
        setProperty(SYNC_BULK_SIZE_PARAM, Integer.toString(bulkSize));


        //SLEEP
        String sleepBetweenPostsStr = getProperty(SYNC_SLEEP_BETWEEN_POSTS_PARAM);
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
        setProperty(SYNC_SLEEP_BETWEEN_POSTS_PARAM, Integer.toString(sleepBetweenPosts));

    }

    private void validateIntegerParameter(String key) {
        String value = getProperty(key);
        if (StringUtils.isNotEmpty(value)) {
            try {
                Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new RuntimeException(String.format("Configuration parameter '%s' must hold integer value, but contains '%s'", key, value));
            }
        }
    }

    private void validateMustParameter(String key) {
        String value = getProperty(key);
        if (StringUtils.isEmpty(value)) {
            throw new RuntimeException(String.format("Configuration parameter '%s' is missing or empty", key));
        }
    }

    public static FetchConfiguration loadPropertiesFromFile(String pathName) {

        FetchConfiguration configuration = new FetchConfiguration();

        try {
            File inputFile = new File(pathName);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            Node root = doc.getDocumentElement();
            if (!root.getNodeName().equals("conf")) {
                throw new RuntimeException("Missing root element <conf> in file : " + pathName);
            }
            parseNodes(root, root.getNodeName(), configuration);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read configuration file : " + pathName, e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Failed to parse configuration file : " + pathName, e);
        } catch (SAXException e) {
            throw new RuntimeException("Failed to parse configuration file : " + pathName, e);
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

    public void setAlmPassword(String value) {
        setProperty(ALM_PASSWORD_PARAM, value);
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

    public void setOctanePassword(String value) {
        setProperty(OCTANE_PASSWORD_PARAM, value);
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

    public void setOutputFile(String outputFile) {
        setProperty(OUTPUT_FILE_PARAM, outputFile);
    }

    public String getOutputFile(){
        return getProperty(OUTPUT_FILE_PARAM);
    }
}
