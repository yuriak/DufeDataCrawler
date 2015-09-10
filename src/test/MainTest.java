package test;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.common.bean.ClusterBean;
import org.common.config.PublicValues;
import org.haikism.textcluster.TFIDFMeasure;
import org.haikism.textcluster.ToCluster;
import org.haikism.textcluster.Tokeniser;
import org.yuriak.common.util.StringUtil;
import org.yuriak.weibo.bean.WeiboSolrBean;
import org.yuriak.weibo.parser.WeiboFileParser;
import org.yuriak.weibo.util.WeiboStringUtil;
public class MainTest {
	public static void main(String[] args) {
		try {
			ArrayList<String> strings=new ArrayList<>();
			ArrayList<WeiboSolrBean> solrBeans=WeiboFileParser.parse4Solr(new File("data/weibo2015_08_11_23_37_11.txt"));
			for (int i = 0; i < 100; i++) {
				String tmpString=WeiboStringUtil.delEmoj(StringUtil.StringFilter(solrBeans.get(i).getContent())).trim().replaceAll("\n", "");
				strings.add(tmpString);
				System.out.println(tmpString);
			}
			
//			for (WeiboSolrBean weiboSolrBean : solrBeans) {
//				String tmpString=WeiboStringUtil.delEmoj(StringUtil.StringFilter(weiboSolrBean.getContent())).trim().replaceAll("\n", "");
//				if (!tmpString.matches("\\s")) {
//					strings.add(tmpString);
//				}
//				System.out.println(tmpString);
//			}
			String[] stringArray=new String[strings.size()];
			for (int i = 0; i < stringArray.length; i++) {
				stringArray[i]=strings.get(i);
			}
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
	        for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < data[i].length; j++) {
					FileUtils.write(new File("data/km.txt"), data[i][j]+" ", true);
				}
				FileUtils.write(new File("data/km.txt"), "\n", true);
			}
			ToCluster cluster=new ToCluster(stringArray, PublicValues.clusterNumber, PublicValues.keywordNumberPerCluster, PublicValues.sentencePerCluster);
			ArrayList<ClusterBean> clusterList=cluster.startCluster();
//			for (ClusterBean clusterBean : clusterList) {
//				for (String key : clusterBean.getKeywords()) {
//					System.out.print(key+"\t");
//				}
//				System.out.println();
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
