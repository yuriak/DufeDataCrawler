package org.yuriak.court.crawler;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import cn.edu.hfut.dmic.webcollector.crawler.BreadthCrawler;
import cn.edu.hfut.dmic.webcollector.model.Links;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.util.RegexRule;

public class CourtCrawler extends BreadthCrawler {
	private JSONArray array;
	
	public CourtCrawler(String crawlPath, boolean autoParse) {
		super(crawlPath, autoParse);
		array=new JSONArray();
	}

	@Override
	public void visit(Page page, Links links) {
		
		System.out.println(links.size());
//		for (String string : links) {
//			System.out.println(string);
//		}
		if (page.getUrl().split("/")[page.getUrl().split("/").length-1].matches("t\\d*_\\d*\\.htm")) {
			/*
			 * 此处过滤文档信息并装入jsonArray
			 */
//			System.out.println(page.getDoc().select("#wenshu").text());
			String string=page.getDoc().select("#wenshu").text();
			JSONObject object=new JSONObject();
			object.put("content", string.trim());
			array.put(object);
		}
	}
	
	public static void main(String[] args) throws Exception {
		RegexRule regexRule=new RegexRule();
		/*
		 * 这是不抓取的正则，去掉了不用的文件类型
		 */
		regexRule.addNegative("^(file|ftp|mailto):");
		regexRule.addNegative("\\.(gif|GIF|jpg|JPG|png|PNG|ico|ICO|css|CSS|sit|SIT|eps|EPS|wmf|WMF|zip|ZIP|ppt|PPT|mpg|MPG|xls|XLS|gz|GZ|rpm|RPM|tgz|TGZ|mov|MOV|exe|EXE|jpeg|JPEG|bmp|BMP|js|JS)$");
		regexRule.addNegative("[?*!@=]");
		/*
		 * 这是需要抓取的正则，如果有别的写法可以自己改
		 */
		regexRule.addPositive("http://www.court.gov.cn/zgcpwsw/[a-z]*/.*");
		
		CourtCrawler crawler=new CourtCrawler("data", true);
		crawler.addSeed("http://www.court.gov.cn/zgcpwsw/");
		/*
		 * 设置每层最大获取的url数量，这个可以加更多10000左右应该够了
		 */
		crawler.setTopN(1000);
		crawler.setRegexRule(regexRule);
		/*
		 * 抓取的数据量多少取决于这个值，大概10层左右能把整个网站抓全
		 */
		crawler.start(4);
		System.out.println(crawler.array.length());
		String time=new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Timestamp(System.currentTimeMillis()));
		FileUtils.writeStringToFile(new File("data/court"+time+".txt"), crawler.array.toString().trim(), "UTF-8",false);
	}
	
	public void getArticle() throws Exception{
		RegexRule regexRule=new RegexRule();
		/*
		 * 这是不抓取的正则，去掉了不用的文件类型
		 */
		regexRule.addNegative("^(file|ftp|mailto):");
		regexRule.addNegative("\\.(gif|GIF|jpg|JPG|png|PNG|ico|ICO|css|CSS|sit|SIT|eps|EPS|wmf|WMF|zip|ZIP|ppt|PPT|mpg|MPG|xls|XLS|gz|GZ|rpm|RPM|tgz|TGZ|mov|MOV|exe|EXE|jpeg|JPEG|bmp|BMP|js|JS)$");
		regexRule.addNegative("[?*!@=]");
		/*
		 * 这是需要抓取的正则，如果有别的写法可以自己改
		 */
		regexRule.addPositive("http://www.court.gov.cn/zgcpwsw/[a-z]*/.*");
		
		this.addSeed("http://www.court.gov.cn/zgcpwsw/");
		/*
		 * 设置每层最大获取的url数量，这个可以加更多10000左右应该够了
		 */
		this.setTopN(1000);
		this.setRegexRule(regexRule);
		/*
		 * 抓取的数据量多少取决于这个值，大概10层左右能把整个网站抓全
		 */
		this.start(10);
		String time=new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Timestamp(System.currentTimeMillis()));
		FileUtils.writeStringToFile(new File("data/court"+time+".txt"), this.array.toString().trim(), "UTF-8",false);
	}
}
