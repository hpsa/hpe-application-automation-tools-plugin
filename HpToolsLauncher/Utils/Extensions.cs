using System.Runtime.InteropServices;
using System;
using System.Security;
using System.Linq;
using System.ComponentModel;

namespace HpToolsLauncher.Utils
{
    internal static class Extensions
    {
        public static SecureString ToSecureString(this string plainString)
        {
            if (plainString == null)
                return null;

            SecureString secureString = new SecureString();
            foreach (char c in plainString.ToCharArray())
            {
                secureString.AppendChar(c);
            }
            return secureString;
        }
        public static string ToPlainString(this SecureString value)
        {
            IntPtr valuePtr = IntPtr.Zero;
            try
            {
                valuePtr = Marshal.SecureStringToBSTR(value);
                return Marshal.PtrToStringBSTR(valuePtr);
            }
            finally
            {
                Marshal.ZeroFreeBSTR(valuePtr);
            }
        }

        public static bool IsNullOrEmpty(this string value)
        {
            return string.IsNullOrEmpty(value);
        }
        public static bool IsNullOrWhiteSpace(this string value)
        {
            return string.IsNullOrWhiteSpace(value);
        }

        public static bool IsEmptyOrWhiteSpace(this string str)
        {
            return str != null && str.Trim() == string.Empty;
        }

        public static bool IsValidUrl(this string url)
        {
            return Uri.IsWellFormedUriString(url, UriKind.RelativeOrAbsolute);
        }

        public static bool EqualsIgnoreCase(this string s1, string s2)
        {
            return (s1 == null || s2 == null) ? (s1 == s2) : s1.Equals(s2, StringComparison.OrdinalIgnoreCase);
        }

        public static bool In(this string str, bool ignoreCase, params string[] values)
        {
            if (ignoreCase)
            {
                return values != null && values.Any((string s) => EqualsIgnoreCase(str, s));
            }
            return In(str, values);
        }

        public static bool In<T>(this T obj, params T[] values)
        {
            return values != null && values.Any((T o) => Equals(obj, o));
        }

        public static string GetEnumDescription(this Enum enumValue)
        {
            var fieldInfo = enumValue.GetType().GetField(enumValue.ToString());
            var descrAttrs = (DescriptionAttribute[])fieldInfo.GetCustomAttributes(typeof(DescriptionAttribute), false);
            return descrAttrs.Length > 0 ? descrAttrs[0].Description : enumValue.ToString();
        }
    }
}
