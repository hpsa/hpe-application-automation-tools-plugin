package com.hpe.application.automation.tools.common.integration;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: yanghanx
 * Date: 5/3/16
 * Time: 10:58 AM
 */
public class JobOperation {

    public static final String LOGIN_SECRET = "x-hp4msecret";
    public static final String SPLIT_COMMA = ";";
    public static final String JSESSIONID = "JSESSIONID";
    public static final String ACCEPT = "Accept";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String COOKIE = "Cookie";
    public static final String SET_COOKIE = "Set-Cookie";
    public static final String EQUAL = "=";

    //about upload
    private final static String CONTENT_TYPE_DOWNLOAD_VALUE = "multipart/form-data; boundary=----";
    private final static String BOUNDARYSTR = "randomstring";

    public static final String LOGIN_URL = "/rest/client/login";
    public static final String CREATE_JOB_URL = "/rest/job/createTempJob";
    public static final String GET_JOB_URL = "/rest/job/";
    public static final String UPLOAD_APP_URL = "/rest/apps/upload";


    //mobile center info
    private String _serverUrl;
    private String _userName;
    private String _password;

    //Proxy Configuration information
    private String proxyHost;
    private String proxyPort;
    private String proxyUserName;
    private String proxyPassword;

    public JobOperation() {

    }

    public JobOperation(String serverUrl, String userName, String password) {
        _userName = userName;
        _password = password;

        _serverUrl = checkUrl(serverUrl);

    }

    public JobOperation(String serverUrl, String userName, String password, String address, String proxyUserName, String proxyPassword) {

        _userName = userName;
        _password = password;

        _serverUrl = checkUrl(serverUrl);

        if (address != null) {

            address = checkUrl(address);

            int i = address.lastIndexOf(':');
            if (i > 0) {
                this.proxyHost = address.substring(0, i);
                this.proxyPort = address.substring(i + 1, address.length());
            } else {
                this.proxyHost = address;
                this.proxyPort = "80";
            }

        }

        this.proxyUserName = proxyUserName;
        this.proxyPassword = proxyPassword;

    }

    public String upload(String appPath) throws HttpConnectionException, IOException {

        String json = null;
        String hp4mSecret = null;
        String jsessionId = null;

        String loginJson = loginToMC();

        try {
            if (loginJson != null) {
                JSONObject jsonObject = (JSONObject) JSONValue.parseStrict(loginJson);
                hp4mSecret = (String) jsonObject.get(LOGIN_SECRET);
                jsessionId = (String) jsonObject.get(JSESSIONID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        File appFile = new File(appPath);

        String uploadUrl = _serverUrl + UPLOAD_APP_URL;

        Map<String, String> headers = new HashMap<String, String>();
        headers.put(LOGIN_SECRET, hp4mSecret);
        headers.put(COOKIE, JSESSIONID + EQUAL + jsessionId);
        headers.put(CONTENT_TYPE, CONTENT_TYPE_DOWNLOAD_VALUE + BOUNDARYSTR);
        headers.put("filename", appFile.getName());


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        StringBuffer content = new StringBuffer();
        content.append("\r\n").append("------").append(BOUNDARYSTR).append("\r\n");
        content.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + appFile.getName() + "\"\r\n");
        content.append("Content-Type: application/octet-stream\r\n\r\n");

        outputStream.write(content.toString().getBytes());

        FileInputStream in = new FileInputStream(appFile);

        byte[] b = new byte[1024];
        int i = 0;
        while ((i = in.read(b)) != -1) {
            outputStream.write(b, 0, i);
        }
        in.close();

        outputStream.write(("\r\n------" + BOUNDARYSTR + "--\r\n").getBytes());

        byte[] bytes = outputStream.toByteArray();

        outputStream.close();

        HttpUtils.ProxyInfo proxyInfo = HttpUtils.setProxyCfg(proxyHost, proxyPort, proxyUserName, proxyPassword);

        HttpResponse response = HttpUtils.post(proxyInfo, uploadUrl, headers, bytes);


        if (response != null && response.getJsonObject() != null) {
            json = response.getJsonObject().toJSONString();
        }
        return json;
    }


    //Login to MC
    public String loginToMC() throws HttpConnectionException {

        JSONObject returnObject = new JSONObject();

        Map<String, String> headers = new HashMap<String, String>();
        headers.put(ACCEPT, "application/json");
        headers.put(CONTENT_TYPE, "application/json;charset=UTF-8");

        JSONObject sendObject = new JSONObject();
        sendObject.put("name", _userName);
        sendObject.put("password", _password);
        sendObject.put("accountName", "default");


        HttpUtils.ProxyInfo proxyInfo = null;

        if (proxyHost != null && proxyPort != null) {
            proxyInfo = HttpUtils.setProxyCfg(proxyHost, proxyPort, proxyUserName, proxyPassword);
        }

        HttpResponse response = HttpUtils.post(proxyInfo, _serverUrl + LOGIN_URL, headers, sendObject.toJSONString().getBytes());

        if (response != null && response.getResponseCode() != null) {
            returnObject.put("code", response.getResponseCode());
        }

        if (response != null && response.getHeaders() != null) {
            Map<String, List<String>> headerFields = response.getHeaders();
            List<String> hp4mSecretList = headerFields.get(LOGIN_SECRET);
            String hp4mSecret = null;
            if (hp4mSecretList != null && hp4mSecretList.size() != 0) {
                hp4mSecret = hp4mSecretList.get(0);
            }
            List<String> setCookieList = headerFields.get(SET_COOKIE);
            String setCookie = null;
            if (setCookieList != null && setCookieList.size() != 0) {
                setCookie = setCookieList.get(0);
            }

            String jsessionId = getJSESSIONID(setCookie);

            if (hp4mSecret == null || jsessionId == null) {
                throw new HttpConnectionException();
            }


            returnObject.put(JSESSIONID, jsessionId);
            returnObject.put(LOGIN_SECRET, hp4mSecret);
            returnObject.put(COOKIE, JSESSIONID + EQUAL + jsessionId);
        }

        return returnObject.toJSONString();
    }

    //create one temp job
    public String createTempJob() throws HttpConnectionException {
        String json = null;
        String hp4mSecret = null;
        String jsessionId = null;

        String loginJson = loginToMC();
        try {
            if (loginJson != null) {
                JSONObject jsonObject = (JSONObject) JSONValue.parseStrict(loginJson);
                hp4mSecret = (String) jsonObject.get(LOGIN_SECRET);
                jsessionId = (String) jsonObject.get(JSESSIONID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean b = CommonUtils.doCheck(hp4mSecret, jsessionId);

        if (b) {

            Map<String, String> headers = new HashMap<String, String>();
            headers.put(LOGIN_SECRET, hp4mSecret);
            headers.put(COOKIE, JSESSIONID + EQUAL + jsessionId);

            HttpUtils.ProxyInfo proxyInfo = null;
            if (proxyHost != null && proxyPort != null) {
                proxyInfo = HttpUtils.setProxyCfg(proxyHost, proxyPort, proxyUserName, proxyPassword);
            }

            HttpResponse response = HttpUtils.get(proxyInfo, _serverUrl + CREATE_JOB_URL, headers, null);

            if (response != null && response.getJsonObject() != null) {
                json = response.getJsonObject().toJSONString();
            }

        }
        return json;
    }

    //get one job by id
    public JSONObject getJobById(String jobUUID) throws HttpConnectionException {
        JSONObject jobJsonObject = null;
        String hp4mSecret = null;
        String jsessionId = null;

        String loginJson = loginToMC();
        try {
            if (loginJson != null) {
                JSONObject jsonObject = (JSONObject) JSONValue.parseStrict(loginJson);
                hp4mSecret = (String) jsonObject.get(LOGIN_SECRET);
                jsessionId = (String) jsonObject.get(JSESSIONID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean b = CommonUtils.doCheck(jobUUID, hp4mSecret, jsessionId);

        if (b) {

            Map<String, String> headers = new HashMap<String, String>();
            headers.put(LOGIN_SECRET, hp4mSecret);
            headers.put(COOKIE, JSESSIONID + EQUAL + jsessionId);

            HttpUtils.ProxyInfo proxyInfo = null;
            if (proxyHost != null && proxyPort != null) {
                proxyInfo = HttpUtils.setProxyCfg(proxyHost, proxyPort, proxyUserName, proxyPassword);
            }
            HttpResponse response = HttpUtils.get(proxyInfo, _serverUrl + GET_JOB_URL + jobUUID, headers, null);

            if (response != null && response.getJsonObject() != null) {
                jobJsonObject = response.getJsonObject();
            }
        }

        return jobJsonObject;
    }

    //parse one job.and get the data we want
    public String getJobJSONData(String jobUUID) throws HttpConnectionException {
        JSONObject jobJSON = getJobById(jobUUID);

        JSONObject returnJSON = new JSONObject();

        JSONObject dataJSON = null;
        if (jobJSON != null) {
            dataJSON = (JSONObject) jobJSON.get("data");
        }

        //Device Capabilities

        if (dataJSON != null) {
            JSONObject returnDeviceCapabilityJSON = new JSONObject();

            JSONObject detailJSON = (JSONObject) dataJSON.get("capableDeviceFilterDetails");
            if (detailJSON != null) {
                String osType = (String) detailJSON.get("platformName");
                String osVersion = (String) detailJSON.get("platformVersion");
                String manufacturerAndModel = (String) detailJSON.get("deviceName");
                String targetLab = (String) detailJSON.get("source");

                returnDeviceCapabilityJSON.put("OS", osType + osVersion);
                returnDeviceCapabilityJSON.put("manufacturerAndModel", manufacturerAndModel);
                returnDeviceCapabilityJSON.put("targetLab", targetLab);
            }

            JSONObject returnDeviceJSON = new JSONObject();
            //specific device
            JSONArray devices = (JSONArray) dataJSON.get("devices");

            if (devices != null) {
                JSONObject deviceJSON = (JSONObject) devices.get(0);
                if (deviceJSON != null) {
                    String deviceID = deviceJSON.getAsString("deviceID");
                    String osType = deviceJSON.getAsString("osType");
                    String osVersion = deviceJSON.getAsString("osVersion");
                    String manufacturerAndModel = deviceJSON.getAsString("model");

                    returnDeviceJSON.put("deviceId", deviceID);
                    returnDeviceJSON.put("OS", osType + " " + osVersion);
                    returnDeviceJSON.put("manufacturerAndModel", manufacturerAndModel);
                }
            }


            //Applications under test
            JSONArray returnExtraJSONArray = new JSONArray();
            JSONArray extraAppJSONArray = (JSONArray) dataJSON.get("extraApps");

            if (extraAppJSONArray != null) {
                Iterator<Object> iterator = extraAppJSONArray.iterator();

                while (iterator.hasNext()) {

                    JSONObject extraAPPJSON = new JSONObject();

                    JSONObject nextJSONObject = (JSONObject) iterator.next();
                    String extraAppName = (String) nextJSONObject.get("name");
                    Boolean instrumented = (Boolean) nextJSONObject.get("instrumented");

                    extraAPPJSON.put("extraAppName", extraAppName);
                    extraAPPJSON.put("instrumented", instrumented ? "Packaged" : "Not Packaged");

                    returnExtraJSONArray.add(extraAPPJSON);
                }
            }

            //Test Definitions
            JSONObject returnDefinitionJSON = new JSONObject();

            JSONObject applicationJSON = (JSONObject) dataJSON.get("application");

            if (applicationJSON != null) {
                String launchApplicationName = (String) applicationJSON.get("name");
                Boolean instrumented = (Boolean) applicationJSON.get("instrumented");

                returnDefinitionJSON.put("launchApplicationName", launchApplicationName);
                returnDefinitionJSON.put("instrumented", instrumented ? "Packaged" : "Not Packaged");
            }

            //Device metrics,Install Restart
            String headerStr = (String) dataJSON.get("header");
            JSONObject headerJSON = parseJSONString(headerStr);

            if (headerJSON != null) {
                JSONObject configurationJSONObject = (JSONObject) headerJSON.get("configuration");
                Boolean restart = (Boolean) configurationJSONObject.get("restartApp");
                Boolean install = (Boolean) configurationJSONObject.get("installAppBeforeExecution");
                Boolean uninstall = (Boolean) configurationJSONObject.get("deleteAppAfterExecution");

                StringBuffer sb = new StringBuffer("");

                if (restart) {
                    sb.append("Restart;");
                }

                if (install) {
                    sb.append("Install;");
                }

                if (uninstall) {
                    sb.append("Uninstall;");
                }

                JSONObject collectJSON = (JSONObject) headerJSON.get("collect");
                StringBuffer deviceMetricsSb = new StringBuffer("");

                //device metrics
                if (collectJSON != null) {
                    Boolean useCPU = (Boolean) collectJSON.get("cpu");
                    Boolean useMemory = (Boolean) collectJSON.get("memory");
                    Boolean useLogs = (Boolean) collectJSON.get("logs");
                    Boolean useScreenshot = (Boolean) collectJSON.get("screenshot");
                    Boolean useFreeMemory = (Boolean) collectJSON.get("freeMemory");

                    if (useCPU) {
                        deviceMetricsSb.append("CPU;");
                    }

                    if (useMemory) {
                        deviceMetricsSb.append("Memory;");
                    }

                    if (useLogs) {
                        deviceMetricsSb.append("Log;");
                    }
                    if (useScreenshot) {
                        deviceMetricsSb.append("Screenshot;");
                    }
                    if (useFreeMemory) {
                        deviceMetricsSb.append("FreeMomery;");
                    }
                }


                returnDefinitionJSON.put("autActions", removeLastSemicolon(sb));
                returnDefinitionJSON.put("deviceMetrics", removeLastSemicolon(deviceMetricsSb));

            }

            returnJSON.put("deviceCapability", returnDeviceCapabilityJSON);
            returnJSON.put("extraApps", returnExtraJSONArray);
            returnJSON.put("definitions", returnDefinitionJSON);
            returnJSON.put("jobUUID", jobUUID);
            returnJSON.put("specificDevice", returnDeviceJSON);
        }

        return returnJSON.toJSONString();
    }

    public String removeLastSemicolon(StringBuffer sb) {
        int len = sb.length();
        if (len > 0) {
            sb = sb.delete(len - 1, len);
            return sb.toString();
        }
        return sb.toString();
    }

    public JSONObject parseJSONString(String jsonString) {
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) JSONValue.parseStrict(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private String getJSESSIONID(String setCookie) {
        String id = null;
        String[] cookies = setCookie.split(SPLIT_COMMA);
        for (int i = 0; i < cookies.length; i++) {
            if (cookies[i].contains(JSESSIONID)) {
                int index = cookies[i].indexOf(EQUAL);
                id = cookies[i].substring(index + 1);
                break;
            }
        }
        return id;
    }


    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public void setProxyUserName(String proxyUserName) {
        this.proxyUserName = proxyUserName;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    private String checkUrl(String serverUrl) {
        if (serverUrl != null) {
            if (serverUrl.endsWith("/")) {
                int index = serverUrl.lastIndexOf("/");
                serverUrl = serverUrl.substring(0, index);
                return serverUrl;
            }

        }
        return serverUrl;
    }


}
