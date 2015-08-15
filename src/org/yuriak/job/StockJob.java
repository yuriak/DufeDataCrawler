package org.yuriak.job;

import java.util.ArrayList;
import java.util.Collections;

import org.yuriak.stock.*;
import org.yuriak.stock.bean.StockBean;
import org.yuriak.stock.crawler.StockIDCrawler;
import org.yuriak.stock.crawler.StockInfoCrawler;
import org.yuriak.stock.util.StockFileUtil;

public class StockJob {
	public StockJob(){
		
	}
	
	public void run() throws Exception{
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		StockIDCrawler crawler=new StockIDCrawler("crawldb");
		StockInfoCrawler infoCrawler=new StockInfoCrawler("crawldb");
		ArrayList<StockBean> stocks=infoCrawler.getStockInfo(crawler.getAllStock());
		Collections.sort(stocks);
		StockFileUtil.writeInfoToFile(stocks);
	}
	
	public static void main(String[] args) {
		int pageNumber=10;
		if (args.length==1) {
			if (!args[0].equals("stock")) {
				return;
			}
		}
		StockJob stockJob=new StockJob();
		try {
			stockJob.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
