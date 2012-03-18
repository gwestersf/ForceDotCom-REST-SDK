/*
 * Copyright (c) 2011, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.force.api.rest.sobject;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import com.force.api.rest.sobject.model.AnySObject;
import com.force.api.rest.sobject.model.SObject;
import com.google.gson.Gson;

/**
 * RestConnection
 * 
 * @author gwester
 */
public class RestSObjectApiClient implements RestSObjectApi {
	
	private final Logger logger = Logger.getLogger(RestSObjectApiClient.class.getName());
		
	private String sessionId;
    private final String baseUrl;
    private final Gson parser;
    private final HttpClient client;
    
    private static final String AUTH_HEADER = "Authorization";
    private static final String AUTH_VALUE_PREFIX = "OAuth ";
    private static final String CHARSET_HEADER = "Accept-Charset";
    private static final String CHARSET_VALUE = "UTF-8";
    private static final String CONTENT_HEADER = "Content-Type";
    private static final String CONTENT_VALUE = "application/json";
    private static final String PRETTY_HEADER = "X-Pretty-Print";
    private static final String PRETTY_VALUE = "1";
    
    private static final String SEPARATOR = "/";
    
    private static final String SOBJECTS_ENDPOINT = "sobjects" + SEPARATOR;
    private static final String QUERY_ENDPOINT = "query?q=";
    private static final String SEARCH_ENDPOINT = "search?q=";
    private static final String RECENT_ENDPOINT = "recent" + SEPARATOR;
    
    private static final String DESCRIBE_SUBENDPOINT = "describe" + SEPARATOR;
    
    private static final String PATCH_PARAMETER = "?_HttpMethod=PATCH";
    
    /**
     * 
     * @param sessionId
     * @param baseUrl
     * @throws RestApiException
     */
    public RestSObjectApiClient(String sessionId, String hostname, double apiVersion) throws RestApiException {
        if (sessionId == null || sessionId.isEmpty() || sessionId.length() < 20) { 
        	throw new IllegalArgumentException("Provide a sessionId");
        }
        if (hostname == null || hostname.isEmpty()) { 
        	throw new IllegalArgumentException("Provide a hostname like na1.salesforce.com");
        }
        if (apiVersion < 22.0d) { 
        	throw new IllegalArgumentException("Provide an API Version 22.0 or higher"); 
        }
        
        this.baseUrl = "https://" + hostname + "/services/data/v" + String.valueOf(apiVersion);
        this.parser = new Gson();
        this.client = new HttpClient();
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * Re-set the session ID if the session expires.
     * 
     * @param sessionId
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public DescribeGlobal describeGlobal() throws IOException, RestApiException {
    	GetMethod method = new GetMethod(baseUrl + SOBJECTS_ENDPOINT);
		String json = executeHttpRequest(method);
		return parser.fromJson(json, DescribeGlobal.class);
	}

    @Override
    public DescribeSobject describeSobject(String sobjectName) throws IOException, RestApiException {
    	GetMethod method = new GetMethod(baseUrl + SOBJECTS_ENDPOINT + sobjectName + SEPARATOR);
		String json = executeHttpRequest(method);
		return parser.fromJson(json, DescribeSobject.class);
	}

    @Override
    public DescribeLayout describeLayout(String sobjectName) throws IOException, RestApiException {
    	GetMethod method = new GetMethod(baseUrl + SOBJECTS_ENDPOINT + sobjectName + SEPARATOR + DESCRIBE_SUBENDPOINT);
		String json = executeHttpRequest(method);
		return parser.fromJson(json, DescribeLayout.class);
	}

    @Override
	public SObject get(String sobjectName, String id) throws IOException, RestApiException {    
		GetMethod method = new GetMethod(baseUrl + SOBJECTS_ENDPOINT + sobjectName + SEPARATOR + id.toString() + SEPARATOR);
		String json = executeHttpRequest(method);
		JSONObject json = new JSONObject(json);
		return new AnySObject(sobjectName, json);
	}

    @Override
	public SObjectResult create(SObject sobject) throws IOException, RestApiException {
    	PostMethod method = new PostMethod(baseUrl + SOBJECTS_ENDPOINT + sobject.getName() + SEPARATOR);
		method.setRequestEntity(new StringRequestEntity(sobject.toJson(), CONTENT_VALUE, CHARSET_VALUE));
		String json = executeHttpRequest(method);
		
		return parser.fromJson(json, SObjectResult.class);
	}

    @Override
	public SObjectResult update(SObject sobject, String id) throws IOException {
    	String url = baseUrl + SOBJECTS_ENDPOINT + sobject.getName() + SEPARATOR + id + SEPARATOR;
    	//override POST by setting paramter on end of URL; Salesforce will route this to doPatch in the servlet
		PostMethod method = new PostMethod(url + PATCH_PARAMETER);
		method.setRequestEntity(new StringRequestEntity(sobject.toJson(), CONTENT_VALUE, CHARSET_VALUE));

		String json = executeHttpRequest(method);
		return parser.fromJson(json, SObjectResult.class);
	}

    @Override
	public SObjectResult delete(String sobjectName, String id) throws IOException, RestApiException {
    	DeleteMethod method = new DeleteMethod(baseUrl + SOBJECTS_ENDPOINT + sobjectName + SEPARATOR + id + SEPARATOR);
		String json = executeHttpRequest(method);
		return parser.fromJson(json, SObjectResult.class);
	}

    @Override
    public QueryResult query(String query) throws IOException, RestApiException {
		if(query == null || query.isEmpty() || !query.contains("SELECT") || !query.contains("FROM")) {
			throw new IllegalArgumentException("Query must be in the form: SELECT+id+FROM+sobject+WHERE+something=else");
		}
		
		GetMethod method = new GetMethod(baseUrl + QUERY_ENDPOINT + query);
		String json = executeHttpRequest(method);
		return parser.fromJson(json, QueryResult.class);
	}

    @Override
	public SearchResult search(String search) throws IOException, RestApiException {
		if(search == null || search.isEmpty() || !search.contains("{") || !search.contains("}") ||  !search.startsWith("FIND")) {
			throw new IllegalArgumentException("Search must be in the form: FIND+{myTerm}");
		}
		
		GetMethod method = new GetMethod(baseUrl + SEARCH_ENDPOINT + search);
		String json = executeHttpRequest(method);
		return parser.fromJson(json, SearchResult.class);
	}

    @Override
	public SearchResult recent() throws IOException, RestApiException {
    	GetMethod method = new GetMethod(baseUrl + RECENT_ENDPOINT);
		String json = executeHttpRequest(method);
		return parser.fromJson(json, SearchResult.class);
	}
	
	/**
	 * HTTP GET
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws RestApiException
	 */
    private String executeHttpRequest(HttpMethod method) throws IOException, RestApiException {
    	method.setRequestHeader(AUTH_HEADER, AUTH_VALUE_PREFIX + sessionId);
    	method.setRequestHeader(CONTENT_HEADER, CONTENT_VALUE);
    	method.setRequestHeader(CHARSET_HEADER, CHARSET_VALUE);
    	method.setRequestHeader(PRETTY_HEADER, PRETTY_VALUE);
    	
    	int status = client.executeMethod(method);
    	String responseBody = IOUtils.toString(method.getResponseBodyAsStream());
    	logger.log(Level.INFO, responseBody);
    	if(status >= 400) {
    		throw new RestApiException(responseBody, status);
    	}
    	return responseBody;
    }
}
