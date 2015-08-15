package org.yuriak.weibo.util;

import java.io.UnsupportedEncodingException;

public class WeiboStringUtil {
	public static String UnicodeDecoder(String source){
		try {
			byte[] sourceByte=source.getBytes("UTF-8");
			return new String(sourceByte,"UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
