package org.yuriak.weibo.crawler;

import org.json.JSONObject;
import org.yuriak.weibo.bean.WeiboArticleBean;
import org.yuriak.weibo.config.CommonValues;
import org.yuriak.weibo.parser.WeiboArticleParser;

import cn.edu.hfut.dmic.webcollector.crawler.DeepCrawler;
import cn.edu.hfut.dmic.webcollector.model.Links;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.HttpRequesterImpl;

public class WeiboArticleCrawler extends DeepCrawler {
	
	WeiboArticleBean articleBean;
	public WeiboArticleCrawler(String crawlPath) {
		super(crawlPath);
		
	}


	@Override
	public Links visitAndGetNextLinks(Page page) {
		articleBean=WeiboArticleParser.parseWeiboArticle(page.getHtml());
		return null;
	}
	
	public WeiboArticleBean getArticle(String cid,String cookie) throws Exception{
		HttpRequesterImpl myRequester=(HttpRequesterImpl)this.getHttpRequester();
		myRequester.setCookie(cookie);
		this.addSeed("http://card.weibo.com/article/aj/articleshow?cid="+cid);
		this.start(1);
		return this.articleBean;
	}
	
	public static void main(String[] args) throws Exception {
		WeiboArticleCrawler crawler=new WeiboArticleCrawler("crawldb");
		WeiboArticleBean bean=crawler.getArticle("1001603860164301204909",CommonValues.COOKIE);
		System.out.println(bean.getTitle());
		System.out.println(bean.getId());
		System.out.println(bean.getContent());
	}
	
	/*
	 * 
	 */
}
