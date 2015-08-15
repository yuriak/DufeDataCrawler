package org.yuriak.weibo.util;

import java.util.Date;


public class WeiboTimeUtil {
	public static String convertLongToDateString(String time){
		long unixLong=Long.parseLong(time)*1000;
		String date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(unixLong));
		return date;
	}
}
