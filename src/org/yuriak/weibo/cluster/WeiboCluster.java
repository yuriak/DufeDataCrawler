package org.yuriak.weibo.cluster;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.common.bean.ClusterBean;
import org.common.config.PublicValues;
import org.haikism.textcluster.TFIDFMeasure;
import org.haikism.textcluster.ToCluster;
import org.haikism.textcluster.Tokeniser;
import org.yuriak.common.cluster.CommonCluster;
import org.yuriak.common.util.StringUtil;
import org.yuriak.common.water.WaterKMeansUtil;
import org.yuriak.weibo.bean.WeiboSolrBean;
import org.yuriak.weibo.parser.WeiboFileParser;
import org.yuriak.weibo.util.WeiboStringUtil;

public class WeiboCluster {
	
	public void cluster(int k,String dataFile,String serverUrl){
		try {
			ArrayList<String> strings=new ArrayList<>();
			ArrayList<WeiboSolrBean> solrBeans=WeiboFileParser.parse4Solr(new File(dataFile));
			for (WeiboSolrBean weiboSolrBean : solrBeans) {
				strings.add(weiboSolrBean.getContent());
			}
			String[] stringArray=new String[strings.size()];
			for (int i = 0; i < strings.size(); i++) {
				stringArray[i]=strings.get(i);
			}
			CommonCluster cluster=new CommonCluster();
			cluster.cluster(k, stringArray,serverUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {
		WeiboCluster weiboCluster=new WeiboCluster();
		weiboCluster.cluster(10,"data/weibo2015_08_11_21_06_08.txt","http://127.0.0.1:54321");
	}
}
