package org.yuriak.weibo.crawler;

import java.util.ArrayList;
import java.util.List;

import org.yuriak.weibo.bean.WeiboCommentBean;
import org.yuriak.weibo.config.CommonValues;
import org.yuriak.weibo.parser.WeiboCommentParser;

import cn.edu.hfut.dmic.webcollector.crawler.DeepCrawler;
import cn.edu.hfut.dmic.webcollector.model.Links;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.HttpRequesterImpl;

public class WeiboCommentCrawler extends DeepCrawler {
	public List<WeiboCommentBean> commentBeans;
	public WeiboCommentCrawler(String crawlPath) {
		super(crawlPath);
	}
	
	@Override
	public Links visitAndGetNextLinks(Page page) {
		commentBeans.addAll(WeiboCommentParser.parseWeiboComment(page.getHtml()));
		return null;
	}
	
	public List<WeiboCommentBean> getComment(String weiboId,int pageNumber,String cookie) throws Exception{
		commentBeans=new ArrayList<>();
		HttpRequesterImpl myRequester=(HttpRequesterImpl)this.getHttpRequester();
		myRequester.setCookie(cookie);
		for (int i = 1; i <= pageNumber; i++) {
			this.addSeed("http://m.weibo.cn/single/rcList?format=cards&id="+weiboId+"&type=comment&page="+i);
		}
		this.start(1);
    	return commentBeans;
	}
	
	public static void main(String[] args) throws Exception {
//		WeiboCommentCrawler weiboCommentCrawler=new WeiboCommentCrawler("data");
//		List<WeiboCommentBean> weiboCommentBeans=weiboCommentCrawler.getComment("3857757754972117", 21);
//		for (WeiboCommentBean weiboCommentBean : weiboCommentBeans) {
//			System.out.println(weiboCommentBean.getId()+"|"+weiboCommentBean.getContent());
//		}
//		System.out.println(weiboCommentBeans.size());
	}
	
}
