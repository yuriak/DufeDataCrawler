package org.yuriak.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class StringUtil {
	public static boolean isNullOrEmpty(String str)
	{
		if (str==null || str.length()==0)
		return true;
		else return false;
	}
	public static String addZeroForNum(String str, int strLength) {
		int strLen = str.length();
		if (strLen < strLength) {
		while (strLen < strLength) {
		StringBuffer sb = new StringBuffer();
		sb.append("0").append(str);//��0
		// sb.append(str).append("0");//�Ҳ�0
		str = sb.toString();
		strLen = str.length();
		}
		}
		return str;
	}
	
	public static String StringFilter(String str) throws PatternSyntaxException {
		// 只允许字母和数字
		// String regEx = "[^a-zA-Z0-9]";
		// 清除掉所有特殊字符
		String regEx = "[`~!@#$%^&*()+=|{}':;',//[//].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		return m.replaceAll("").trim();
	}
}
