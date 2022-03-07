using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Web.Compilation;
using HpToolsLauncher.Properties;

namespace HpToolsLauncher
{
    public class McConnectionInfo
    {
        private const string PAIR_SEPARATOR = "=";
        private const string TOKEN_SEPARATOR = ";";

        // auth types for MC
        public enum AuthType
        {
            UsernamePassword,
            AuthToken
        }

        public struct AuthTokenInfo
        {
            public string ClientId { get; set; }
            public string SecretKey { get; set; }
        }

        // if token auth was specified this is populated
        private AuthTokenInfo _tokens;
        private string _execToken;
        private AuthType _authType = AuthType.UsernamePassword;

        public string MobileUserName { get; set; }
        public string MobilePassword { get; set; }

        public string MobileExecToken
        {
            get
            {
                return _execToken;
            }
            set
            {
                _execToken = value;
                _tokens = ParseExecToken();
            }
        }

        public AuthType MobileAuthType
        {
            get
            {
                return _authType;
            }
            private set
            {
                _authType = value;
            }
        }

        public string MobileHostAddress { get; set; }
        public string MobileHostPort { get; set; }
        public string MobileTenantId { get; set; }
        public int MobileUseSSL { get; set; }
        public int MobileUseProxy { get; set; }
        public int MobileProxyType { get; set; }
        public string MobileProxySetting_Address { get; set; }
        public int MobileProxySetting_Port { get; set; }
        public int MobileProxySetting_Authentication { get; set; }
        public string MobileProxySetting_UserName { get; set; }
        public string MobileProxySetting_Password { get; set; }

        public McConnectionInfo()
        {
            MobileHostPort = "8080";
            MobileUserName = string.Empty;
            MobileExecToken = string.Empty;
            MobilePassword = string.Empty;
            MobileHostAddress = string.Empty;
            MobileTenantId = string.Empty;
            MobileUseSSL = 0;

            MobileUseProxy = 0;
            MobileProxyType = 0;
            MobileProxySetting_Address = string.Empty;
            MobileProxySetting_Port = 0;
            MobileProxySetting_Authentication = 0;
            MobileProxySetting_UserName = string.Empty;
            MobileProxySetting_Password = string.Empty;
        }

        /// <summary>
        /// Parses the execution token and separates into three parts: clientId, secretKey and tenantId
        /// </summary>
        /// <returns></returns>
        /// <exception cref="ArgumentException"></exception>
        private AuthTokenInfo ParseExecToken()
        {
            // exec token consists of three parts:
            // 1. client id
            // 2. secret key
            // 3. optionally tenant id
            // separator is ;
            // key-value pairs are separated with =

            // e.g., "client=oauth2-QHxvc8bOSz4lwgMqts2w@microfocus.com; secret=EHJp8ea6jnVNqoLN6HkD; tenant=999999999;"
            // "client=oauth2-OuV8k3snnGp9vJugC1Zn@microfocus.com; secret=6XSquF1FUD4CyQM7fb0B; tenant=999999999;"
            // "client=oauth2-OuV8k3snnGp9vJugC1Zn@microfocus.com; secret=6XSquF1FUD7CyQM7fb0B; tenant=999999999;"
            var execToken = MobileExecToken.Trim();

            var ret = new AuthTokenInfo();

            // it may or may not contains surrounding quotes
            if (execToken.StartsWith("\"") && execToken.EndsWith("\"") && execToken.Length > 1)
            {
                execToken = execToken.Substring(1, execToken.Length - 2);
            }

            if (execToken.Length == 0) return ret; // empty string was given as token, may semnalize that it wasn't specified

            var tokens = execToken.Split(TOKEN_SEPARATOR.ToCharArray(), StringSplitOptions.RemoveEmptyEntries);

            if (tokens.Length != 3) throw new ArgumentException(Resources.McInvalidToken);
            if (!tokens.All(token => token.Contains(PAIR_SEPARATOR)))
                throw new ArgumentException(string.Format(Resources.McMalformedTokenInvalidKeyValueSeparator, PAIR_SEPARATOR));

            // key-values are separated by =, we need its value, the key is known
            foreach (var token in tokens)
            {
                var parts = token.Split(PAIR_SEPARATOR.ToCharArray());

                if (parts.Length != 2)
                    throw new ArgumentException(Resources.McMalformedTokenMissingKeyValuePair);

                var key = parts[0].Trim();
                var value = parts[1].Trim();

                if ("client".Equals(key, StringComparison.OrdinalIgnoreCase))
                {
                    ret.ClientId = value;
                } else if ("secret".Equals(key, StringComparison.OrdinalIgnoreCase))
                {
                    ret.SecretKey = value;
                } else if ("tenant".Equals(key, StringComparison.OrdinalIgnoreCase))
                {
                    MobileTenantId = value;
                }
                else
                {
                    throw new ArgumentException(string.Format(Resources.McMalformedTokenInvalidKey, key));
                }
            }

            _authType = AuthType.AuthToken;
            return ret;
        }

        /// <summary>
        /// Returns the parsed tokens from the execution token.
        /// </summary>
        /// <returns></returns>
        public AuthTokenInfo GetAuthToken()
        {
            return _tokens;
        }

        public override string ToString()
        {
            string McConnectionStr =
                 string.Format("UFT Mobile HostAddress: {0}, Port: {1}, Username: {2}, TenantId: {3}, UseSSL: {4}, UseProxy: {5}, ProxyType: {6}, ProxyAddress: {7}, ProxyPort: {8}, ProxyAuth: {9}, ProxyUser: {10}",
                 MobileHostAddress, MobileHostPort, MobileUserName, MobileTenantId, MobileUseSSL, MobileUseProxy, MobileProxyType, MobileProxySetting_Address, MobileProxySetting_Port, MobileProxySetting_Authentication,
                 MobileProxySetting_UserName);
            return McConnectionStr;
        }
    }

}
