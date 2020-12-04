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
using System.ComponentModel;
using System.Diagnostics;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;

namespace HpToolsLauncher
{
    public static class ProcessExtensions
    {
        /// <summary>
        /// Get the parent process for a given process handle.
        /// </summary>
        /// <param name="hProcess">the process handle</param>
        /// <returns>The parent process</returns>
        private static Process GetParentProcess(IntPtr hProcess)
        {
            NativeProcess.PROCESS_BASIC_INFORMATION pbi = new NativeProcess.PROCESS_BASIC_INFORMATION();
            int pbiLength = Marshal.SizeOf(pbi);
            int returnLength = 0;

            int status = NativeProcess.NtQueryInformationProcess(hProcess,NativeProcess.PROCESSINFOCLASS.ProcessBasicInformation,
                ref pbi,pbiLength,out returnLength);

            if(status != 0)
            {
                throw new Win32Exception(status);
            }

            try
            {
                return Process.GetProcessById(pbi.InheritedFromUniqueProcessId.ToInt32());
            }
            catch (ArgumentException)
            { // Not found
                return null;
            }
        }
        /// <summary>
        /// Returns the parent process of a given process
        /// </summary>
        /// <param name="process">the process for which to find the parent</param>
        /// <returns>the parent process</returns>
        public static Process Parent(this Process process)
        {
            return GetParentProcess(process.Handle);
        }
    }
}
