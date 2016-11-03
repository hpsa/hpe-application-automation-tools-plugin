// (c) Copyright 2012 Hewlett-Packard Development Company, L.P. 
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

using System;

namespace HpToolsLauncher
{
    public class TestRunResults
    {
        public TestRunResults()
        {
            FatalErrors = -1;
        }

        private TestState m_enmTestState = TestState.Unknown;
        private TestState m_enmPrevTestState = TestState.Unknown;
        private bool m_hasWarnings = false;

        public bool HasWarnings
        {
            get { return m_hasWarnings; }
            set { m_hasWarnings = value; }
        }
     
        public string TestPath { get; set; }
        public string TestName { get; set; }
        public string TestGroup { get; set; }
        public string ErrorDesc { get; set; }
        public string FailureDesc { get; set; }
        public string ConsoleOut { get; set; }
        public string ConsoleErr { get; set; }
        public TimeSpan Runtime { get; set; }
        public string TestType { get; set; }
        public string ReportLocation { get; set; }
        public int FatalErrors { get; set; }
        public TestState TestState
        {
            get { return m_enmTestState; }
            set { m_enmTestState = value; }
        }

        public TestState PrevTestState
        {
            get { return m_enmPrevTestState; }
            set { m_enmPrevTestState = value; }
        }

        public int PrevRunId { get; set; }
    }
}
