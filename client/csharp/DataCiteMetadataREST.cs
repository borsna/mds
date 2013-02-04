using System;
using System.Collections.Generic;
using System.Text;
using System.Net;
using System.IO;

namespace DataCiteMetadataREST{
    /// <summary>
    /// Swedish National Data Service (SND) <http://snd.gu.se>, 2012
    /// Class for using the REST service
    /// use test prefix 10.5072 when testing
    /// username and password is provided from your local allocator
    //  For web management and documentation see: https://mds.datacite.org
    /// </summary>
    class DataCiteMetadataREST{
        private string uri = "https://mds.datacite.org";
        private string username = "";
        private string password = "";
		private bool   testMode = false;

        public DataCiteMetadataREST(string username, string password){
            this.username = username;
            this.password = password;
        }

        public DataCiteMetadataREST(string username, string password, string uri){
            this.username = username;
            this.password = password;
            this.uri = uri;
        }

        /// <summary>
        /// Get the url for the specefied DOI
        /// </summary>
        /// <param name="doi">The DOI to resolve</param>
        /// <returns>URL for the specefied DOI</returns>
        public string getDoi(string doi){
            return makeRequest(uri + "/doi/" + doi);
        }

        /// <summary>
        /// Creates or update a DOI
        /// </summary>
        /// <param name="doi">DOI</param>
        /// <param name="url">URL used when resolving DOI</param>
        /// <returns>Status</returns>
        public string setDoi(string doi, string url){
            string message = "doi="+doi+System.Environment.NewLine;
            message += "url=" + url;

            return makeRequest(uri + "/doi", "POST", message, "text/plain;charset=UTF-8");
        }

        /// <summary>
        /// Get the metadata for the specefied DOI
        /// </summary>
        /// <param name="doi">The DOI to get the metadata for</param>
        /// <returns>string containing the response (XML)</returns>
        public string getMetadata(string doi){
            return makeRequest(uri + "/metadata/" + doi);
        }

        /// <summary>
        /// Posts the metadata to the datacite provider
        /// </summary>
        /// <param name="metadata">metadata (XML)</param>
        /// <returns>Response from server</returns>
        public string setMetadata(string metadata){
            return makeRequest(uri + "/metadata", "POST", metadata, "application/xml;charset=UTF-8");
        }

        public string deleteMetadata(string doi){
            return makeRequest(uri + "/doi/" + doi, "DELETE");
        }

        public string getMedia(string doi){
            return makeRequest(uri + "/media/" + doi);
        }

        /// <summary>
        /// Set media for the specefied DOI
        /// </summary>
        /// <param name="doi">The dDOI to set the media for</param>
        /// <param name="mediaMap">One or multiple values for media mime-type, url</param>
        /// <returns>status of the request</returns>
        public string setMedia(string doi, Dictionary<string,string> mediaMap){
            string message = string.Empty;

            foreach (var m in mediaMap){
                if (message != string.Empty){
                    message += System.Environment.NewLine;
                }
                message += m.Key + "=" + m.Value;
            }

            return makeRequest(uri + "/media/" + doi, "POST", message, "text/plain;charset=UTF-8");
        }

        /// <summary>
        /// Makes a HTTP-request with GET/POST/PUT/DELETE
        /// </summary>
        /// <param name="uri">The URI for the request</param>
        /// <param name="method">Method GET/POST/DELETE/PUT</param>
        /// <param name="message">Message body to send for POST</param>
        /// <param name="contentType">The content type of the message</param>
        /// <returns>The reponse from the the HTTP-request</returns>
        private string makeRequest(string uri, string method = "GET", string message = "", string contentType = "text/xml"){
            if (testMode){
                uri += "?testMode=true";
            }

            var request = (HttpWebRequest)WebRequest.Create(uri);
            request.PreAuthenticate = true;
            request.Credentials = new NetworkCredential(username, password);
            request.Method = WebRequestMethods.Http.Post;

            request.Method = method;

            request.ContentLength = 0;
            request.ContentType = contentType;

            if (method == "POST"){
                UTF8Encoding encoding = new UTF8Encoding();

                byte[] data = encoding.GetBytes(message);

                request.ContentLength = data.Length;
                Stream requestStream = request.GetRequestStream();
                requestStream.Write(data, 0, data.Length);
            }

            string result = string.Empty;
            try{
                using (HttpWebResponse response = (HttpWebResponse)request.GetResponse()){
                    using (Stream responseStream = response.GetResponseStream()){
                        using (System.IO.StreamReader readStream = new System.IO.StreamReader(responseStream, Encoding.UTF8)){
                            result = readStream.ReadToEnd();
                            return result;
                        }
                    }
                }
            }
            catch (Exception exp){
                return exp.Message;
            }
        }

        /// <summary>
        /// The Uri for the DataCite backend default: https://mds.datacite.org
        /// </summary>
		public string Uri{
			get{return uri;}
			set{uri = value;}
		}
		
        /// <summary>
        /// When set to true the request will not change the database nor will the 
        /// DOI handle will be registered or updated
        /// Default value is false
        /// </summary>
		public bool TestMode{
			get{return testMode;}
			set{testMode = value;}
		}
    }
}
