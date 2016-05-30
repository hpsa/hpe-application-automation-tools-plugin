package com.hpe.application.automation.tools.common.integration;

/**
 * Created with IntelliJ IDEA.
 * User: yanghanx
 * Date: 4/26/16
 * Time: 1:26 PM
 */
public class CommonUtils {


    private CommonUtils(){

    }

    public static boolean doCheck(String... args) {

        for (String arg : args) {
            if (arg == null || arg == "" || arg.length() == 0) {
                return false;
            }
        }
        return true;
    }


}
