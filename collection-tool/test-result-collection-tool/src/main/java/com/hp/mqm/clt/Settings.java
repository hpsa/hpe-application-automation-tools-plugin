package com.hp.mqm.clt;

import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class Settings {

    private static final String DEFAULT_CONFIG_FILENAME = "config.properties";

    private static final String PROP_SERVER = "server";
    private static final String PROP_SHARED_SPACE = "sharedspace";
    private static final String PROP_WORKSPACE = "workspace";
    private static final String PROP_USER = "user";
    private static final String PROP_PROXY_HOST = "proxyhost";
    private static final String PROP_PROXY_PORT = "proxyport";
    private static final String PROP_PROXY_USER = "proxyuser";

    private String server;
    private Integer sharedspace;
    private Integer workspace;
    private String user;
    private String password;

    private String proxyHost;
    private Integer proxyPort;
    private String proxyUser;
    private String proxyPassword;

    private boolean internal = false;
    private boolean skipErrors = false;
    private String outputFile;

    private List<String> tags;
    private List<String> fields;

    private Integer release;
    private List<Integer> productAreas;
    private List<Integer> backlogItems;
    private Long started;

    private List<String> inputXmlFileNames;

    private DefaultConfigFilenameProvider defaultConfigFilenameProvider = new ImplDefaultConfigFilenameProvider();

    // CODE REVIEW, Johnny, 1*Oct2015 - when the URISyntaxException is thrown?
    public void load(String filename) throws IOException, URISyntaxException {
        File configFile = new File((filename != null) ? filename : defaultConfigFilenameProvider.getDefaultConfigFilename());
        if (!configFile.isFile() || !configFile.canRead()) {
            return;
        }

        Properties properties = new Properties();
        InputStream inputStream = new FileInputStream(configFile);
        properties.load(inputStream);
        inputStream.close();
        server = properties.getProperty(PROP_SERVER);
        sharedspace = properties.getProperty(PROP_SHARED_SPACE) != null ? Integer.valueOf(properties.getProperty(PROP_SHARED_SPACE)) : null;
        workspace = properties.getProperty(PROP_WORKSPACE) != null ? Integer.valueOf(properties.getProperty(PROP_WORKSPACE)) : null;
        user = properties.getProperty(PROP_USER);
        proxyHost = properties.getProperty(PROP_PROXY_HOST);
        if (StringUtils.isNotEmpty(properties.getProperty(PROP_PROXY_PORT))) {
            proxyPort = Integer.valueOf(properties.getProperty(PROP_PROXY_PORT));
        }
        proxyUser = properties.getProperty(PROP_PROXY_USER);
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public Integer getSharedspace() {
        return sharedspace;
    }

    public void setSharedspace(Integer sharedspace) {
        this.sharedspace = sharedspace;
    }

    public Integer getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Integer workspace) {
        this.workspace = workspace;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return (password == null) ? null : new String(Base64.decodeBase64(password), StandardCharsets.UTF_8);
    }

    public void setPassword(String password) {
        this.password = (password == null) ? null : Base64.encodeBase64String(password.getBytes(StandardCharsets.UTF_8));
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getProxyPassword() {
        return (proxyPassword == null) ? null : new String(Base64.decodeBase64(proxyPassword), StandardCharsets.UTF_8);
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = (proxyPassword == null) ? null : Base64.encodeBase64String(proxyPassword.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public boolean isSkipErrors() {
        return skipErrors;
    }

    public void setSkipErrors(boolean skipErrors) {
        this.skipErrors = skipErrors;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public Integer getRelease() {
        return release;
    }

    public void setRelease(Integer release) {
        this.release = release;
    }

    public List<Integer> getProductAreas() {
        return productAreas;
    }

    public void setProductAreas(String[] productAreas) throws ParseException {
        List<Integer> productAreasList = null;
        if (productAreas != null && productAreas.length > 0) {
            productAreasList = new LinkedList<Integer>();
            for (String productArea : productAreas) {
                Integer productAreaID;
                try {
                    productAreaID = Integer.parseInt(productArea);
                } catch (NumberFormatException e) {
                    throw new ParseException("Unable to parse string to product area ID: " + productArea);
                }
                productAreasList.add(productAreaID);
            }
        }
        this.productAreas = productAreasList;
    }

    public List<Integer> getBacklogItems() {
        return backlogItems;
    }

    public void setBacklogItems(String[] backlogItems) throws ParseException {
        List<Integer> backlogItemsList = null;
        if (backlogItems != null && backlogItems.length > 0) {
            backlogItemsList = new LinkedList<Integer>();
            for (String backlogItem : backlogItems) {
                Integer backlogItemID;
                try {
                    backlogItemID = Integer.parseInt(backlogItem);
                } catch (NumberFormatException e) {
                    throw new ParseException("Unable to parse string to backlog item ID: " + backlogItem);
                }
                backlogItemsList.add(backlogItemID);
            }
        }
        this.backlogItems = backlogItemsList;
    }

    public Long getStarted() {
        return started;
    }

    public void setStarted(Long started) {
        this.started = started;
    }

    public List<String> getInputXmlFileNames() {
        return inputXmlFileNames;
    }

    public void setInputXmlFileNames(List<String> inputXmlFileNames) {
        this.inputXmlFileNames = inputXmlFileNames;
    }

    /**
     * To be used by tests only.
     */
    public void setDefaultConfigFilenameProvider(DefaultConfigFilenameProvider defaultConfigFilenameProvider) {
        this.defaultConfigFilenameProvider = defaultConfigFilenameProvider;
    }

    private static class ImplDefaultConfigFilenameProvider implements DefaultConfigFilenameProvider {
        @Override
        public String getDefaultConfigFilename() {
            return DEFAULT_CONFIG_FILENAME;
        }
    }

    public interface DefaultConfigFilenameProvider {

        String getDefaultConfigFilename();

    }
}
