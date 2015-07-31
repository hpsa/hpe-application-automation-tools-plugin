package com.hp.application.automation.bamboo.tasks;
/**
 * Created by schernikov on 7/29/2015.
 */
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

public class WindowsRegistry {

    private static final boolean IsX64()
    {
        String arch = System.getProperty("os.arch");
        if(arch == null) {
            return false;
        }
        return arch.contains("64");
    }

    private static final String softwareKey = "SOFTWARE\\";
    private static final String wow6432prefix = "SOFTWARE\\WOW6432NODE\\";
    private static final String GetArchKeyName(String key)
    {
        String keyUpper = key.toUpperCase();
        if(IsX64() && !keyUpper.startsWith(wow6432prefix) && keyUpper.startsWith(softwareKey))
        {
            String newKey = wow6432prefix+key.substring(softwareKey.length());
            return newKey;
        }
        else
        {
            return key;
        }
    }

    public static final String readHKLMString(String key, String value) {
        try {
            String newKey = GetArchKeyName(key);
            String result = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, newKey, value);
            return result;
        }
        catch (Throwable e)
        {
            return "";
        }
    }

}