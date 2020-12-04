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
using System.Linq;
using System.Runtime.Serialization;
using System.Text;

namespace HpToolsLauncher.ParallelTestRunConfiguraion
{
    public class UFTSettings
    {
        public MCSettings mc { get; set; }
    }
    public class MCSettings
    {
        public string protocol { get; set; }
        public string hostname { get; set; }
        public int port { get; set; }
        public string pathname { get; set; }
        public string account { get; set; }
        public string username { get; set; }
        public string password { get; set; }
        public string tenantId { get; set; }
        public string workspaceId { get; set; }
        public ProxySettings proxy { get; set; }
    }
    public class ProxySettings
    {
        public string type { get; set; }
        public string hostname { get; set; }
        public int port { get; set; }
        public string pathname { get; set; }
        public AuthenticationSettings authentication { get; set; }
    }
    public class AuthenticationSettings
    {
        public string username { get; set; }
        public string password { get; set; }
    }

    public class TestRunConfigurationBase
    {
        /// <summary>
        /// The Report Path
        /// </summary>
        public string reportPath { get; set; }
        /// <summary>
        /// The Log Folder
        /// </summary>
        public string logFolder { get; set; }
        /// <summary>
        /// The Log Level
        /// </summary>
        public string logLevel { get; set; }
        /// <summary>
        /// Run Mode: Normal|Fast , default is Fast
        /// </summary>
        public string runMode { get; set; }

        public UFTSettings settings { get; set; }
    }

    public class ParallelTestRunConfiguration : TestRunConfigurationBase
    {
        /// <summary>
        /// The Test Runs
        /// </summary>
        public ParallelTestRunConfigurationItem[] parallelRuns { get; set; }
    }

    /// <summary>
    /// TestRun Configuration for qtdrv
    /// </summary>
    public class TestRunConfiguration : TestRunConfigurationBase
    {
        /// <summary>
        /// GUI Test Path
        /// e.g. test = C:\GUITest1
        /// </summary>
        public string test { get; set; }
        /// <summary>
        /// The Web|Mobile Environment (Optional)
        /// </summary>
        public Environment env { get; set; }
    }

    /// <summary>
    /// Test run result for ParallelRunner
    /// </summary>
    public class RunResult : TestRunConfiguration
    {
        public string errorMessage { get; set; }
    }

    /// <summary>
    /// TestRun Configuration for ParallelRunner
    /// </summary>
    public class ParallelTestRunConfigurationItem : TestRunConfiguration
    {
        /// <summary>
        /// Report Suffix (Optional)
        /// e.g. reportSuffix= ID_C3ZDU,reportPath = C:\parallelexecution\Res1, test= C:\GUITest1
        /// Full ReportPath = C:\parallelexecution\Res1\GUITest1_ID_C3ZDU
        /// </summary>
        public string reportSuffix { get; set; }
    }

    public class Environment
    {
        /// <summary>
        /// Web Environment
        /// </summary>
        public WebEnvironment web { get; set; }
        /// <summary>
        /// Mobile Environment
        /// </summary>
        public MobileEnvironment mobile { get; set; }
    }

    #region Web Environment
    /// <summary>
    /// SRF Web Desktop Environment
    /// </summary>
    public class WebEnvironment
    {
        /// <summary>
        ///  Lab "LocalBrowser"|"MobileCenter"|"SRF"
        /// </summary>
        public string lab { get; set; }
        public string platform { get; set; }
        public string browser { get; set; }
        public string resolution { get; set; }
        public string tunnelName { get; set; }
    }

    #endregion

    #region Mobile Environment
    public class MobileEnvironment
    {
        /// <summary>
        ///  Lab "Disable"|"MobileCenter"|"SRF"
        /// </summary>
        public string lab { get; set; }
        public Device device { get; set; }
    }

    /// <summary>
    /// Device Info
    /// e.g.
    /// "deviceID": "EYKNTO6999999999",
    /// "model": "8681-A01",
    /// "osVersion": "5.1",
    /// "osType": "ANDROID",
    /// "manufacturer": "QiKU"
    /// </summary>
    public class Device
    {
        public string deviceID { get; set; }
        public string manufacturer { get; set; }
        public string model { get; set; }
        public string osVersion { get; set; }
        public string osType { get; set; }
    }
    #endregion
}
