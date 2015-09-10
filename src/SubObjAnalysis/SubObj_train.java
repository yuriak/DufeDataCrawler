package SubObjAnalysis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.haikism.common.bean.SubObjSentenceBean;
import org.haikism.common.bean.TwoPosBean;
import org.haikism.common.config.FileOperation;

/**
 * 用于训练主观性文本，获得模型文件的类
 * 使用方法：
 * @author Haikism
 */
public class SubObj_train {

	// 存放每句话的词性，第一层list用于存放每句话，第二层用于存放某句话的词性
	private List<SubObjSentenceBean> SubObjSentenceBeans;
	// 装有主观的2-pos组合的list
	private List<TwoPosBean> proPertyOfWordsList;
	// 根据各2-pos组合的卡方值和阀值来确定该组合是写入模型文件
	private double threshodValue = 1;
	//训练文件名
	private String trainDataName="res/trainData_SubObjAnalysis.txt";

	/**
	 * 构造函数，训练用的文件名默认为："res/trainData_SubObjAnalysis.txt"
	 * @param threshodValue 根据各2-pos组合的卡方值和阀值来确定该组合是写入模型文件，阀值由测试时设定。默认值是1
	 */
	public SubObj_train(double threshodValue) {
		this.threshodValue=threshodValue;
		SubObjSentenceBeans = new ArrayList<SubObjSentenceBean>();
		proPertyOfWordsList = new ArrayList<TwoPosBean>();
	}
	
	/**
	 * 构造函数
	 * @param threshodValue 根据各2-pos组合的卡方值和阀值来确定该组合是写入模型文件，阀值由测试时设定。默认值是1
	 * @param 训练文件名
	 */
	public SubObj_train(double threshodValue,String trainDataName) {
		this.trainDataName=trainDataName;
		this.threshodValue=threshodValue;
		SubObjSentenceBeans = new ArrayList<SubObjSentenceBean>();
		proPertyOfWordsList = new ArrayList<TwoPosBean>();
	}

	/**
	 *  训练以获得模型，模型文件命名为：trainData_SubObjAnalysis.txt
	 */
	public void train() {
		String allContent = FileOperation.readTxtFile(new File(
				trainDataName));
		// System.out.println(allContent);
		String[] sentence = allContent.split("\r\n");

		for (int i = 0; i < sentence.length; i++) {
			String[] eachSentence = sentence[i].split("/");
			SubObjSentenceBean subObjSentenceBean = new SubObjSentenceBean();
			if (eachSentence[0].equals("1")) {
				// 1的时候为主观句
				subObjSentenceBean.setSubject(true);
			} else {
				// -1的时候为客观句
				subObjSentenceBean.setSubject(false);
			}

			List<Term> terms = ToAnalysis.parse(eachSentence[1]);// 分词
			// 提取词性，放到subObjSentenceBean中
			for (int j = 0; j < terms.size(); j++) {
				// 判断某个词是否有词性
				if (terms.get(j).toString().split("/").length > 1) {
					subObjSentenceBean.getPropertyOfWords().add(
							terms.get(j).toString().split("/")[1]);
				}
			}
			SubObjSentenceBeans.add(subObjSentenceBean);
			System.out.println((i + 1) + ": 添加的词性有："
					+ subObjSentenceBean.getPropertyOfWords() + ",isSubject:"
					+ subObjSentenceBean.isSubject());
		}

		for (int i = 0; i < SubObjSentenceBeans.size(); i++) {
			SubObjSentenceBean subObjSentenceBean = SubObjSentenceBeans.get(i);

			String thePattern = "";

			for (int j = 0; j < subObjSentenceBean.getPropertyOfWords().size() - 1; j++) {
				thePattern = subObjSentenceBean.getPropertyOfWords().get(j)
						+ "-"
						+ subObjSentenceBean.getPropertyOfWords().get(j + 1);

				boolean alreadyAdded = false;
				for (int j2 = 0; j2 < proPertyOfWordsList.size(); j2++) {
					if (proPertyOfWordsList.get(j2).getPattern()
							.equals(thePattern)) {
						alreadyAdded = true;
						break;
					}
				}

				if (!alreadyAdded) {
					TwoPosBean twoPosBean = new TwoPosBean();
					twoPosBean.setPattern(thePattern);
					proPertyOfWordsList.add(twoPosBean);
				}
			}
		}

		// for (int i = 0; i < proPertyOfWordsList.size(); i++) {
		// System.out.print(proPertyOfWordsList.get(i).getPattern()+"  ");
		// }

		double A;// A表示主观句中包含该pattern的数目
		double B;// B表示客观句中包含该pattern的数目
		double C;// C表示主观句中不包含该pattern的数目
		double D;// D表示客观句中不包含该pattern的数目

		boolean hasTheTwoPos;
		for (int i = 0; i < proPertyOfWordsList.size(); i++) {
			double N = SubObjSentenceBeans.size(); // N表示主客观句子的总数
			A = 0;
			B = 0;
			C = 0;
			D = 0;
			for (int j = 0; j < SubObjSentenceBeans.size(); j++) {

				hasTheTwoPos = false;
				if (!SubObjSentenceBeans.get(j).isSubject()) {
					// 客观句
					for (int j2 = 0; j2 < SubObjSentenceBeans.get(j)
							.getPropertyOfWords().size() - 1; j2++) {
						String thePattern = SubObjSentenceBeans.get(j)
								.getPropertyOfWords().get(j2)
								+ "-"
								+ SubObjSentenceBeans.get(j)
										.getPropertyOfWords().get(j2 + 1);
						if (thePattern.equals(proPertyOfWordsList.get(i)
								.getPattern())) {
							B++;
							hasTheTwoPos = true;
							break;
						}
					}
					if (!hasTheTwoPos) {
						D++;
					}
				}

				else {
					// 主观句
					hasTheTwoPos = false;
					for (int j2 = 0; j2 < SubObjSentenceBeans.get(j)
							.getPropertyOfWords().size() - 1; j2++) {
						String thePattern = SubObjSentenceBeans.get(j)
								.getPropertyOfWords().get(j2)
								+ "-"
								+ SubObjSentenceBeans.get(j)
										.getPropertyOfWords().get(j2 + 1);
						if (thePattern.equals(proPertyOfWordsList.get(i)
								.getPattern())) {
							A++;
							hasTheTwoPos = true;
							// System.out.println((j+1)+"句中有"+thePattern+"  这个2-pos组合");
							break;
						}
					}
					if (!hasTheTwoPos) {
						C++;
					}
				}
			}

			// System.out.println("thePattern:"+proPertyOfWordsList.get(i).getPattern()+"    A:"+A+" B:"+B+" C:"+C+" D:"+D);
			proPertyOfWordsList.get(i).setChi_square(
					N * (A * D - C * B) * (A * D - C * B)
							/ ((A + C) * (B + D) * (A + B) * (C + D)));
			// System.out.println("计算的卡方值："+proPertyOfWordsList.get(i).getChi_square());

			// 计算查准率=（检索出的相关信息量/检索出的信息总量）x100% 作为权重
			proPertyOfWordsList.get(i).setWeight(A / (A + B));
		}
		writeToModel();
	}

	/**
	 *  将训练的结果写入到模型文件中
	 */
	private void writeToModel() {
		String writeContent = "";
		for (int i = 0; i < proPertyOfWordsList.size(); i++) {
			if (proPertyOfWordsList.get(i).getChi_square() > threshodValue) {
				// 格式为：每行一个2-pos组合：pattern/weight
				writeContent += proPertyOfWordsList.get(i).getPattern() + "/";
				writeContent += proPertyOfWordsList.get(i).getWeight() + "\r\n";
			}
		}
		FileOperation.writeTxtFile(writeContent, new File(
				"res/model_SubObj.txt"),false);
	}

}
