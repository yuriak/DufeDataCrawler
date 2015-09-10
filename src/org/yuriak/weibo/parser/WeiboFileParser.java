package org.yuriak.weibo.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.apache.solr.common.util.DateUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.yuriak.weibo.bean.WeiboEntityBean;
import org.yuriak.weibo.bean.WeiboSolrBean;
import org.yuriak.weibo.util.WeiboStringUtil;

public class WeiboFileParser {
	
	public static ArrayList<WeiboSolrBean> parse4Solr(File sourceFile) throws Exception{
		Collection<String> format=new HashSet<String>();
		format.add("yy-MM-dd HH:mm:ss");
		ArrayList<WeiboSolrBean> solrBeans=new ArrayList<>();
		String source=FileUtils.readFileToString(sourceFile, "utf-8");
		JSONArray mainArray=new JSONArray(source);
		for (int i = 0; i < mainArray.length(); i++) {
			WeiboSolrBean bean=new WeiboSolrBean();
			JSONObject weiboObject=mainArray.getJSONObject(i);
			bean.setId(weiboObject.getString("id"));
			bean.setWeiboid(weiboObject.getString("id"));
			bean.setDate(DateUtil.parseDate(weiboObject.getString("time"), format));
			bean.setType("weibo");
			JSONObject weiboEntityObject=weiboObject.getJSONObject("weiboEntity");
			JSONObject contentObject=weiboEntityObject.getJSONObject("content");
			if (contentObject.getString("text").contains("转发微博")) {
				JSONObject retweetedObject=contentObject.getJSONObject("retweeted_status");
				bean.setContent(WeiboStringUtil.delHTMLTag(contentObject.getString("text")+"//"+retweetedObject.getString("text")));
			}else {
				bean.setContent(WeiboStringUtil.delHTMLTag(contentObject.getString("text")));
			}
			solrBeans.add(bean);
			if (contentObject.getInt("comments_count")>0) {
				JSONArray commentArray=weiboObject.getJSONArray("comments");
				for (int j = 0; j < commentArray.length(); j++) {
					WeiboSolrBean commentBean=new WeiboSolrBean();
					JSONObject commentObject=commentArray.getJSONObject(j);
					commentBean.setId(commentObject.getString("id"));
					commentBean.setType("comment");
					commentBean.setWeiboid(bean.getWeiboid());
					JSONObject commentContentObject=commentObject.getJSONObject("content");
					commentBean.setContent(WeiboStringUtil.delHTMLTag(commentContentObject.getString("text")));
					commentBean.setDate(bean.getDate());
					solrBeans.add(commentBean);
				}
			}
		}
		return solrBeans;
	}
	
	public static void main(String[] args) throws Exception {
		ArrayList<WeiboSolrBean> solrBeans=WeiboFileParser.parse4Solr(new File("data/weibo2015_08_11_23_37_11.txt"));
		for (WeiboSolrBean weiboSolrBean : solrBeans) {
			System.out.println(weiboSolrBean.getContent());
		}
	}
}
