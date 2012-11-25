// (c) Copyright 2012 Hewlett-Packard Development Company, L.P. 
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Diagnostics;
using HpToolsLauncher.Properties;

namespace HpToolsLauncher
{
    public class FileSystemTestsRunner : RunnerBase, IDisposable
    {
        #region Members

        
        private List<TestInstance> _tests;
        private static string _uftViewerPath;
        private int _errors, _fail;
        private bool _useUFTLicense;
        private TimeSpan _timeout = TimeSpan.MaxValue;
        private Stopwatch _stopwatch = null;
        private string _abortFilename = System.IO.Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location) + "\\stop" + Launcher.UniqueTimeStamp + ".txt";

        //saves runners for cleaning up at the end.
        private Dictionary<TestType, IFileSysTestRunner> _colRunnersForCleanup = new Dictionary<TestType, IFileSysTestRunner>();


        public const string UftJUnitRportName = "uftRunnerRoot";

        #endregion

        /// <summary>
        /// creates instance of the runner given a source.
        /// </summary>
        /// <param name="sources"></param>
        /// <param name="timeout"></param>
        /// <param name="backgroundWorker"></param>
        /// <param name="useUFTLicense"></param>
        public FileSystemTestsRunner(List<string> sources,
            TimeSpan timeout,
            bool useUFTLicense = false
            )
        {

            //search if we have any testing tools installed
            if (!Helper.IsTestingToolsInstalled(TestStorageType.FileSystem))
            {
                ConsoleWriter.WriteErrLine(Resources.FileSystemTestsRunner_No_HP_testing_tool_is_installed_on + System.Environment.MachineName);
                Environment.Exit((int)Launcher.ExitCodeEnum.Failed);
            }

            _timeout = timeout;
            _stopwatch = Stopwatch.StartNew();

            _useUFTLicense = useUFTLicense;
            _tests = new List<TestInstance>();

            List<string> testGroup;

            //go over all sources, and create a list of all tests
            foreach (string source in sources)
            {

                try
                {
                    //--handle directories which contain test subdirs (recursively)
                    if (Helper.IsDirectory(source))
                    {

                        testGroup = Helper.GetTestsLocations(source);
                    }
                    //--handle mtb files (which contain links to tests)
                    else
                    {
                        testGroup = new List<string>();
                        if (source.TrimEnd().EndsWith(".mtb", StringComparison.CurrentCultureIgnoreCase))
                        {
                            IMtbManager manager = new MtbManager();
                            testGroup = manager.Parse(source);
                        }
                    }
                }
                catch (Exception)
                {
                    testGroup = new List<string>();
                }

                //add all tests in group, with the root directory as a group
                if (testGroup.Count > 1)
                {
                    testGroup.ForEach(t =>
                    {
                        _tests.Add(new TestInstance(t, source.TrimEnd("\\/".ToCharArray()).Replace(".", "_")));
                    });
                }

                //--handle single test dir, add it with no group
                else if (testGroup.Count == 1)
                {
                    _tests.Add(new TestInstance(testGroup[0], "<None>"));
                }
            }

            if (_tests == null || _tests.Count == 0)
            {
                ConsoleWriter.WriteLine("===============================\nThere are no valid tests to run!\n===============================");
                Environment.Exit((int)Launcher.ExitCodeEnum.Failed);
            }

            ConsoleWriter.WriteLine(_tests.Count + " tests found:");
            _tests.ForEach(t => ConsoleWriter.WriteLine("" + t.TestName));
            ConsoleWriter.WriteLine("================================================");
        }

        /// <summary>
        /// runs all tests given to this runner and returns a suite of run resutls
        /// </summary>
        /// <returns>The rest run results for each test</returns>
        public override TestSuiteRunResults Run()
        {
            //create a new Run Results object
            TestSuiteRunResults activeRunDesc = new TestSuiteRunResults();

            double totalTime = 0;
            try
            {
                var start = DateTime.Now;
                foreach (var test in _tests)
                {
                    if (RunCancelled()) break;

                    var testStart = DateTime.Now;

                    string errorReason = string.Empty;
                    TestRunResults runResult = null;
                    try
                    {
                        runResult = RunUftTest(test.TestName, ref errorReason);
                    }
                    catch (Exception ex)
                    {
                        runResult = new TestRunResults();
                        runResult.TestState = TestState.Error;
                        runResult.ErrorDesc = ex.Message;
                        runResult.TestName = test.TestName;
                    }

                    //get the original source for this test, for grouping tests under test classes
                    runResult.TestGroup = test.TestSource;

                    activeRunDesc.TestRuns.Add(runResult);

                    //if fail was dtermind before this step, continue
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
                                if (RunCancelled())
                                {
                                    runResult.ErrorDesc = HpToolsLauncher.Properties.Resources.ExceptionUserCancelled;
                                }
                                else
                                {
                                    runResult.ErrorDesc = HpToolsLauncher.Properties.Resources.ExceptionExternalProcess;
                                }
                            }
                            runResult.ReportLocation = null;
                            runResult.TestState = TestState.Error;
                        }
                    }

                    if (runResult.TestState == TestState.Passed && runResult.HasWarnings)
                    {
                        ConsoleWriter.WriteLine("Test result: Succeeded with Warnings");
                    }
                    else
                    {
                        ConsoleWriter.WriteLine("Test result: " + runResult.TestState);
                    }

                    ConsoleWriter.WriteLine(DateTime.Now.ToString(Launcher.DateFormat) + " Test complete: " + runResult.TestPath + "\n-------------------------------------------------------------------------------------------------------");

                    UpdateCounters(runResult.TestState);
                    var testTotalTime = (DateTime.Now - testStart).TotalSeconds;
                }
                totalTime = (DateTime.Now - start).TotalSeconds;
            }
            finally
            {
                activeRunDesc.NumTests = _tests.Count;
                activeRunDesc.NumErrors = _errors;
                activeRunDesc.TotalRunTime = TimeSpan.FromSeconds(totalTime);
                activeRunDesc.NumFailures = _fail;

                foreach (IFileSysTestRunner cleanupRunner in _colRunnersForCleanup.Values)
                {
                    cleanupRunner.CleanUp();
                }
            }

            return activeRunDesc;
        }

        /// <summary>
        /// checks if timeout has expired
        /// </summary>
        /// <returns></returns>
        private bool CheckTimeout()
        {
            TimeSpan timeleft = _timeout - _stopwatch.Elapsed;
            return (timeleft > TimeSpan.Zero);
        }

        /// <summary>
        /// creates a correct type of runner and runs a single test.
        /// </summary>
        /// <param name="testPath"></param>
        /// <param name="errorReason"></param>
        /// <returns></returns>
        private TestRunResults RunUftTest(string testPath, ref string errorReason)
        {
            var type = Helper.GetTestType(testPath);
            IFileSysTestRunner runner = null;
            switch (type)
            {
                case TestType.ST:
                    runner = new ApiTestRunner(this, _timeout - _stopwatch.Elapsed);
                    break;
                case TestType.QTP:
                    runner = new GuiTestRunner(this, _useUFTLicense, _timeout - _stopwatch.Elapsed);
                    break;
            }


            if (runner != null)
            {
                if (!_colRunnersForCleanup.ContainsKey(type))
                    _colRunnersForCleanup.Add(type, runner);

                Stopwatch s = Stopwatch.StartNew();
                var results = runner.RunTest(testPath, ref errorReason, RunCancelled);
                results.Runtime = s.Elapsed;


                return results;
            }

            //check for abortion
            if (System.IO.File.Exists(_abortFilename))
            {

                ConsoleWriter.WriteLine("Test run Was aborted by user, stopping all tests.");

                //remove the file (got the message)
                System.IO.File.Delete(_abortFilename);

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
            if (_stopwatch.Elapsed > _timeout)
            {
                if (!_blnRunCancelled)
                {
                    ConsoleWriter.WriteLine("==============\nJob timed out!\n==============");

                    Launcher.ExitCode = Launcher.ExitCodeEnum.Aborted;
                    _blnRunCancelled = true;
                }
            }

            if (System.IO.File.Exists(_abortFilename))
            {
                if (!_blnRunCancelled)
                {
                    ConsoleWriter.WriteLine("========================\nJob was aborted by user!\n========================");
                    Launcher.ExitCode = Launcher.ExitCodeEnum.Aborted;
                    _blnRunCancelled = true;
                }
            }
            return _blnRunCancelled;
        }

        /// <summary>
        /// sums errors and failed tests
        /// </summary>
        /// <param name="testState"></param>
        private void UpdateCounters(TestState testState)
        {
            switch (testState)
            {
                case TestState.Error:
                    _errors += 1;
                    break;
                case TestState.Failed:
                    _fail += 1;
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

        /// <summary>
        /// an internall class which holds info about the test instance before it's run, mainly used for describing it's group
        /// </summary>
        private class TestInstance
        {
            string m_testName;

            public string TestName
            {
                get { return m_testName; }
                set { m_testName = value; }
            }
            string m_testSource;

            public string TestSource
            {
                get { return m_testSource; }
                set { m_testSource = value; }
            }

            public TestInstance(string testName, string source)
            {
                TestName = testName;
                m_testSource = source;
            }
        }
    }
}
