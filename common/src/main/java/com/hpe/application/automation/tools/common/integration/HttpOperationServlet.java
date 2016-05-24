package com.hpe.application.automation.tools.common.integration;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created with IntelliJ IDEA.
 * User: yanghanx
 * Date: 4/22/16
 * Time: 9:18 AM
 */
public class HttpOperationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String method = req.getParameter("method");

        //mobile center
        String serverUrl = req.getParameter("mcServerURLInput");
        String userName = req.getParameter("mcUserNameInput");
        String password = req.getParameter("mcPasswordInput");

        //proxy
        String proxyAddress = req.getParameter("proxyAddress");
        String proxyUserName = req.getParameter("proxyUserName");
        String proxyPassword = req.getParameter("proxyPassword");


        String msg = "";
        JobOperation operation = new JobOperation(serverUrl, userName, password, proxyAddress, proxyUserName, proxyPassword);

        if (CommonUtils.doCheck(method, serverUrl, userName, password)) {

            try {

                if ("loginToMC".equals(method)) {
                    msg = operation.loginToMC();
                } else if ("createTempJob".equals(method)) {
                    msg = operation.createTempJob();
                } else if ("getJobJSONData".equals(method)) {
                    String jobUUID = req.getParameter("jobUUID");
                    msg = operation.getJobJSONData(jobUUID);
                }

            } catch (Exception e) {

            }
        }
        writeJSON(resp, msg);

    }

    private void writeJSON(HttpServletResponse resp, String info) throws ServletException, IOException {
        PrintWriter writer = null;
        try {
            writer = resp.getWriter();

            writer.write(info);

            writer.flush();
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }


}
