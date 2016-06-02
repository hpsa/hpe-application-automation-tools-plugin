using System;
using System.Runtime.InteropServices;

namespace HpToolsLauncher
{
    public static class WinUserNativeMethods
    {
        private static class EncodeUtilsWrap
        {
            [DllImport("EncodeUtilsWrap", CallingConvention = CallingConvention.Cdecl)]
            public static extern void UnprotectBSTRFromBase64([MarshalAs(UnmanagedType.BStr)] string input, [MarshalAs(UnmanagedType.BStr)] out string result);

            [DllImport("EncodeUtilsWrap", CallingConvention = CallingConvention.Cdecl)]
            public static extern void ProtectBSTRToBase64([MarshalAs(UnmanagedType.BStr)] string input, [MarshalAs(UnmanagedType.BStr)] out string result);
        }

        private static class EncodeUtilsWrapD
        {
            [DllImport("EncodeUtilsWrapD", CallingConvention = CallingConvention.Cdecl)]
            public static extern void UnprotectBSTRFromBase64([MarshalAs(UnmanagedType.BStr)] string input, [MarshalAs(UnmanagedType.BStr)] out string result);

            [DllImport("EncodeUtilsWrapD", CallingConvention = CallingConvention.Cdecl)]
            public static extern void ProtectBSTRToBase64([MarshalAs(UnmanagedType.BStr)] string input, [MarshalAs(UnmanagedType.BStr)] out string result);
        }

        public static string ProtectBSTRToBase64(string clearData)
        {
            string result = null;

            try
            {
                EncodeUtilsWrap.ProtectBSTRToBase64(clearData, out result);
            }
            catch (DllNotFoundException)
            {
                try
                {
                    EncodeUtilsWrapD.ProtectBSTRToBase64(clearData, out result);
                }
                catch (DllNotFoundException)
                {
                    
                }
            }

            return result;
        }

        public static string UnprotectBSTRFromBase64(string protectedData)
        {
            if (string.IsNullOrEmpty(protectedData))
                return string.Empty;

            string result = null;

            try
            {
                EncodeUtilsWrap.UnprotectBSTRFromBase64(protectedData, out result);
            }
            catch (DllNotFoundException)
            {
                EncodeUtilsWrapD.UnprotectBSTRFromBase64(protectedData, out result);
            }

            return result;
        }
    }
}
