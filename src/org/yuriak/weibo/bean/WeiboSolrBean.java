package org.yuriak.weibo.bean;

import java.util.Date;

import org.apache.solr.client.solrj.beans.Field;

public class WeiboSolrBean {
	public String getWeiboid() {
		return weiboid;
	}
	public void setWeiboid(String weiboid) {
		this.weiboid = weiboid;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	@Field
	private String id;
	@Field
	private String weiboid;
	@Field
	private String type;
	@Field
	private String content;
	@Field
	private Date date;
}
