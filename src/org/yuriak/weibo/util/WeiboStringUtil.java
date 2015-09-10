package org.yuriak.weibo.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

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
	
	public static String delHTMLTag(String htmlStr){ 
        String regEx_script="<script[^>]*?>[\\s\\S]*?<\\/script>"; //定义script的正则表达式 
        String regEx_style="<style[^>]*?>[\\s\\S]*?<\\/style>"; //定义style的正则表达式 
        String regEx_html="<[^>]+>"; //定义HTML标签的正则表达式 
        
        Pattern p_script=Pattern.compile(regEx_script,Pattern.CASE_INSENSITIVE); 
        Matcher m_script=p_script.matcher(htmlStr); 
        htmlStr=m_script.replaceAll(""); //过滤script标签 
        
        Pattern p_style=Pattern.compile(regEx_style,Pattern.CASE_INSENSITIVE); 
        Matcher m_style=p_style.matcher(htmlStr); 
        htmlStr=m_style.replaceAll(""); //过滤style标签 
        
        Pattern p_html=Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE); 
        Matcher m_html=p_html.matcher(htmlStr); 
        htmlStr=m_html.replaceAll(""); //过滤html标签 

       return htmlStr.trim(); //返回文本字符串 
    }
	
	public static String delEmoj(String source){
		Pattern emojRegEx=Pattern.compile("\\[.*\\]");
		Matcher emojMatcher=emojRegEx.matcher(source);
		return source=emojMatcher.replaceAll("").trim();
	}
	
	public static void main(String[] args) throws IOException {
	}
	
}
