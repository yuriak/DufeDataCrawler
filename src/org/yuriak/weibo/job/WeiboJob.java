package org.yuriak.weibo.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.generic.NEW;
import org.apache.commons.collections.map.StaticBucketMap;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.json.JSONArray;
import org.yuriak.weibo.bean.WeiboBean;
import org.yuriak.weibo.bean.WeiboCommentBean;
import org.yuriak.weibo.bean.WeiboEntityBean;
import org.yuriak.weibo.config.CommonValues;
import org.yuriak.weibo.crawler.WeiboCommentCrawler;
import org.yuriak.weibo.crawler.WeiboEntityCrawler;
import org.yuriak.weibo.util.WeiboAttentionUtil;
import org.yuriak.weibo.util.WeiboStringUtil;
import org.yuriak.weibo.util.WeiboTimeUtil;

import cn.edu.hfut.dmic.webcollector.weiboapi.WeiboCN;

public class WeiboJob {
	public List<WeiboBean> weiboBeans;
	public List<String> uids;
	private int weiboEntityPageNumber=5;
	private int weiboCommentPageNumber=5;
	private int addFriendAttentionNumber=10;
	private static String username="yurix@sohu.com";//用户名和密码只在重新登录时候用
	private static String password="yurix301604";
	private String cookie;
	public WeiboJob(int wep,int wcp,int afn,String username,String password){
		weiboEntityPageNumber=wep;
		weiboCommentPageNumber=wcp;
		addFriendAttentionNumber=afn;
		weiboBeans=new ArrayList<>();
		uids=new ArrayList<>();
		try {
			this.cookie=WeiboCN.getSinaCookie(username, password);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void run() throws Exception{
		//实例化爬虫的时候要设置crawl的db的临时文件夹
		WeiboEntityCrawler weiboEntityCrawler=new WeiboEntityCrawler("crawldb");
		//先获得微博
		List<WeiboEntityBean> weiboEntityBeans=weiboEntityCrawler.getWeiboEntity(weiboEntityPageNumber,cookie);
		for (WeiboEntityBean weiboEntityBean : weiboEntityBeans) {
			//再获取评论
			WeiboCommentCrawler weiboCommentCrawler=new WeiboCommentCrawler("crawldb");
			List<WeiboCommentBean> weiboCommentBeans=new ArrayList<>();
			int commentNumber=weiboEntityBean.getCommentNumber();
			if (commentNumber>0) {
				if (commentNumber/10+1>weiboCommentPageNumber) {
					weiboCommentBeans=weiboCommentCrawler.getComment(weiboEntityBean.getId(), weiboCommentPageNumber,cookie);
				}else {
					weiboCommentBeans=weiboCommentCrawler.getComment(weiboEntityBean.getId(), commentNumber/10+1,cookie);
				}
				for (WeiboCommentBean weiboCommentBean : weiboCommentBeans) {
					uids.add(weiboCommentBean.getUid());
				}
			}
			WeiboBean weiboBean=new WeiboBean();
			weiboBean.setId(weiboEntityBean.getId());
			weiboBean.setContent(WeiboBean.makeSimpleJson(weiboEntityBean, weiboCommentBeans));
			weiboBeans.add(weiboBean);
		}
		//把所有数据整合装进一个Json然后写文件
		JSONArray array=new JSONArray();
		for (int i = 0; i < weiboBeans.size(); i++) {
			array.put(weiboBeans.get(i).getContent());
		}
		String time=new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Timestamp(System.currentTimeMillis()));
		FileUtils.writeStringToFile(new File(System.getProperty("user.dir")+File.separator+"data/weibo"+time+".txt"), array.toString(), "UTF-8",false);
		//从提取n个用户加关注
		WeiboAttentionUtil.addAllAttention(uids.subList(0, addFriendAttentionNumber),cookie);
	}
	
	public static void main(String[] args) throws Exception {
		//可以在命令行下运行，第一个参数是微博的页数，第二个是评论的页数，都是每页10条信息。如果不设置就用10，这里没做过多的Validate，可以以后扩展
		int wep=10;
		int wcp=10;
		int afn=10;
		String username="yurix@sohu.com";
		String password="yurix301604";
		if (args.length==6) {
			if (!args[0].equals("weibo")) {
				return;
			}
			username=String.valueOf(args[1]);
			password=String.valueOf(args[2]);
			wep=Integer.valueOf(args[3]);
			wcp=Integer.valueOf(args[4]);
			afn=Integer.valueOf(args[5]);
		}
		WeiboJob job=new WeiboJob(wep,wcp,afn,username,password);
		job.run();
    	
//    	SolrQuery query=new SolrQuery("*:*");
//    	query.setStart(0);
//    	query.setRows(100);
//    	QueryResponse response=server.query(query);
//    	SolrDocumentList documentList=response.getResults();
//    	Iterator<SolrDocument> iterator=documentList.iterator();
//    	while (iterator.hasNext()) {
//			System.out.println(iterator.next());
//		}
    	
    	
	}
	
}
