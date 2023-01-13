using System;
using System.IO;
using System.Security;
using System.Security.Cryptography;
using System.Text;

namespace HpToolsLauncher.Utils
{
    public static class Encrypter
    {
        private const string KeyPath = @"secrets/.hptoolslaunchersecret.key";
        private static readonly string SecretKey;
        private static readonly RSACryptoServiceProvider Rsa;

        static Encrypter()
        {
#if DEBUG
            return;
#else
            SecretKey = Environment.GetEnvironmentVariable("hptoolslauncher.key");
            var keyPath = Environment.GetEnvironmentVariable("hptoolslauncher.rootpath");

            if (string.IsNullOrEmpty(SecretKey) || string.IsNullOrEmpty(keyPath))
                throw new ArgumentException("Invalid environment, no secretkey or root path was set, aborting.");

            keyPath += Path.DirectorySeparatorChar + KeyPath;

            string cnt;
            try
            {
                cnt = File.ReadAllText(keyPath);
            }
            catch (IOException)
            {
                ConsoleWriter.WriteErrLine("Failed to open file with decryption key.");
                throw new ArgumentException(
                    "Check the secret key for the hptoolslauncher in the secrets directory or force a new key pair.");
            }
            catch (SecurityException)
            {
                ConsoleWriter.WriteErrLine("You do not have access to open the file with the decryption key.");
                throw new ArgumentException("Check the permissions for the secret key in the secrets directory.");
            }
            catch (UnauthorizedAccessException)
            {
                ConsoleWriter.WriteErrLine("You do not have access to open the file with the decryption key or something else has occurred.");
                throw new ArgumentException("Check the permissions for the secret key in the secrets directory or the existence of the file.");
            }

            var pkXml = DecryptWithPwd(cnt);
            try
            {
                Rsa = new RSACryptoServiceProvider();
                Rsa.FromXmlString(pkXml); // init
            }
            catch (CryptographicException)
            {
                ConsoleWriter.WriteErrLine("The cryptography provider could not be acquired.");
                throw new ArgumentException("Try forcing a new key pair.");
            }
            catch (ArgumentNullException)
            {
                ConsoleWriter.WriteErrLine("No valid private key were provided for cryptography.");
                throw new ArgumentException("Try forcing a new key pair generation.");
            }
#endif
        }

        /// <summary>
        /// Decrypts the data with the node's private key.
        /// </summary>
        /// <param name="textToDecrypt"></param>
        /// <returns></returns>
        public static string Decrypt(string textToDecrypt)
        {
#if DEBUG
            return textToDecrypt;
#else
            var encryptedBytes = Convert.FromBase64String(textToDecrypt);
            byte[] text;
            try
            {
                text = Rsa.Decrypt(encryptedBytes, false);
            }
            catch (CryptographicException)
            {  
                ConsoleWriter.WriteErrLine("Failed to decrypt data using private key, try forcing a new public-private key pair.");
                throw new ArgumentException("Decryption failed using private key.");
            }

            return Encoding.UTF8.GetString(text);
#endif
        }

        /// <summary>
        /// Internal usage only, used for private key decryption.
        /// </summary>
        /// <param name="textToDecrypt"></param>
        /// <returns></returns>
        private static string DecryptWithPwd(string textToDecrypt)
        {
#if DEBUG
            return textToDecrypt;
#else
            var rijndaelCipher = new RijndaelManaged
            {
                BlockSize = 0x80,
                KeySize = 0x100,
                Mode = CipherMode.CBC,
                Padding = PaddingMode.PKCS7
            };

            var encryptedData = Convert.FromBase64String(textToDecrypt);
            var pwdBytes = Encoding.UTF8.GetBytes(SecretKey);

            var ivBytes = new byte[0x10];
            Array.Copy(encryptedData, ivBytes, 16);
            var cntBytes = new byte[encryptedData.Length - 16];
            Array.Copy(encryptedData, 16, cntBytes, 0, cntBytes.Length);

            rijndaelCipher.Key = pwdBytes;
            rijndaelCipher.IV = ivBytes;

            byte[] plainText;
            try
            {
                plainText = rijndaelCipher.CreateDecryptor().TransformFinalBlock(cntBytes, 0, cntBytes.Length);
            }
            catch (CryptographicException)
            {
                ConsoleWriter.WriteErrLine("Failed to decrypt using AES, possibly master key have changed since the encryption.");
                throw new ArgumentException(
                    "Try forcing a new public-private key pair on this node, by deselecting encryption in Node configurations.");
            }

            return Encoding.UTF8.GetString(plainText);
#endif
        }
    }
}
