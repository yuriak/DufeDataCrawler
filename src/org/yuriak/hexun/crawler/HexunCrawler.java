package org.yuriak.hexun.crawler;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.yuriak.hexun.bean.HexunNewsBean;

import cn.edu.hfut.dmic.webcollector.crawler.DeepCrawler;
import cn.edu.hfut.dmic.webcollector.model.Links;
import cn.edu.hfut.dmic.webcollector.model.Page;

public class HexunCrawler extends DeepCrawler {
	ArrayList<HexunNewsBean> newsBeans;
	int pageNumber;
	public HexunCrawler(String crawlPath) {
		super(crawlPath);
	}

	@Override
	public Links visitAndGetNextLinks(Page page) {
		Links links=new Links();
		if (!page.getUrl().endsWith(".html")) {
			//第一层获取页数
			Document document=page.getDoc();
			Elements elements=document.select(".listdh").get(0).getElementsByTag("script");
			//从js中拿到总页数
			int number=Integer.valueOf(elements.get(elements.size()-1).html().split(";")[0].split("\\{")[1].split("=")[1].trim());
			links.add(seeds.get(0)+"/index.html");
			if (pageNumber>number) {
				for (int i = number-1; i > 1; i--) {
					links.add(seeds.get(0)+"/index-"+i+".html");
				}
			}else {
				for (int i = number-1; i > number-pageNumber+1; i--) {
					links.add(seeds.get(0)+"/index-"+i+".html");
				}
			}
		}else if (page.getUrl().split("/")[4].startsWith("index")) {
			//第二获得文章链接
			Document document=page.getDoc();
			Elements elements=document.select(".temp01").get(0).getElementsByTag("li");
			for (Element element : elements) {
				if (element.getElementsByTag("a").size()>1) {
					links.add(element.getElementsByTag("a").get(1).attr("href"));
				}else {
					links.add(element.getElementsByTag("a").get(0).attr("href"));
				}
			}
		}else if (page.getUrl().split("/")[4].matches("^\\d*\\.html$")) {
			//第三层获得正文装进bean
			Document document=page.getDoc();
			HexunNewsBean bean=new HexunNewsBean();
			bean.setUrl(page.getUrl());
			bean.setTitle(document.select("#artibodyTitle").size()>0?document.select("#artibodyTitle").get(0).getElementsByTag("h1").text():"");
			bean.setContent(document.select("#artibody").size()>0?document.select("#artibody").get(0).text():"");
			bean.setTime(document.select(".gray").size()>0?document.select(".gray").get(0).text():"");
			bean.setSource(document.select(".gray").size()>0?document.select(".gray").get(1).getElementsByTag("a").text():"");
			bean.setType(document.select(".crumbs_L").size()>0?document.select(".crumbs_L").get(0).getElementsByTag("a").get(1).text():"");
			newsBeans.add(bean);
		}
		return links;
	}
	
	public void getInfo(String seedUrl,int pageNumber) throws Exception{
		this.pageNumber=pageNumber;
		this.newsBeans=new ArrayList<>();
		if (this.seeds.size()==0) {
			this.addSeed(seedUrl);
		}else {
			this.seeds.set(0, seedUrl);
		}
		this.start(3);
		
		/*
		 * 装json，写文件，这块儿可以拿到job中再进行扩展，然后这块儿可以返回一个beanList。
		 */
		JSONObject mainObject=new JSONObject();
		JSONArray array=new JSONArray();
		for (HexunNewsBean bean : this.newsBeans) {
			JSONObject object=new JSONObject();
			object.put("title", bean.getTitle());
			object.put("content", bean.getContent());
			object.put("time", bean.getTime());
			object.put("source", bean.getSource());
			object.put("type", bean.getType());
			object.put("url", bean.getUrl());
			array.put(object);
		}
		mainObject.put("hxInfo", array);
		String time=new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Timestamp(System.currentTimeMillis()));
		FileUtils.writeStringToFile(new File("data/hxInfo"+time+".txt"), mainObject.toString(), "utf-8");
		
	}
	public static void main(String[] args) throws Exception {
		
	}

}
