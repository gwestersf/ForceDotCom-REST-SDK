package com.force.api.rest.sobject.model;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Maps;

/**
 * 
 * @author gwester
 */
public class AnySObject extends SObject {
	
	private final String name;
	private final Map<String, Object> fields;
	
	public AnySObject(String sobjectName) {
		name = sobjectName;
		fields = Maps.<String, Object>newHashMap();
	}
	
	public AnySObject(String sobjectName, JSONObject json) throws JSONException {
		name = sobjectName;
		fields = Maps.<String, Object>newHashMap();
		@SuppressWarnings("unchecked")
		Iterator<String> iter = json.keys();
		while(iter.hasNext()) {
			String key = iter.next();
			fields.put(key, json.get(key));
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void setFields(Map<String, Object> fieldNameValuePairs) {
		fields.putAll(fieldNameValuePairs);
	}
	
	public void setDouble(String fieldName, double value) {
		fields.put(fieldName, Double.valueOf(value));
	}
	
	public void setBoolean(String fieldName, boolean value) {
		fields.put(fieldName, Boolean.valueOf(value));
	}
	
	public void setString(String fieldName, String value) {
		fields.put(fieldName, value);
	}

	@Override
	public Set<String> getFieldNames() {
		return fields.keySet();
	}

	@Override
	public Object getField(String name) {
		return fields.get(name);
	}

	@Override
	public String toJson() throws JSONException {
		JSONObject json = new JSONObject();
		for(String field : fields.keySet()) {
			json.put(field, fields.get(field));
		}
		return json.toString();
	}
}
