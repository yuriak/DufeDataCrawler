package org.yuriak.main;

import org.yuriak.court.job.CourtJob;
import org.yuriak.hexun.job.HexunJob;
import org.yuriak.job.StockJob;
import org.yuriak.weibo.job.WeiboJob;

public class Launcher {
	public Launcher(){
		
	}
	
	public static void main(String[] args) {
		if (args.length!=0) {
			if (args[0].equals("weibo")) {
				try {
					WeiboJob.main(args);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else if (args[0].equals("hexun")) {
				try {
					HexunJob.main(args);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else if(args[0].equals("court")) {
				CourtJob.main(args);
			}else if (args[0].equals("stock")) {
				StockJob.main(args);
			}
			else {
				System.out.println("wrong command");
			}
		}else {
			System.out.println("input command");
		}
	}
	
	public void run(){
		
	}
}
