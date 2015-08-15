package org.yuriak.weibo.crawler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.yuriak.weibo.bean.WeiboBean;
import org.yuriak.weibo.bean.WeiboEntityBean;
import org.yuriak.weibo.config.CommonValues;
import org.yuriak.weibo.parser.WeiboEntityParser;

import cn.edu.hfut.dmic.webcollector.crawler.DeepCrawler;
import cn.edu.hfut.dmic.webcollector.model.Links;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.HttpRequester;
import cn.edu.hfut.dmic.webcollector.net.HttpRequesterImpl;
import cn.edu.hfut.dmic.webcollector.weiboapi.WeiboCN;

public class WeiboEntityCrawler extends DeepCrawler{
	List<WeiboEntityBean> weiboEntityBeans;
	public WeiboEntityCrawler(String crawlPath) {
		super(crawlPath);
	}
	
	@Override
	public Links visitAndGetNextLinks(Page page) {
		weiboEntityBeans.addAll(WeiboEntityParser.parse(page.getHtml()));
		System.out.println();
		return null;
	}
	
	public List<WeiboEntityBean> getWeiboEntity(int pageNumber,String cookie) throws Exception{
		weiboEntityBeans=new ArrayList<>();
		HttpRequesterImpl myRequester=(HttpRequesterImpl) 
		this.getHttpRequester();
        myRequester.setCookie(cookie);
        for (int i = 1; i <= pageNumber; i++) {
        	this.addSeed(CommonValues.WEIBO_BASE_URL+"/index/feed?format=cards&page="+i);
		}
        this.start(1);
        return weiboEntityBeans;
	}
	
	public static void main(String[] args) throws Exception {
//		WeiboEntityCrawler weiboCrawler=new WeiboEntityCrawler("data");
//		weiboCrawler.getWeiboEntity(1);
//		System.out.println(weiboCrawler.weiboEntityBeans.size());
//		for (WeiboEntityBean weiboEntityBean : weiboCrawler.weiboEntityBeans) {
//			System.out.println(weiboEntityBean.getContent());
//		}
	}
	
}
