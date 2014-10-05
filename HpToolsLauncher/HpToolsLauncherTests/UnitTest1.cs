// (c) Copyright 2012 Hewlett-Packard Development Company, L.P. 
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

using System;
using System.Text;
using System.Collections.Generic;
using System.Linq;

using Microsoft.VisualStudio.TestTools.UnitTesting;
using HpToolsLauncher;
using System.IO;
using System.Xml.Linq;

namespace HpToolsLauncherTests
{
    [TestClass]
    public class UnitTest1
    {
        string testIniFile = @"c:\stam.ini";
        string testPropertiesFile = @"c:\tempPropFile.txt";

        [TestInitialize]
        public void TestsInit()
        {
            File.WriteAllText(testIniFile, "[Files]\nTest1=b\nTest2=c\n\n[Test1]\nparam1=\"\"=\\=ss=sא\nparam2=bbb\n\n[Test2]\nparaאm1=tעtt");
            File.WriteAllText(testPropertiesFile, "#\n#Thu Sep 06 11:36:38 IDT 2012\na\\==\\=\"''b\nasd\\u05D2asd=\\u05D2\\u05D3\\u05DB\\u05D3\\u05D2\\u05DB");
        }

        [TestCleanup]
        public void TestsCleanUp()
        {
            File.Delete(testIniFile);
            File.Delete(testPropertiesFile);
        }


        [TestMethod]
        public void TestQcTestSetFolderFromAPIRunner()
        {
            string file1 = "c:\\stam1.ini";
            JavaProperties props = new JavaProperties();
            props["TestSet1"] = "Aaron\\";
            props["TestSet2"] = "Tomer";
            props["almServer"] = "http://vmsoa22:8080/qcbin";
            props["almUser"] = "sa";
            props["almPassword"] = "";
            props["almDomain"] = "Default";
            props["almProject"] = "Aaron";
            props["almRunMode"] = "RUN_LOCAL";
            props["almTimeout"] = "-1";
            props["almRunHost"] = "";
            props.Save(file1, "");
            Launcher runner = new Launcher("false", file1, TestStorageType.Alm);

            runner.Run();
        }

        [TestMethod]
        public void TestMtbxReadFile()
        {
            string content = "<Mtbx><Test name=\"test1\" path=\"${workspace}\\test1\"><Parameter name=\"mee\" value=\"12\" type=\"int\"/>		<Parameter name=\"mee1\" value=\"12.0\" type=\"double\"/><Parameter name=\"mee2\" value=\"abc\" type=\"string\"/></Test><Test name=\"test2\" path=\"${workspace}\\test2\"><Parameter name=\"mee\" value=\"12\" type=\"int\"/><Parameter name=\"mee1\" value=\"12.0\" type=\"double\"/>		<Parameter name=\"mee2\" value=\"abc\" type=\"string\"/><Parameter name=\"mee3\" value=\"123.5\" type=\"float\"/>	</Test></Mtbx>";
            List<TestInfo> tests = MtbxManager.LoadMtbx(content, "TestGroup1");
            Assert.IsTrue(tests.Count == 2);
        }

        [TestMethod]
        public void TestGenerateApiXmlFile()
        {
            string content = "<Mtbx><Test name=\"test1\" path=\"${workspace}\\test1\"><Parameter name=\"mee\" value=\"12\" type=\"int\"/>		<Parameter name=\"mee1\" value=\"12.0\" type=\"double\"/><Parameter name=\"mee2\" value=\"abc\" type=\"string\"/></Test><Test name=\"test2\" path=\"${workspace}\\test2\"><Parameter name=\"mee\" value=\"12\" type=\"int\"/><Parameter name=\"mee1\" value=\"12.0\" type=\"double\"/>		<Parameter name=\"mee2\" value=\"abc\" type=\"string\"/><Parameter name=\"mee3\" value=\"123.5\" type=\"float\"/>	</Test></Mtbx>";
            List<TestInfo> tests = MtbxManager.LoadMtbx(content, "dunno");
            string xmlContent = tests[0].GenerateAPITestXmlForTest();
            //XDocument doc = XDocument.Parse(xmlContent);
            Assert.IsTrue(xmlContent.Contains("<mee2>abc</mee2>"));
            Assert.IsTrue(xmlContent.Contains("name=\"mee\" type=\"xs:int\""));
        }

        [TestMethod]
        public void TestQcTestRunFromAPIRunner()
        {
            string file1 = "c:\\stam1.ini";
            JavaProperties props = new JavaProperties();
            props["TestSet1"] = "Aaron\\Amit";
            props["almServer"] = "http://vmsoa22:8080/qcbin";
            props["almUser"] = "sa";
            props["almPassword"] = "";
            props["almDomain"] = "Default";
            props["almProject"] = "Aaron";
            props["almRunMode"] = "RUN_LOCAL";
            props["almTimeout"] = "-1";
            props["almRunHost"] = "";
            props.Save(file1, "");
            Launcher runner = new Launcher("false", file1, TestStorageType.Alm);

            runner.Run();
        }

        [TestMethod]
        public void TestGetTestStateFromReport()
        {
            TestRunResults res = new TestRunResults { ReportLocation = @"c:\Temp\report\" };
            TestState state = Helper.GetTestStateFromReport(res);
        }

        [TestMethod]
        public void TestQcTestRun()
        {
            AlmTestSetsRunner runner = new AlmTestSetsRunner("http://vmsoa22:8080/qcbin/",
                "sa",
                "",
                "DEFAULT",
                "Aaron",
                100000,
                QcRunMode.RUN_LOCAL,
                null,
                new List<string> { "Aaron\\Amit" });

            if (runner.Connected)
                runner.Run();

            //runner.RunTestSet(
            //    "Aaron",
            //    "Amit",
            //    100000,
            //    QcRunMode.RUN_LOCAL,
            //    null);
        }

        //[TestMethod]
        //public void RunAPITestWithParams()
        //{
        //    var jenkinsEnv = new Dictionary<string,string>();
        //    jenkinsEnv.Add("workspace","c:\\tests");
        //    FileSystemTestsRunner runner = new FileSystemTestsRunner(new List<string>() { @"c:\workspace\mtbx\stam.mtbx" }, TimeSpan.FromMinutes(10), 500, TimeSpan.FromMinutes(10), new List<string>() { "" }, jenkinsEnv);
        //    runner.Run();
        //}

        //[TestMethod]
        //public void RunGUITestWithParams()
        //{ 
        //}

        [TestMethod]
        public void TestJavaPropertiesSaveAndLoad()
        {
            JavaProperties props = new JavaProperties();
            JavaProperties props1 = new JavaProperties();
            props.Add("a=", "b=\"''");
            props.Store("c:\\tempPropFileCsharp.txt", "comment1");
            props1.Load("c:\\tempPropFileCsharp.txt");
            File.Delete("c:\\tempPropFileCsharp.txt");
            Assert.AreEqual(props1["a="], props["a="]);
        }

        [TestMethod]
        public void TestIniFile()
        {
            try
            {
                IniManager m = new IniManager(testIniFile);
                HashSet<string> sects = m.GetSectionNames();
                HashSet<string> fileEnts = m.GetEntryNames("Files");
                Dictionary<string, string> FilesDict = m.GetSectionAsDictionary("Files");
                Dictionary<string, string> Test1Dict = m.GetSectionAsDictionary("Test1");
                Dictionary<string, string> Test2Dict = m.GetSectionAsDictionary("Test2");
                var dict = IniManager.LoadIniFileAsDictionary(testIniFile);

                Assert.IsTrue(FilesDict.Count > 0);
                Assert.IsTrue(Test1Dict.Count > 0);
                Assert.IsTrue(dict.Count > 0);
            }
            catch
            {
                Assert.Fail();
            }
        }

    }
}
