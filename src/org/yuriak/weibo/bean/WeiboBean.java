package org.yuriak.weibo.bean;

import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class WeiboBean {
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
	
	
	public static JSONObject makeSimpleJson(WeiboEntityBean weiboEntityBean,List<WeiboCommentBean> weiboCommentBeans){
		/*
		 * 制作简单的整合json结构
		 */
		JSONObject mainObject=new JSONObject();
		JSONObject entityObject=new JSONObject();
		JSONArray commentArray=new JSONArray();
		for (WeiboCommentBean weiboCommentBean : weiboCommentBeans) {
			JSONObject commentObject=new JSONObject();
			commentObject.put("content", weiboCommentBean.getContent());
			commentObject.put("id", weiboCommentBean.getId());
			commentArray.put(commentObject);
		}
		entityObject.put("content", weiboEntityBean.getContent());
		mainObject.put("comments", commentArray);
		mainObject.put("weiboEntity", entityObject);
		mainObject.put("time", weiboEntityBean.getTime());
		mainObject.put("id", weiboEntityBean.getId());
		return mainObject;
	}
}
