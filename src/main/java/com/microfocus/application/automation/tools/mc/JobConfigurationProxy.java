/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2023 Micro Focus or one of its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.mc;

import com.microfocus.application.automation.tools.model.AuthModel;
import com.microfocus.application.automation.tools.model.ProxySettings;
import com.microfocus.application.automation.tools.sse.common.StringUtils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * communicate with MC servers, login to MC, upload application to MC server, create job, get job details.
 */
public class JobConfigurationProxy {

    private static final String TOKEN = "token";
    private static final String EXTRA_APPS = "extraApps";
    private static final String INSTRUMENTED = "instrumented";
    private static final String PACKAGED = "Packaged";
    private static final String NOT_PACKAGED = "Not Packaged";
    private static JobConfigurationProxy instance = null;

    private JobConfigurationProxy() {
    }

    public static JobConfigurationProxy getInstance() {
        if (instance == null) {
            instance = new JobConfigurationProxy();
        }
        return instance;
    }

    //Login to MC
    public JSONObject loginToMC(String mcUrl, AuthModel authModel, ProxySettings proxy) {

        JSONObject returnObject = new JSONObject();
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put(Constants.ACCEPT, "application/json");
            headers.put(Constants.CONTENT_TYPE, "application/json;charset=UTF-8");

            JSONObject sendObject = new JSONObject();
            if (null == proxy) {
                proxy = new ProxySettings();
            }
            HttpResponse response;
            if ("base".equals(authModel.getValue())) {
                String tempUsername = authModel.getMcUserName();
                if (!StringUtils.isNullOrEmpty(authModel.getMcTenantId())) {
                    tempUsername += "#" + authModel.getMcTenantId();
                }
                sendObject.put("name", tempUsername);
                sendObject.put("password", authModel.getMcPassword());
                sendObject.put("accountName", "default");
                response = HttpUtils.doPost(HttpUtils.setProxyCfg(proxy.getFsProxyAddress(), proxy.getFsProxyUserName(), proxy.getFsProxyPassword()), mcUrl + Constants.LOGIN_URL, headers, sendObject.toJSONString().getBytes());
            } else {
                headers.put(Constants.ACCEPT, "application/json");
                headers.put(Constants.CONTENT_TYPE, "application/json;charset=UTF-8");
                if (Oauth2TokenUtil.validate(authModel.getMcExecToken())) {
                    sendObject.put("client", Oauth2TokenUtil.getClient());
                    sendObject.put("secret", Oauth2TokenUtil.getSecret());
                    sendObject.put("tenant", Oauth2TokenUtil.getTenant());
                    response = HttpUtils.doPost(HttpUtils.setProxyCfg(proxy.getFsProxyAddress(), proxy.getFsProxyUserName(), proxy.getFsProxyPassword()), mcUrl + Constants.LOGIN_URL_OAUTH, headers, sendObject.toJSONString().getBytes());
                } else {
                    System.out.println("ERROR:: oauth token is invalid.");
                    return returnObject;
                }
            }
            return parseLoginResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnObject;
    }

    private JSONObject parseLoginResponse(HttpResponse response) {
        if (response == null || response.getHeaders() == null) {
            return null;
        }
        Map<String, List<String>> headerFields = response.getHeaders();
        List<String> hp4mSecretList = headerFields.get(Constants.LOGIN_SECRET);
        JSONObject returnObject = new JSONObject();
        if (hp4mSecretList != null && !hp4mSecretList.isEmpty()) {
            setToRespJSON(returnObject, Constants.LOGIN_SECRET, hp4mSecretList.get(0));
        }
        List<String> setCookieList = headerFields.get(Constants.SET_COOKIE);
        if (setCookieList == null || setCookieList.isEmpty()) {
            return returnObject;
        }
        StringBuilder cookies = new StringBuilder();
        String setCookie = setCookieList.get(0);
        String tenantCookie = null;
        String oauth2Cookie = null;

        for (String str : setCookieList) {
            if (str.contains(Constants.JSESSIONID) && str.startsWith(Constants.JSESSIONID)) {
                setCookie = str;
                cookies.append(str).append(';');
            } else if (str.contains(Constants.TENANT_COOKIE) && str.startsWith(Constants.TENANT_COOKIE)) {
                tenantCookie = str;
                cookies.append(str).append(';');
            } else if (str.contains(Constants.OAUTH2_COOKIE_KEY) && str.startsWith(Constants.OAUTH2_COOKIE_KEY)) {
                oauth2Cookie = str;
            }
        }
        setToRespJSON(returnObject, Constants.JSESSIONID, getCookieValue(setCookie, Constants.JSESSIONID));
        setToRespJSON(returnObject, Constants.TENANT_COOKIE, getCookieValue(tenantCookie, Constants.TENANT_COOKIE));
        setToRespJSON(returnObject, Constants.OAUTH2_COOKIE_KEY, getCookieValue(oauth2Cookie, Constants.OAUTH2_COOKIE_KEY));
        setToRespJSON(returnObject, Constants.COOKIE, cookies.toString());

        return returnObject;
    }

    //upload app to MC
    public JSONObject upload(String mcUrl, AuthModel authModel, ProxySettings proxy, String appPath) throws IOException {
        File appFile = new File(appPath);

        String uploadUrl = mcUrl + Constants.APP_UPLOAD;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        StringBuilder content = new StringBuilder();
        content.append("\r\n").append("------").append(Constants.BOUNDARYSTR).append("\r\n");
        content.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + appFile.getName() + "\"\r\n");
        content.append("Content-Type: application/octet-stream\r\n\r\n");

        outputStream.write(content.toString().getBytes());

        try (FileInputStream in = new FileInputStream(appFile)) {
            byte[] b = new byte[1024];
            int i = 0;
            while ((i = in.read(b)) != -1) {
                outputStream.write(b, 0, i);
            }
        }

        outputStream.write(("\r\n------" + Constants.BOUNDARYSTR + "--\r\n").getBytes());

        byte[] bytes = outputStream.toByteArray();

        outputStream.close();
        if (null == proxy) {
            proxy = new ProxySettings();
        }
        Map<String, String> headers = new HashMap<>();
        JSONObject loginJson = loginToMC(mcUrl, authModel, proxy);
        if (loginJson != null) {
            String hp4mSecret = (String) loginJson.get(Constants.LOGIN_SECRET);
            String jsessionId = (String) loginJson.get(Constants.JSESSIONID);
            headers.put(Constants.LOGIN_SECRET, hp4mSecret);
            String cookies = Constants.JESEEIONEQ + jsessionId;
            if (TOKEN.equals(authModel.getValue())) {
                String oauth = (String) loginJson.get(Constants.OAUTH2_COOKIE_KEY);
                if (!StringUtils.isNullOrEmpty(oauth)) {
                    cookies += (";" + Constants.OAUTH2_COOKIE_KEY + "=" + (String) loginJson.get(Constants.OAUTH2_COOKIE_KEY));
                } else {
                    System.out.println("ERROR:: loginToMC failed with null oauth cookie.");
                }
            }
            headers.put(Constants.COOKIE, cookies);
        }

        headers.put(Constants.CONTENT_TYPE, Constants.CONTENT_TYPE_DOWNLOAD_VALUE + Constants.BOUNDARYSTR);
        headers.put(Constants.FILENAME, appFile.getName());

        HttpUtils.ProxyInfo proxyInfo = HttpUtils.setProxyCfg(proxy.getFsProxyAddress(), proxy.getFsProxyUserName(), proxy.getFsProxyPassword());
        HttpResponse response = HttpUtils.doPost(proxyInfo, uploadUrl, headers, bytes);

        if (response != null && response.getJsonObject() != null) {
            return response.getJsonObject();
        }
        return null;
    }

    //create one temp job
    public String createTempJob(String mcUrl, AuthModel authModel, ProxySettings proxy) {
        try {
            JSONObject loginJson = loginToMC(mcUrl, authModel, proxy);
            if (loginJson == null) {
                return null;
            }
            String hp4mSecret = (String) loginJson.get(Constants.LOGIN_SECRET);
            String jsessionId = (String) loginJson.get(Constants.JSESSIONID);

            if (thereIsNoArgumentNullOrEmpty(hp4mSecret, jsessionId)) {
                Map<String, String> headers = new HashMap<>();
                headers.put(Constants.LOGIN_SECRET, hp4mSecret);
                StringBuilder cookies = new StringBuilder(Constants.JESEEIONEQ).append(jsessionId);
                if (TOKEN.equals(authModel.getValue())) {
                    cookies.append(';').append(Constants.OAUTH2_COOKIE_KEY).append('=').append((String) loginJson.get(Constants.OAUTH2_COOKIE_KEY));
                }
                headers.put(Constants.COOKIE, cookies.toString());
                HttpUtils.ProxyInfo proxyInfo = proxy == null ? null : HttpUtils.setProxyCfg(proxy.getFsProxyAddress(), proxy.getFsProxyUserName(), proxy.getFsProxyPassword());
                HttpResponse response = HttpUtils.doGet(proxyInfo, mcUrl + Constants.CREATE_JOB_URL, headers, null);

                if (response != null && response.getJsonObject() != null) {
                    JSONObject job = response.getJsonObject();
                    if (job != null && job.get("data") != null) {
                        JSONObject data = (JSONObject) job.get("data");
                        return data.getAsString("id");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //get one job by id
    public JSONObject getJobById(String mcUrl, AuthModel authModel, ProxySettings proxy, String jobUUID) {
        JSONObject jobJsonObject = null;

        try {
            JSONObject loginJson = loginToMC(mcUrl, authModel, proxy);
            if (loginJson == null) {
                return null;
            }
            String hp4mSecret = (String) loginJson.get(Constants.LOGIN_SECRET);
            String jsessionId = (String) loginJson.get(Constants.JSESSIONID);

            if (thereIsNoArgumentNullOrEmpty(jobUUID, hp4mSecret, jsessionId)) {
                Map<String, String> headers = new HashMap<>();
                headers.put(Constants.LOGIN_SECRET, hp4mSecret);
                String cookies = Constants.JESEEIONEQ + jsessionId;
                if (TOKEN.equals(authModel.getValue())) {
                    cookies += (";" + Constants.OAUTH2_COOKIE_KEY + "=" + (String) loginJson.get(Constants.OAUTH2_COOKIE_KEY));
                }
                headers.put(Constants.COOKIE, cookies);
                HttpUtils.ProxyInfo proxyInfo = proxy == null ? null : HttpUtils.setProxyCfg(proxy.getFsProxyAddress(), proxy.getFsProxyUserName(), proxy.getFsProxyPassword());
                HttpResponse response = HttpUtils.doGet(proxyInfo, mcUrl + Constants.GET_JOB_UEL + jobUUID, headers, null);

                if (response != null && response.getJsonObject() != null) {
                    jobJsonObject = response.getJsonObject();
                }
                if (jobJsonObject != null) {
                    jobJsonObject = (JSONObject) jobJsonObject.get(Constants.DATA);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return removeIcon(jobJsonObject);
    }

    //parse one job.and get the data we want
    public JSONObject getJobJSONData(String mcUrl, AuthModel authModel, ProxySettings proxy, String jobUUID) {
        JSONObject jobJSON = getJobById(mcUrl, authModel, proxy, jobUUID);

        JSONObject returnJSON = new JSONObject();
        if (jobJSON == null) {
            return returnJSON;
        }
        //Device Capabilities
        JSONObject returnDeviceCapabilityJSON = new JSONObject();

        JSONObject detailJSON = (JSONObject) jobJSON.get("capableDeviceFilterDetails");
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
        JSONArray devices = (JSONArray) jobJSON.get("devices");

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
        StringBuilder extraApps = new StringBuilder();
        JSONArray extraAppJSONArray = (JSONArray) jobJSON.get(EXTRA_APPS);

        if (extraAppJSONArray != null) {
            Iterator<Object> iterator = extraAppJSONArray.iterator();

            while (iterator.hasNext()) {
                JSONObject extraAPPJSON = new JSONObject();

                JSONObject nextJSONObject = (JSONObject) iterator.next();
                String extraAppName = (String) nextJSONObject.get("name");
                Boolean instrumented = (Boolean) nextJSONObject.get(INSTRUMENTED);

                extraAPPJSON.put("extraAppName", extraAppName);
                extraAPPJSON.put(INSTRUMENTED, instrumented ? PACKAGED : NOT_PACKAGED);
                if (extraApps.length() > 1) {
                    extraApps.append(";\n");
                }
                extraApps.append(extraAppName).append("\t\t").append(instrumented ? PACKAGED : NOT_PACKAGED);

                returnExtraJSONArray.add(extraAPPJSON);
            }
        }
        //Test Definitions
        JSONObject returnDefinitionJSON = new JSONObject();

        JSONObject applicationJSON = (JSONObject) jobJSON.get("application");

        if (applicationJSON != null) {
            String launchApplicationName = (String) applicationJSON.get("name");
            Boolean instrumented = (Boolean) applicationJSON.get(INSTRUMENTED);

            returnDefinitionJSON.put("launchApplicationName", launchApplicationName);
            returnDefinitionJSON.put(INSTRUMENTED, instrumented ? PACKAGED : NOT_PACKAGED);
        }

        //Device metrics,Install Restart
        String headerStr = (String) jobJSON.get("header");
        JSONObject headerJSON = parseJSONString(headerStr);
        if (headerJSON != null) {
            JSONObject configurationJSONObject = (JSONObject) headerJSON.get("configuration");
            Boolean restart = (Boolean) configurationJSONObject.get("restartApp");
            Boolean install = (Boolean) configurationJSONObject.get("installAppBeforeExecution");
            Boolean uninstall = (Boolean) configurationJSONObject.get("deleteAppAfterExecution");

            StringBuilder sb = new StringBuilder("");
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
            StringBuilder deviceMetricsSb = new StringBuilder("");
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
        returnJSON.put(EXTRA_APPS, extraApps.toString());
        returnJSON.put("extraApps2", returnExtraJSONArray);
        returnJSON.put("definitions", returnDefinitionJSON);
        returnJSON.put("jobUUID", jobUUID);
        returnJSON.put("deviceJSON", returnDeviceJSON);
        return returnJSON;
    }

    private JSONObject parseJSONString(String jsonString) {
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) JSONValue.parseStrict(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private String getCookieValue(String setCookie, String cookieName) {
        if (StringUtils.isNullOrEmpty(setCookie)) {
            return null;
        }
        String id = null;
        String[] cookies = setCookie.split(Constants.SPLIT_COMMA);
        for (int i = 0; i < cookies.length; i++) {
            if (cookies[i].contains(cookieName)) {
                int index = cookies[i].indexOf(Constants.EQUAL);
                id = cookies[i].substring(index + 1);
                break;
            }
        }
        return id;
    }

    private boolean thereIsNoArgumentNullOrEmpty(String... args) {
        for (String arg : args) {
            if (StringUtils.isNullOrEmpty(arg)) {
                return false;
            }
        }
        return true;
    }

    private String removeLastSemicolon(StringBuilder sb) {
        String result = sb.toString();
        int indexOf = result.lastIndexOf(";");
        if (indexOf > 0) {
            result = result.substring(0, indexOf);
        }
        return result;
    }

    private JSONObject removeIcon(JSONObject jobJSON) {
        if (jobJSON != null) {
            JSONObject applicationJSONObject = (JSONObject) jobJSON.get("application");
            if (applicationJSONObject != null) {
                applicationJSONObject.remove(Constants.ICON);
            }
            JSONArray extArr = (JSONArray) jobJSON.get(EXTRA_APPS);
            if (extArr != null) {
                Iterator<Object> iterator = extArr.iterator();
                while (iterator.hasNext()) {
                    JSONObject extAppJSONObject = (JSONObject) iterator.next();
                    extAppJSONObject.remove(Constants.ICON);
                }
            }
        }
        return jobJSON;
    }

    private void setToRespJSON(JSONObject returnObject, String key, String value) {
        if (thereIsNoArgumentNullOrEmpty(key, value) && null != returnObject)
            returnObject.put(key, value);
    }
}
