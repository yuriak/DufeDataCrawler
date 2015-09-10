package org.yuriak.solr;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;

import org.apache.bcel.generic.NEW;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.DateUtil;

public class SolrTest {
	public static void main(String[] args) throws Exception {
		SolrServer server=new HttpSolrServer("http://192.168.1.113:8080/solr/collection1");
		SolrInputDocument solrInputDocument=new SolrInputDocument();
    	solrInputDocument.addField("id", "1");
    	solrInputDocument.addField("type", "weibo");
    	solrInputDocument.addField("content", "test");
    	HashSet<String> format=new HashSet<>();
    	format.add("yy-MM-dd HH:mm:ss");
    	solrInputDocument.addField("date", DateUtil.parseDate("2015-09-02 19:10:00",format));
//    	server.deleteByQuery("*:*");
//    	solrInputDocument.addField("_version_", "1");
    	server.add(solrInputDocument);
		
    	server.commit();
    	System.out.println("done");
	}
}
