using System;
using System.Collections.Generic;
using System.Linq;
using Analysis.Api;
using Analysis.ApiLib;

namespace LRAnalysisLauncher
{
    public abstract class Helper
    {
  
        /// <summary>
        /// Returns the number of run vusers from VuserStateGraph graph
        /// </summary>
        /// <returns>Number of run vusers per catagory</returns>
        public static Dictionary<string, int> GetVusersCountByStatus(LrAnalysis lrAnalysis)
        {


            var vuserDictionary = new Dictionary<string, int>(3)
            {
                {"Passed", 0},
                {"Stopped", 0},
                {"Failed", 0},
                {"Error", 0}
            };

            var vUserGraph = lrAnalysis.Session.OpenGraph("VuserSummary");
            if (vUserGraph == null)
            {
                return vuserDictionary;
            }

            var filterDimension = vUserGraph.Filter["Vuser End Status"];
            foreach (var vuserType in vuserDictionary.Keys.ToList())
            {
                filterDimension.ClearValues();
                filterDimension.AddDiscreteValue(vuserType);
                vUserGraph.ApplyFilterAndGroupBy();
                var sum = vUserGraph.Series.Sum(val => val.GraphStatistics.Maximum);
                vuserDictionary[vuserType] = (int) sum;
            }

            return vuserDictionary;
        }


        /// <summary>
        /// Calculating the number of transactions by status
        /// </summary>
        /// <returns>Transactions by status</returns>
        public static Dictionary<string, Dictionary<string, double>> CalcFailedTransPercent(LrAnalysis lrAnalysis)
        { 
          
            var transactionGraph = lrAnalysis.Session.OpenGraph("TransactionSummary");

            foreach (FilterItem fi in transactionGraph.Filter)
            {
                fi.ClearValues();
                fi.IsActive = false;
                transactionGraph.ApplyFilterAndGroupBy();
            }


            var transDictionary = new Dictionary<string, Dictionary<string, double> > () ;

            transactionGraph.Granularity = 4;
            
            var filterDimension = transactionGraph.Filter["Transaction End Status"];
            foreach (var series in transactionGraph.Series)
            {
                SeriesAttributeValue a;
                if (!series.Attributes.TryGetValue("Event Name", out a)) continue;
                SeriesAttributeValue transEndStatusAttr;

                if (!series.Attributes.TryGetValue("Transaction End Status", out transEndStatusAttr)) continue;

                Dictionary<string, double> value;
                if (!transDictionary.TryGetValue(a.Value.ToString(), out value))
                {
                    transDictionary.Add(a.Value.ToString(),
                        new Dictionary<string, double>() {{"Pass", 0}, {"Fail", 0}, {"Stop", 0}});
                }
                (transDictionary[a.Value.ToString()])[transEndStatusAttr.Value.ToString()] = series.Points[0].Value;
            }
        
            return transDictionary;
        }



        /// <summary>
        /// Returns scenario duration
        /// </summary>
        /// <returns>Scenario duration</returns>
        protected int GetScenarioDuration(LrAnalysis lrAnalysis)
        {
            var rc = lrAnalysis.Session.Runs[0].EndTime - lrAnalysis.Session.Runs[0].StartTime;
            return rc;
        }
    }
}