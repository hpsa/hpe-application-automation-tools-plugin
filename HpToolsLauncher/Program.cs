// (c) Copyright 2012 Hewlett-Packard Development Company, L.P. 
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

using System;
using System.Collections.Generic;
using System.Linq;
using HpToolsLauncher.Properties;

namespace HpToolsLauncher
{
    public enum TestStorageType
    {
        Alm,
        FileSystem,
        LoadRunner,
        Unknown
    }

    class Program
    {
        private static readonly Dictionary<string, string> argsDictionary = new Dictionary<string, string>();

        static void Main(string[] args)
        {
            ConsoleWriter.WriteLine(Resources.GeneralStarted);
            ConsoleQuickEdit.Disable();
            if (args.Count() == 0 || args.Contains("/?"))
            {
                ShowHelp();
                return;
            }
            for (int i = 0; i < args.Count(); i = i + 2)
            {
                string key = args[i].StartsWith("-") ? args[i].Substring(1) : args[i];
                string val = i + 1 < args.Count() ? args[i + 1].Trim() : String.Empty;
                argsDictionary[key] = val;
            }
            string paramFileName, runtype;
            string failOnTestFailed = "N";
            argsDictionary.TryGetValue("runtype", out runtype);
            argsDictionary.TryGetValue("paramfile", out paramFileName);
            TestStorageType enmRuntype = TestStorageType.Unknown;

            if (!Enum.TryParse<TestStorageType>(runtype, true, out enmRuntype))
                enmRuntype = TestStorageType.Unknown;

            if (string.IsNullOrEmpty(paramFileName))
            {
                ShowHelp();
                return;
            }
            var apiRunner = new Launcher(failOnTestFailed, paramFileName, enmRuntype);

            apiRunner.Run();
        }

        private static void ShowHelp()
        {
            Console.WriteLine("HPE Automation Tools Command Line Executer");
            Console.WriteLine();
            Console.Write("Usage: HpToolsLauncher.exe");
            Console.Write("  -paramfile ");
            Console.ForegroundColor = ConsoleColor.Cyan;
            Console.Write("<a file in key=value format> ");
            Console.ResetColor();
            Console.WriteLine();
            Console.WriteLine();
            Console.WriteLine("-paramfile is required in for the program to run");
            Console.WriteLine("the parameter file may contain the following fields:");
            Console.WriteLine("\trunType=<Alm/FileSystem/LoadRunner>");
            Console.WriteLine("\talmServerUrl=http://<server>:<port>/qcbin");
            Console.WriteLine("\talmUserName=<user>");
            Console.WriteLine("\talmPassword=<password>");
            Console.WriteLine("\talmDomain=<domain>");
            Console.WriteLine("\talmProject=<project>");
            Console.WriteLine("\talmRunMode=<RUN_LOCAL/RUN_REMOTE/RUN_PLANNED_HOST>");
            Console.WriteLine("\talmTimeout=<-1>/<numberOfSeconds>");
            Console.WriteLine("\talmRunHost=<hostname>");
            Console.WriteLine("\tTestSet<number starting at 1>=<testSet>/<AlmFolder>");
            Console.WriteLine("\tTest<number starting at 1>=<testFolderPath>/<a Path ContainingTestFolders>/<mtbFilePath>");
            Console.WriteLine("* the last two fields may recur more than once with different index numbers");
            Environment.Exit((int)Launcher.ExitCodeEnum.Failed);
        }
    }
}
