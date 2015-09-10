package SubObjAnalysis;

import java.io.File;

import org.haikism.common.config.FileOperation;
import org.nlpcn.commons.lang.standardization.SentencesUtil;

/**
 * 用于确定两个阀值的类
 * @author Haikism
 *
 */
public class SetThresholdValue {
	
	//用于确定2-pos组合的阀值
	public double thresholdValue1;
	//用于确定某句子是否为主观句的阀值
	public double thresholdValue2;
	//用来测试的文本路径，默认是训练的数据，其格式必须和训练数据一样
	String testDataPath="res/trainData_SubObjAnalysis.txt";

	/**
	 * 无参构造函数
	 */
	public SetThresholdValue()
	{
		
	}
	
	/**
	 * @param thresholdValue1 用于确定2-pos组合的阀值
	 * @param thresholdValue2 用于确定某句子是否为主观句的阀值
	 * @testDataPath 用来测试的文本路径，默认是训练的数据，其格式必须和训练数据一样
	 */
	public SetThresholdValue(double thresholdValue1,double thresholdValue2,String testDataPath)
	{
		this.thresholdValue1=thresholdValue1;
		this.thresholdValue2=thresholdValue2;
		this.testDataPath=testDataPath;
	}
	
	
	/**
	 * @param thresholdValue1 用于确定2-pos组合的阀值
	 * @param thresholdValue2 用于确定某句子是否为主观句的阀值
	 */
	public void setThresholdValue(double thresholdValue1,double thresholdValue2)
	{
		this.thresholdValue1=thresholdValue1;
		this.thresholdValue2=thresholdValue2;
	}
	
	
	public double getAccuracy()
	{
		String allContent="";
		//先根据thresholdValue1训练
		SubObj_train subObj_train = new SubObj_train(thresholdValue1);
		subObj_train.train();
		//再根据thresholdValue2预测,并计算准确率(查准率)
		SubObj_predict subObj_predict = new SubObj_predict(thresholdValue2);
		allContent=FileOperation.readTxtFile(new File(testDataPath));
		String[] sentences=allContent.split("\r\n");
		String []eachSentence;
		boolean isSubject;
		double predictSubNum=0;//预测出的为主观句的数量
		double trueSubNum=0;   //其中实际为主观句的数量
		for (int i = 0; i < sentences.length; i++) 
		{
			System.out.println(sentences[i]);
			eachSentence=sentences[i].split("/");
			//判断该句是否为主观句
			if (eachSentence[0].equals("1")) {
				isSubject=true;
			}
			else{
				isSubject=false;
			}
			if (subObj_predict.predict(eachSentence[1])) {
				predictSubNum++;
				if (isSubject) {
					trueSubNum++;
				}
			}
		}
		System.out.println("trueSubNum:"+trueSubNum+"  predictSubNum:"+predictSubNum);
		return trueSubNum/predictSubNum;
		
	}
	
}
