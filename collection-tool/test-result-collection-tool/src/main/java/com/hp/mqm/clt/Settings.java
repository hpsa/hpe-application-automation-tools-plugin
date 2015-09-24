package com.hp.mqm.clt;

import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

public class Settings {

    private static final String DEFAULT_CONFIG_FILENAME = "config.properties";

    private static final String PROP_SERVER = "server";
    private static final String PROP_SHARED_SPACE = "sharedspace";
    private static final String PROP_WORKSPACE = "workspace";
    private static final String PROP_USER = "user";

    private String server;
    private Integer sharedspace;
    private Integer workspace;

    private String user;
    private String password;

    private boolean internal = false;
    private String outputFile;

    private List<String> tags;
    private List<String> fields;

    private Integer release;
    private Integer productArea;
    private Integer requirement;

    private List<String> fileNames;

    private DefaultConfigFilenameProvider defaultConfigFilenameProvider = new ImplDefaultConfigFilenameProvider();

    public void load(String filename) throws IOException, URISyntaxException {
        File propertiesFile = null;
        URL defaultConfigFile = getClass().getResource(defaultConfigFilenameProvider.getDefaultConfigFilename());
        if (filename != null) {
            propertiesFile = new File(filename);
        } else if (defaultConfigFile != null) {
            propertiesFile = new File(defaultConfigFile.toURI());
        }
        if (propertiesFile == null || !propertiesFile.canRead()) {
            return;
        }

        Properties properties = new Properties();
        InputStream inputStream = new FileInputStream(propertiesFile);
        properties.load(inputStream);
        inputStream.close();
        server = properties.getProperty(PROP_SERVER);
        sharedspace = properties.getProperty(PROP_SHARED_SPACE) != null ? Integer.valueOf(properties.getProperty(PROP_SHARED_SPACE)) : null;
        workspace = properties.getProperty(PROP_WORKSPACE) != null ? Integer.valueOf(properties.getProperty(PROP_WORKSPACE)) : null;
        user = properties.getProperty(PROP_USER);
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

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
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

    public Integer getProductArea() {
        return productArea;
    }

    public void setProductArea(Integer productArea) {
        this.productArea = productArea;
    }

    public Integer getRequirement() {
        return requirement;
    }

    public void setRequirement(Integer requirement) {
        this.requirement = requirement;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
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
