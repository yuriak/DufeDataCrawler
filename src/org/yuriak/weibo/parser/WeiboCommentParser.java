package org.yuriak.weibo.parser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.yuriak.weibo.bean.WeiboCommentBean;
import org.yuriak.weibo.util.WeiboStringUtil;

public class WeiboCommentParser {
	public static List<WeiboCommentBean> parseWeiboComment(String source){
		List<WeiboCommentBean> weiboCommentBeans=new ArrayList<>();
		JSONArray array=new JSONArray(source);
		JSONObject object;
		if (array.length()>1) {
			object=array.getJSONObject(1);
		}else {
			object=array.getJSONObject(0);
		}
		JSONArray cardGroup=object.getJSONArray("card_group");
		if ((cardGroup==null)||(cardGroup.length()==0)) {
			return weiboCommentBeans;
		}
		for (int i = 0; i < cardGroup.length(); i++) {
			JSONObject cardObject=cardGroup.getJSONObject(i);
			WeiboCommentBean weiboCommentBean=new WeiboCommentBean();
			weiboCommentBean.setId(cardObject.get("id").toString());
			weiboCommentBean.setUid(cardObject.getJSONObject("user").get("id").toString());
			weiboCommentBean.setContent(cardObject);
			weiboCommentBeans.add(weiboCommentBean);
		}
		return weiboCommentBeans;
	}
}
