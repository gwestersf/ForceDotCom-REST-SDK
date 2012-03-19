package com.force.api.rest.sobject;

import java.net.URLEncoder;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.force.api.rest.sobject.model.AnySObject;
import com.force.api.rest.sobject.model.SObject;

public class RestSObjectApiClientTest {
	
	private RestSObjectApi client;
	private String sobjectName;
	
	@Before
	public void setUp() throws Exception {
		String sid = "";
		client = new RestSObjectApiClient(sid, "na12.salesforce.com");
		
		//TODO: parameterize this class to test many entities
		sobjectName = "Account";
	}
	
	@After
	public void tearDown() {
		client = null;
		sobjectName = null;
	}
	
	@Test
	public void testDescribeGlobal() throws Exception {
		client.describeGlobal();
	}
	
	@Test
	public void testDescribeLayout() throws Exception {
		client.describeLayout(sobjectName);
	}
	
	@Test
	public void testDescribeSObject() throws Exception {
		client.describeSobject(sobjectName);
	}
	
	@Test
	public void testCreate() throws Exception {
		AnySObject sobject = new AnySObject(sobjectName);
		sobject.setString("Name", "test1234");
		client.create(sobject);
	}
	
	@Test
	public void testGetSObjectFail() throws Exception {
		String badId = "005xx" + "xxxxx" + "xxxxx" + "xxx";
		client.get(sobjectName, badId);
	}
	
	@Test
	public void testUpdateFail() throws Exception {
		String badId = "005xx" + "xxxxx" + "xxxxx" + "xxx";
		JSONObject json = new JSONObject();
		json.put("Name", "test1234");
		json.put("Id", badId);
		SObject sobject = new AnySObject(sobjectName, json);
		client.update(sobject);
	}
	
	@Test
	public void testDeleteFail() throws Exception {
		String badId = "005xx" + "xxxxx" + "xxxxx" + "xxx";
		client.delete(sobjectName, badId);
	}
	
	@Test
	public void testQuery() throws Exception {
		String query = "SELECT id, name FROM " + sobjectName;
		String encodedQuery = URLEncoder.encode(query, "UTF-8");
		client.query(encodedQuery);
	}
	
	@Test
	public void testRecent() throws Exception {
		client.recent();
	}
	
	@Test
	public void testSearch() throws Exception {
		String query = "FIND {\"test\"}";
		String encodedQuery = URLEncoder.encode(query, "UTF-8");
		client.search(encodedQuery);
	}
	
}
