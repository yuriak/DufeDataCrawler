package org.haikism.common.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.omg.CORBA.DoubleSeqHolder;
import org.common.bean.RelationPairBean;
import org.common.config.PublicValues;
import org.haikism.common.bean.FeatureWordsBean;
import org.haikism.common.bean.SentiWordsBean;

/**
 * 用于将数据库中的数据转换为符合LIBSVM格式要求的数据的功能类
 * @author Haikism
 */
public class ConvertToLIBSVMData {

	private String[] forbiddenCharacter = {"t","s","f","r","m","q","p","c","u","e","y","o","k","x","w"};
	private String[] data;
	private String fileName; // 要写入的数据文件名
	private File LIBSVM_tmp_fle;
	private List<RelationPairBean> relationPairBeans;
	private List<FeatureWordsBean> featureWordsBeans;
	private List<SentiWordsBean> sentiWordsBeans;
	private String wordName;
	private boolean stop;
	private int stoppedThread;
	private int tmpCount;
	private static final String[] privativeWords={"并非","不","不对","不再","不曾","不至于","毫不","毫无","绝非","决非","没","没有","未","未必","未尝","未曾","永不","不大","不很","从不","尚未","不太"};//否定词
	
    //程度副词
	private static final String[][] degreeAdvWords={{"最","最为","极","及其","极其","极度","极为","极端"},
		{"太","至","至为","顶","过","过于","过分","分外","万分","何等"},
		{"很","挺","怪","老","非常","特别","相当","十分","甚","甚为","异常","深为","满","蛮","够","多","多么","殊","何其","尤其","无比","尤为"},
		{"不甚","不胜","好","好不","颇","颇为","大","大为"},
		{"稍稍","稍微","稍许","略微","略为","多少"},
		{"较","比较","较为","还"},
		{"有点","有些"}
	};
	
	//语法距离修正系数
	private static final double ALPHA=1;
	
	private double[] degreeAdvWordsWeight={1.5,1.4,1.3,1.2,1.1,0.9,0.8};   //程度副词权重

	/**
	 * 
	 * @param data 用于预测的字符串数组
	 * @param fileName 输出的LIBSVM要求的文件的文件名
	 */
	public ConvertToLIBSVMData(String[] data, String fileName) {
//		for (int i = 0; i < data.length; i++) {
//			System.out.println(data[i]);
//		}
		this.data = data;
		this.fileName = fileName;
		LIBSVM_tmp_fle=new File(fileName);
		//先删除原来的缓存文件，重新写入
		if (LIBSVM_tmp_fle.exists()) {
			LIBSVM_tmp_fle.delete();
		}
	}

	public List<SentiWordsBean> readFromSentiWordsExcel()
	{
		//装有情感倾向词和对应情感倾向值的的bean列表
		List<SentiWordsBean> sentiWordsBeans = new ArrayList<SentiWordsBean>();	
		InputStream is;
		Cell wordsNameCell;
		Cell sentiValueCell;
		String sentiValueCellConcent;
		Cell polarityCell;
		String polarityCellContent;
		int sentimentValue;
		try 
		{
			is = new FileInputStream(PublicValues.EMOVOC_XLS_PATH);
			jxl.Workbook rwb = Workbook.getWorkbook(is);
			Sheet rs = (Sheet) rwb.getSheet(0); // 获得第一张表
			
			for (int i = 1; i < 40267; i++) {
				SentiWordsBean theSentiWordsBean = new SentiWordsBean(); 
				//设置情感词名称
				wordsNameCell = ((jxl.Sheet) rs).getCell(0, i);
				theSentiWordsBean.setWordsName(wordsNameCell.getContents()) ;
				sentiValueCell = ((jxl.Sheet) rs)
						.getCell(5, i); // 倾向值
				sentiValueCellConcent = sentiValueCell.getContents();
				polarityCell = ((jxl.Sheet) rs).getCell(6, i); // 根据极性判断倾向值的正负
				polarityCellContent = polarityCell.getContents();

				sentimentValue = Integer.parseInt(sentiValueCellConcent);
				switch (Integer.parseInt(polarityCellContent)) {
				case 0: // 0代表中性
					sentimentValue = 0; // 中性倾向值为0
					break;
				case 1: // 1代表褒义
						// 褒义倾向值为正数
					break;
				case 2: // 2代表贬义
					sentimentValue = -sentimentValue;// 贬义倾向值为负数
					break;
				case 3: // 3代表贬义褒义都有
					sentimentValue = 0;// 和中性倾向一样，设为0
					break;
				default:
					break;
				}
				theSentiWordsBean.setSentiValue(sentimentValue);
				sentiWordsBeans.add(theSentiWordsBean);
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return sentiWordsBeans;
		
	}
	
	/**
	 * 将字符串数组转化为LIBSVM格式的数据
	 * 
	 * @param data
	 */
	public void convertFromArray() {
		for (int i = 0; i < data.length; i++) {
			System.out.println("Analyzing Comment: "+(i+1)+" in "+data.length);
			convertFromString(data[i]);
		}
	}
	
	// 在分词结果中过滤词汇(未标注的词汇也去掉)
	public void convertFromString(String rawData) {
		featureWordsBeans = new ArrayList<FeatureWordsBean>();
		//从excel中读取的情感词表
		sentiWordsBeans=readFromSentiWordsExcel();	
		String data="";
		//判断输入的字符串是用于训练的还是用于预测的，用于训练的格式为：-1(1)/评论内容
		if (rawData.split("/").length>1) {
			data=rawData.split("/")[1]; //用于训练
		}
		else {
			data=rawData; //用于预测
		}
		
		//添加网络词和表情符的情感倾向，然后在句子中去掉，以免网络词语和表情符影响分词和依存分析效果
		for (int i = 39731; i <=40265; i++) {
			if (data.contains(sentiWordsBeans.get(i).wordsName)) {
				FeatureWordsBean newFeatureWordsBean = new FeatureWordsBean();
				newFeatureWordsBean.setWordName(sentiWordsBeans.get(i).getWordsName());
				newFeatureWordsBean.setNumInWordPool(i);
				newFeatureWordsBean.setSentimentValue(sentiWordsBeans.get(i).getSentiValue());
				data.replace(sentiWordsBeans.get(i).wordsName, "");
				featureWordsBeans.add(newFeatureWordsBean);
			}
		}

		//将评论分成句子
		String[] eachSentence=data.split(CommonValue.regParser);
		//遍历每一个句子
		for (int f = 0; f < eachSentence.length; f++) {
			//可能出现分出来的句子长度为0的情况
			if (eachSentence[f].length()==0) {
				continue;
			}
			List<Term> terms = ToAnalysis.parse(eachSentence[f]);// 分词
			
			//过滤掉不需要的词性
			for (int i = 0; i < terms.size(); i++) {
				for (int j = 0; j < forbiddenCharacter.length; j++) {
					if (terms.get(i).toString().split("/").length == 1
							|| terms.get(i).toString().split("/")[1]
									.equals(forbiddenCharacter[j])) {
						terms.remove(i);
						break;
					}
				}
			}

			try {		
				//从LTP上得到输入句子的所有依存关系（ADV,VOB,CMP）
				relationPairBeans=ltpCloudUtil.analysis(eachSentence[f]);
				
				// 每个词语都在情感词库里查找，如果找到了，根据其词语名称，序号，倾向值生成一个featureWordsBean放在featureWordsBeans中
				for (int i = 0; i < terms.size(); i++) {
					wordName = terms.get(i).toString().split("/")[0];
					stop=false;
//					stoppedThread=0;
//					tmpCount=0;
//					ArrayList<Thread> threadList=new ArrayList<>();
//					for (int t = 0; t < 8; t++) {
//						tmpCount=t;
//						Thread thread=new Thread(new Runnable() {
//							@Override
//							public void run() {
//								if (tmpCount!=7) {
//									loop((sentiWordsBeans.size()/8)*tmpCount, (sentiWordsBeans.size()/8)*(tmpCount+1));
//								}else {
//									loop((sentiWordsBeans.size()/8)*tmpCount, sentiWordsBeans.size());
//								}
//								
//							}
//						});
//						threadList.add(thread);
//					}
//					
//					for (Thread thread : threadList) {
//						thread.start();
//					}
//					while (true) {
//						if (stop) {
//							break;
//						}
//						if(stoppedThread==8){
//							break;
//						}
//					}
					
//					System.out.println(0+"-"+(sentiWordsBeans.size()/8)*1);
//					System.out.println((sentiWordsBeans.size()/8)*1+"-"+(sentiWordsBeans.size()/8)*2);
//					System.out.println((sentiWordsBeans.size()/8)*2+"-"+(sentiWordsBeans.size()/8)*3);
//					System.out.println((sentiWordsBeans.size()/8)*3+"-"+(sentiWordsBeans.size()/8)*4);
//					System.out.println((sentiWordsBeans.size()/8)*4+"-"+(sentiWordsBeans.size()/8)*5);
//					System.out.println((sentiWordsBeans.size()/8)*5+"-"+(sentiWordsBeans.size()/8)*6);
//					System.out.println((sentiWordsBeans.size()/8)*6+"-"+(sentiWordsBeans.size()/8)*7);
//					System.out.println((sentiWordsBeans.size()/8)*7+"-"+(sentiWordsBeans.size()));
					
					/*
					 * 8
					 */
					
					
//					Thread t1=new Thread(new Runnable() {
//						
//						@Override
//						public void run() {
//							loop(0, (sentiWordsBeans.size()/8)*1);
//						}
//					});
//					
//					Thread t2=new Thread(new Runnable() {
//						
//						@Override
//						public void run() {
//							// TODO Auto-generated method stub
//							loop((sentiWordsBeans.size()/8)*1, (sentiWordsBeans.size()/8)*2);
//						}
//					});
//					Thread t3=new Thread(new Runnable() {
//						
//						@Override
//						public void run() {
//							// TODO Auto-generated method stub
//							loop((sentiWordsBeans.size()/8)*2, (sentiWordsBeans.size()/8)*3);
//						}
//					});
//					Thread t4=new Thread(new Runnable() {
//						
//						@Override
//						public void run() {
//							// TODO Auto-generated method stub
//							loop((sentiWordsBeans.size()/8)*3, (sentiWordsBeans.size()/8)*4);
//						}
//					});
//					Thread t5=new Thread(new Runnable() {
//						
//						@Override
//						public void run() {
//							// TODO Auto-generated method stub
//							loop((sentiWordsBeans.size()/8)*4, (sentiWordsBeans.size()/8)*5);
//						}
//					});
//					Thread t6=new Thread(new Runnable() {
//						
//						@Override
//						public void run() {
//							// TODO Auto-generated method stub
//							loop((sentiWordsBeans.size()/8)*5, (sentiWordsBeans.size()/8)*6);
//						}
//					});
//					Thread t7=new Thread(new Runnable() {
//						
//						@Override
//						public void run() {
//							// TODO Auto-generated method stub
//							loop((sentiWordsBeans.size()/8)*6, (sentiWordsBeans.size()/8)*7);
//						}
//					});
//					Thread t8=new Thread(new Runnable() {
//						
//						@Override
//						public void run() {
//							// TODO Auto-generated method stub
//							loop((sentiWordsBeans.size()/8)*7, sentiWordsBeans.size());
//						}
//					});
					
					
					/*
					 * 4
					 * 
					 */
//					Thread t1=new Thread(new Runnable() {
//						
//						@Override
//						public void run() {
//							loop(0, (sentiWordsBeans.size()/4)*1);
//						}
//					});
//					
//					Thread t2=new Thread(new Runnable() {
//						
//						@Override
//						public void run() {
//							// TODO Auto-generated method stub
//							loop((sentiWordsBeans.size()/4)*1, (sentiWordsBeans.size()/4)*2);
//						}
//					});
//					Thread t3=new Thread(new Runnable() {
//						
//						@Override
//						public void run() {
//							// TODO Auto-generated method stub
//							loop((sentiWordsBeans.size()/4)*2, (sentiWordsBeans.size()/4)*3);
//						}
//					});
//					Thread t4=new Thread(new Runnable() {
//						
//						@Override
//						public void run() {
//							// TODO Auto-generated method stub
//							loop((sentiWordsBeans.size()/4)*3, (sentiWordsBeans.size()));
//						}
//					});
					
					
					
					/*
					 * 16
					 */
					
					Thread t1=new Thread(new Runnable() {
						
						@Override
						public void run() {
							loop(0, (sentiWordsBeans.size()/16)*1);
						}
					});
					
					Thread t2=new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							loop((sentiWordsBeans.size()/16)*1, (sentiWordsBeans.size()/16)*2);
						}
					});
					Thread t3=new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							loop((sentiWordsBeans.size()/16)*2, (sentiWordsBeans.size()/16)*3);
						}
					});
					Thread t4=new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							loop((sentiWordsBeans.size()/16)*3, (sentiWordsBeans.size()/16)*4);
						}
					});
					Thread t5=new Thread(new Runnable() {
						
						@Override
						public void run() {
							loop((sentiWordsBeans.size()/16)*4, (sentiWordsBeans.size()/16)*5);
						}
					});
					
					Thread t6=new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							loop((sentiWordsBeans.size()/16)*5, (sentiWordsBeans.size()/16)*6);
						}
					});
					Thread t7=new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							loop((sentiWordsBeans.size()/16)*6, (sentiWordsBeans.size()/16)*7);
						}
					});
					Thread t8=new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							loop((sentiWordsBeans.size()/16)*7, (sentiWordsBeans.size()/16)*8);
						}
					});
					Thread t9=new Thread(new Runnable() {
						
						@Override
						public void run() {
							loop((sentiWordsBeans.size()/16)*8, (sentiWordsBeans.size()/16)*9);
						}
					});
					
					Thread t10=new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							loop((sentiWordsBeans.size()/16)*9, (sentiWordsBeans.size()/16)*10);
						}
					});
					Thread t11=new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							loop((sentiWordsBeans.size()/16)*10, (sentiWordsBeans.size()/16)*11);
						}
					});
					Thread t12=new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							loop((sentiWordsBeans.size()/16)*11, (sentiWordsBeans.size()/16)*12);
						}
					});
					Thread t13=new Thread(new Runnable() {
						
						@Override
						public void run() {
							loop((sentiWordsBeans.size()/16)*12, (sentiWordsBeans.size()/16)*13);
						}
					});
					
					Thread t14=new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							loop((sentiWordsBeans.size()/16)*13, (sentiWordsBeans.size()/16)*14);
						}
					});
					Thread t15=new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							loop((sentiWordsBeans.size()/16)*14, (sentiWordsBeans.size()/16)*15);
						}
					});
					Thread t16=new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							loop((sentiWordsBeans.size()/16)*15, (sentiWordsBeans.size()));
						}
					});
					
					
					t1.start();
					t2.start();
					t3.start();
					t4.start();
					t5.start();
					t6.start();
					t7.start();
					t8.start();
					t9.start();
					t10.start();
					t11.start();
					t12.start();
					t13.start();
					t14.start();
					t15.start();
					t16.start();
					while (t1.isAlive()||t2.isAlive()||t3.isAlive()||t4.isAlive()||t5.isAlive()||t6.isAlive()||t7.isAlive()||t8.isAlive()||t9.isAlive()||t10.isAlive()||t11.isAlive()||t12.isAlive()||t13.isAlive()||t14.isAlive()||t15.isAlive()||t16.isAlive()) {
						if (stop) {
							break;
						}
					}
//					while (t1.isAlive()||t2.isAlive()||t3.isAlive()||t4.isAlive()) {
//						if (stop) {
//							break;
//						}
//					}
//					while (true) {
//						if (!t1.isAlive()&&!t2.isAlive()&&!t3.isAlive()&&!t4.isAlive()&&!t5.isAlive()&&!t6.isAlive()&&!t7.isAlive()&&!t8.isAlive()) {
//							break;
//						}
//					}
//					while (true) {
//						if (!t1.isAlive()&&!t2.isAlive()&&!t3.isAlive()&&!t4.isAlive()) {
//							break;
//						}
//					}
					while (true) {
						if (!t1.isAlive()&&!t2.isAlive()&&!t3.isAlive()&&!t4.isAlive()&&!t5.isAlive()&&!t6.isAlive()&&!t7.isAlive()&&!t8.isAlive()&&!t9.isAlive()&&!t10.isAlive()&&!t11.isAlive()&&!t12.isAlive()&&!t13.isAlive()&&!t14.isAlive()&&!t15.isAlive()&&!t16.isAlive()) {
							break;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
//		for (FeatureWordsBean bean : featureWordsBeans) {
//			System.out.println(bean.getNumInWordPool());
//		}
//		Collections.sort(featureWordsBeans);// 根据在情感词库中的序号进行小到大排序
			
		if (rawData.split("/").length>1) {
			//积极情感
			if (rawData.split("/")[0].equals("1")) {
				FileOperation.writeFeatureWordsBeans(featureWordsBeans, LIBSVM_tmp_fle,true);
			}
			else {
				FileOperation.writeFeatureWordsBeans(featureWordsBeans, LIBSVM_tmp_fle,false);
			}
		}
		else {
			//1是label，预测时即便不知道label值也要随便填一个
			FileOperation.writeFeatureWordsBeans(featureWordsBeans, LIBSVM_tmp_fle,true);
		}
	}
	
	
	private void loop(int start,int end){
		for (int j = start; j < end; j++) {
			if (stop) {
//				stoppedThread++;
				return;
			}
			//在情感词库里找到了词
			if (wordName.equals(sentiWordsBeans.get(j).getWordsName())) {
				stop=true;
//				System.out.println(sentiWordsBeans.get(j).getWordsName());
				FeatureWordsBean newFeatureWordsBean = new FeatureWordsBean();
				newFeatureWordsBean.setWordName(wordName);
				newFeatureWordsBean.setNumInWordPool(j);
				newFeatureWordsBean.setSentimentValue(sentiWordsBeans.get(j).getSentiValue());
				
				//将包含情感词的VOB和CMP结构转化为ADV结构
				for (int k = 0; k < relationPairBeans.size(); k++) {
					if (relationPairBeans.get(k).getFirstWordName().equals(wordName)&&(relationPairBeans.get(k).relation.equals("VOB")||relationPairBeans.get(k).relation.equals("CMP"))) {
						int wordId_tmp=relationPairBeans.get(k).getSecondId();
						for (int l = 0; l < relationPairBeans.size(); l++) {
							if (relationPairBeans.get(l).getRelation().equals("ADV")&&relationPairBeans.get(l).secondId==wordId_tmp) {
								
								RelationPairBean newRelationPairBean=new RelationPairBean();
								
								newRelationPairBean.setFirstId(relationPairBeans.get(l).getFirstId());
								newRelationPairBean.setSecondId(relationPairBeans.get(k).getFirstId());
								newRelationPairBean.setFirstWordName(relationPairBeans.get(l).getFirstWordName());
								newRelationPairBean.setSecondWordName(relationPairBeans.get(k).getFirstWordName());
								newRelationPairBean.setFirstPos(relationPairBeans.get(l).getFirstPos());
								newRelationPairBean.setSecondPos(relationPairBeans.get(k).getFirstPos());
								newRelationPairBean.setRelation("ADV");
								relationPairBeans.add(newRelationPairBean);//添加到list的后面
							}
						}
					}
					else if(relationPairBeans.get(k).getSecondWordName().equals(wordName)&&relationPairBeans.get(k).getRelation().equals("ADV")){
						//ADV关系中存在情感词
						
						boolean isStop=false;
						//遍历否定词数组
						for (int k2 = 0; k2 < privativeWords.length; k2++) {
							if (privativeWords[k2].equals(relationPairBeans.get(k).getFirstWordName())) {
								//更新情感值
								newFeatureWordsBean.sentimentValue=newFeatureWordsBean.sentimentValue*(-1)/(ALPHA*Math.abs(relationPairBeans.get(k).getFirstId()-relationPairBeans.get(k).getSecondId()));
								isStop=true;
								break;
							}
						}
						if (!isStop) {
							//遍历程度副词数组
							for (int k2 = 0; k2 < degreeAdvWords.length; k2++) {
								for (int l = 0; l < degreeAdvWords[k2].length; l++) {
									if (degreeAdvWords[k2][l].equals(relationPairBeans.get(k).getFirstWordName())) {
										isStop=true;
										newFeatureWordsBean.setSentimentValue(newFeatureWordsBean.getSentimentValue()*degreeAdvWordsWeight[k2]);
										break;
									}
								}
								if (isStop) {
									break;
								}
							}
						}
					}
				}
				featureWordsBeans.add(newFeatureWordsBean);
			}
		}
//		stoppedThread++;
	}
	public static void main(String[] args) {
		
		String []testSentence={
				//消极
				 "-1/#HKIFF#2015。这是什么玩意？又名《汪星人崛起》之惊变28天音乐改变狗生？毫无逻辑的学生作品剧本，手持镜头晃的想吐，表演0分的女主以及人类，狗狗倒是演技不俗。主人公狗你能一步步找到仇人复仇你敢不敢先找回家啊？唯一的萌点是那只白色的狗虽然最后还是没用。只想无限地喷。"
				,"-1/家里有三只爱犬的我都觉得太做作了...垃圾，无聊"
				,"-1/#HKIFF#电影节目前为止看到最垃圾的电影。导演明显拍狗比拍人更用心，对狗导戏比对人更擅长，居然还找了个面瘫来演女主角，中二的表演真是让人哭笑不得。电影从人狗分离开始就变成《猩球崛起》狗狗版，配乐剪辑拍法越来越好莱坞，可惜那种童话式的文艺稚气始终未脱，越来越狗血，越演越扯蛋。  iPhone"
				,"-1/快进着看了一下，预计我会给一颗星。纯主观讨厌虐狗。"
				,"-1/技术没问题，但观感不适，狗狗不是你们玩弄的道具。"
				,"-1/HKIFF】拍得硬邦邦，父女间的互动尤其莫名，似乎一切摹写就是为了冲着结局那幅画面去的，消极，悲观，无聊"
				,"-1/开篇弄那个牛把我恶心的不要不要，后面简直血腥的怕做噩梦。还有为什么爸爸会亲自给女儿穿鞋？"
				,"-1/陈词滥调，煽情做作，故事毫无新意，手法故弄玄虚。看开头知结尾，陈芝麻烂谷子。人的戏凌乱且不知所云，故事编的一塌糊涂。到处都是逻辑漏洞和随意的转折。"
				,"-1/为什么大家要演这种烂片,无趣，不开心，恶心，单调"
				,"-1/期待了足足有半年，预告里面就是所有的精彩镜头了，感觉把所有的钱都花到那几场不怎么激烈的动作戏里面了，End of Watch里面的真实感也没保持，剧本也写得不好太碎，只有两个镜头有意思其他都不咋样，说阿诺突破自己，并没有，虽然不愿意承认，但是演得太差了……还是get to the chopppaaaa吧……"
				,"-1/哦 不对 这简直没有剧情，没有逻辑，没有故事....... 完全不知道发生了什么，只有暴力吧大概..... 同事还因为应该看电影时喝什么饮料，差点和我打起来 ;)"
				,"-1/虽然有施瓦辛格的加入也不能挽救二流影片的命运，破坏者除了枪战没有一点动作镜头，没有了施瓦辛格招牌式的出演相信没几个人会觉得他是动作明星，二流的商业片不注重剧情和镜头感，就连最激烈的枪战也让人提不起精神，这样的影片在美国应该是属于中下等的水平，想看阿诺的人还是死了心吧。"
				,"-1/情节疏漏、悬念无果、节奏松弛错乱、貌似业余的动作指导，作为已有所成就的导演与编剧合作之下的产物，竟然是这个样子，实在让人大跌眼镜，比last stand差了好多。浪费了Enos那么好的演技。"
				,"-1/这电影是烂，平淡无奇，主线不吸引人。"
				,"-1/我想说我几乎都没耐心看完了好吗···反正就是无聊的打打杀杀。"
				,"-1/其实这部片里面除了斯瓦辛格还是有大牌的，比如那个开雪佛兰的黑人。。。但是影片实在是不敢恭维。。。应该是我看过斯瓦辛格演过影片里面最难看的片吧。。。衔接实在是太差了。。。老大为报仇自己黑了钱不说，引起内乱，背后下黑手居然是女队友。。。整个故事被导演讲得一塌糊涂。。。"
				,"-1/烂片，垃圾，不够精彩"
				,"-1/杨幂,生活中是个不好笑的自黑段子手,工作上是个零演技的演员.本质上是个不好看的花瓶.AB和黄二明在生活中不像情侣,在戏里不像兄妹,归根到底:两人的演技都很屎.大家别再笑四娘作家也能当导演,这部戏的导演是教主经纪人,感觉烂片的下限又被拉低了.教主是投资人,恋情炒得再凶,我就不看就不给你们凑分子钱."
				,"-1/拍的稀巴烂，演的稀巴烂。简言之，这是一部会让所有<讨厌黄晓明 杨幂 ab等演员及杨文军等主创>的人看了开心的电影。看到你们这么不思进取，我就放心了。"
				,"-1/《何以笙箫默》彻底打破了电影自身的桎梏，肆意泼墨的跳跃性剧情，无需逻辑的人设关系，导演编剧毅然认定了所有人都以将本自小说的情景烂熟于心，再辅之足够亮瞎双眸的超强柔光预以突显该片演员那异乎常人的演技，宛同预告式的电影，无疑开创了渣作模式的新纪元。"
				//积极
				,"1/精彩之至。让人记住太多的面孔。"
				,"1/真是好看死了，三个我大爱的爷们彪戏，没有比这更爽的了，华丽时代背景+完美叙事剪辑，从情节到细节，从演技到设计，无可挑剔。"
				,"1/bud太牛逼了！真是条汉子！，好厉害"
				,"1/就爱那个年代的美国，英雄，喜欢"
				,"1/黑脸白脸，goog cop bad cop。精彩到位，错综有致，足够有劲的铁三角警匪悬疑片。"
				,"1/曲折的悬疑剧情，纯属的多线叙事，最最重要的是三个独具魅力的警察形象。"
				,"1/往往隐藏最深的才是最邪恶的…看过最牛逼的警匪片，凯文斯派西卢塞尔克劳盖皮尔斯三大男主角实在没话说，性格各异大飙演技，还未成大牌的时候表演就如此有张力…故事环环相扣，案中案却别有洞天，人物复杂但都对故事有推动作用…小说故事设计巧妙，改编的也成功，不得而知是否纯粹真实事件还是又经加工"
				,"1/乱象丛生的黑色年代，错综复杂的敌我交锋；实在令人赞叹到咋舌。"
				,"1/多线叙事，一个相当复杂的故事，环环相扣，虽没有太多震撼但却足够严谨，通过警官的视点揭示了美国社会复杂的利益关系，同时也对人性对“正义”做出了解读，经典作品。"
				,"1/果然是经典，返回头看现在很多电影都有这部片子的影子。"
				,"1/如此完美的剧情片会永久载入电影史。 对话和情节交叉暗含深意，彼此映照，令人在最后的恍然大悟之际瞬间闪回前剧细节。 当时想当刑警的Ed虽然想都没想就承认自己不会在嫌犯背后开枪以免他的律师最终帮他脱罪，但是最终当现实情景到来的关头，他却想都没有想就坚定的朝嫌犯的背后开了枪。"
				,"1/丝丝入扣的复杂而完美的剧情，几大影帝嘉年华似的表演，完美的诠释了警察内部的黑暗与斗争，本片在同类型的电影题材中，绝对是可以排进前十名的电影"
				,"1/好久没看好莱坞电影了，故事精巧，剧本扎实，运镜流畅，警匪片中的优质片目！PS：凯文·史派西和罗素·克劳我真心不太分得清谁是谁啊。。"
				,"1/以前我最喜欢罪恶之城里老布的Magnum……不过L.A.P.D.之后我更喜欢Russell的M1霰弹枪了！！！"
				,"1/思维缜密逻辑清晰，你妈罗素克劳我刚看出硬汉啊 出去打埃斯利那段简直跟丧尸一样 挺好看的戳穿一个惊天大阴谋！！"
				,"1/好剧本和强大的演员阵容。"
				,"1/这阵容，想不好看都难。kevin spacey被打穿胸口的镜头翻来覆去看了几遍，真你妈好演员。公众形象永远高于人民知道真相的权利，在哪又不是这样呢？"
				,"1/剧情相当之连贯，看起来一气呵成，结局也很有颠覆意味"
				,"1/本片并不复杂，三个警察，一个油滑，一个暴力，一个智慧，刚开始格格不入，却因为一个案件的蹊跷卷在了一起，发现了警察局副局长贩毒以及想要接管黑社会的内幕，最后拳头赢得了从良的妓女，智慧赢得全世界，只因他们都有一个信念，罗罗托马斯，正义！影片的前半部分实在精彩，不拖沓还能交代三人性格"
				,"1/好莱坞版的两杆大烟枪．这些日后的超级实力派大碗们合伙为我们奉献了一场经典的警匪电影！"
		};
		
		ConvertToLIBSVMData convertToLIBSVMData=new ConvertToLIBSVMData(testSentence, "res/LIBSVM_tmp");
		convertToLIBSVMData.convertFromArray();
	}
}
