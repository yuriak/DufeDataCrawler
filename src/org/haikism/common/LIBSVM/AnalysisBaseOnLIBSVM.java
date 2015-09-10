package org.haikism.common.LIBSVM;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.InputMap;

import SubObjAnalysis.SubObj_predict;

import org.haikism.common.config.CommonValue;
import org.haikism.common.config.ConvertToLIBSVMData;

import libsvm.svm_parameter;




/**
 * 根据LIBSVM进行情感分析
 * @author Haikism
 */
public class AnalysisBaseOnLIBSVM {
	//生成的中间文件，里面存有符合LIBSVM要求的数据
	public String LIBSVM_tmp="res/LIBSVM_tmp";
	//已经训练好的模型文件
	public String LIBSVM_model="res/LIBSVM.model";
	private String []trainArgs={LIBSVM_tmp,LIBSVM_model};
	private String []predictArgs={LIBSVM_tmp,LIBSVM_model,"res/LIBSVM_predict.txt"};
	//-l代表最小值，-u代表最大值，-s后面的两个字符串一个是生成的scale标准，一个是用于训练的文件
	private String []scaleArgs_train={"-l","-1", "-u","1","-s","res/LIBSVM_scale","res/LIBSVM_tmp"};
	//用于将预测数据标准化，-r后的两个字符串分别代表使用的scale标准和用于预测的文件
	private String []scaleArgs_predict={"-r","res/LIBSVM_scale","res/LIBSVM_tmp"};
	//选择是否进行主客观分析，默认是false
	private boolean isSubObjAnalysis=false;

	
	/**
	 * 此构造方法对训练和预测的参数不作设置，使用默认的参数
	 */
	public AnalysisBaseOnLIBSVM()
	{
		
	}
	
	/**
	 * 设置训练和预测的参数的构造方法
	 * @param inputFile 输入的字符串数组
	 * @param trainArgs 形如： {"-s","1","-t","3","-w","5","我是训练用的文件路径","我是训练完以后保存模型的路径"}
	 *                  前面的英文字母是各种参数，无则使用默认的参数(可以直接使用-v 10,表示交叉训练参数10次)
	 *                  训练文件的路径必须有
	 *                  保存模型的路径可有可无
	 * @param predictArgs 形如：{"预测文件路径", "使用的模型文件路径","结果文件路径" }
	 * @param isSubObjAnalysis 是否进行主客观分析，true：进行，false：不进行
	 */
	public AnalysisBaseOnLIBSVM(String[] inputString,String []trainArgs,String []predictArgs,boolean isSubObjAnalysis)
	{
		this.isSubObjAnalysis=isSubObjAnalysis;
		this.trainArgs=trainArgs;
		this.predictArgs=predictArgs;
		if (isSubObjAnalysis) {
			inputString=subObjAnalysis(inputString);
		}
		//将字符串数组转换为符合LIBSVM要求的数据，存放到res/LIBSVM_tmp中
		ConvertToLIBSVMData cd=new ConvertToLIBSVMData(inputString, LIBSVM_tmp);
		cd.convertFromArray();
	}

	/**
	 * 
	 * @param inputFile 输入的字符串数组
	 * @param predictFile 预测文件的路径
	 * @param isSubObjAnalysis 是否进行主客观分析
	 */
	public AnalysisBaseOnLIBSVM(String inputString[],String predictFile,boolean isSubObjAnalysis)
	{
		this.isSubObjAnalysis=isSubObjAnalysis;
		//如果需要主客观分析，则先将输入的字符串进行处理
		if (isSubObjAnalysis) {
			inputString=subObjAnalysis(inputString);
		}
//		for (int i = 0; i < inputString.length; i++) {
//			System.out.println(inputString[i]);
//		}
		//将字符串数组转换为符合LIBSVM要求的数据，存放到res/LIBSVM_tmp中
		ConvertToLIBSVMData cd=new ConvertToLIBSVMData(inputString, LIBSVM_tmp);
		cd.convertFromArray();
		predictArgs=new String [3];
		predictArgs[0]=LIBSVM_tmp;
		predictArgs[1]=LIBSVM_model;
		predictArgs[2]=predictFile;
	}

	//进行主客观分析，生成临时文件
	public String[] subObjAnalysis(String inputString[])
	{
	
		
		boolean isForTrain;
		String []inputSentiment=new String[inputString.length];
		//判断是用于训练的输入还是用于预测的输入
		if (inputString[0].split("/").length>1&&(inputString[0].split("/")[0].equals("1")||inputString[0].split("/")[0].equals("-1"))) {
			//用于训练
			isForTrain=true;
			//保存情感的值
			for (int i = 0; i < inputString.length; i++) {
				inputSentiment[i] = inputString[i].split("/")[0];
				inputString[i]=inputString[i].split("/")[1];
			}
		}
		else {
			//用于预测
			isForTrain=false;
		}
		
		//对每一个长度大于6的句子进行主客观分析。主观的和长度小于等于6的放到inputString中，中间用逗号隔开
		SubObj_predict subobj_predict = new SubObj_predict(0.005);//阀值为0.005
		List <String> result=new ArrayList<String>();
		List <String> tmpResult=new ArrayList<String>();
		for (int i = 0; i < inputString.length; i++) {
			result.add(inputString[i]);
			for (int h = 0; h < CommonValue.parser.length; h++) {
				for (int j = 0; j < result.size(); j++) {
					String[] part=result.get(j).split(CommonValue.parser[h]);
					for (int k = 0; k < part.length; k++) {
						tmpResult.add(part[k]);
					}
				}
				result.clear();
				result.addAll(tmpResult);
				tmpResult.clear();
			}
			if (isForTrain) {
				inputString[i]=inputSentiment[i]+"/";
			}
			for (int j = 0; j < result.size(); j++) {
				//过短的句子就不进行主客观分析了
				if (result.get(j).length()<=6) {
					inputString[i]+=result.get(j)+",";
				}
				else {
					if (subobj_predict.predict(result.get(j))) {
						inputString[i]+=result.get(j)+",";
					}
				}
			}
//			System.out.println("原来："+inputString[i]);
			result.clear();
		}
		
//		System.out.println("-------");
		//删去进行主客观分析后没有内容的句子
		int deleteNum=0;
		if (isForTrain) {
			for (int i = 0; i < inputString.length; i++) {
				if (inputString[i].split("/").length<=1) {
					deleteNum++;
					inputString[i]="";
				}
			}
		}
		else {
			for (int i = 0; i < inputString.length; i++) {
				if (inputString[i].length()==0) {
					deleteNum++;
				}
			}
		}
		String []newInputString = new String [inputString.length-deleteNum];
		int j=0;
		for (int i = 0; i < inputString.length; i++) {
			if (inputString[i].length()>0) {
				newInputString[j]=inputString[i];
				j++;
			}
		}
		
//		for (int i = 0; i < newInputString.length; i++) {
//			System.out.println("删除后："+newInputString[i]);
//		}
		
		return newInputString;
	}
	
	/**
	 * 训练SVM的模型
	 */
	public void train()
	{
		try 
		{		
	        svm_scale.main(scaleArgs_train);  
	        svm_scale.main(scaleArgs_predict);
			svm_train.main(trainArgs); 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 使用训练的模型进行预测
	 * @return 预测的精度，因为即便是用来预测的数据也要对label进行标注，所以返回的实际上是预测结果相对于预测数据文件的准确率，测试的时候可以使用
	 */
	public double predict()
	{
		try {
			return svm_predict.main(predictArgs);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	
	public double predict_fortest(String[] predictArg)
	{
		try {
			return svm_predict.main(predictArg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	
	
	
	
	
}
