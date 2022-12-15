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
using System.Diagnostics;
using System.IO;
using System.Reflection;
using HpToolsLauncher.Properties;
using HpToolsLauncher.TestRunners;
using HpToolsLauncher.RTS;
using System.Threading;
using System.Linq;

namespace HpToolsLauncher
{
    public class FileSystemTestsRunner : RunnerBase, IDisposable
    {
        #region Members

        private const string SPACES = "    ";

        Dictionary<string, string> _jenkinsEnvVariables;
        private List<TestInfo> _tests;
        private static string _uftViewerPath;
        private int _errors, _fail, _warnings;
        private bool _useUFTLicense;
        private bool _displayController;
        private string _analysisTemplate;
        private SummaryDataLogger _summaryDataLogger;
        private List<ScriptRTSModel> _scriptRTSSet;
        private TimeSpan _timeout = TimeSpan.MaxValue;
        private string _uftRunMode;
        private Stopwatch _stopwatch = null;
        private string _abortFilename = System.IO.Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location) + "\\stop" + Launcher.UniqueTimeStamp + ".txt";
        private string _encoding;

        //LoadRunner Arguments
        private int _pollingInterval;
        private TimeSpan _perScenarioTimeOutMinutes;
        private List<string> _ignoreErrorStrings;

        // parallel runner related information
        private Dictionary<string, List<string>> _parallelRunnerEnvironments;

        //saves runners for cleaning up at the end.
        private Dictionary<TestType, IFileSysTestRunner> _colRunnersForCleanup = new Dictionary<TestType, IFileSysTestRunner>();

        private McConnectionInfo _mcConnection;
        private string _mobileInfoForAllGuiTests;
        private bool _printInputParams;

        #endregion

        private void InitCommonFields(bool printInputParams,
                                    TimeSpan timeout,
                                    string uftRunMode,
                                    int controllerPollingInterval,
                                    TimeSpan perScenarioTimeOutMinutes,
                                    List<string> ignoreErrorStrings,
                                    Dictionary<string, string> jenkinsEnvVariables,
                                    McConnectionInfo mcConnection,
                                    string mobileInfo,
                                    Dictionary<string, List<string>> parallelRunnerEnvironments,
                                    bool displayController,
                                    string analysisTemplate,
                                    SummaryDataLogger summaryDataLogger,
                                    List<ScriptRTSModel> scriptRtsSet,
                                    string reportPath,
                                    string xmlResultsFullFileName,
                                    string encoding,
                                    bool useUftLicense = false)
        {
            _jenkinsEnvVariables = jenkinsEnvVariables;
            //search if we have any testing tools installed
            if (!Helper.IsTestingToolsInstalled(TestStorageType.FileSystem))
            {
                ConsoleWriter.WriteErrLine(string.Format(Resources.FileSystemTestsRunner_No_HP_testing_tool_is_installed_on, Environment.MachineName));
                Environment.Exit((int)Launcher.ExitCodeEnum.Failed);
            }

            _timeout = timeout;
            ConsoleWriter.WriteLine("FileSystemTestRunner timeout is " + _timeout);
            _stopwatch = Stopwatch.StartNew();

            _pollingInterval = controllerPollingInterval;
            _perScenarioTimeOutMinutes = perScenarioTimeOutMinutes;
            _ignoreErrorStrings = ignoreErrorStrings;

            _useUFTLicense = useUftLicense;
            _displayController = displayController;
            _analysisTemplate = analysisTemplate;
            _summaryDataLogger = summaryDataLogger;
            _scriptRTSSet = scriptRtsSet;
            _printInputParams = printInputParams;

            _mcConnection = mcConnection;
            _mobileInfoForAllGuiTests = mobileInfo;

            _parallelRunnerEnvironments = parallelRunnerEnvironments;
            _xmlBuilder.XmlName = xmlResultsFullFileName;
            _encoding = encoding;
            _uftRunMode = uftRunMode;

            ConsoleWriter.WriteLine("UFT Mobile connection info is - " + _mcConnection.ToString());

            if (reportPath != null)
            {
                ConsoleWriter.WriteLine("Results directory is: " + reportPath);
            }
        }

        /// <summary>
        /// creates instance of the runner given a source.
        /// </summary>
        /// <param name="sources"></param>
        /// <param name="timeout"></param>
        /// <param name="uftRunMode"></param>
        /// <param name="scriptRtsSet"></param>
        /// <param name="reportPath"></param>
        /// <param name="controllerPollingInterval"></param>
        /// <param name="perScenarioTimeOutMinutes"></param>
        /// <param name="ignoreErrMsgs"></param>
        /// <param name="jenkinsEnvVars"></param>
        /// <param name="mcConnection"></param>
        /// <param name="mobileInfo"></param>
        /// <param name="parallelRunnerEnvs"></param>
        /// <param name="displayController"></param>
        /// <param name="analysisTemplate"></param>
        /// <param name="summaryDataLogger"></param>
        /// <param name="useUftLicense"></param>
        public FileSystemTestsRunner(List<TestData> sources,
                                    List<TestParameter> @params,
                                    bool printInputParams,
                                    TimeSpan timeout,
                                    string uftRunMode,
                                    int controllerPollingInterval,
                                    TimeSpan perScenarioTimeOutMinutes,
                                    List<string> ignoreErrMsgs,
                                    Dictionary<string, string> jenkinsEnvVars,
                                    McConnectionInfo mcConnection,
                                    string mobileInfo,
                                    Dictionary<string, List<string>> parallelRunnerEnvs,
                                    bool displayController,
                                    string analysisTemplate,
                                    SummaryDataLogger summaryDataLogger,
                                    List<ScriptRTSModel> scriptRtsSet,
                                    string reportPath,
                                    string xmlResultsFullFileName,
                                    string encoding,
                                    bool useUftLicense = false)
        {
            InitCommonFields(printInputParams, timeout, uftRunMode, controllerPollingInterval, perScenarioTimeOutMinutes, ignoreErrMsgs, jenkinsEnvVars, mcConnection, mobileInfo, parallelRunnerEnvs, displayController, analysisTemplate, summaryDataLogger, scriptRtsSet, reportPath, xmlResultsFullFileName, encoding, useUftLicense);

            _tests = GetListOfTestInfo(sources, @params, jenkinsEnvVars);

            if (_tests == null || _tests.Count == 0)
            {
                ConsoleWriter.WriteLine(Resources.FsRunnerNoValidTests);
                Environment.Exit((int)Launcher.ExitCodeEnum.Failed);
            }

            // if a custom path was provided,set the custom report path for all the valid tests(this will overwrite the default location)
            if (reportPath != null)
            {
                _tests.ForEach(test => test.ReportPath = reportPath);
            }

            ConsoleWriter.WriteLine(string.Format(Resources.FsRunnerTestsFound, _tests.Count));

            foreach (var test in _tests)
            {
                ConsoleWriter.WriteLine(string.Empty + test.TestName);
                if (parallelRunnerEnvs.ContainsKey(test.TestId))
                {
                    parallelRunnerEnvs[test.TestId].ForEach(env => ConsoleWriter.WriteLine(SPACES + env));
                }
            }

            ConsoleWriter.WriteLine(Resources.GeneralDoubleSeperator);
        }

        public FileSystemTestsRunner(List<TestInfo> tests,
                                    bool printInputParams,
                                    TimeSpan timeout,
                                    string uftRunMode,
                                    int controllerPollingInterval,
                                    TimeSpan perScenarioTimeOutMinutes,
                                    List<string> ignoreErrMsgs,
                                    Dictionary<string, string> jenkinsEnvVars,
                                    McConnectionInfo mcConnection,
                                    string mobileInfo,
                                    Dictionary<string, List<string>> parallelRunnerEnvs,
                                    bool displayController,
                                    string analysisTemplate,
                                    SummaryDataLogger summaryDataLogger,
                                    List<ScriptRTSModel> scriptRtsSet,
                                    string reportPath,
                                    string xmlResultsFullFileName,
                                    string encoding,
                                    bool useUftLicense = false)
        {
            InitCommonFields(printInputParams, timeout, uftRunMode, controllerPollingInterval, perScenarioTimeOutMinutes, ignoreErrMsgs, jenkinsEnvVars, mcConnection, mobileInfo, parallelRunnerEnvs, displayController, analysisTemplate, summaryDataLogger, scriptRtsSet, reportPath, xmlResultsFullFileName, encoding, useUftLicense);

            _tests = tests;
            if (_tests == null || _tests.Count == 0)
            {
                ConsoleWriter.WriteLine(Resources.FsRunnerNoValidTests);
                Environment.Exit((int)Launcher.ExitCodeEnum.Failed);
            }

            ConsoleWriter.WriteLine(string.Format(Resources.FsRunnerTestsFound, _tests.Count));

            foreach (var test in _tests)
            {
                ConsoleWriter.WriteLine(string.Empty + test.TestName);
                if (parallelRunnerEnvs.ContainsKey(test.TestId))
                {
                    parallelRunnerEnvs[test.TestId].ForEach(env => ConsoleWriter.WriteLine(SPACES + env));
                }
            }

            ConsoleWriter.WriteLine(Resources.GeneralDoubleSeperator);
        }

        public static TestInfo GetFirstTestInfo(TestData source, Dictionary<string, string> jenkinsEnvVars)
        {
            var tests = GetListOfTestInfo(new List<TestData>() { source }, jenkinsEnvVars: jenkinsEnvVars);
            return tests.FirstOrDefault();
        }
        public static List<TestInfo> GetListOfTestInfo(List<TestData> sources, List<TestParameter> @params = null, Dictionary<string, string> jenkinsEnvVars = null)
        {
            List<TestInfo> tests = new List<TestInfo>();

            int idx = 1;
            //go over all sources, and create a list of all tests
            foreach (TestData source in sources)
            {
                //this will contain all the tests found in the source
                List<TestInfo> testGroup = new List<TestInfo>();
                try
                {
                    //--handle directories which contain test subdirectories (recursively)
                    if (Helper.IsDirectory(Helper.GetTestPathWithoutParams(source.Tests)))
                    {
                        string testPath = Helper.GetTestPathWithoutParams(source.Tests);
                        var testsLocations = Helper.GetTestsLocations(testPath);

                        foreach (var loc in testsLocations)
                        {
                            var test = new TestInfo(loc, loc, testPath, source.Id);

                            try
                            {
                                //we need to check for inline params and props params as well, for backward compatibility
                                SetInlineParams(source.Tests, ref test);
                                SetPropsParams(@params, idx, ref test);
                            }
                            catch (ArgumentException)
                            {
                                ConsoleWriter.WriteErrLine(string.Format(Resources.FsRunnerErrorParameterFormat, testPath));
                            }

                            testGroup.Add(test);
                        }
                    }
                    //--handle mtb files (which contain links to tests)
                    else
                    //file might be LoadRunner scenario or
                    //mtb file (which contain links to tests)
                    //other files are dropped
                    {
                        testGroup = new List<TestInfo>();
                        FileInfo fi = new FileInfo(source.Tests);
                        if (fi.Extension == Helper.LoadRunnerFileExtention)
                            testGroup.Add(new TestInfo(source.Tests, source.Tests, source.Tests, source.Id));
                        else if (fi.Extension == Helper.MtbFileExtension)
                        {
                            MtbManager manager = new MtbManager();
                            var paths = manager.Parse(source.Tests);
                            foreach (var p in paths)
                            {
                                testGroup.Add(new TestInfo(p, p, source.Tests, source.Id));
                            }
                        }
                        else if (fi.Extension == Helper.MtbxFileExtension)
                        {
                            testGroup = MtbxManager.Parse(source.Tests, jenkinsEnvVars, source.Tests);

                            // set the test Id for each test from the group
                            // this is important for parallel runner
                            foreach (var testInfo in testGroup)
                            {
                                testInfo.TestId = source.Id;
                            }
                        }
                    }
                }
                catch (Exception)
                {
                    testGroup = new List<TestInfo>();
                }

                //--handle single test dir, add it with no group
                if (testGroup.Count == 1)
                {
                    testGroup[0].TestGroup = "Test group";
                }

                //we add the found tests to the test list
                tests.AddRange(testGroup);

                ++idx;
            }
            return tests;
        }

        /// <summary>
        /// runs all tests given to this runner and returns a suite of run results
        /// </summary>
        /// <returns>The rest run results for each test</returns>
        public override TestSuiteRunResults Run()
        {
            //create a new Run Results object
            TestSuiteRunResults activeRunDesc = new TestSuiteRunResults();
            bool isNewTestSuite;
            testsuite ts = _xmlBuilder.TestSuites.GetTestSuiteOrDefault(activeRunDesc.SuiteName, JunitXmlBuilder.ClassName, out isNewTestSuite);
            ts.tests += _tests.Count;

            double totalTime = 0;
            try
            {
                var start = DateTime.Now;

                Dictionary<string, int> indexList = new Dictionary<string, int>();
                foreach (var test in _tests)
                {
                    indexList[test.TestPath] = 0;
                }

                Dictionary<string, int> rerunList = CreateDictionary(_tests);
                Dictionary<string, string> prevTestOutParams = null;

                for (int x = 0; x < _tests.Count; x++)
                {
                    var test = _tests[x];
                    if (indexList[test.TestPath] == 0)
                    {
                        indexList[test.TestPath] = 1;
                    }

                    if (RunCancelled()) break;

                    string errorReason = string.Empty;
                    TestRunResults runResult = null;
                    try
                    {
                        if (prevTestOutParams != null && prevTestOutParams.Count > 0)
                        {
                            foreach (var param in test.Params)
                            {
                                if (param.Source != null && prevTestOutParams.ContainsKey(param.Source))
                                {
                                    param.Value = prevTestOutParams[param.Source];
                                }
                            }
                            prevTestOutParams = null;
                        }
                        Dictionary<string, string> outParams = null;
                        runResult = RunHpToolsTest(test, ref errorReason, out outParams);
                        if (outParams != null && outParams.Count > 0)
                            prevTestOutParams = outParams;
                        else
                            prevTestOutParams = null;
                    }
                    catch (Exception ex)
                    {
                        runResult = new TestRunResults
                        {
                            TestState = TestState.Error,
                            ErrorDesc = ex.Message,
                            TestName = test.TestName,
                            TestPath = test.TestPath
                        };
                    }
                    runResult.TestInfo = test;

                    //get the original source for this test, for grouping tests under test classes
                    runResult.TestGroup = test.TestGroup;

                    activeRunDesc.TestRuns.Add(runResult);

                    //if fail was terminated before this step, continue
                    if (runResult.TestState != TestState.Failed)
                    {
                        if (runResult.TestState != TestState.Error)
                        {
                            Helper.GetTestStateFromReport(runResult);
                        }
                        else
                        {
                            if (string.IsNullOrEmpty(runResult.ErrorDesc))
                            {
                                runResult.ErrorDesc = RunCancelled() ? Resources.ExceptionUserCancelled : Resources.ExceptionExternalProcess;
                            }
                            runResult.ReportLocation = null;
                            runResult.TestState = TestState.Error;
                        }
                    }

                    if (runResult.TestState == TestState.Passed && runResult.HasWarnings)
                    {
                        runResult.TestState = TestState.Warning;
                        ConsoleWriter.WriteLine(Resources.FsRunnerTestDoneWarnings);
                    }
                    else
                    {
                        ConsoleWriter.WriteLine(string.Format(Resources.FsRunnerTestDone, runResult.TestState));
                    }

                    UpdateCounters(runResult.TestState, ts);

                    //create test folders
                    if (rerunList[test.TestPath] > 0)
                    {
                        if (!Directory.Exists(Path.Combine(test.TestPath, "Report1")))
                        {
                            rerunList[test.TestPath]--;
                        }
                        else
                        {
                            indexList[test.TestPath]++;
                            rerunList[test.TestPath]--;
                        }
                    }

                    //update report folder
                    string uftReportDir = Path.Combine(test.TestPath, "Report");
                    string uftReportDirNew = Path.Combine(test.TestPath, string.Format("Report{0}", indexList[test.TestPath]));
                    ConsoleWriter.WriteLine(string.Format("uftReportDir is {0}", uftReportDirNew));
                    try
                    {
                        if (Directory.Exists(uftReportDir))
                        {
                            if (Directory.Exists(uftReportDirNew))
                            {
                                Helper.DeleteDirectory(uftReportDirNew);
                            }

                            Directory.Move(uftReportDir, uftReportDirNew);
                        }
                    }
                    catch(Exception)
                    {
                        Thread.Sleep(1000);
                        Directory.Move(uftReportDir, uftReportDirNew);
                    }

                    // Create or update the xml report. This function is called after each test execution in order to have a report available in case of job interruption
                    _xmlBuilder.CreateOrUpdatePartialXmlReport(ts, runResult, isNewTestSuite && x==0);
                    ConsoleWriter.WriteLineWithTime("Test complete: " + runResult.TestPath + "\n-------------------------------------------------------------------------------------------------------");
                }

                totalTime = (DateTime.Now - start).TotalSeconds;
            }
            finally
            {
                activeRunDesc.NumTests = _tests.Count;
                activeRunDesc.NumErrors = _errors;
                activeRunDesc.TotalRunTime = TimeSpan.FromSeconds(totalTime);
                activeRunDesc.NumFailures = _fail;
                activeRunDesc.NumWarnings = _warnings;

                foreach (IFileSysTestRunner cleanupRunner in _colRunnersForCleanup.Values)
                {
                    cleanupRunner.CleanUp();
                }
            }

            return activeRunDesc;
        }


        private Dictionary<string, int> CreateDictionary(List<TestInfo> validTests)
        {
            var rerunList = new Dictionary<string, int>();
            foreach (var item in validTests)
            {
                if (!rerunList.ContainsKey(item.TestPath))
                {
                    rerunList.Add(item.TestPath, 1);
                }
                else
                {
                    rerunList[item.TestPath]++;
                }
            }

            return rerunList;
        }

        /// <summary>
        /// Sets the test's inline parameters.
        /// </summary>
        /// <param name="testPath"></param>
        /// <param name="test"></param>
        /// <exception cref="ArgumentException"></exception>
        private static void SetInlineParams(string testPath, ref TestInfo test)
        {
            // the inline test path does not contain any parameter specification
            if (testPath.IndexOf("\"") == -1) return;

            // the inline test path does contain parameter specification
            string strParams = testPath.Substring(testPath.IndexOf("\"", StringComparison.Ordinal)).Trim();
            string[] @params = strParams.Split(new char[] { ',' }, StringSplitOptions.RemoveEmptyEntries);

            if (@params == null || @params.Length == 0) return;

            IList<string> paramNames, paramValues;

            if (!Helper.ValidateInlineParams(@params, out paramNames, out paramValues))
            {
                throw new ArgumentException();
            }

            string paramType;
            long _unused;
            for (int i = 0; i < paramNames.Count; ++i)
            {
                paramType = long.TryParse(paramValues[i], out _unused) ? "number" : "string";
                test.Params.Add(new TestParameterInfo() { Name = paramNames[i], Type = paramType, Value = paramValues[i] });
            }
        }

        /// <summary>
        /// Sets the test's parameters from the props (CI args).
        /// </summary>
        private static void SetPropsParams(List<TestParameter> @params, int idx, ref TestInfo test)
        {
            if (@params != null && @params.Count > 0)
            {
                // all the parameters that belong to this test
                List<TestParameter> testParams = @params.FindAll(elem => elem.TestIdx.Equals(idx));
                foreach (TestParameter param in testParams)
                {
                    test.Params.Add(new TestParameterInfo() { Name = param.ParamName, Type = param.ParamType, Value = param.ParamVal });
                }
            }

            return;
        }

        /// <summary>
        /// checks if timeout has expired
        /// </summary>
        /// <returns></returns>
        private bool CheckTimeout()
        {
            TimeSpan timeLeft = _timeout - _stopwatch.Elapsed;
            return (timeLeft > TimeSpan.Zero);
        }

        /// <summary>
        /// creates a correct type of runner and runs a single test.
        /// </summary>
        /// <param name="testInfo"></param>
        /// <param name="errorReason"></param>
        /// <returns></returns>
        private TestRunResults RunHpToolsTest(TestInfo testInfo, ref string errorReason, out Dictionary<string, string> outParams)
        {
            outParams = new Dictionary<string, string>();
            var testPath = testInfo.TestPath;

            var type = Helper.GetTestType(testPath);

            // if we have at least one environment for parallel runner,
            // then it must be enabled
            var isParallelRunnerEnabled = _parallelRunnerEnvironments.Count > 0;

            if (isParallelRunnerEnabled && type == TestType.QTP)
            {
                type = TestType.ParallelRunner;
            }
            // if the current test is an api test ignore the parallel runner flag
            // and just continue as usual
            else if (isParallelRunnerEnabled && type == TestType.ST)
            {
                ConsoleWriter.WriteLine("ParallelRunner does not support API tests, treating as normal test.");
            }

            IFileSysTestRunner runner = null;
            switch (type)
            {
                case TestType.ST:
                    runner = new ApiTestRunner(this, _timeout - _stopwatch.Elapsed, _encoding);
                    break;
                case TestType.QTP:
                    runner = new GuiTestRunner(this, _useUFTLicense, _timeout - _stopwatch.Elapsed, _uftRunMode, _mcConnection, _mobileInfoForAllGuiTests, _printInputParams);
                    break;
                case TestType.LoadRunner:
                    AppDomain.CurrentDomain.AssemblyResolve += Helper.HPToolsAssemblyResolver;
                    runner = new PerformanceTestRunner(this, _timeout, _pollingInterval, _perScenarioTimeOutMinutes, _ignoreErrorStrings, _displayController, _analysisTemplate, _summaryDataLogger, _scriptRTSSet);
                    break;
                case TestType.ParallelRunner:
                    runner = new ParallelTestRunner(this, _timeout - _stopwatch.Elapsed, _mcConnection, _mobileInfoForAllGuiTests, _parallelRunnerEnvironments);
                    break;
            }

            if (runner != null)
            {
                if (!_colRunnersForCleanup.ContainsKey(type))
                    _colRunnersForCleanup.Add(type, runner);

                Stopwatch s = Stopwatch.StartNew();

                var results = runner.RunTest(testInfo, ref errorReason, RunCancelled, out outParams);
                if (results.ErrorDesc != null && results.ErrorDesc.Equals(TestState.Error))
                {
                    Environment.Exit((int)Launcher.ExitCodeEnum.Failed);
                }

                results.Runtime = s.Elapsed;
                if (type == TestType.LoadRunner)
                    AppDomain.CurrentDomain.AssemblyResolve -= Helper.HPToolsAssemblyResolver;

                return results;
            }

            //check for abortion
            if (File.Exists(_abortFilename))
            {

                ConsoleWriter.WriteLine(Resources.GeneralStopAborted);

                //stop working 
                Environment.Exit((int)Launcher.ExitCodeEnum.Aborted);
            }

            return new TestRunResults { ErrorDesc = "Unknown TestType", TestState = TestState.Error };
        }


        /// <summary>
        /// checks if run was cancelled/aborted
        /// </summary>
        /// <returns></returns>
        public bool RunCancelled()
        {
            //if timeout has passed
            if (_stopwatch.Elapsed > _timeout && !_blnRunCancelled)
            {
                ConsoleWriter.WriteLine(Resources.SmallDoubleSeparator);
                ConsoleWriter.WriteLine(Resources.GeneralTimedOut);
                ConsoleWriter.WriteLine(Resources.SmallDoubleSeparator);

                Launcher.ExitCode = Launcher.ExitCodeEnum.Aborted;
                _blnRunCancelled = true;
            }

            return _blnRunCancelled;
        }

        /// <summary>
        /// sums errors and failed tests
        /// </summary>
        /// <param name="testState"></param>
        private void UpdateCounters(TestState testState, testsuite ts)
        {
            switch (testState)
            {
                case TestState.Error:
                    _errors++;
                    ts.errors++;
                    break;
                case TestState.Failed:
                    _fail++;
                    ts.failures++;
                    break;
                case TestState.Warning:
                    _warnings++;
                    break;
            }
        }


        /// <summary>
        /// Opens the report viewer for the given report directory
        /// </summary>
        /// <param name="reportDirectory"></param>
        public static void OpenReport(string reportDirectory)
        {
            Helper.OpenReport(reportDirectory, ref _uftViewerPath);
        }
    }
}
