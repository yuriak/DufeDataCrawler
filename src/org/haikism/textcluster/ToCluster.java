package org.haikism.textcluster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.haikism.common.config.FileOperation;
import org.haikism.common.config.KeyWordComputer;
import org.haikism.common.config.Keyword;
import org.common.bean.ClusterBean;
import org.yuriak.common.util.MyLog;

public  class ToCluster
{
	//分成几类
	public int clusterNum;
	//用于分类的文档的路径
	public String filePath;
	//用于保存结果文件的路径
	public String outputFileName;
	//装有所有句子的数组(经过处理的)
	public String[] sentences;
	//禁止使用的关键词
	public String[] stopKeyWords={"快讯","中国","今年","男子","人民日报"};
	//装有所有句子的数组(原始的)
	public String[] sentences_raw;
	//从一个句子中提取的关键词的数量
	public int keyWordNum=6;
	//从某一类的最少句子数，不能低于2
	public int leastSentencesNumEachCluster=3;
	//用于输出的集群列表
	private ArrayList<ClusterBean> clusterBeans;
	
	
	//output
	
	public int eventNumber;
	/**
	 * 构造函数
	 * @param clusterNum 将新闻分成几类
	 * @param outputFileName 用于分类的文档的路径
	 * @param keyWordNum 从一个句子中提取的关键词的数量
	 */
	public ToCluster(int clusterNum,String filePath,int keyWordNum,String outputFileName,int leastSentencesNumEachCluster)
	{
		this.leastSentencesNumEachCluster=leastSentencesNumEachCluster;
		this.outputFileName=outputFileName;
		this.keyWordNum=keyWordNum;
		this.clusterNum=clusterNum;
		this.filePath=filePath;
	}
	
	public ToCluster(String[] sentences,int clusterNum,int keyWordNum,int leastSentencesNumEachCluster){
		this.sentences=sentences;
		this.leastSentencesNumEachCluster=leastSentencesNumEachCluster;
		this.keyWordNum=keyWordNum;
		this.clusterNum=clusterNum;
		this.eventNumber=0;
		clusterBeans=new ArrayList<>();
	}
	
	/**
	 * 对文档内容进行一些预处理（微博数据）
	 */
//	public void dataPreProcess_weibo()
//	{
//		//读取文档
//        sentences = FileOperation.readTxtFile(new File(filePath)).split("\r\n");
//        sentences_raw=FileOperation.readTxtFile(new File(filePath)).split("\r\n");
//        String []publisherName={"凤凰周刊","路透中文网Reuters","环球市场播报","网络新闻联播","21世纪经济报道",
//        		"新浪财经","新华视点","环球时报","新国际","每日经济新闻","军报记者","环球资讯广播",
//        		"新浪新闻","搜狐视频","中国新闻周刊","中国日报","新商报","央视新闻","凤凰网","凤凰视频",
//        		"新快报","央视影音","微天下","新京报","中国独家报道"};
//        for (int i = 0; i < sentences.length; i++) 
//        {
//        	//过滤网址
//        	sentences[i] = sentences[i].replaceAll("<a[^>]+>([^<]+)</a>", "");
//        	sentences_raw[i] = sentences_raw[i].replaceAll("<a[^>]+>([^<]+)</a>", "");
//    		//过滤发新闻的微博
//        	for (int j = 0; j < publisherName.length; j++) {
//				sentences[i]=sentences[i].replaceAll(publisherName[j], "");
//			}
//        	System.out.println(sentences[i]);
//        }
//		
//	}
	
	/**
	 * 对于某一类中的所有句子(共n条)，如果他们中有m个能够提取出来相同的关键词，
	 * 那么则将能提取出公共关键词的m个句子视为合理的分类结果，
	 * 而m根据这个函数来确定，在这里，(n-m)逐渐增加，用二次函数来表示，
	 * n-m最大值为41(当句子的数量为100时)，以后就不再增加
	 * @param sentencesNUm 某一类中的句子数量
	 * @return
	 */
	public int func(int sentencesNum)
	{
		double rate = -0.00412371134*(double)sentencesNum+1.01237113;
		if (sentencesNum<=101) {
			return (int)(rate*(double)sentencesNum);
		}
		else {
			return sentencesNum-40;
		}
	}
	
	public ArrayList<ClusterBean> startCluster() throws Exception{
		try {
			TFIDFMeasure tf = new TFIDFMeasure(sentences, new Tokeniser());
	        //生成k-means的输入数据，是一个联合数组，第一维表示文档个数，
	        //第二维表示所有文档分出来的所有词
	        double[][] data = new double[sentences.length][];
	        int docCount = sentences.length; //文档个数
	        int dimension = tf.get_numTerms();//所有词的数目
	        for (int i = 0; i < docCount; i++)
	        {
	            for (int j = 0; j < dimension; j++)
	            {
	                data[i] = tf.GetTermVector2(i); //获取第i个文档的TFIDF权重向量
	            }
	        }
	        //初始化k-means算法，第一个参数表示输入数据，第二个参数表示要聚成几个类
	        WawaKMeans kmeans = new WawaKMeans(data, clusterNum);
	        //开始迭代
	        kmeans.Start();
	        //获取聚类结果并输出
	        WawaCluster[] clusters = kmeans.getClusters();
	        
	        for(WawaCluster cluster : clusters){
	        	//members装有每一类别中的（在文档中的）句子序号
	            List<Integer> members = cluster.CurrentMembership;
	            //剔除句子太少的分类
	            if (members.size()<leastSentencesNumEachCluster) {
					continue;
				}
	            
	            //用来存放该类的句子
	    	    List<String> sentences_thisCluster = new ArrayList<String>();
	    	    //用来存放每一个句子的关键词，序号是与上面对应的
	    	    List<List<Keyword>> keyWordOfSentences = new ArrayList<List<Keyword>>();
	    	    //装有有共同关键词的句子的序号
	    	    List<Integer> selectedSentencesSeq= new ArrayList<Integer>();
	    	    
	    	    for (int i : members)
	            {
	            	sentences_thisCluster.add(sentences[i]);
	            	//提取关键词
	            	KeyWordComputer kwc = new KeyWordComputer(keyWordNum);
	            	List<Keyword> result = (List<Keyword>) kwc.computeArticleTfidf(sentences[i]);
	            	//移除禁用的关键词
	            	for (int j = 0; j < result.size(); j++) {
						for (int h = 0; h < stopKeyWords.length; h++) {
							if (result.get(j).toString().split("/")[0].equals(stopKeyWords[h])) {
								result.remove(j);
								break;
							}
						}
					}
	            	keyWordOfSentences.add(result);
	            }
	    	    
	    	    int thresholdSentencesNum=func(sentences_thisCluster.size());
	            boolean isFind=false;
	            int keySentenceNum=-1;
	            for (int i = 0; i < sentences_thisCluster.size(); i++) {
					for (int j = 0; j < keyWordOfSentences.get(i).size(); j++) {
						int commonKeyWordsSentencesNum=1;
						selectedSentencesSeq.clear();
						selectedSentencesSeq.add(i);
						for (int h = 0; h < sentences_thisCluster.size(); h++) {
							if (i!=h) {
								for (int k = 0; k < keyWordOfSentences.get(h).size(); k++) {
									if (keyWordOfSentences.get(i).get(j).equals(keyWordOfSentences.get(h).get(k))) {
										commonKeyWordsSentencesNum++;
										selectedSentencesSeq.add(h);
										break;
									}
								}
							}
						}
						//只要有一个关键词满足条件即可
						if (commonKeyWordsSentencesNum>=thresholdSentencesNum) {
							isFind=true;
							keySentenceNum=i;
							break;
						}
					}
					if (isFind) {
						break;
					}
				}
	            if (keySentenceNum!=-1) {
	            	ClusterBean cBean=new ClusterBean();
	            	ArrayList<String> keywords=new ArrayList<>();
	            	ArrayList<String> sentenceList=new ArrayList<>();
	            	for (int i = 0; i < keyWordOfSentences.get(keySentenceNum).size(); i++) {
	            		int commonKeyWordsSentencesNum=1;
						for (int j = 0; j < sentences_thisCluster.size(); j++) {
							if (j!=keySentenceNum) {
								for (int h = 0; h < keyWordOfSentences.get(j).size(); h++) {
									if (keyWordOfSentences.get(keySentenceNum).get(i).equals(keyWordOfSentences.get(j).get(h))) {
										commonKeyWordsSentencesNum++;
										break;
									}
								}
							}
						}
						//写入关键词
						if (commonKeyWordsSentencesNum>=thresholdSentencesNum) {
							keywords.add(keyWordOfSentences.get(keySentenceNum).get(i).toString().split("/")[0]);
						}
					}
	            	for (int i = 0; i < selectedSentencesSeq.size(); i++) {
	            		sentenceList.add(sentences[members.get(selectedSentencesSeq.get(i))]);
	            	}
	            	cBean.setKeywords(keywords);
	            	cBean.setSentences(sentenceList);
	            	clusterBeans.add(cBean);
	            	System.out.println("Get New Event,Event Number: "+clusterBeans.size());
				}
	        }
	        return clusterBeans;
		} catch (Exception e) {
			MyLog.INFO(e.getMessage());
		}
		return null;
		 
	}
	
//	public void startCluster()
//	{
//        //初始化TFIDF测量器，用来生产每个文档的TFIDF权重
//        TFIDFMeasure tf = new TFIDFMeasure(sentences, new Tokeniser());
//        //生成k-means的输入数据，是一个联合数组，第一维表示文档个数，
//        //第二维表示所有文档分出来的所有词
//        double[][] data = new double[sentences.length][];
//        int docCount = sentences.length; //文档个数
//        int dimension = tf.get_numTerms();//所有词的数目
//        for (int i = 0; i < docCount; i++)
//        {
//            for (int j = 0; j < dimension; j++)
//            {
//                data[i] = tf.GetTermVector2(i); //获取第i个文档的TFIDF权重向量
//            }
//        }
//        //初始化k-means算法，第一个参数表示输入数据，第二个参数表示要聚成几个类
//        WawaKMeans kmeans = new WawaKMeans(data, clusterNum);
//        //开始迭代
//        kmeans.Start();
//        //获取聚类结果并输出
//        WawaCluster[] clusters = kmeans.getClusters();
//        
//        //想txt文件中输出的结果
//        String output="";
//        //遍历分出的每一个类别
//        for(WawaCluster cluster : clusters){
//        	//members装有每一类别中的（在文档中的）句子序号
//            List<Integer> members = cluster.CurrentMembership;
//            //剔除句子太少的分类
//            if (members.size()<leastSentencesNumEachCluster) {
//				continue;
//			}
//            
//            //用来存放该类的句子
//    	    List<String> sentences_thisCluster = new ArrayList<String>();
//    	    //用来存放每一个句子的关键词，序号是与上面对应的
//    	    List<List<Keyword>> keyWordOfSentences = new ArrayList<List<Keyword>>();
//    	    //装有有共同关键词的句子的序号
//    	    List<Integer> selectedSentencesSeq= new ArrayList<Integer>();
//            for (int i : members)
//            {
//            	sentences_thisCluster.add(sentences[i]);
//            	//提取关键词
//            	KeyWordComputer kwc = new KeyWordComputer(keyWordNum);
//            	List<Keyword> result = kwc.computeArticleTfidf(sentences[i]);
//            	//移除禁用的关键词
//            	for (int j = 0; j < result.size(); j++) {
//					for (int h = 0; h < stopKeyWords.length; h++) {
//						if (result.get(j).toString().split("/")[0].equals(stopKeyWords[h])) {
//							result.remove(j);
//							break;
//						}
//					}
//				}
//            	keyWordOfSentences.add(result);
//            }
//            
//            //提取共有关键词的句子的阀值
//            int thresholdSentencesNum=func(sentences_thisCluster.size());
//            boolean isFind=false;
//            int keySentenceNum=-1;
//            for (int i = 0; i < sentences_thisCluster.size(); i++) {
//				for (int j = 0; j < keyWordOfSentences.get(i).size(); j++) {
//					int commonKeyWordsSentencesNum=1;
//					selectedSentencesSeq.clear();
//					selectedSentencesSeq.add(i);
//					for (int h = 0; h < sentences_thisCluster.size(); h++) {
//						if (i!=h) {
//							for (int k = 0; k < keyWordOfSentences.get(h).size(); k++) {
//								if (keyWordOfSentences.get(i).get(j).equals(keyWordOfSentences.get(h).get(k))) {
//									commonKeyWordsSentencesNum++;
//									selectedSentencesSeq.add(h);
//									break;
//								}
//							}
//						}
//					}
//					//只要有一个关键词满足条件即可
//					if (commonKeyWordsSentencesNum>=thresholdSentencesNum) {
//						isFind=true;
//						keySentenceNum=i;
//						break;
//					}
//				}
//				if (isFind) {
//					break;
//				}
//			}
//            
//            //keySentenceNum为-1的时候代表该组不符合要求
//            if (keySentenceNum!=-1) {
//            	for (int i = 0; i < keyWordOfSentences.get(keySentenceNum).size(); i++) {
//            		int commonKeyWordsSentencesNum=1;
//					for (int j = 0; j < sentences_thisCluster.size(); j++) {
//						if (j!=keySentenceNum) {
//							for (int h = 0; h < keyWordOfSentences.get(j).size(); h++) {
//								if (keyWordOfSentences.get(keySentenceNum).get(i).equals(keyWordOfSentences.get(j).get(h))) {
//									commonKeyWordsSentencesNum++;
//									break;
//								}
//							}
//						}
//					}
//					//写入关键词
//					if (commonKeyWordsSentencesNum>=thresholdSentencesNum) {
//						output+=keyWordOfSentences.get(keySentenceNum).get(i).toString().split("/")[0]+"/";
//						System.out.println(output);
//					}
//				}
//            	//去掉最后一个"/"
//            	output=output.substring(0,output.length()-1);
//            	//换行
//            	output+="\r\n";
//            	for (int i = 0; i < selectedSentencesSeq.size(); i++) {
////					output+=sentences_thisCluster.get(selectedSentencesSeq.get(i))+"\r\n";
//            		output+=sentences_raw[members.get(selectedSentencesSeq.get(i))]+"\r\n";
//            	}
//			}
//        }
//        FileOperation.writeTxtFile(output, new File(outputFileName), false);
//	}
}
