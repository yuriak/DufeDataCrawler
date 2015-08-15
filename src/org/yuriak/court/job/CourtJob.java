package org.yuriak.court.job;

import org.yuriak.court.crawler.CourtCrawler;
import org.yuriak.hexun.crawler.HexunCrawler;

public class CourtJob {
	public CourtJob(){
		
	}
	
	public void run(){
		CourtCrawler courtCrawler=new CourtCrawler("crawldb", true);
		try {
			courtCrawler.getArticle();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		if (args.length==1&&args[0].equals("court")) {
			CourtJob courtJob=new CourtJob();
			courtJob.run();
		}
	}
}
