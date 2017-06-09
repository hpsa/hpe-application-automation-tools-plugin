package com.hpe.application.automation.tools.sse.common;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */

public class StringUtils {
    
    public static final String NEW_LINE = System.getProperty("line.separator");
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String PATH_SEPARATOR = System.getProperty("path.separator");
    
    public static final String EMPTY_STRING = "";
    public static final String SPACE = " ";
    public static final String PERIOD = ".";
    public static final String TAB = "\t";
    
    public static boolean isNullOrEmpty(String value) {
        
        return (value == null) || (value.length() == 0);
    }
}
