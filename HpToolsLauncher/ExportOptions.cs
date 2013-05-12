// (c) Copyright 2012 Hewlett-Packard Development Company, L.P. 
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

namespace HpToolsLauncher
{
    enum ExportTypeSuffixEnum
    {
        ExportTypeSuffix_SD, //step details
        ExportTypeSuffix_DT, //data table
        ExportTypeSuffix_SM, //system monitor
        ExportTypeSuffix_SR, //screen recorder
        ExportTypeSuffix_LT  //log tracking
    }

    public class ExportOptions
    {
        public const string ExportDataTable = "ExportDataTable";
        public const string ExportForFailed = "ExportForFailed";
        public const string ExportLogTracking = "ExportLogTracking";
        public const string ExportScreenRecorder = "ExportScreenRecorder";
        public const string ExportStepDetails = "ExportStepDetails";
        public const string ExportSystemMonitor = "ExportSystemMonitor";
        public const string ExportLocation = "ExportLocation";
        public const string XslPath = "XSLPath";
        public const string ExportFormat = "ExportFormat";
        public const string ExportType = "ExportType";

    }
}