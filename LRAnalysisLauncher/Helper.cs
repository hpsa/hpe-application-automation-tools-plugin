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
        public static Dictionary<string, double> CalcFailedTransPercent(LrAnalysis lrAnalysis)
        {
            var transactionGraph = lrAnalysis.Session.Runs[0].OpenGraph("TransactionSummary");
            var transDictionary = new Dictionary<string, double>(3)
            {
                {"Pass", 0},
                {"Fail", 0},
                {"Stop", 0}
            };

            var filterDimension = transactionGraph.Filter["Transaction End Status"];
            foreach (var transactionStatusType in transDictionary.Keys.ToList())
            {
                filterDimension.ClearValues();
                filterDimension.AddDiscreteValue(transactionStatusType);
                transactionGraph.ApplyFilterAndGroupBy();
                var sum = transactionGraph.Series.Sum(val => val.GraphStatistics.Maximum);
                transDictionary[transactionStatusType] = (int)sum;
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