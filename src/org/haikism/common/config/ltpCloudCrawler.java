package org.haikism.common.config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection.Request;


import org.jsoup.nodes.Document;

import cn.edu.hfut.dmic.webcollector.crawler.DeepCrawler;
import cn.edu.hfut.dmic.webcollector.model.Links;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.HttpRequester;
import cn.edu.hfut.dmic.webcollector.net.HttpRequesterImpl;
import cn.edu.hfut.dmic.webcollector.net.HttpResponse;
import cn.edu.hfut.dmic.webcollector.net.RequestConfig;

public class ltpCloudCrawler extends DeepCrawler {

	private String result;
	public ltpCloudCrawler(String crawlPath) throws Exception {
		super(crawlPath);
	}

	@Override
	public Links visitAndGetNextLinks(Page page) {
		// TODO Auto-generated method stub
		result=page.getHtml();
		return null;
	}
	
	public static String getResult(String text) throws Exception{
		ltpCloudCrawler crawler=new ltpCloudCrawler("/");
		crawler.addSeed("http://ltpapi.voicecloud.cn/analysis/?api_key=w3P7b4V6ELLIozEEDMhG8LZCrZ8ciPGAyV7N2wyc&pattern=all&format=json&text="+URLEncoder.encode(text, "UTF-8"));
		crawler.start(1);
		return crawler.result;
	}
	
	public static void main(String[] args) throws Exception {
		
	}

}
