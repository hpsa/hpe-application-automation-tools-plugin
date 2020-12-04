/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2021 Micro Focus or one of its affiliates.
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

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.InteropServices;

namespace HpToolsLauncher
{
    public class ConsoleQuickEdit
    {
        const uint ENABLE_QUICK_EDIT_FLAG = 0x0040;

        // STD_INPUT_HANDLE (DWORD): -10 is the standard input device.
        const int STD_INPUT_HANDLE = -10;

        [DllImport("kernel32.dll", SetLastError = true)]
        static extern IntPtr GetStdHandle(int nStdHandle);

        [DllImport("kernel32.dll")]
        static extern bool GetConsoleMode(IntPtr hConsoleHandle, out uint lpMode);

        [DllImport("kernel32.dll")]
        static extern bool SetConsoleMode(IntPtr hConsoleHandle, uint dwMode);

        private static bool SetNewConsoleMode(IntPtr consoleHandle, uint consoleMode)
        {
// set the new mode
            if (!SetConsoleMode(consoleHandle, consoleMode))
            {
                // ERROR: Unable to set console mode
                return false;
            }
            return true;
        }

        public static bool Enable()
        {
            IntPtr consoleHandle = GetStdHandle(STD_INPUT_HANDLE);
            // get current console mode
            uint consoleMode;
            if (!GetCurrentConsoleMode(consoleHandle, out consoleMode))
            {
                return false;
            }
            consoleMode &= ENABLE_QUICK_EDIT_FLAG;
            if (!SetNewConsoleMode(consoleHandle, consoleMode))
            {
                return false;
            }
            return true;
        }


        public static bool Disable()
        {
            IntPtr consoleHandle = GetStdHandle(STD_INPUT_HANDLE);
            //Get current console mode
            uint consoleMode;
            if (!GetCurrentConsoleMode(consoleHandle, out consoleMode))
            {
                return false;
            }
            consoleMode &= ~ENABLE_QUICK_EDIT_FLAG;
            //Set new console mode
            if (!SetNewConsoleMode(consoleHandle, consoleMode))
            {
                return false;
            }
            return true;
        }

        private static bool GetCurrentConsoleMode(IntPtr consoleHandle, out uint consoleMode)
        {
            if (!GetConsoleMode(consoleHandle, out consoleMode))
            {
                // ERROR: Unable to get console mode.
                return false;
            }
            return true;
        }
    }
}