package org.yuriak.common.water;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;

public class WaterHttpUtil {

	public static String post(String url,HashMap<String, String> params) throws IOException{
		if (params==null) {
			return Jsoup.connect(url).method(Method.POST).ignoreContentType(true).ignoreHttpErrors(true).execute().parse().text();
		}else {
			return Jsoup.connect(url).data(params).method(Method.POST).ignoreContentType(true).ignoreHttpErrors(true).execute().parse().text();
		}
	}
	
	public static String get(String url) throws IOException{
		return Jsoup.connect(url).method(Method.GET).ignoreContentType(true).ignoreHttpErrors(true).execute().parse().text();
	}
	
	public static void main(String[] args) throws IOException {
//		System.out.println(Jsoup.connect("http://127.0.0.1:54321/3/CreateFrame").ignoreContentType(true).ignoreHttpErrors(true).method(Method.POST).execute().parse().text());
//		System.out.println(Jsoup.connect("http://127.0.0.1:54321/3/ImportFiles?path=E:\\TDDownload\\h2o-3.0.1.7\\h2o-3.0.1.7\\.\\data\\test.csv")
//				.ignoreContentType(true)
//				.get()
//				.text());
//		System.out.println(Jsoup.connect("http://127.0.0.1:54321/3/ParseSetup")
//				.data("source_frames","[\"nfs:\\E:\\TDDownload\\h2o-3.0.1.7\\h2o-3.0.1.7\\.\\data\\test.csv\"]")
//				.method(Method.GET.POST)
//				.ignoreHttpErrors(true)
//				.ignoreContentType(true)
//				.execute()
//				.parse()
//				.text());
//		System.out.println();
		
		HashMap<String, String> params=new HashMap<>();
		params.put("source_frames", "[\"nfs:\\E:\\TDDownload\\h2o-3.0.1.7\\h2o-3.0.1.7\\.\\data\\test.csv\"]");
		System.out.println(post("http://127.0.0.1:54321/3/ParseSetup", params));
	}
}
