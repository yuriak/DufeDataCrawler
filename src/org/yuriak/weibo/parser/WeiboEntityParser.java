package org.yuriak.weibo.parser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.yuriak.weibo.bean.WeiboBean;
import org.yuriak.weibo.bean.WeiboEntityBean;
import org.yuriak.weibo.util.WeiboStringUtil;
import org.yuriak.weibo.util.WeiboTimeUtil;

public class WeiboEntityParser {
	public static List<WeiboEntityBean> parse(String source){
		List<WeiboEntityBean> weiboEntityBeans=new ArrayList<>();
		JSONArray array=new JSONArray(source);
		for (int i = 0; i < array.length(); i++) {
			JSONObject object=array.getJSONObject(i);
			JSONArray cardGroup=object.getJSONArray("card_group");
			for (int j = 0; j < cardGroup.length(); j++) {
				JSONObject cardObject=cardGroup.getJSONObject(j);
				JSONObject mBlog=cardObject.getJSONObject("mblog");
				WeiboEntityBean entityBean=new WeiboEntityBean();
				entityBean.setId(mBlog.get("id").toString());
				entityBean.setTime(WeiboTimeUtil.convertLongToDateString(mBlog.get("created_timestamp").toString()));
				entityBean.setCommentNumber(mBlog.getInt("comments_count"));
				entityBean.setContent(mBlog);
				weiboEntityBeans.add(entityBean);
			}
		}
		return weiboEntityBeans;
	}
}
