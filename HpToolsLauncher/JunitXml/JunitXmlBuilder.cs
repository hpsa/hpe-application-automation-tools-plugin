// (c) Copyright 2012 Hewlett-Packard Development Company, L.P. 
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

using System.IO;
using System.Xml.Serialization;

namespace HpToolsLauncher
{
    public class JunitXmlBuilder : IXmlBuilder
    {
        private string _xmlName = "APIResults.xml";

        public string XmlName
        {
            get { return _xmlName; }
            set { _xmlName = value; }
        }
        //public const string ClassName = "uftRunner";
        public const string ClassName = "HPToolsFileSystemRunner";
        public const string RootName = "uftRunnerRoot";

        XmlSerializer _serializer = new XmlSerializer(typeof(testsuites));

        testsuites _testSuites = new testsuites();


        public JunitXmlBuilder()
        {
            _testSuites.name = RootName;
        }

        /// <summary>
        /// converts all data from the test resutls in to the Junit xml format and writes the xml file to disk.
        /// </summary>
        /// <param name="results"></param>
        public void CreateXmlFromRunResults(TestSuiteRunResults results)
        {
            _testSuites = new testsuites();

            testsuite ts = new testsuite
            {
                errors = results.NumErrors.ToString(),
                tests = results.NumTests.ToString(),
                failures = results.NumFailures.ToString(),
                name = results.SuiteName,
                package = ClassName
            };
            foreach (TestRunResults testRes in results.TestRuns)
            {
                testcase tc;
                if (testRes.TestType == TestType.LoadRunner.ToString())
                {
                    tc = CreateXmlFromLRRunResults(testRes);
                }
                else
                {
                    tc = CreateXmlFromUFTRunResults(testRes);
                }
                ts.AddTestCase(tc);
            }
            _testSuites.AddTestsuite(ts);

            if (File.Exists(XmlName))
                File.Delete(XmlName);

            using (Stream s = File.OpenWrite(XmlName))
            {
                _serializer.Serialize(s, _testSuites);
            }
        }

        private testcase CreateXmlFromLRRunResults(TestRunResults testRes)
        {
            return CreateXmlFromUFTRunResults(testRes);
            
        }

        private testcase CreateXmlFromUFTRunResults(TestRunResults testRes)
        {
            testcase tc = new testcase
            {
                systemout = testRes.ConsoleOut,
                systemerr = testRes.ConsoleErr,
                report = testRes.ReportLocation,
                classname = "All-Tests." + ((testRes.TestGroup == null) ? "" : testRes.TestGroup.Replace(".", "_")),
                name = testRes.TestPath,
                type = testRes.TestType,
                time = testRes.Runtime.TotalSeconds.ToString()
            };

            if (!string.IsNullOrWhiteSpace(testRes.FailureDesc))
                tc.AddFailure(new failure { message = testRes.FailureDesc });

            switch (testRes.TestState)
            {
                case TestState.Passed:
                    tc.status = "pass";
                    break;
                case TestState.Failed:
                    tc.status = "fail";
                    break;
                case TestState.Error:
                    tc.status = "error";
                    break;
                case TestState.Warning:
                    tc.status = "warning";
                    break;
                default:
                    tc.status = "pass";
                    break;
            }
            if (!string.IsNullOrWhiteSpace(testRes.ErrorDesc))
                tc.AddError(new error { message = testRes.ErrorDesc });
            return tc;
        }




    }
}
