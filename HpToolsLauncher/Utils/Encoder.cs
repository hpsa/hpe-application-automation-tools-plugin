using System;
using System.Runtime.InteropServices;

namespace HpToolsLauncher.Utils
{
    public static class Encoder
    {
        [DllImport("Encode.dll")]
        private static extern IntPtr MicCryptEncrypt([MarshalAs(UnmanagedType.LPWStr)] string dataToBeEncrypted);

        [DllImport("Encode.dll")]
        private static extern IntPtr MicCryptDecrypt([MarshalAs(UnmanagedType.LPWStr)] string dataToBeDecrypted);

        [DllImport("Encode.dll")]
        private static extern void MicCryptDestroyStr(IntPtr stirngToBeDestroyed);

        public static string Encode(string dataToBeEncrypted)
        {
            if (string.IsNullOrEmpty(dataToBeEncrypted))
            {
                return dataToBeEncrypted;
            }

            var encryptedDataPtr = MicCryptEncrypt(dataToBeEncrypted);
            string encrpted = Marshal.PtrToStringAuto(encryptedDataPtr);

            MicCryptDestroyStr(encryptedDataPtr);
            return encrpted;
        }

        public static string Decode(string dataToBeDecrypted)
        {
            if (string.IsNullOrEmpty(dataToBeDecrypted))
            {
                return dataToBeDecrypted;
            }

            var decryptedDataPtr = MicCryptDecrypt(dataToBeDecrypted);
            string decrpted = Marshal.PtrToStringAuto(decryptedDataPtr);
            MicCryptDestroyStr(decryptedDataPtr);
            return decrpted;
        }
    }
}
