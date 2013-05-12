//© Copyright 2013 Hewlett-Packard Development Company, L.P.
//Permission is hereby granted, free of charge, to any person obtaining a copy of this software
//and associated documentation files (the "Software"), to deal in the Software without restriction,
//including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
//and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
//subject to the following conditions:

//The above copyright notice and this permission notice shall be included in all copies or
//substantial portions of the Software.

//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
//INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
//PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
//LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
//TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE 
//OR OTHER DEALINGS IN THE SOFTWARE.

using System;
using System.IO;
using System.Xml;
using Analysis.Api;
//using Analysis.ApiLib;
using Analysis.ApiLib.Sla;
using LRAnalysisLauncher.Properties;
using HpToolsLauncher;
using Analysis.ApiSL;
using Analysis.Api.Dictionaries;

namespace LRAnalysisLauncher
{
    class Program
    {

        static Program()
        {
            AppDomain.CurrentDomain.AssemblyResolve += new ResolveEventHandler(CurrentDomain_AssemblyResolve);
        }
        //args: lrr location, lra location, html report location
        static int Main(string[] args)
        {
            //Console.WriteLine("in LrAnlysisLauncher");
            try
            {
                if (args.Length != 3)
                {
                    ShowHelp();
                    return (int)Launcher.ExitCodeEnum.Aborted;
                }

                string lrrlocation = args[0];
                string lralocation = args[1];
                string htmlLocation = args[2];

                LrAnalysis analysis = new LrAnalysis();

                Session session = analysis.Session;
                if (session.Create(lralocation, lrrlocation))
                {
                    HtmlReportMaker reportMaker = session.CreateHtmlReportMaker();
                    reportMaker.CreateDefaultHtmlReport(Path.Combine(Path.GetDirectoryName(htmlLocation), "IE", Path.GetFileName(htmlLocation)), ApiBrowserType.IE);
                    reportMaker.CreateDefaultHtmlReport(Path.Combine(Path.GetDirectoryName(htmlLocation), "Netscape", Path.GetFileName(htmlLocation)), ApiBrowserType.Netscape);


                    XmlDocument xmlDoc = new XmlDocument();

                    session.ErrorMessages.LoadValuesIfNeeded();
                    if (session.ErrorMessages.Count != 0)
                    {
                        Console.WriteLine(Resources.ErrorsReportTitle);
                        XmlElement errorRoot = xmlDoc.CreateElement("Errors");
                        xmlDoc.AppendChild(errorRoot);
                        foreach (ErrorMessage err in session.ErrorMessages)
                        {
                            XmlElement elem = xmlDoc.CreateElement("Error");
                            elem.SetAttribute("ID", err.ID.ToString());
                            elem.AppendChild(xmlDoc.CreateTextNode(err.Name));
                            Console.WriteLine("ID: " + err.ID + " Name: " + err.Name);
                            errorRoot.AppendChild(elem);
                        }

                        xmlDoc.Save(Path.Combine(Path.GetDirectoryName(Path.GetDirectoryName(lrrlocation)), "Errors.xml"));

                        xmlDoc.RemoveAll();
                        Console.WriteLine("");
                    }
                    session.Close();
                    Console.WriteLine(Resources.SLAReportTitle);
                    SlaResult slaResult = Session.CalculateSla(lralocation, true);
                    XmlElement root = xmlDoc.CreateElement("SLA");
                    xmlDoc.AppendChild(root);

                    foreach (SlaWholeRunRuleResult a in slaResult.WholeRunRules)
                    {
                        XmlElement elem = xmlDoc.CreateElement(a.RuleName);
                        Console.WriteLine(Resources.DoubleLineSeperator);
                        elem.SetAttribute("FullName", a.RuleUiName);
                        Console.WriteLine("Full Name : " + a.RuleUiName);
                        Console.WriteLine("Measurement : " + a.Measurement);
                        elem.SetAttribute("Measurement", a.Measurement.ToString());
                        Console.WriteLine("Goal Value : " + a.GoalValue);
                        elem.SetAttribute("GoalValue", a.GoalValue.ToString());
                        Console.WriteLine("Actual value : " + a.ActualValue);
                        elem.SetAttribute("ActualValue", a.ActualValue.ToString());
                        Console.WriteLine("status : " + a.Status);
                        elem.AppendChild(xmlDoc.CreateTextNode(a.Status.ToString()));
                        Console.WriteLine(Resources.DoubleLineSeperator);
                        root.AppendChild(elem);
                    }


                    foreach (SlaTimeRangeRuleResult a in slaResult.TimeRangeRules)
                    {
                        XmlElement rule = xmlDoc.CreateElement(a.RuleName);
                        Console.WriteLine(Resources.DoubleLineSeperator);
                        Console.WriteLine("Full Name : " + a.RuleUiName);
                        rule.SetAttribute("FullName", a.RuleUiName);
                        Console.WriteLine("Measurement : " + a.Measurement);
                        rule.SetAttribute("Measurement", a.Measurement.ToString());
                        Console.WriteLine("CriteriaMeasurement Value : " + a.CriteriaMeasurement);
                        rule.SetAttribute("CriteriaMeasurementValue", a.CriteriaMeasurement.ToString());
                        Console.WriteLine("LoadThresholds value : ");
                        foreach (SlaLoadThreshold slat in a.LoadThresholds)
                        {
                            XmlElement loadThr = xmlDoc.CreateElement("SlaLoadThreshold");
                            loadThr.SetAttribute("StartLoadValue", slat.StartLoadValue.ToString());
                            loadThr.SetAttribute("EndLoadValue", slat.EndLoadValue.ToString());
                            loadThr.SetAttribute("ThresholdValue", slat.ThresholdValue.ToString());
                            rule.AppendChild(loadThr);

                        }
                        XmlElement timeRanges = xmlDoc.CreateElement("TimeRanges");
                        foreach (SlaTimeRangeInfo slatri in a.TimeRanges)
                        {
                            XmlElement subsubelem = xmlDoc.CreateElement("TimeRangeInfo");
                            subsubelem.SetAttribute("StartTime", slatri.StartTime.ToString());
                            subsubelem.SetAttribute("EndTime", slatri.EndTime.ToString());
                            subsubelem.SetAttribute("GoalValue", slatri.GoalValue.ToString());
                            subsubelem.SetAttribute("ActualValue", slatri.ActualValue.ToString());
                            subsubelem.SetAttribute("LoadValue", slatri.LoadValue.ToString());
                            subsubelem.InnerText = slatri.Status.ToString();
                            timeRanges.AppendChild(subsubelem);
                        }
                        rule.AppendChild(timeRanges);
                        Console.WriteLine("status : " + a.Status);
                        rule.AppendChild(xmlDoc.CreateTextNode(a.Status.ToString()));

                        root.AppendChild(rule);

                        Console.WriteLine(Resources.DoubleLineSeperator);
                    }

                    //write XML to location:
                    xmlDoc.Save(Path.Combine(Path.GetDirectoryName(Path.GetDirectoryName(lrrlocation)), "SLA.xml"));

                }
                else
                {

                    Console.WriteLine(Resources.CannotCreateSession);
                    return (int)Launcher.ExitCodeEnum.Aborted;
                }

                session.Close();
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
                Console.WriteLine(e.StackTrace);
                return (int)Launcher.ExitCodeEnum.Aborted;
            }
            finally
            {
                //close analysis?
            }

            return (int)Launcher.ExitCodeEnum.Passed;

        }

        static System.Reflection.Assembly CurrentDomain_AssemblyResolve(object sender, ResolveEventArgs args)
        {
            System.Reflection.AssemblyName name = new System.Reflection.AssemblyName(args.Name);
            if (name.Name.ToLowerInvariant().EndsWith(".resources")) return null;
            string installPath = HpToolsLauncher.Helper.getLRInstallPath();
            if (installPath == null)
            {
                Console.WriteLine(Resources.CannotLocateInstallDir);
                Environment.Exit((int)Launcher.ExitCodeEnum.Aborted);
            }
            //Console.WriteLine(Path.Combine(installPath, "bin", name.Name + ".dll"));
            return System.Reflection.Assembly.LoadFrom(Path.Combine(installPath, "bin", name.Name + ".dll"));
        }

        private static void ShowHelp()
        {
            Console.WriteLine("HP LoadRunner Analysis Command Line Executer");
            Console.WriteLine();
            Console.Write("Usage: LRAnalysisLauncher.exe");
            Console.ForegroundColor = ConsoleColor.Cyan;
            Console.Write("[.lrr file location] [.lra output location] [html report output folder]");
            Console.ResetColor();
            Environment.Exit((int)Launcher.ExitCodeEnum.Failed);
        }
    }
}
