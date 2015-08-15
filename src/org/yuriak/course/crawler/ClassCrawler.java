package org.yuriak.course.crawler;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.select.Elements;
import org.jsoup.Jsoup;

public class ClassCrawler {
	/*
	 * 
	 * 选课脚本，没有用，忽略
	 * 
	 */
	public static void main(String[] args) throws Exception {
		Connection connection=Jsoup.connect("http://202.199.165.193/");
		Response response=connection.execute();
		String string=response.cookie("JSESSIONID");
		System.out.println(connection.get().select("#vchart").get(0).attr("src"));
		HttpClient client=(HttpClient) HttpClientBuilder.create();
//		Jsoup.connect("http://202.199.165.193/loginAction.do").data("zjh1=&tips=&lx=&evalue=&eflag=&fs=&dzslh=&zjh=2012210036&mm=LIAUYI&v_yzm=ehq6").cookie("JSESSIONID", "");
//		String string="";
//		Jsoup.connect("http://202.199.165.193/xkAction.do").referrer("http://202.199.165.193/xkAction.do?actionType=2&pageNumber=20").cookie("", arg1)
	}
	
	private static void getImages(String src) throws IOException {

        String folder = null;
        
        //Exctract the name of the image from the src attribute
        int indexname = src.lastIndexOf("/");

        if (indexname == src.length()) {
            src = src.substring(1, indexname);
        }

        indexname = src.lastIndexOf("/");
        String name = src.substring(indexname, src.length());

        System.out.println(name);

        //Open a URL Stream
        URL url = new URL(src);
        InputStream in = url.openStream();
        OutputStream out = new BufferedOutputStream(new FileOutputStream( "data/"+ name));

        for (int b; (b = in.read()) != -1;) {
            out.write(b);
        }
        out.close();
        in.close();

    }
}
