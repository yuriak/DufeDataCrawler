package org.yuriak.common.cluster;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.common.bean.ClusterBean;
import org.common.config.PublicValues;
import org.haikism.textcluster.TFIDFMeasure;
import org.haikism.textcluster.ToCluster;
import org.haikism.textcluster.Tokeniser;
import org.yuriak.common.util.StringUtil;
import org.yuriak.common.water.WaterKMeansUtil;
import org.yuriak.weibo.bean.WeiboSolrBean;
import org.yuriak.weibo.parser.WeiboFileParser;
import org.yuriak.weibo.util.WeiboStringUtil;

public class CommonCluster {
	
	public void cluster(int k,String[] SourceData,String serverUrl){
		try {
			String[] stringArray=SourceData;
			TFIDFMeasure tf = new TFIDFMeasure(stringArray, new Tokeniser());
	        //生成k-means的输入数据，是一个联合数组，第一维表示文档个数，
	        //第二维表示所有文档分出来的所有词
	        double[][] data = new double[stringArray.length][];
	        int docCount = stringArray.length; //文档个数
	        int dimension = tf.get_numTerms();//所有词的数目
	        for (int i = 0; i < docCount; i++)
	        {
	            for (int j = 0; j < dimension; j++)
	            {
	                data[i] = tf.GetTermVector2(i); //获取第i个文档的TFIDF权重向量
	            }
	        }
	        String uniqueID=UUID.randomUUID()+"";
	        String numDataFilename="km-numdata-"+uniqueID+".csv";
	        for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < data[i].length; j++) {
					FileUtils.write(new File("data/"+numDataFilename), data[i][j]+",", true);
				}
				FileUtils.write(new File("data/"+numDataFilename), "\n", true);
			}
			int[] clusterData= runKMeans(k, new File("data/"+numDataFilename), serverUrl);
			String resultDataFilename="km-resultdata-"+uniqueID+".txt";
			HashMap<Integer, List<String>> senMap=classify(k, stringArray, clusterData);
			Iterator<Integer> iterator=senMap.keySet().iterator();
			while (iterator.hasNext()) {
				Integer keyInteger=(Integer)iterator.next();
				System.out.println("=========第"+keyInteger+"类==========");
				FileUtils.write(new File("data/"+resultDataFilename), "=========第"+keyInteger+"类==========\n", true);
				List<String> senStrings=senMap.get(keyInteger);
				for (String string : senStrings) {
					System.out.println(string);
					FileUtils.write(new File("data/"+resultDataFilename), string+"\n", true);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {
		
	}
	
	private HashMap<Integer, List<String>> classify(int k,String[] sentence,int[] clusterData){
		HashMap<Integer, List<String>> sentenceMap=new HashMap<>();
		for (int i = 0; i < k ; i++) {
			List<String> tmpSentence=new ArrayList<>();
			for (int j = 0; j < clusterData.length; j++) {
				if (clusterData[j]==i) {
					tmpSentence.add(sentence[j]);
				}
			}
			sentenceMap.put(i, tmpSentence);
		}
		return sentenceMap;
	}
	
	private int[] runKMeans(int k,File dataFile,String serverUrl) throws IOException, InterruptedException{
		WaterKMeansUtil kMeansUtil=new WaterKMeansUtil(serverUrl);
		kMeansUtil.importFiles(dataFile.getAbsolutePath());
		Thread.currentThread().sleep(1000);
		kMeansUtil.parse(kMeansUtil.sourceFrame, -1, "CSV", 44, false);
		Thread.currentThread().sleep(1000);
		kMeansUtil.buildKMeansModel(kMeansUtil.destinationFrame, 0, "", true, k, 1000, "Furthest", false, true);
		Thread.currentThread().sleep(1000);
		kMeansUtil.predict(kMeansUtil.kmeansID, kMeansUtil.destinationFrame);
		Thread.currentThread().sleep(1000);
		return kMeansUtil.getData(kMeansUtil.predictionID);
	}
}
