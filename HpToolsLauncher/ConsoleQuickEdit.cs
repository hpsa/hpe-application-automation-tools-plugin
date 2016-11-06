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