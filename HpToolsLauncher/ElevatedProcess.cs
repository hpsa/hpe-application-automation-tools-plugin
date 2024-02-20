/*
 * Certain versions of software accessible here may contain branding from Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.
 * This software was acquired by Micro Focus on September 1, 2017, and is now offered by OpenText.
 * Any reference to the HP and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright 2012-2023 Open Text
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors ("Open Text") are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ___________________________________________________________________
 */

using System;
using System.Diagnostics;
using System.Linq;
using System.Runtime.InteropServices;

namespace HpToolsLauncher
{
    [Serializable]
    public class ElevatedProcessException : Exception
    {
        public ElevatedProcessException(string message) : base(message) { }
        public ElevatedProcessException(string message, Exception innerException) : base(message, innerException) { }
    }

    public class ElevatedProcess : IDisposable
    {
        private readonly string _path;
        private readonly string _arguments;
        private readonly string _workDirectory;
        private NativeProcess.PROCESS_INFORMATION _processInformation;
        private const uint STILL_ACTIVE = 259;
        private const uint INFINITE = 0xFFFFFFFF;

        public ElevatedProcess(string path, string arguments, string workDirectory)
        {
            _path = path;
            _arguments = arguments;
            _workDirectory = workDirectory;
        }

        private int GetExitCode()
        {
            uint exitCode;
            if (!NativeProcess.GetExitCodeProcess(_processInformation.hProcess, out exitCode))
            {
                return 0;
            }

            return (int)exitCode;
        }

        public int ExitCode
        {
            get
            {
                return GetExitCode();
            }
        }

        public bool HasExited
        {
            get
            {
                return GetExitCode() != STILL_ACTIVE;
            }
        }

        public void StartElevated()
        {
            Process process;
            try
            {
                process = Process.GetProcessesByName("explorer").FirstOrDefault();
            }
            catch (InvalidOperationException e)
            {
                throw new ElevatedProcessException("An error has occurred while trying to find the 'explorer' process: ", e);
            }

            if (process == null)
            {
                throw new ElevatedProcessException("No process with the name 'explorer' found!");
            }

            // we can retrieve the token information from explorer
            int explorerPid = process.Id;

            // open the explorer process with the necessary flags
            IntPtr hProcess = NativeProcess.OpenProcess(NativeProcess.ProcessAccessFlags.DuplicateHandle | NativeProcess.ProcessAccessFlags.QueryInformation, false, explorerPid);

            if (hProcess == IntPtr.Zero)
            {
                throw new ElevatedProcessException("OpenProcess() failed with error code: " + Marshal.GetLastWin32Error());
            }

            IntPtr hUser;

            // get the secondary token from the explorer process
            if (!NativeProcess.OpenProcessToken(hProcess, NativeProcess.TOKEN_QUERY | NativeProcess.TOKEN_DUPLICATE | NativeProcess.TOKEN_ASSIGN_PRIMARY, out hUser))
            {
                NativeProcess.CloseHandle(hProcess);

                throw new ElevatedProcessException("OpenProcessToken() failed with error code: " + Marshal.GetLastWin32Error());
            }

            IntPtr userToken;

            // convert the secondary token to a primary token
            if (!NativeProcess.DuplicateTokenEx(hUser, NativeProcess.MAXIMUM_ALLOWED, IntPtr.Zero, NativeProcess.SECURITY_IMPERSONATION_LEVEL.SecurityIdentification,
                NativeProcess.TOKEN_TYPE.TokenPrimary, out userToken))
            {
                NativeProcess.CloseHandle(hUser);
                NativeProcess.CloseHandle(hProcess);

                throw new ElevatedProcessException("DuplicateTokenEx() failed with error code: " + Marshal.GetLastWin32Error());
            }

            // the explorer session id will be used in order to launch the given executable
            uint sessionId;

            if (!NativeProcess.ProcessIdToSessionId((uint)explorerPid, out sessionId))
            {
                throw new ElevatedProcessException("ProcessIdToSessionId() failed with error code: " + Marshal.GetLastWin32Error());
            }

            uint tokenInformationLen = (uint)Marshal.SizeOf(sessionId);

            // set the session id
            if (!NativeProcess.SetTokenInformation(userToken, NativeProcess.TOKEN_INFORMATION_CLASS.TokenSessionId, ref sessionId, tokenInformationLen))
            {
                NativeProcess.CloseHandle(hUser);
                NativeProcess.CloseHandle(hProcess);
                NativeProcess.CloseHandle(userToken);

                throw new ElevatedProcessException("SetTokenInformation failed with: " + Marshal.GetLastWin32Error());
            }

            if (!NativeProcess.ImpersonateLoggedOnUser(userToken))
            {
                NativeProcess.CloseHandle(hUser);
                NativeProcess.CloseHandle(hProcess);
                NativeProcess.CloseHandle(userToken);

                throw new ElevatedProcessException("ImpersonateLoggedOnUser failed with error code: " + Marshal.GetLastWin32Error());
            }

            // these handles are no longer needed
            NativeProcess.CloseHandle(hUser);
            NativeProcess.CloseHandle(hProcess);

            NativeProcess.STARTUPINFO startupInfo = new NativeProcess.STARTUPINFO();
            NativeProcess.PROCESS_INFORMATION pInfo = new NativeProcess.PROCESS_INFORMATION();
            startupInfo.cb = Marshal.SizeOf(pInfo);

            string commandLine = string.Format("{0} {1}", _path, _arguments);

            IntPtr pEnv;

            // create a new environment block for the process
            if (!NativeProcess.CreateEnvironmentBlock(out pEnv, userToken, false))
            {
                throw new ElevatedProcessException("CreateEnvironmentBlock() failed with error code: " + Marshal.GetLastWin32Error());
            }

            // create the process with the retrieved token
            if (!NativeProcess.CreateProcessAsUser(userToken, null, commandLine, IntPtr.Zero, IntPtr.Zero, false,
                NativeProcess.CreateProcessFlags.CREATE_UNICODE_ENVIRONMENT | NativeProcess.CreateProcessFlags.CREATE_SUSPENDED |
                NativeProcess.CreateProcessFlags.CREATE_NO_WINDOW, pEnv, _workDirectory, ref startupInfo, out pInfo))
            {
                NativeProcess.CloseHandle(userToken);

                if (pEnv != IntPtr.Zero)
                {
                    NativeProcess.DestroyEnvironmentBlock(pEnv);
                }

                throw new ElevatedProcessException("CreateProcessAsUser() failed with error code: " + Marshal.GetLastWin32Error());
            }

            NativeProcess.ResumeThread(pInfo.hThread);

            // the environment block can be destroyed now
            if (pEnv != IntPtr.Zero)
            {
                NativeProcess.DestroyEnvironmentBlock(pEnv);
            }

            // save the process information
            _processInformation = pInfo;

            NativeProcess.RevertToSelf();
        }

        public void WaitForExit()
        {
            NativeProcess.WaitForSingleObject(_processInformation.hProcess, INFINITE);
        }

        public bool WaitForExit(int milliseconds)
        {
            NativeProcess.WaitForSingleObject(_processInformation.hProcess, (uint)milliseconds);

            return HasExited;
        }

        public void Kill()
        {
            NativeProcess.TerminateProcess(_processInformation.hProcess, 0);
        }

        public void Dispose()
        {
            Close();
        }

        public void Close()
        {
            // close the handles before the object is destroyed
            NativeProcess.CloseHandle(_processInformation.hProcess);
            NativeProcess.CloseHandle(_processInformation.hThread);
        }
    }
}