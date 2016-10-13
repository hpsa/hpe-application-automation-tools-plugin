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


            Dictionary<string, int> vuserDictionary = new Dictionary<string, int>(3);
            vuserDictionary.Add("Passed", 0);
            vuserDictionary.Add("Error", 0);
            vuserDictionary.Add("Failed", 0);
           
            double sum;
            foreach (KeyValuePair<string, int> vuserType in vuserDictionary)
            {
                Graph vUserGraph = lrAnalysis.Session.Runs[0].Graphs["VuserStateGraph"];
                if (vUserGraph == null)
                {
                    return null;
                }
                var filterDimension = vUserGraph.Filter["Vuser End Status"];
                filterDimension.ClearValues();
                filterDimension.AddDiscreteValue(vuserType.Key);
                vUserGraph.ApplyFilterAndGroupBy();
                sum = vUserGraph.Series.Sum(val => val.GraphStatistics.Maximum);

                vuserDictionary[vuserType.Key] = (int) sum;
            }

            return vuserDictionary;
        }
    }
}