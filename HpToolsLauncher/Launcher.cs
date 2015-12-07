// (c) Copyright 2012 Hewlett-Packard Development Company, L.P. 
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using HpToolsLauncher.Properties;

namespace HpToolsLauncher
{
    public enum CIName
    {
        Hudson,
        Jenkins,
        TFS,
        CCNET,
    }

    public class Launcher
    {
        private IXmlBuilder _xmlBuilder;
        private bool _ciRun = false;
        private readonly string _paramFileName = null;
        private JavaProperties _ciParams = new JavaProperties();
        private TestStorageType _runtype = TestStorageType.Unknown;
        private readonly string _failOnUftTestFailed;
        private static ExitCodeEnum _exitCode = ExitCodeEnum.Passed;
        private static string _dateFormat = "dd/MM/yyyy HH:mm:ss";

        public static string DateFormat
        {
            get { return Launcher._dateFormat; }
            set { Launcher._dateFormat = value; }
        }

        /// <summary>
        /// if running an alm job theses strings are mandatory:
        /// </summary>
        private string[] requiredParamsForQcRun = { "almServerUrl",
                                 "almUserName",
                                 "almPassword",
                                 "almDomain",
                                 "almProject",
                                 "almRunMode",
                                 "almTimeout",
                                 "almRunHost"};

        /// <summary>
        /// a place to save the unique timestamp which shows up in properties/results/abort file names
        /// this timestamp per job run.
        /// </summary>
        public static string UniqueTimeStamp { get; set; }

        public enum ExitCodeEnum
        {
            Passed = 0,
            Failed = 1,
            Unstable = 2,
            Aborted = 3
        }
        /// <summary>
        /// saves the exit code in case we want to run all tests but fail at the end since a file wasn't found
        /// </summary>
        public static ExitCodeEnum ExitCode
        {
            get { return Launcher._exitCode; }
            set { Launcher._exitCode = value; }
        }


        /// <summary>
        /// constructor
        /// </summary>
        /// <param name="failOnTestFailed"></param>
        /// <param name="paramFileName"></param>
        /// <param name="runtype"></param>
        public Launcher(string failOnTestFailed, string paramFileName, TestStorageType runtype)
        {
            _runtype = runtype;
            if (paramFileName != null)
                _ciParams.Load(paramFileName);
            _paramFileName = paramFileName;

            _failOnUftTestFailed = string.IsNullOrEmpty(failOnTestFailed) ? "N" : failOnTestFailed;
        }

        static String secretkey = "EncriptionPass4Java";

        /// <summary>
        /// decrypts strings which were encrypted by Encrypt (in the c# or java code, mainly for qc passwords)
        /// </summary>
        /// <param name="textToDecrypt"></param>
        /// <param name="key"></param>
        /// <returns></returns>
        string Decrypt(string textToDecrypt, string key)
        {
            RijndaelManaged rijndaelCipher = new RijndaelManaged();
            rijndaelCipher.Mode = CipherMode.CBC;
            rijndaelCipher.Padding = PaddingMode.PKCS7;

            rijndaelCipher.KeySize = 0x80;
            rijndaelCipher.BlockSize = 0x80;
            byte[] encryptedData = Convert.FromBase64String(textToDecrypt);
            byte[] pwdBytes = Encoding.UTF8.GetBytes(key);
            byte[] keyBytes = new byte[0x10];
            int len = pwdBytes.Length;
            if (len > keyBytes.Length)
            {
                len = keyBytes.Length;
            }
            Array.Copy(pwdBytes, keyBytes, len);
            rijndaelCipher.Key = keyBytes;
            rijndaelCipher.IV = keyBytes;
            byte[] plainText = rijndaelCipher.CreateDecryptor().TransformFinalBlock(encryptedData, 0, encryptedData.Length);
            return Encoding.UTF8.GetString(plainText);
        }

        /// <summary>
        /// encrypts strings to be decrypted by decrypt function(in the c# or java code, mainly for qc passwords)
        /// </summary>
        /// <param name="textToEncrypt"></param>
        /// <param name="key"></param>
        /// <returns></returns>
        string Encrypt(string textToEncrypt, string key)
        {
            RijndaelManaged rijndaelCipher = new RijndaelManaged();
            rijndaelCipher.Mode = CipherMode.CBC;
            rijndaelCipher.Padding = PaddingMode.PKCS7;

            rijndaelCipher.KeySize = 0x80;
            rijndaelCipher.BlockSize = 0x80;
            byte[] pwdBytes = Encoding.UTF8.GetBytes(key);
            byte[] keyBytes = new byte[0x10];
            int len = pwdBytes.Length;
            if (len > keyBytes.Length)
            {
                len = keyBytes.Length;
            }
            Array.Copy(pwdBytes, keyBytes, len);
            rijndaelCipher.Key = keyBytes;
            rijndaelCipher.IV = keyBytes;
            ICryptoTransform transform = rijndaelCipher.CreateEncryptor();
            byte[] plainText = Encoding.UTF8.GetBytes(textToEncrypt);
            return Convert.ToBase64String(transform.TransformFinalBlock(plainText, 0, plainText.Length));
        }

        /// <summary>
        /// writes to console using the ConsolWriter class
        /// </summary>
        /// <param name="message"></param>
        private static void WriteToConsole(string message)
        {
            ConsoleWriter.WriteLine(message);
        }

        /// <summary>
        /// analyzes and runs the tests given in the param file.
        /// </summary>
        public void Run()
        {
            _ciRun = true;
            if (_runtype == TestStorageType.Unknown)
                Enum.TryParse<TestStorageType>(_ciParams["runType"], true, out _runtype);
            if (_runtype == TestStorageType.Unknown)
            {
                WriteToConsole(Resources.LauncherNoRuntype);
                return;
            }

            if (!_ciParams.ContainsKey("resultsFilename"))
            {
                WriteToConsole(Resources.LauncherNoResFilenameFound);
                return;
            }
            string resultsFilename = _ciParams["resultsFilename"];

            if (_ciParams.ContainsKey("uniqueTimeStamp"))
            {
                UniqueTimeStamp = _ciParams["uniqueTimeStamp"];
            }
            else
            {
                UniqueTimeStamp = resultsFilename.ToLower().Replace("results", "").Replace(".xml", "");
            }

            //create the runner according to type
            IAssetRunner runner = CreateRunner(_runtype, _ciParams);

            //runner instantiation failed (no tests to run or other problem)
            if (runner == null)
            {
                Environment.Exit((int)Launcher.ExitCodeEnum.Failed);
            }

            //run the tests!
            RunTests(runner, resultsFilename);


            if (Launcher.ExitCode != ExitCodeEnum.Passed)
                Environment.Exit((int)Launcher.ExitCode);
        }

        /// <summary>
        /// creates the correct runner according to the given type
        /// </summary>
        /// <param name="runType"></param>
        /// <param name="ciParams"></param>
        IAssetRunner CreateRunner(TestStorageType runType, JavaProperties ciParams)
        {
            IAssetRunner runner = null;
            switch (runType)
            {
                case TestStorageType.Alm:
                    //check that all required parameters exist
                    foreach (string param1 in requiredParamsForQcRun)
                    {
                        if (!_ciParams.ContainsKey(param1))
                        {
                            ConsoleWriter.WriteLine(string.Format(Resources.LauncherParamRequired, param1));
                            return null;
                        }
                    }

                    //parse params that need parsing
                    double dblQcTimeout = int.MaxValue;
                    if (!double.TryParse(_ciParams["almTimeout"], out dblQcTimeout))
                    {
                        ConsoleWriter.WriteLine(Resources.LauncherTimeoutNotNumeric);
                        dblQcTimeout = int.MaxValue;
                    }

                    ConsoleWriter.WriteLine(string.Format(Resources.LuancherDisplayTimout, dblQcTimeout));

                    QcRunMode enmQcRunMode = QcRunMode.RUN_LOCAL;
                    if (!Enum.TryParse<QcRunMode>(_ciParams["almRunMode"], true, out enmQcRunMode))
                    {
                        ConsoleWriter.WriteLine(Resources.LauncherIncorrectRunmode);
                        enmQcRunMode = QcRunMode.RUN_LOCAL;
                    }
                    ConsoleWriter.WriteLine(string.Format(Resources.LauncherDisplayRunmode, enmQcRunMode.ToString()));

                    //go over testsets in the parameters, and collect them
                    List<string> sets = GetParamsWithPrefix("TestSet");

                    if (sets.Count == 0)
                    {
                        ConsoleWriter.WriteLine(Resources.LauncherNoTests);
                        return null;
                    }

                    //create an Alm runner
                    runner = new AlmTestSetsRunner(_ciParams["almServerUrl"],
                                     _ciParams["almUserName"],
                                     Decrypt(_ciParams["almPassword"], secretkey),
                                     _ciParams["almDomain"],
                                     _ciParams["almProject"],
                                     dblQcTimeout,
                                     enmQcRunMode,
                                     _ciParams["almRunHost"],
                                     sets);
                    break;
                case TestStorageType.FileSystem:

                    //get the tests
                    IEnumerable<string> tests = GetParamsWithPrefix("Test");

                    IEnumerable<string> jenkinsEnvVariablesWithCommas = GetParamsWithPrefix("JenkinsEnv");
                    Dictionary<string, string> jenkinsEnvVariables = new Dictionary<string,string>();
                    foreach (string var in jenkinsEnvVariablesWithCommas)
                    { 
                        string[] nameVal = var.Split(",;".ToCharArray());
                        jenkinsEnvVariables.Add(nameVal[0], nameVal[1]);
                    }
                    //parse the timeout into a TimeSpan
                    TimeSpan timeout = TimeSpan.MaxValue;
                    if (_ciParams.ContainsKey("fsTimeout"))
                    {
                        string strTimoutInSeconds = _ciParams["fsTimeout"];
                        if (strTimoutInSeconds.Trim() != "-1")
                        {
                            int intTimoutInSeconds = 0;
                            int.TryParse(strTimoutInSeconds, out intTimoutInSeconds);
                            timeout = TimeSpan.FromSeconds(intTimoutInSeconds);
                        }
                    }

                    //LR specific values:
                    //default values are set by JAVA code, in com.hp.application.automation.tools.model.RunFromFileSystemModel.java

                    int pollingInterval = 30;
                    if (_ciParams.ContainsKey("controllerPollingInterval"))
                        pollingInterval = int.Parse(_ciParams["controllerPollingInterval"]);

                    TimeSpan perScenarioTimeOut = TimeSpan.MaxValue;
                    if (_ciParams.ContainsKey("PerScenarioTimeOut"))
                    {
                        string strTimoutInSeconds = _ciParams["PerScenarioTimeOut"];
                        if (strTimoutInSeconds.Trim() != "-1")
                        {
                            int intTimoutInSeconds = 0;
                            if (int.TryParse(strTimoutInSeconds, out intTimoutInSeconds))
                                perScenarioTimeOut = TimeSpan.FromMinutes(intTimoutInSeconds);
                        }
                    }

                    char[] delim = { '\n' };
                    List<string> ignoreErrorStrings = new List<string>();
                    if (_ciParams.ContainsKey("ignoreErrorStrings"))
                    {
                        ignoreErrorStrings.AddRange(_ciParams["ignoreErrorStrings"].Split(delim, StringSplitOptions.RemoveEmptyEntries));
                    }

                    
                    if (tests == null || tests.Count() == 0)
                    {
                        WriteToConsole(Resources.LauncherNoTestsFound);
                    }

                    List<string> validTests = Helper.ValidateFiles(tests);

                    if (tests != null && tests.Count() > 0 && validTests.Count == 0)
                    {
                        ConsoleWriter.WriteLine(Resources.LauncherNoValidTests);
                        return null;
                    }
                    string fsAppParamName = "";
                    string appIdentifier = "";
                    if (_ciParams.ContainsKey("fsAppParamName"))
                    {
                        fsAppParamName = _ciParams["fsAppParamName"];
                        if (_ciParams.ContainsKey(fsAppParamName))
                        {
                            appIdentifier = _ciParams[fsAppParamName];
                        }
                    }

                    runner = new FileSystemTestsRunner(validTests, timeout, pollingInterval, perScenarioTimeOut, ignoreErrorStrings, jenkinsEnvVariables, fsAppParamName, appIdentifier);

                    break;

                default:
                    runner = null;
                    break;
            }
            return runner;
        }

        private List<string> GetParamsWithPrefix(string prefix)
        {
            int idx = 1;
            List<string> parameters = new List<string>();
            while (_ciParams.ContainsKey(prefix + idx))
            {
                string set = _ciParams[prefix + idx];
                if (set.StartsWith("Root\\"))
                    set = set.Substring(5);
                set = set.TrimEnd("\\".ToCharArray());
                parameters.Add(set.TrimEnd());
                ++idx;
            }
            return parameters;
        }

        /// <summary>
        /// used by the run fuction to run the tests
        /// </summary>
        /// <param name="runner"></param>
        /// <param name="resultsFile"></param>
        private void RunTests(IAssetRunner runner, string resultsFile)
        {
            try
            {
                if (_ciRun)
                {
                    _xmlBuilder = new JunitXmlBuilder();
                    _xmlBuilder.XmlName = resultsFile;
                }

                TestSuiteRunResults results = runner.Run();

                if (results == null)
                    Environment.Exit((int)Launcher.ExitCodeEnum.Failed);

                _xmlBuilder.CreateXmlFromRunResults(results);

                //if there is an error
                if (results.TestRuns.Any(tr => tr.TestState == TestState.Failed || tr.TestState == TestState.Error))
                {
                    Launcher.ExitCode = Launcher.ExitCodeEnum.Failed;
                }

                //this is the total run summary
                ConsoleWriter.ActiveTestRun = null;
                string runStatus = (Launcher.ExitCode == ExitCodeEnum.Passed || Launcher.ExitCode == ExitCodeEnum.Unstable) ? "Job succeeded" : "Job failed";
                int numFailures = results.TestRuns.Count(t => t.TestState == TestState.Failed);
                int numSuccess = results.TestRuns.Count(t => t.TestState == TestState.Passed);
                int numErrors = results.TestRuns.Count(t => t.TestState == TestState.Error);
                ConsoleWriter.WriteLine(Resources.LauncherDoubleSeperator);
                ConsoleWriter.WriteLine(string.Format(Resources.LauncherDisplayStatistics, runStatus, results.TestRuns.Count, numSuccess, numFailures, numErrors));

                if (!runner.RunWasCancelled)
                {
                    results.TestRuns.ForEach(tr => ConsoleWriter.WriteLine(((tr.HasWarnings) ? "Warning".PadLeft(7) : tr.TestState.ToString().PadRight(7)) + ": " + tr.TestPath));
                    
                    ConsoleWriter.WriteLine(Resources.LauncherDoubleSeperator);
                    if (ConsoleWriter.ErrorSummaryLines != null && ConsoleWriter.ErrorSummaryLines.Count > 0)
                    {
                        ConsoleWriter.WriteLine("Job Errors summary:");
                        ConsoleWriter.ErrorSummaryLines.ForEach(line => ConsoleWriter.WriteLine(line));
                    }
                }

                //ConsoleWriter.WriteLine("Returning " + runStatus + ".");
            }
            finally
            {
                try
                {
                    runner.Dispose();
                }
                catch (Exception ex)
                {
                    ConsoleWriter.WriteLine(string.Format(Resources.LauncherRunnerDisposeError, ex.Message));
                };
            }

        }

    }
}
