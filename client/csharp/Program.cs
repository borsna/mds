using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;

/* Swedish National Data Service (SND) <http://snd.gu.se>, 2012
 * Test program for the class DataCiteMetadataREST
 * use test prefix 10.5072 when testing
 * username and password is provided from your local allocator
 * For web management and documentation see: https://mds.datacite.org
 */

namespace DataCiteMetadataREST{
    class Program{
        static void Main(string[] args){
            string username = "USER";
            string password = "password";

            DataCiteMetadataREST dataCite = new DataCiteMetadataREST(username, password);

            //Load sample data
            string sampleMetadata = System.IO.File.ReadAllText(@"..\sample-metadata.xml");

            //Test for geting metadata
            System.Console.WriteLine("Get metadata for DOI 10.5072/test-data0042 :");
            System.Console.WriteLine(dataCite.getMetadata("10.5072/test-data0042"));
            
            //Test for posting metadata
            System.Console.WriteLine("Posting metadata sample-metadata.xml :");
            System.Console.WriteLine(dataCite.setMetadata(sampleMetadata));

            //Test for creating/updating DOI
            System.Console.WriteLine("Set DOI 10.5072/test-data0042 URL to: http://example.com/data/42");
            System.Console.WriteLine(dataCite.setDoi("test-data0042", "http://example.com/data/42"));

            //Test for resolving DOI
            System.Console.WriteLine("Get DOI 10.5072/test-data0042 :");
            System.Console.WriteLine(dataCite.getDoi("10.5072/test-data0042"));

            System.Console.Read();
        }
    }
}