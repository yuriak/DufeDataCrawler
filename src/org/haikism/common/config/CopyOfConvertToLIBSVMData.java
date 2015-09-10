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
public class CopyOfConvertToLIBSVMData {

	private String[] forbiddenCharacter = {"t","s","f","r","m","q","p","c","u","e","y","o","k","x","w"};
	private String[] data;
	private String fileName; // 要写入的数据文件名
	private File LIBSVM_tmp_fle;
	private List<RelationPairBean> relationPairBeans;
	private String wordName;
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
	public CopyOfConvertToLIBSVMData(String[] data, String fileName) {
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
			
			for (int i = 1; i < 28002; i++) {
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
		List<FeatureWordsBean> featureWordsBeans = new ArrayList<FeatureWordsBean>();
		//从excel中读取的情感词表
		List<SentiWordsBean> sentiWordsBeans=readFromSentiWordsExcel();	
		String data="";
		//判断输入的字符串是用于训练的还是用于预测的，用于训练的格式为：-1(1)/评论内容
		if (rawData.split("/").length>1) {
			data=rawData.split("/")[1]; //用于训练
		}
		else {
			data=rawData; //用于预测
		}
		
		//添加网络词和表情符的情感倾向，然后在句子中去掉，以免网络词语和表情符影响分词和依存分析效果
		for (int i = 27466; i <=28000; i++) {
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
					for (int j = 0; j < sentiWordsBeans.size(); j++) {
						wordName = terms.get(i).toString().split("/")[0];
						//在情感词库里找到了词
						if (wordName.equals(sentiWordsBeans.get(j).getWordsName())) {
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
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		Collections.sort(featureWordsBeans);// 根据在情感词库中的序号进行小到大排序
		
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
}
