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
using System.Linq;
using System.Text;

namespace HpToolsLauncher
{
    public interface IProcessAdapter
    {
        int ExitCode { get; }

        bool HasExited { get; }

        void Start();

        void WaitForExit();

        bool WaitForExit(int milliseconds);

        void Kill();

        void Close();
    }

    public class ProcessAdapter : IProcessAdapter
    {
        private Process Process { get; set; }

        public int ExitCode
        {
            get
            {
                return Process.ExitCode;
            }
        }

        public bool HasExited
        {
            get
            {
                return Process.HasExited;
            }
        }

        public ProcessAdapter(Process process)  { Process = process; }

        public void Start()
        {
            Process.Start();
        }

        public void WaitForExit()
        {
            Process.WaitForExit();
        }

        public bool WaitForExit(int milliseconds)
        {
            return Process.WaitForExit(milliseconds);
        }

        public void Kill()
        {
            Process.Kill();
        }

        public void Close()
        {
            Process.Close();
        }
    }

    public class ElevatedProcessAdapter : IProcessAdapter
    {
        private ElevatedProcess ElevatedProcess { get; set; }

        public int ExitCode
        {
            get
            {
                return ElevatedProcess.ExitCode;
            }
        }

        public bool HasExited
        {
            get
            {
                return ElevatedProcess.HasExited;
            }
        }

        public ElevatedProcessAdapter(ElevatedProcess elevatedProcess)  { this.ElevatedProcess = elevatedProcess; }

        public void Start()
        {
            ElevatedProcess.StartElevated();
        }

        public void WaitForExit()
        {
            ElevatedProcess.WaitForExit();
        }

        public bool WaitForExit(int milliseconds)
        {
            return ElevatedProcess.WaitForExit(milliseconds);
        }

        public void Kill()
        {
            ElevatedProcess.Kill();
        }

        public void Close()
        {
            ElevatedProcess.Close();
        }
    }

    public static class ProcessAdapterFactory
    {
        /// <summary>
        /// Create a process adapter based on the type of process.
        /// </summary>
        /// <param name="process">the process object</param>
        /// <returns>an adapter for the given process, null if no adapter available</returns>
        public static IProcessAdapter CreateAdapter(object process)
        { 
            if (process is Process) return new ProcessAdapter((Process)process);
            if (process is ElevatedProcess) return new ElevatedProcessAdapter((ElevatedProcess)process );

            return null;
        }
    }
}
