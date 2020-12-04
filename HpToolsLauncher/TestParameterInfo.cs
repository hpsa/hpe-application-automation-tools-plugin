/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2021 Micro Focus or one of its affiliates.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ___________________________________________________________________
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace HpToolsLauncher
{
    public class TestParameterInfo
    {
        string _name;

        public string Name
        {
            get { return _name; }
            set { _name = value; }
        }
        string _value;

        public string Value
        {
            get { return _value; }
            set { _value = value; }
        }
        string _type;

        public string Type
        {
            get { return _type; }
            set { _type = value; }
        }
        

        /// <summary>
        /// parses the value string and returns an object of the specified type.
        /// </summary>
        /// <returns></returns>
        public object ParseValue()
        {
            object val = null;
            bool ok = false;
            switch (this.Type.ToLower())
            {
                case "int":
                
                    int v;
                    ok = int.TryParse(this.Value, out v);
                    if (ok)
                    {
                        val = v;
                    }
                    break;
                case "number":
                case "password":
                case "string":
                case "any":
                    val = this.Value;
                    break;
                case "float":
                    float v1;
                    ok = float.TryParse(this.Value, out v1);
                    if (ok)
                    {
                        val = v1;
                    }

                    break;
                case "double":
                
                    double v2;
                    ok = double.TryParse(this.Value, out v2);
                    if (ok)
                    {
                        val = v2;
                    }
                    break;
                case "datetime":
                case "date":
                    DateTime v3;
                    ok = DateTime.TryParseExact(this.Value,
                        new string[] {  
                                            "yyyy-MM-ddTHH:mm:ss",
                                            "dd/MM/yyyy HH:mm:ss.fff",
                                            "dd/M/yyyy HH:mm:ss.fff",
                                            "d/MM/yyyy HH:mm:ss.fff",
                                            "dd/MM/yyyy hh:mm:ss.fff tt" ,
                                            "d/MM/yyyy hh:mm:ss.fff tt" ,
                                            "dd/M/yyyy hh:mm:ss.fff tt" ,
                                            "d/M/yyyy hh:mm:ss.fff tt" ,
                                            "dd-MM-yyyy HH:mm:ss.fff",
                                            "dd.MM.yyyy HH:mm:ss.fff",
                                            "dd.MM.yyyy hh:mm:ss.fff tt" ,
                                            "dd/MM/yyyy HH:mm:ss",
                                            "dd-MM-yyyy HH:mm:ss",
                                            "d/MM/yyyy HH:mm:ss",
                                            "d/MM/yyyy hh:mm:ss tt",
                                            "d-MM-yyyy HH:mm:ss",
                                            "d.MM.yyyy HH:mm:ss",
                                            "d.MM.yyyy hh:mm:ss tt" ,
                                            "dd/MM/yyyy",
                                            "dd-MM-yyyy",
                                            "dd.MM.yyyy",
                                            "d/MM/yyyy" ,
                                            "d-MM-yyyy" ,
                                            "d.MM.yyyy" ,
                                            "M/d/yyyy HH:mm:ss.fff",
                                            "M.d.yyyy hh:mm:ss.fff tt",
                                            "M.d.yyyy HH:mm:ss.fff",
                                            "M/d/yyyy hh:mm:ss.fff t",
                                            "MM/dd/yyyy hh:mm:ss.fff tt",
                                            "MM/d/yyyy hh:mm:ss.fff tt",
                                            "MM/dd/yyyy HH:mm:ss",
                                            "MM.dd.yyyy HH:mm:ss",
                                            "M/dd/yyyy HH:mm:ss.fff",
                                            "M/dd/yyyy hh:mm:ss.fff tt",
                                            "M.dd.yyyy HH:mm:ss.fff",
                                            "M.dd.yyyy hh:mm:ss.fff tt",
                                            "MM/dd/yyyy",
                                            "MM.dd.yyyy",
                                            "M/dd/yyyy",
                                            "M.dd.yyyy"
                                            },
                        null,
                        System.Globalization.DateTimeStyles.None,
                        out v3);

                    //ok = DateTime.TryParse(param.Value, out v3);
                    if (ok)
                    {
                        val = v3;
                    }
                    break;

                case "long":
                    long v4;
                    ok = long.TryParse(this.Value, out v4);
                    if (ok)
                    {
                        val = v4;
                    }
                    break;
                case "boolean":
                    bool v5;
                    ok = bool.TryParse(this.Value, out v5);
                    if (ok)
                    {
                        val = v5;
                    }
                    break;
                case "decimal":
                    decimal v6;
                    ok = decimal.TryParse(this.Value, out v6);
                    if (ok)
                    {
                        val = v6;
                    }
                    break;
            }
            return val;
        }
    }
}
