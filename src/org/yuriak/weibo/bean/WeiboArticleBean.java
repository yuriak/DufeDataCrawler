package org.yuriak.weibo.bean;

import org.json.JSONObject;

public class WeiboArticleBean {
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public JSONObject getOriginContent() {
		return originContent;
	}
	public void setOriginContent(JSONObject originContent) {
		this.originContent = originContent;
	}
	private String id;
	private String title;
	private String content;
	private JSONObject originContent;
}
