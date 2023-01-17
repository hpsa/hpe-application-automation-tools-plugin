
using System.Security;

namespace HpToolsLauncher.Utils
{
    public class RunAsUser
    {
        private readonly string _username;
        private readonly string _encodedPwd;
        private readonly SecureString _pwd;

        public RunAsUser(string username, string encodedPwd)
        {
            _username = username;
            _encodedPwd = encodedPwd;
            _pwd =  Encoder.Decode(_encodedPwd).ToSecureString();
        }
        public RunAsUser(string username, SecureString pwd)
        {
            _username = username;
            _pwd = pwd;
            _encodedPwd = Encoder.Encode(_pwd.ToPlainString());
        }
        public string Username
        {
            get { return _username; }
        }

        public string EncodedPassword
        {
            get { return _encodedPwd; }
        }

        public SecureString Password
        {
            get { return _pwd; }
        }
    }
}