/*
 *
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 *
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace HpToolsLauncher.RTS
{
    /// <summary>
    /// Model class which contains the script name and runtime settings (currently additional attributes)
    /// </summary>
    public class ScriptRTSModel
    {
        private string scriptName;
        private List<AdditionalAttributeModel> additionalAttributes;

        public ScriptRTSModel(string scriptName)
        {
            this.scriptName = scriptName;
            additionalAttributes = new List<AdditionalAttributeModel>();
        }

        public string GetScriptName()
        {
            return scriptName;
        }
        public void AddAdditionalAttribute(AdditionalAttributeModel additionalAttribute)
        {
            additionalAttributes.Add(additionalAttribute);
        }
        
        /**
         * Convert additional attribute model to key value struct
         */
        public List<RTSHelper.KeyValuePair> GetKeyValuePairs()
        {
            List<RTSHelper.KeyValuePair> keyValuePairs = new List<RTSHelper.KeyValuePair>();

            foreach (AdditionalAttributeModel additionalAttribute in additionalAttributes) {
                keyValuePairs.Add(
                    new RTSHelper.KeyValuePair(
                        additionalAttribute.GetName(),
                        additionalAttribute.GetValue()
                    )
                );
                keyValuePairs.Add(
                    new RTSHelper.KeyValuePair(
                        "~" + additionalAttribute.GetName(),
                        additionalAttribute.GetDescription()
                    )
                );
            }

            return keyValuePairs;
        }
    }
}
