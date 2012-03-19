package com.force.api.rest.sobject;

/**
 * 
 * @author gwester
 */
public class ResultInfo {
	private String type;
	private String url;

	public String getType() {
		return this.type;
	}
	public String getUrl() {
		return this.url;
	}

	protected void setType(String type) {
		this.type = type;
	}
	protected void setUrl(String url) {
		this.url = url;
	}
}
