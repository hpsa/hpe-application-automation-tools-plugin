
namespace HpToolsLauncher.Utils
{
    public class RunAsUser
    {
        private string _username;
        private string _encodedPwd;

        public RunAsUser(string username, string encodedPwd)
        {
            _username = username;
            _encodedPwd = encodedPwd;
        }
        public string Username
        {
            get { return _username; }
        }
        public string EncodedPassword
        {
            get { return _encodedPwd; }
        }
    }
}