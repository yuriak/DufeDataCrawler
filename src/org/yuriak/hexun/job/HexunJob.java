package org.yuriak.hexun.job;

import org.yuriak.hexun.crawler.HexunCrawler;

public class HexunJob {
	private int pageNumber;
	
	public HexunJob(int pageNumber){
		this.pageNumber=pageNumber;
	}
	public void run() throws Exception{
		HexunCrawler hexunCrawler=new HexunCrawler("crawldb");
		//分类别爬取，可能会导致无法区分文件，这个地方可以自己扩展。
		hexunCrawler.getInfo("http://futures.hexun.com/integratednews", pageNumber);
		hexunCrawler.getInfo("http://futures.hexun.com/agriculturenews", pageNumber);
		hexunCrawler.getInfo("http://futures.hexun.com/nyzx", 10);
		hexunCrawler.getInfo("http://futures.hexun.com/industrynews", pageNumber);
		hexunCrawler.getInfo("http://futures.hexun.com/chemicalnews", pageNumber);
	}
	public static void main(String[] args) throws Exception {
		int pageNumber=10;
		if (args.length==2) {
			if (!args[0].equals("hexun")) {
				return;
			}
			pageNumber=Integer.valueOf(args[1]);
		}
		HexunJob hexunJob=new HexunJob(pageNumber);
		hexunJob.run();
	}
}
