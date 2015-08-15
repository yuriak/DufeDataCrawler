package org.yuriak.weibo.bean;

import org.json.JSONObject;

public class WeiboCommentBean {
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public JSONObject getContent() {
		return content;
	}
	public void setContent(JSONObject content) {
		this.content = content;
	}
	private String id;
	private JSONObject content;
	private String uid;
}
