using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace HpToolsLauncher
{
    public class TestData
    {
        public TestData(string tests,string id)
        {
            this.Tests = tests;
            this.Id = id;
        }

        public string Tests{get;set;}
        public string Id { get; set; }

        public override string ToString()
        {
            return Id + ": " + Tests;
        }

    }
}
