/**
 © Copyright 2015 Hewlett Packard Enterprise Development LP

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */
package com.hpe.application.automation.bamboo.tasks;

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