// (c) Copyright 2012 Hewlett-Packard Development Company, L.P. 
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

using System;
using System.Linq;
using System.IO;
using System.Xml;
using QTObjectModelLib;
using Resources = HpToolsLauncher.Properties.Resources;
using System.Threading;
using System.Diagnostics;
using System.Collections.Generic;

namespace HpToolsLauncher
{
    public class GuiTestRunner : IFileSysTestRunner
    {
        private readonly IAssetRunner _runNotifier;
        private readonly object _lockObject = new object();
        private TimeSpan _timeLeftUntilTimeout = TimeSpan.MaxValue;
        private Stopwatch _stopwatch = null;
        private Application _qtpApplication;
        private ParameterDefinitions _qtpParamDefs;
        private Parameters _qtpParameters;
        private bool _useUFTLicense;
        private RunCancelledDelegate _runCancelled;

        /// <summary>
        /// constructor
        /// </summary>
        /// <param name="runNotifier"></param>
        /// <param name="useUftLicense"></param>
        /// <param name="timeLeftUntilTimeout"></param>
        public GuiTestRunner(IAssetRunner runNotifier, bool useUftLicense, TimeSpan timeLeftUntilTimeout)
        {
            _timeLeftUntilTimeout = timeLeftUntilTimeout;
            _stopwatch = Stopwatch.StartNew();
            _runNotifier = runNotifier;
            _useUFTLicense = useUftLicense;
        }

        #region QTP

        /// <summary>
        /// runs the given test and returns resutls
        /// </summary>
        /// <param name="testPath"></param>
        /// <param name="errorReason"></param>
        /// <param name="runCanclled"></param>
        /// <returns></returns>
        public TestRunResults RunTest(string testPath, ref string errorReason, RunCancelledDelegate runCanclled)
        {

            TestRunResults runDesc = new TestRunResults();
            ConsoleWriter.ActiveTestRun = runDesc;
            ConsoleWriter.WriteLine(DateTime.Now.ToString(Launcher.DateFormat) + " Running: " + testPath);
            runDesc.ReportLocation = Helper.CreateTempDir();
            runDesc.TestPath = testPath;
            runDesc.TestState = TestState.Unknown;

            _runCancelled = runCanclled;

            if (!Helper.IsQtpInstalled())
            {
                runDesc.TestState = TestState.Error;
                runDesc.ErrorDesc = "QTP is not installed on " + System.Environment.MachineName;
                ConsoleWriter.WriteErrLine(runDesc.ErrorDesc);
                Environment.ExitCode = (int)Launcher.ExitCodeEnum.Failed;
                return runDesc;
            }   

            try
            {
                var type = Type.GetTypeFromProgID("Quicktest.Application");

                lock (_lockObject)
                {
                    _qtpApplication = Activator.CreateInstance(type) as Application;

                    // Check for required Addins
                    LoadNeededAddins(testPath);

                    if (!_qtpApplication.Launched)
                    {
                        if (_runCancelled())
                        {
                            QTPTestCleanup();
                            runDesc.TestState = TestState.Error;
                            return runDesc;
                        }
                        // Launch application after set Addins
                        _qtpApplication.Launch();
                        _qtpApplication.Visible = false;

                    }
                }
            }
            catch (Exception e)
            {
                errorReason = Resources.QtpNotLaunchedError;
                runDesc.TestState = TestState.Error;
                runDesc.ReportLocation = "";
                runDesc.ErrorDesc = errorReason;
                return runDesc;
            }

            if (_qtpApplication.Test != null && _qtpApplication.Test.Modified)
            {
                var message = Resources.QtpNotLaunchedError;
                errorReason = message;
                runDesc.TestState = TestState.Error;
                runDesc.ErrorDesc = errorReason;
                return runDesc;
            }

            _qtpApplication.UseLicenseOfType(_useUFTLicense
                                                 ? tagUnifiedLicenseType.qtUnifiedFunctionalTesting
                                                 : tagUnifiedLicenseType.qtNonUnified);

            if (!HandleInputParameters(testPath, ref errorReason))
            {
                runDesc.TestState = TestState.Error;
                runDesc.ErrorDesc = errorReason;
                return runDesc;
            }

            GuiTestRunResult guiTestRunResult = ExecuteQTPRun(runDesc);
            runDesc.ReportLocation = guiTestRunResult.ReportPath;

            if (!guiTestRunResult.IsSuccess)
            {
                runDesc.TestState = TestState.Error;
                return runDesc;
            }

            if (!HandleOutputArguments(ref errorReason))
            {
                runDesc.TestState = TestState.Error;
                runDesc.ErrorDesc = errorReason;
                return runDesc;
            }

            QTPTestCleanup();


            return runDesc;
        }

        /// <summary>
        /// performs global cleanup code for this type of runner
        /// </summary>
        public void CleanUp()
        {
            try
            {
                //if we don't have a qtp instance, create one
                if (_qtpApplication == null)
                {
                    var type = Type.GetTypeFromProgID("Quicktest.Application");
                    _qtpApplication = Activator.CreateInstance(type) as Application;
                }

                //if the app is running, close it.
                if (_qtpApplication.Launched)
                    _qtpApplication.Quit();
            }
            catch
            {
                //nothing to do. (cleanup code should not throw exceptions, and there is no need to log this as an error in the test)
            }
        }

        static HashSet<string> _colLoadedAddinNames = null;
        /// <summary>
        /// Set the test Addins 
        /// </summary>
        private void LoadNeededAddins(string fileName)
        {
            bool blnNeedToLoadAddins = false;

            //if not launched, we have no addins.
            if (!_qtpApplication.Launched)
                _colLoadedAddinNames = null;

            try
            {
                HashSet<string> colCurrentTestAddins = new HashSet<string>();

                object erroDescription;
                var testAddinsObj = _qtpApplication.GetAssociatedAddinsForTest(fileName);
                object[] testAddins = (object[])testAddinsObj;

                foreach (string addin in testAddins)
                {
                    colCurrentTestAddins.Add(addin);
                }

                if (_colLoadedAddinNames != null)
                {
                    //check if we have a missing addin (and need to quit Qtp, and reload with new addins)
                    foreach (string addin in testAddins)
                    {
                        if (!_colLoadedAddinNames.Contains(addin))
                        {
                            blnNeedToLoadAddins = true;
                            break;
                        }
                    }

                    //check if there is no extra addins that need to be removed
                    if (_colLoadedAddinNames.Count != colCurrentTestAddins.Count)
                    {
                        blnNeedToLoadAddins = true;
                    }
                }
                else
                {
                    //first time = load addins.
                    blnNeedToLoadAddins = true;
                }

                _colLoadedAddinNames = colCurrentTestAddins;

                //the addins need to be refreshed, load new addins
                if (blnNeedToLoadAddins)
                {
                    if (_qtpApplication.Launched)
                        _qtpApplication.Quit();
                    _qtpApplication.SetActiveAddins(ref testAddinsObj, out erroDescription);
                }

            }
            catch (Exception)
            {
                // Try anyway to run the test
            }
        }


        /// <summary>
        /// Activate all Installed Addins 
        /// </summary>
        private void ActivateAllAddins()
        {
            try
            {
                // Get Addins collection
                Addins qtInstalledAddins = _qtpApplication.Addins;

                if (qtInstalledAddins.Count > 0)
                {
                    string[] qtAddins = new string[qtInstalledAddins.Count];

                    // Addins Object is 1 base order
                    for (int idx = 1; idx <= qtInstalledAddins.Count; ++idx)
                    {
                        // Our list is 0 base order
                        qtAddins[idx - 1] = qtInstalledAddins[idx].Name;
                    }

                    object erroDescription;
                    var addinNames = (object)qtAddins;

                    _qtpApplication.SetActiveAddins(ref addinNames, out erroDescription);
                }
            }
            catch (Exception)
            {
                // Try anyway to run the test
            }
        }

        /// <summary>
        /// runs the given test QTP and returns results
        /// </summary>
        /// <param name="testResults">the test results object containing test info and also receiving run results</param>
        /// <returns></returns>
        private GuiTestRunResult ExecuteQTPRun(TestRunResults testResults)
        {
            GuiTestRunResult result = new GuiTestRunResult { IsSuccess = true };
            try
            {
                if (Directory.Exists(testResults.ReportLocation))
                {
                    try
                    {
                        Directory.Delete(testResults.ReportLocation, true);
                    }
                    catch (Exception)
                    {
                        Console.WriteLine("Unable to delete report folder " + testResults.ReportLocation);
                    }
                }
                if (!Directory.Exists(testResults.ReportLocation)) Directory.CreateDirectory(testResults.ReportLocation);
                Type runResultsOptionstype = Type.GetTypeFromProgID("QuickTest.RunResultsOptions");
                var options = (RunResultsOptions)Activator.CreateInstance(runResultsOptionstype);
                options.ResultsLocation = testResults.ReportLocation;
                _qtpApplication.Options.Run.RunMode = "Fast";

                //Check for cancel before executing
                if (_runCancelled())
                {
                    testResults.TestState = TestState.Error;
                    testResults.ErrorDesc = "Test run was Canceled";
                    ConsoleWriter.WriteLine("Test run was Canceled");
                    result.IsSuccess = false;
                    return result;
                }
                ConsoleWriter.WriteLine("Running Test: " + testResults.TestPath);

                _qtpApplication.Test.Run(options, false, _qtpParameters);

                result.ReportPath = Path.Combine(testResults.ReportLocation, "Report");
                int slept = 0;
                while (slept < 20000 && _qtpApplication.GetStatus().Equals("Ready"))
                {
                    Thread.Sleep(50);
                    slept += 50;
                }


                while (!_runCancelled() && (_qtpApplication.GetStatus().Equals("Running") || _qtpApplication.GetStatus().Equals("Busy")))
                {
                    Thread.Sleep(200);
                    if (_timeLeftUntilTimeout - _stopwatch.Elapsed <= TimeSpan.Zero)
                    {
                        _qtpApplication.Test.Stop();
                        testResults.TestState = TestState.Error;
                        testResults.ErrorDesc = "Timeout has expired.";
                        ConsoleWriter.WriteLine("Timeout has expired.");

                        result.IsSuccess = false;
                        return result;
                    }
                }

                if (_runCancelled())
                {
                    QTPTestCleanup();
                    testResults.TestState = TestState.Error;
                    testResults.ErrorDesc = "Test run was Canceled";
                    ConsoleWriter.WriteLine("Test run was Canceled");
                    Launcher.ExitCode = Launcher.ExitCodeEnum.Aborted;
                    result.IsSuccess = false;
                    return result;
                }
                string lastError = _qtpApplication.Test.LastRunResults.LastError;

                //read the lastError
                if (!String.IsNullOrEmpty(lastError))
                {
                    testResults.TestState = TestState.Error;
                    testResults.ErrorDesc = lastError;
                }

                // the way to check the logical success of the target QTP test is: app.Test.LastRunResults.Status == "Passed".
                if (_qtpApplication.Test.LastRunResults.Status.Equals("Passed"))
                {
                    testResults.TestState = TestState.Passed;

                }
                else if (_qtpApplication.Test.LastRunResults.Status.Equals("Warning"))
                {
                    testResults.TestState = TestState.Passed;
                    testResults.HasWarnings = true;

                    if (Launcher.ExitCode != Launcher.ExitCodeEnum.Failed && Launcher.ExitCode != Launcher.ExitCodeEnum.Aborted)
                        Launcher.ExitCode = Launcher.ExitCodeEnum.Unstable;
                }
                else
                {
                    testResults.TestState = TestState.Failed;
                    testResults.FailureDesc = "Test failed";

                    Launcher.ExitCode = Launcher.ExitCodeEnum.Failed;
                }
            }
            catch (NullReferenceException e)
            {
                ConsoleWriter.WriteLine("Error: " + e.Message + "\n" + e.StackTrace);
                testResults.TestState = TestState.Error;
                testResults.ErrorDesc = Resources.QtpRunError;

                result.IsSuccess = false;
                return result;
            }
            catch (SystemException e)
            {
                KillQtp();
                ConsoleWriter.WriteLine("Error: " + e.Message + "\n" + e.StackTrace);
                testResults.TestState = TestState.Error;
                testResults.ErrorDesc = Resources.QtpRunError;

                result.IsSuccess = false;
                return result;
            }
            catch (Exception e2)
            {

                ConsoleWriter.WriteLine("Error: " + e2.Message + "\n" + e2.StackTrace);
                testResults.TestState = TestState.Error;
                testResults.ErrorDesc = Resources.QtpRunError;

                result.IsSuccess = false;
                return result;
            }


            return result;
        }

        private void KillQtp()
        {
            //error during run, process may have crashed (need to cleanup, close QTP and qtpRemote for next test to run correctly)
            CleanUp();

            //kill the qtp automation, to make sure it will run correctly next time
            Process[] processes = Process.GetProcessesByName("qtpAutomationAgent");
            Process qtpAuto = processes.Where(p => p.SessionId == Process.GetCurrentProcess().SessionId).FirstOrDefault();
            if (qtpAuto != null)
                qtpAuto.Kill();
        }

        private bool HandleOutputArguments(ref string errorReason)
        {
            try
            {
                var outputArguments = new XmlDocument { PreserveWhitespace = true };
                outputArguments.LoadXml("<Arguments/>");

                for (int i = 0; i < _qtpParamDefs.Count; ++i)
                {
                    var pd = _qtpParamDefs[i];
                    if (pd.InOut == qtParameterDirection.qtParamDirOut)
                    {
                        var node = outputArguments.CreateElement(pd.Name);
                        var value = _qtpParameters[pd.Name].Value;
                        if (value != null)
                            node.InnerText = value.ToString();

                        outputArguments.DocumentElement.AppendChild(node);
                    }
                }
            }
            catch (Exception e)
            {
                errorReason = Resources.QtpNotLaunchedError;
                return false;
            }
            return true;
        }

        private bool HandleInputParameters(string fileName, ref string errorReason)
        {
            try
            {
                string path = fileName;

                if (_runCancelled())
                {
                    QTPTestCleanup();
                    return false;
                }

                _qtpApplication.Open(path, true, false);
                _qtpParamDefs = _qtpApplication.Test.ParameterDefinitions;
                _qtpParameters = _qtpParamDefs.GetParameters();
            }
            catch (Exception e)
            {
                errorReason = Resources.QtpRunError;
                return false;
            }
            return true;

        }

        /// <summary>
        /// stops and closes qtp test, to make sure nothing is left floating after run.
        /// </summary>
        private void QTPTestCleanup()
        {
            try
            {
                lock (_lockObject)
                {
                    if (_qtpApplication == null)
                    {
                        return;
                    }

                    var qtpTest = _qtpApplication.Test;
                    if (qtpTest != null)
                    {
                        if (_qtpApplication.GetStatus().Equals("Running") || _qtpApplication.GetStatus().Equals("Busy"))
                        {
                            try
                            {
                                _qtpApplication.Test.Stop();
                            }
                            catch (Exception e)
                            {
                            }
                            finally
                            {
                                
                            }
                        }
                    }
                }
            }
            catch (Exception ex)
            {
            }

            _qtpParameters = null;
            _qtpParamDefs = null;
            _qtpApplication = null;
        }

        #endregion


        /// <summary>
        /// holds the resutls for a GUI test
        /// </summary>
        private class GuiTestRunResult
        {
            public GuiTestRunResult()
            {
                ReportPath = "";
            }

            public bool IsSuccess { get; set; }
            public string ReportPath { get; set; }
        }
    }
}
