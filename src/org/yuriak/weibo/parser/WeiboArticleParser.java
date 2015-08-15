package org.yuriak.weibo.parser;

import org.json.JSONObject;
import org.yuriak.weibo.bean.WeiboArticleBean;

public class WeiboArticleParser {
	public static WeiboArticleBean parseWeiboArticle(String source){
		WeiboArticleBean bean=new WeiboArticleBean();
		JSONObject mainObject=new JSONObject(source);
		JSONObject dataObject=mainObject.getJSONObject("data");
		JSONObject configObject=dataObject.getJSONObject("config");
		bean.setId(configObject.get("cid").toString());
		//这里把正文装进去，但是我没写去标签的模块，可以自己扩展，因为里面可能有图片或者其他多媒体内容
		bean.setContent(dataObject.getString("article"));
		bean.setTitle(dataObject.getString("title"));
		bean.setOriginContent(mainObject);
		return bean;
	}
}
