using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;

namespace HpToolsLauncher.RTS
{
    public class RTSHelper
    {
        public struct KeyValuePair
        {
            public string key;
            public string value;

            public KeyValuePair(string key, string value)
            {
                this.key = key;
                this.value = value;
            }
        }

        private const string END_OF_LINE = "\\r\\n";
        public const string COMMAND_ARGUMENTS = "CommandArguments";

        private string m_iniFileText;
        private int m_startSectionIndex;
        private int m_endSectionIndex;
        private bool m_sectionExists;
        private string m_sectionName;
        private List<KeyValuePair> m_inputKeyValuePairs;
        private List<KeyValuePair> m_keyValuePairs;

        /**
         * Set the begging and the end index of a section
         */
        private void SetSectionIndexes()
        {
            Regex regex = new Regex("\\[.*?\\]");
            MatchCollection matches = regex.Matches(m_iniFileText);

            bool sectionFound = false;
            foreach (Match match in matches)
            {
                if (sectionFound == false)
                {
                    if (match.Value.Equals(m_sectionName))
                    {
                        m_startSectionIndex = match.Index;
                        sectionFound = true;
                    }
                }
                else
                {
                    m_endSectionIndex = match.Index;
                    break;
                }
            }

            if (m_startSectionIndex != -1)
            {
                m_startSectionIndex += m_sectionName.Length;
            }
            else
            {
                CreateSection();
            }
        }

        /**
         * 
         * Create the section if it doesn't exist 
         */
        private void CreateSection()
        {
            m_iniFileText = m_iniFileText.Insert(m_endSectionIndex, m_sectionName);

            m_startSectionIndex = m_endSectionIndex + m_sectionName.Length;
            m_endSectionIndex = m_iniFileText.Length;
            m_sectionExists = false;
        }

        /**
         * Set initial key-value pairs for the section
         */
        private void SetKeyValuePairs()
        {
            if (!m_sectionExists)
            {
                return;
            }
            string partial = m_iniFileText.Substring(m_startSectionIndex, m_endSectionIndex - m_startSectionIndex);
            string[] keyValuePairs = partial.Split(new string[] { END_OF_LINE }, StringSplitOptions.RemoveEmptyEntries);

            foreach (string keyValuePair in keyValuePairs)
            {
                var results = keyValuePair.Split('=');
                if (results.Length > 1)
                {
                    m_keyValuePairs.Add(new KeyValuePair(results[0], results[1]));
                }
            }
        }

        /**
         * Reconstruct the section with updates or new key-value pairs
         */
        private void ConstructUpdatedSection()
        {
            string sectionText = "";
            foreach (KeyValuePair keyValuePair in m_keyValuePairs)
            {
                sectionText += END_OF_LINE + keyValuePair.key + "=" + keyValuePair.value;
            }
            sectionText += END_OF_LINE;

            UpdateIniFileText(sectionText);
        }

        /**
         * Updates the ini file text with the reconstructed section
         */
        private void UpdateIniFileText(string sectionText)
        {
            m_iniFileText = m_iniFileText.Remove(m_startSectionIndex, m_endSectionIndex - m_startSectionIndex).Insert(m_startSectionIndex, sectionText);
        }

        public RTSHelper(string iniFileText, string sectionName, List<KeyValuePair> inputKeyValuePairs)
        {
            m_startSectionIndex = -1;
            m_iniFileText = iniFileText;
            m_endSectionIndex = m_iniFileText.Length;
            m_keyValuePairs = new List<KeyValuePair>();
            m_sectionExists = true;

            SetSectionName(sectionName);
            SetInputKeyValuePairs(inputKeyValuePairs);

            SetSectionIndexes();
            SetKeyValuePairs();

            foreach (KeyValuePair keyValuePair in m_inputKeyValuePairs)
            {
                UpdateKeyValuePair(keyValuePair);
            }

            ConstructUpdatedSection();
        }

        private void SetSectionName(string sectionName)
        {
            m_sectionName = "[" + sectionName + "]";
        }

        private void SetInputKeyValuePairs(List<KeyValuePair> inputKeyValuePairs)
        {
            m_inputKeyValuePairs = inputKeyValuePairs;
        }

        /**
         * Updates the value of the key if it exists
         * Creates the key otherwise
         */
        private void UpdateKeyValuePair(KeyValuePair keyValuePair)
        {
            //Check if key exists
            bool keyExists = false;
            for (int i = 0; i < m_keyValuePairs.Count && m_sectionExists; i++)
            {
                if (m_keyValuePairs[i].key.Equals(keyValuePair.key))
                {
                    m_keyValuePairs[i] = new KeyValuePair(m_keyValuePairs[i].key, keyValuePair.value);
                    keyExists = true;
                    break;
                }
            }
            //Key doesn't exist, create new entry
            if (!keyExists)
            {
                m_keyValuePairs.Add(new KeyValuePair(keyValuePair.key, keyValuePair.value));
            }
        }

        public string GetUpdatedIniFileText()
        {
            return m_iniFileText;
        }
    }
}
