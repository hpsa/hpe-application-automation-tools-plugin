// (c) Copyright 2012 Hewlett-Packard Development Company, L.P. 
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.hp.application.automation.tools.model;

import hudson.EnvVars;
import hudson.util.Secret;
import hudson.util.VariableResolver;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

public class RunFromFileSystemModel {

	private String fsTests;
	private String fsTimeout;
	private String controllerPollingInterval = "30";
	private String perScenarioTimeOut = "10";
	private String ignoreErrorStrings;
	private String mcServerName;
	private String fsUserName;
	private Secret fsPassword;
	private String fsAppPath;
	private String fsAppParamName;

    private final static String JSESSIONID = "JSESSIONID";
    private final static String HPMCSECRET = "x-hp4msecret";
    private final static String BOUNDARYSTR = "randomstring";
    private final static String DATA = "data";
    private final static String IDENTIFIER = "identifier";
    private final static String GETAPPID = "/rest/apps/getAppById/";
    private final static String MC_LOGIN = "/rest/client/login";
    private final static String APP_UPLOAD = "/rest/apps/upload";
    private final static String CONTENT_TYPE = "Content-type";
    private final static String ACCEPT = "Accept";
    private final static String CONTENT_TYPE_VALUE = "application/json; charset=UTF-8";
    private final static String METHOD_GET = "GET";
    private final static String METHOD_POST = "POST";
    private final static String CONTENT_TYPE_DOWNLOAD_VALUE = "multipart/form-data; boundary=----";
    private final static String FILENAME = "filename";
    private final static String JSON = "application/json";
    private final static String MC_USERNAME = "name";
    private final static String MC_PASSWORD = "password";
    private final static String MC_ACCOUNT = "accountName";
    private final static String MC_ACCOUNT_VALUE = "default";
    private final static String SETCOOKIE = "Set-Cookie";
    private final static String COOKIE = "Cookie";
    private final static String SEMICOLON = ";";
    private final static String EQUAL = "=";

    private Map<String,String> headers = null;

	@DataBoundConstructor
	public RunFromFileSystemModel(String fsTests, String fsTimeout, String controllerPollingInterval,String perScenarioTimeOut, String ignoreErrorStrings, String mcServerName, String fsUserName, String fsPassword, String fsAppPath, String fsAppParamName) {

		this.fsTests = fsTests;

		if (!this.fsTests.contains("\n")) {
			this.fsTests += "\n";
		}

		this.fsTimeout = fsTimeout;
		
		
		this.perScenarioTimeOut = perScenarioTimeOut;
		this.controllerPollingInterval = controllerPollingInterval;
		this.ignoreErrorStrings = ignoreErrorStrings;

        this.mcServerName = mcServerName;
        this.fsUserName = fsUserName;
        this.fsPassword = Secret.fromString(fsPassword);
        this.fsAppPath = fsAppPath;
        this.fsAppParamName = fsAppParamName;

	}


	public String getFsTests() {
		return fsTests;
	}

	public String getFsTimeout() {
		return fsTimeout;
	}

    public String getMcServerName() {
        return mcServerName;
    }

    public String getFsUserName() {
        return fsUserName;
    }

    public String getFsPassword() {
        return fsPassword.getPlainText();
    }

    public String getFsAppPath() {
        return fsAppPath;
    }

    public String getFsAppParamName() {
        return fsAppParamName;
    }

	
	/**
	 * @return the controllerPollingInterval
	 */
	public String getControllerPollingInterval() {
		return controllerPollingInterval;
	}

	/**
	 * @param controllerPollingInterval the controllerPollingInterval to set
	 */
	public void setControllerPollingInterval(String controllerPollingInterval) {
		this.controllerPollingInterval = controllerPollingInterval;
	}

	/**
	 * @return the ignoreErrorStrings
	 */
	public String getIgnoreErrorStrings() {
		return ignoreErrorStrings;
	}


	/**
	 * @param ignoreErrorStrings the ignoreErrorStrings to set
	 */
	public void setIgnoreErrorStrings(String ignoreErrorStrings) {
		this.ignoreErrorStrings = ignoreErrorStrings;
	}



	/**
	 * @return the perScenarioTimeOut
	 */
	public String getPerScenarioTimeOut() {
		return perScenarioTimeOut;
	}

	/**
	 * @param perScenarioTimeOut the perScenarioTimeOut to set
	 */
	public void setPerScenarioTimeOut(String perScenarioTimeOut) {
		this.perScenarioTimeOut = perScenarioTimeOut;
	}

	public Properties getProperties(EnvVars envVars,
			VariableResolver<String> varResolver) {
		return CreateProperties(envVars, varResolver);
	}

	public Properties getProperties() {
		return CreateProperties(null, null);
	}

    public String getAppIndentifier(String mcServerUrl) {
        JSONObject data = null;
        JSONObject obj = null;
        String identifier = "";
        if(StringUtils.isEmpty(mcServerUrl) || StringUtils.isEmpty(fsUserName) || StringUtils.isEmpty(fsAppParamName) || StringUtils.isEmpty(fsAppPath)){
            return identifier;
        }
        obj = getApplication(mcServerUrl);
        if(obj != null){
            try {
                data = (JSONObject) JSONValue.parseStrict(obj.getAsString(DATA));
                identifier = data.getAsString(IDENTIFIER);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return identifier;
    }

	private Properties CreateProperties(EnvVars envVars,
			VariableResolver<String> varResolver) {
		Properties props = new Properties();
		
		if (!StringUtils.isEmpty(this.fsTests)) {
			String expandedFsTests = envVars.expand(fsTests);
			String[] testsArr = expandedFsTests.replaceAll("\r", "").split("\n");

			int i = 1;

			for (String test : testsArr) {
				props.put("Test" + i, test);
				i++;
			}
		} else {
			props.put("fsTests", "");
		}

		
		if (StringUtils.isEmpty(fsTimeout)){
			props.put("fsTimeout", "-1");	
		}
		else{
			props.put("fsTimeout", "" + fsTimeout);
		}
		
		
		if (StringUtils.isEmpty(controllerPollingInterval)){
			props.put("controllerPollingInterval", "30");
		}
		else{
			props.put("controllerPollingInterval", "" + controllerPollingInterval);
		}
		
		if (StringUtils.isEmpty(perScenarioTimeOut)){
			props.put("PerScenarioTimeOut", "10");
		}
		else{
			props.put("PerScenarioTimeOut", ""+ perScenarioTimeOut);
		}
		
		if (!StringUtils.isEmpty(ignoreErrorStrings.replaceAll("\\r|\\n", ""))){
			props.put("ignoreErrorStrings", ""+ignoreErrorStrings.replaceAll("\r", ""));
		}


		return props;
	}

    private JSONObject getApplication(String mcServerUrl){
        URL url= null;
        JSONObject obj = null;
        String appUUID = getAppUUID(mcServerUrl);
        String appURL = mcServerUrl + GETAPPID + appUUID;
        try {
            url = new URL(appURL);
            HttpURLConnection conn=(HttpURLConnection) url.openConnection();
            conn.setRequestMethod(METHOD_GET);
            conn.setDoOutput(true);
            conn.setRequestProperty(CONTENT_TYPE, CONTENT_TYPE_VALUE);
            for(String key : headers.keySet()){
                conn.setRequestProperty(key, headers.get(key));
            }
            conn.connect();
            if(conn.getResponseCode() == 200){
                obj = getResponse(conn.getInputStream());
            }
            conn.disconnect();
        }catch(Exception e){
            e.printStackTrace();
        }
        return obj;
    }

    private String getAppUUID(String mcServerUrl){
        String appUUID = "";
        String uploadAppUrl = mcServerUrl + APP_UPLOAD;
        URL url= null;
        try {
            File file = new File(fsAppPath);
            url = new URL(uploadAppUrl);
            HttpURLConnection conn=(HttpURLConnection) url.openConnection();
            conn.setRequestMethod(METHOD_POST);
            conn.setDoOutput(true);
            headers = getHeaders(mcServerUrl);
            for(String key : headers.keySet()){
                conn.setRequestProperty(key, headers.get(key));
            }
            conn.setRequestMethod(METHOD_POST);
            conn.setDoOutput(true);
            conn.setRequestProperty(CONTENT_TYPE, CONTENT_TYPE_DOWNLOAD_VALUE + BOUNDARYSTR);
            conn.setRequestProperty(FILENAME, file.getName());
            conn.connect();
            OutputStream out = new DataOutputStream(conn.getOutputStream());
            StringBuffer content = new StringBuffer();
            content.append("\r\n").append("------").append(BOUNDARYSTR).append("\r\n");
            content.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n");
            content.append("Content-Type: application/octet-stream\r\n\r\n");
            out.write(content.toString().getBytes());
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            int bytes = 0;
            byte[] bufferOut = new byte[1024];
            while ((bytes = in.read(bufferOut)) != -1) {
                out.write(bufferOut, 0, bytes);
            }
            in.close();
            out.write(("\r\n------" + BOUNDARYSTR + "--\r\n").getBytes());
            out.flush();
            out.close();
            int code = conn.getResponseCode();
            if(code == HttpURLConnection.HTTP_OK){
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer res = new StringBuffer();
                String line;
                while ((line = reader.readLine()) != null){
                    res.append(line);
                }
                JSONObject obj  = (JSONObject) JSONValue.parseStrict(res.toString());
                appUUID = obj.getAsString(DATA);
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appUUID;
    }

    private Map<String,String> getHeaders(String mcServerUrl){
        String loginUrl = mcServerUrl + MC_LOGIN;
        headers = new HashMap<String,String>();
        URL url= null;
        try {
            url = new URL(loginUrl);
            JSONObject obj = new JSONObject();
            HttpURLConnection conn=(HttpURLConnection) url.openConnection();
            conn.setRequestMethod(METHOD_POST);
            conn.setDoOutput(true);
            conn.setRequestProperty(ACCEPT, JSON);
            conn.setRequestProperty(CONTENT_TYPE,CONTENT_TYPE_VALUE);
            obj.put(MC_USERNAME, fsUserName);
            obj.put(MC_PASSWORD, getFsPassword());
            obj.put(MC_ACCOUNT, MC_ACCOUNT_VALUE);
            OutputStream out = conn.getOutputStream();
            out.write(obj.toString().getBytes());
            out.flush();
            out.close();
            conn.connect();
            if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                String mcSecret = conn.getHeaderField(HPMCSECRET);
                String setCookie = conn.getHeaderField(SETCOOKIE);
                String[] cookies = setCookie.split(SEMICOLON);
                for(int i = 0; i<cookies.length; i++){
                    String cookie = cookies[i];
                    if(cookie.contains(JSESSIONID)){
                        int equalIndex = cookie.indexOf(EQUAL);
                        String cookieValue = cookie.substring(equalIndex + 1);
                        headers.put(JSESSIONID,cookieValue);
                        headers.put(COOKIE, JSESSIONID + EQUAL + cookieValue);
                        break;
                    }
                }
                headers.put(HPMCSECRET,mcSecret);
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return headers;
    }

    private JSONObject getResponse(InputStream is){
        JSONObject obj = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuffer res = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null){
                res.append(line);
            }
            obj  = (JSONObject)JSONValue.parseStrict(res.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }
}
