using System;
using System.Linq;
using HpToolsLauncher.Properties;
using HpToolsLauncher.Utils;

namespace HpToolsLauncher
{
    public class McConnectionInfo
    {
        private const string PAIR_SEPARATOR = "=";
        private const string TOKEN_SEPARATOR = ";";
        private const string YES = "Yes";
        private const string NO = "No";
        private const string SYSTEM = "System";
        private const string HTTP = "Http";
        private const string HTTPS = "Https";
        private const string PORT_8080 = "8080";
        private const string PORT_443 = "443";
        private const string CLIENT = "client";
        private const string SECRET = "secret";
        private const string TENANT = "tenant";
        private const int ZERO = 0;
        private const int ONE = 1;
        private static readonly char[] SLASH = new char[] { '/' };
        private static readonly char[] COMMA = new char[] { ':' };
        private static readonly char[] DBL_QUOTE = new char[] { '"' };

        private const string MOBILEHOSTADDRESS = "MobileHostAddress";
        private const string MOBILEUSESSL = "MobileUseSSL";
        private const string MOBILEUSERNAME = "MobileUserName";
        private const string MOBILEPASSWORD = "MobilePassword";
        private const string MOBILETENANTID = "MobileTenantId";
        private const string MOBILEEXECTOKEN = "MobileExecToken";
        private const string MOBILEUSEPROXY = "MobileUseProxy";
        private const string MOBILEPROXYTYPE = "MobileProxyType";
        private const string MOBILEPROXYSETTING_ADDRESS = "MobileProxySetting_Address";
        private const string MOBILEPROXYSETTING_AUTHENTICATION = "MobileProxySetting_Authentication";
        private const string MOBILEPROXYSETTING_USERNAME = "MobileProxySetting_UserName";
        private const string MOBILEPROXYSETTING_PASSWORD = "MobileProxySetting_Password";

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
        private AuthTokenInfo _token;
        private string _execToken;
        private AuthType _authType = AuthType.UsernamePassword;

        public string UserName { get; set; }
        public string Password { get; set; }

        public string ExecToken
        {
            get
            {
                return _execToken;
            }
            set
            {
                _execToken = value;
                _token = ParseExecToken();
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

        public string HostAddress { get; set; }
        public string HostPort { get; set; }
        public string TenantId { get; set; }
        public int UseSslAsInt { get; set; }
        public int UseProxyAsInt { get; set; }
        public bool UseProxy { get { return UseProxyAsInt == ONE; } }
        public int ProxyType { get; set; }
        public string ProxyAddress { get; set; }
        public int ProxyPort { get; set; }
        public int ProxyAuth { get; set; }
        public string ProxyUserName { get; set; }
        public string ProxyPassword { get; set; }

        public McConnectionInfo()
        {
            HostPort = PORT_8080;
            UserName = string.Empty;
            ExecToken = string.Empty;
            Password = string.Empty;
            HostAddress = string.Empty;
            TenantId = string.Empty;
            UseSslAsInt = ZERO;

            UseProxyAsInt = ZERO;
            ProxyType = ZERO;
            ProxyAddress = string.Empty;
            ProxyPort = ZERO;
            ProxyAuth = ZERO;
            ProxyUserName = string.Empty;
            ProxyPassword = string.Empty;
        }

        public McConnectionInfo(JavaProperties ciParams) : this()
        {
            if (ciParams.ContainsKey(MOBILEHOSTADDRESS))
            {
                //ssl
                bool useSSL = false;
                if (ciParams.ContainsKey(MOBILEUSESSL))
                {
                    string mcUseSSL = ciParams[MOBILEUSESSL];
                    if (!string.IsNullOrEmpty(mcUseSSL))
                    {
                        UseSslAsInt = int.Parse(mcUseSSL);
                        int mcUseSslAsInt;
                        int.TryParse(ciParams[MOBILEUSESSL], out mcUseSslAsInt);
                        UseSslAsInt = mcUseSslAsInt;
                        useSSL = mcUseSslAsInt == ONE;
                    }
                }

                string mcServerUrl = ciParams[MOBILEHOSTADDRESS].Trim();
                if (string.IsNullOrEmpty(mcServerUrl))
                {
                    throw new NoMcConnectionException();
                }
                //url is something like http://xxx.xxx.xxx.xxx:8080
                string[] arr = mcServerUrl.Split(COMMA, StringSplitOptions.RemoveEmptyEntries);
                if (arr.Length == 1)
                {
                    if (arr[0].Trim().In(true, HTTP, HTTPS))
                        throw new ArgumentException(string.Format(Resources.McInvalidUrl, mcServerUrl));
                    HostAddress = arr[0].TrimEnd(SLASH);
                    HostPort = useSSL ? PORT_443 : PORT_8080;
                }
                else if (arr.Length == 2)
                {
                    if (arr[0].Trim().In(true, HTTP, HTTPS))
                    {
                        HostAddress = arr[1].Trim(SLASH);
                        HostPort = useSSL ? PORT_443 : PORT_8080;
                    }
                    else
                    {
                        HostAddress = arr[0].Trim(SLASH);
                        HostPort = arr[1].Trim();
                    }
                }
                else if (arr.Length == 3)
                {
                    HostAddress = arr[1].Trim(SLASH);
                    HostPort = arr[2].Trim();
                }

                if (HostAddress.Trim() == string.Empty)
                {
                    throw new ArgumentException(Resources.McEmptyHostAddress);
                }

                //mc username
                if (ciParams.ContainsKey(MOBILEUSERNAME))
                {
                    string mcUsername = ciParams[MOBILEUSERNAME];
                    if (!string.IsNullOrEmpty(mcUsername))
                    {
                        UserName = mcUsername;
                    }
                }

                //mc password
                if (ciParams.ContainsKey(MOBILEPASSWORD))
                {
                    string mcPassword = ciParams[MOBILEPASSWORD];
                    if (!string.IsNullOrEmpty(mcPassword))
                    {
                        Password = Encrypter.Decrypt(mcPassword);
                    }
                }

                //mc tenantId
                if (ciParams.ContainsKey(MOBILETENANTID))
                {
                    string mcTenantId = ciParams[MOBILETENANTID];
                    if (!string.IsNullOrEmpty(mcTenantId))
                    {
                        TenantId = mcTenantId;
                    }
                }

                //mc exec token	
                if (ciParams.ContainsKey(MOBILEEXECTOKEN))
                {
                    var mcExecToken = ciParams[MOBILEEXECTOKEN];
                    if (!string.IsNullOrEmpty(mcExecToken))
                    {
                        ExecToken = Encrypter.Decrypt(mcExecToken);
                    }
                }

                //Proxy enabled flag
                if (ciParams.ContainsKey(MOBILEUSEPROXY))
                {
                    string useProxy = ciParams[MOBILEUSEPROXY];
                    if (!string.IsNullOrEmpty(useProxy))
                    {
                        UseProxyAsInt = int.Parse(useProxy);
                    }
                }

                //Proxy type
                if (ciParams.ContainsKey(MOBILEPROXYTYPE))
                {
                    string proxyType = ciParams[MOBILEPROXYTYPE];
                    if (!string.IsNullOrEmpty(proxyType))
                    {
                        ProxyType = int.Parse(proxyType);
                    }
                }

                //proxy address
                string proxyAddress = ciParams.GetOrDefault(MOBILEPROXYSETTING_ADDRESS);
                if (!string.IsNullOrEmpty(proxyAddress))
                {
                    // data is something like "16.105.9.23:8080"
                    string[] strArrayForProxyAddress = proxyAddress.Split(new char[] { ':' });
                    if (strArrayForProxyAddress.Length == 2)
                    {
                        ProxyAddress = strArrayForProxyAddress[0];
                        ProxyPort = int.Parse(strArrayForProxyAddress[1]);
                    }
                }


                //Proxy authentication
                if (ciParams.ContainsKey(MOBILEPROXYSETTING_AUTHENTICATION))
                {
                    string proxyAuthentication = ciParams[MOBILEPROXYSETTING_AUTHENTICATION];
                    if (!string.IsNullOrEmpty(proxyAuthentication))
                    {
                        ProxyAuth = int.Parse(proxyAuthentication);
                    }
                }

                //Proxy username
                if (ciParams.ContainsKey(MOBILEPROXYSETTING_USERNAME))
                {
                    string proxyUsername = ciParams[MOBILEPROXYSETTING_USERNAME];
                    if (!string.IsNullOrEmpty(proxyUsername))
                    {
                        ProxyUserName = proxyUsername;
                    }
                }

                //Proxy password
                if (ciParams.ContainsKey(MOBILEPROXYSETTING_PASSWORD))
                {
                    string proxyPassword = ciParams[MOBILEPROXYSETTING_PASSWORD];
                    if (!string.IsNullOrEmpty(proxyPassword))
                    {
                        ProxyPassword = Encrypter.Decrypt(proxyPassword);
                    }
                }
            }
            else
            {
                throw new NoMcConnectionException();
            }
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
            var execToken = ExecToken.Trim().Trim(DBL_QUOTE);

            var ret = new AuthTokenInfo();
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

                if (CLIENT.EqualsIgnoreCase(key))
                {
                    ret.ClientId = value;
                }
                else if (SECRET.EqualsIgnoreCase(key))
                {
                    ret.SecretKey = value;
                }
                else if (TENANT.EqualsIgnoreCase(key))
                {
                    TenantId = value;
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
            return _token;
        }

        private string UseSslAsStr { get { return UseSslAsInt == ONE ? YES : NO; } }

        private string UseProxyAsStr { get { return UseProxyAsInt == ONE ? YES : NO; } }

        private string ProxyTypeAsStr { get { return ProxyType == ONE ? SYSTEM : HTTP; } }

        private string ProxyAuthAsStr { get { return ProxyAuth == ONE ? YES : NO; } }

        public override string ToString()
        {
            string usernameOrClientId = string.Empty;
            if (MobileAuthType == AuthType.AuthToken)
            {
                usernameOrClientId = string.Format("ClientId: {0}", _token.ClientId);
            }
            else if (MobileAuthType == AuthType.UsernamePassword)
            {
                usernameOrClientId = string.Format("Username: {0}", UserName);
            }
            string strProxy = string.Format("UseProxy: {0}", UseProxyAsStr);
            if (UseProxy)
            {
                strProxy += string.Format(", ProxyType: {0}, ProxyAddress: {1}, ProxyPort: {2}, ProxyAuth: {3}, ProxyUser: {4}", ProxyTypeAsStr, ProxyAddress, ProxyPort, ProxyAuthAsStr, ProxyUserName);
            }
            return string.Format("HostAddress: {0}, Port: {1}, AuthType: {2}, {3}, TenantId: {4}, UseSSL: {5}, {6}", HostAddress, HostPort, MobileAuthType, usernameOrClientId, TenantId, UseSslAsStr, strProxy);
        }
    }

    public class NoMcConnectionException : Exception
    {
    }
}
