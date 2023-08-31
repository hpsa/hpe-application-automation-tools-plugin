/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2023 Micro Focus or one of its affiliates.
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

using HpToolsLauncher.TestRunners;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Xml.Linq;
using System.Xml.Schema;
using HpToolsLauncher.Properties;
using HpToolsLauncher.Utils;

namespace HpToolsLauncher
{
    public class TestInfo
    {
        private const string TEST_INPUT_PARAMETERS_XML = "TestInputParameters.xml";
        private const string SCHEMA = "Schema";
        private const string XS = "xs";
        private const string ARGUMENTS = "Arguments";
        private const string ELEMENT = "element";
        private const string NAME = "name";
        private const string TYPE = "type";
        private const string DATETIME = "datetime";
        private const string DATE = "date";
        private const string BOOLEAN = "boolean";
        private const string YYYY_MM_ddTHH_mm_ss = "yyyy-MM-ddTHH:mm:ss";
        private const string VALUES = "Values";
        private const string COLON = ":";
        private const char DOT = '.';
        private const char UNDERSCORE = '_';
        private readonly char[] _slashes = "\\/".ToCharArray();

        public TestInfo(string testPath)
        {
            TestPath = testPath;
        }

        public string GenerateAPITestXmlForTest(Dictionary<string, object> paramDict, bool printInputParams)
        {
            string paramXmlFileName = Path.Combine(TestPath, TEST_INPUT_PARAMETERS_XML);
            XDocument doc = XDocument.Load(paramXmlFileName);
            var schemaTmp = doc.Descendants(SCHEMA).First().Elements().First();
            var paramNs = schemaTmp.GetNamespaceOfPrefix(XS);
            string schemaStr = schemaTmp.ToString();
            XElement xArgs = doc.Descendants(ARGUMENTS).FirstOrDefault();

            // parameter specifications according to schema
            var xParams = doc.Descendants(paramNs + ELEMENT)
                .Where(node => ARGUMENTS == (string)node.Attribute(NAME))
                .Descendants(paramNs + ELEMENT).ToList();

            if (xArgs != null && xParams.Count != 0)
                foreach (XElement arg in xArgs.Elements())
                {
                    string paramName = arg.Name.ToString();

                    if (!paramDict.ContainsKey(paramName))
                    {
                        continue;
                    }

                    var param = paramDict[paramName];

                    // spec belonging to this parameter
                    var paramSpec = xParams.Find(elem => paramName == (string)elem.Attribute(NAME));
                    // its type
                    string paramType;
                    if (paramSpec != null)
                    {
                        var typeSpec = paramSpec.Attribute(TYPE);

                        if (typeSpec != null)
                        {
                            var tmpVal = typeSpec.Value;
                            var startIdx = tmpVal.IndexOf(COLON, StringComparison.Ordinal);
                            paramType = typeSpec.Value.Substring(startIdx != 0 ? startIdx + 1 : startIdx);
                        }
                        else continue;
                    }
                    else continue; // no spec found for given parameter, skipping

                    // verify its type according to the spec
                    if (!ApiTestRunner.VerifyParameterValueType(param, paramType))
                    {
                        ConsoleWriter.WriteErrLine(string.Format(Resources.GeneralParameterTypeMismatchWith2Types, paramName, paramType, param.GetType()));
                    }
                    else
                    {
                        arg.Value = NormalizeParamValue(paramName, param, paramType);
                        if (printInputParams)
                            ConsoleWriter.WriteLine(string.Format(Resources.GeneralParameterUsage, paramName, paramType.ToLower().In(DATETIME, DATE) ? ((DateTime)param).ToShortDateString() : param));
                    }
                }

            string argumentSectionStr = doc.Descendants(VALUES).First().Elements().First().ToString();
            try
            {
                XDocument doc1 = XDocument.Parse(argumentSectionStr);
                XmlSchema schema = XmlSchema.Read(new MemoryStream(Encoding.ASCII.GetBytes(schemaStr), false), null);

                XmlSchemaSet schemas = new XmlSchemaSet();
                schemas.Add(schema);

                string validationMessages = string.Empty;
                doc1.Validate(schemas, (o, e) =>
                {
                    validationMessages += e.Message + Environment.NewLine;
                });

                if (!string.IsNullOrWhiteSpace(validationMessages))
				{
                    ConsoleWriter.WriteErrLine("Parameter schema validation errors, falling back to default parameter definitions: \n" + validationMessages);
                    return string.Empty;
                }
            }
            catch
            {
                ConsoleWriter.WriteErrLine("An error occured while creating ST parameter file, check the validity of TestInputParameters.xml in your test directory and of your mtbx file");
            }

            return doc.ToString();
        }

        private string NormalizeParamValue(string name, object param, string type)
        {
            switch (type.ToLower())
            {
                case DATETIME:
                case DATE:
                    try
                    {
                        return ((DateTime)param).ToString(YYYY_MM_ddTHH_mm_ss);
                    }
                    catch
                    {
                        ConsoleWriter.WriteErrLine("Incorrect dateTime value format in parameter: " + name);
                        return string.Empty;
                    }
                case BOOLEAN:
                    return param.ToString().ToLower();
                default:
                    return param.ToString();
            }
        }

        public TestInfo(string testPath, string testName) : this(testPath)
        {
            TestName = testName;
        }

        public TestInfo(string testPath, string testName, string testGroup): this(testPath, testName)
        {
            TestGroup = testGroup;
        }

        public TestInfo(string testPath, string testName, string testGroup, string testId) : this(testPath, testName, testGroup)
        {
            TestId = testId;
        }

        public TestInfo(string testId, TestInfo test)
        {
            TestId = testId;
            TestName = test.TestName;
            TestPath = test.TestPath;
            TestGroup = test.TestGroup;
            _params = test.Params;
            ReportPath = test.ReportPath;
            DataTablePath = test.DataTablePath;
            IterationInfo = test.IterationInfo;
        }

        private List<TestParameterInfo> _params = new List<TestParameterInfo>();
        string _testName;
        string _testGroup;
        string _dataTablePath;
        IterationInfo _iterationInfo;

        public string TestGroup
        {
            get { return _testGroup; }
            set { _testGroup = value.TrimEnd(_slashes).Replace(DOT, UNDERSCORE); }
        }

        public string TestName
        {
            get { return _testName; }
            set { _testName = value; }
        }
        string _testPath;

        public string TestPath
        {
            get { return _testPath; }
            set { _testPath = value; }
        }

        // the path where the report will be saved
        public string ReportPath { get; set; }

        public string TestId { get; set; }

        public List<TestParameterInfo> Params
        {
            get { return _params; }
            set { _params = value; }
        }

        public string DataTablePath
        {
            get { return _dataTablePath; }
            set { _dataTablePath = value; }
        }

        public IterationInfo IterationInfo
        {
            get { return _iterationInfo; }
            set { _iterationInfo = value; }
        }

        internal Dictionary<string, object> GetParameterDictionaryForQTP()
        {
            Dictionary<string, object> retval = new Dictionary<string, object>();
            foreach (var param in _params)
            {
                object val = param.ParseValue();
                retval.Add(param.Name, val);
            }
            return retval;
        }
    }
}
