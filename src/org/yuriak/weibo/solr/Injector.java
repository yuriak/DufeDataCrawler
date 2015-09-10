package org.yuriak.weibo.solr;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.DateUtil;
import org.yuriak.weibo.bean.WeiboSolrBean;
import org.yuriak.weibo.parser.WeiboFileParser;

public class Injector {
	public static void inject(String url,Collection<WeiboSolrBean> weiboSolrBeans) throws Exception{
		SolrServer server=new HttpSolrServer("http://192.168.1.113:8080/solr/collection1");
		SolrInputDocument solrInputDocument=new SolrInputDocument();
    	server.addBeans(weiboSolrBeans);
    	
//    	server.deleteByQuery("*:*");
    	server.commit();
    	System.out.println("done");
	}
	
	public static void main(String[] args) throws Exception {
		Collection<WeiboSolrBean> weiboSolrBeans=WeiboFileParser.parse4Solr(new File("data/weibo2015_08_11_23_37_11.txt"));
		Injector.inject("", weiboSolrBeans);
	}
}
