package SubObjAnalysis;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.haikism.common.bean.SubObjSentenceBean;
import org.haikism.common.config.FileOperation;

/**
 * 用于根据模型进行主观句预测的类
 * 使用方法：创建对象，调用对象的predict函数即可
 * @author Haikism
 */
public class SubObj_predict {

	// 装有主观的2-pos组合的HashMap
	private Map proPertyOfWordsMap;
	// 用于判断某句子是否为主观句的阀值
	private double threshodValue = 1;

	
	/**
	 * 构造函数
	 * @param threshodValue 用于判断某句子是否为主观句的阀值，阀值由测试时设定，默认值是1
	 */
	@SuppressWarnings("rawtypes")
	public SubObj_predict(double threshodValue) {
		this.threshodValue=threshodValue;
		proPertyOfWordsMap = new HashMap();
		readModel();
	}

	/**
	 * 预测
	 * 
	 * @param sentence 用于判断主客观性的句子
	 * @return 返回值为真，则为主观句，否则为客观句
	 */
	@SuppressWarnings("rawtypes")
	public boolean predict(String sentence) {
		// 该句子的主观性总得分
		double Subj = 0;
		SubObjSentenceBean subObjSentenceBean = new SubObjSentenceBean();
		List<Term> terms = ToAnalysis.parse(sentence);// 分词
		// 提取词性，放到subObjSentenceBean中
		for (int j = 0; j < terms.size(); j++) {
			// 判断某个词是否有词性
			if (terms.get(j).toString().split("/").length > 1) {
				subObjSentenceBean.getPropertyOfWords().add(
						terms.get(j).toString().split("/")[1]);
			}
		}

		Iterator it = proPertyOfWordsMap.keySet().iterator();
		String key;
		double value;
		// 遍历该句的所有2-pos组合
		for (int i = 0; i < subObjSentenceBean.getPropertyOfWords().size() - 1; i++) {
			String theTwoPos = subObjSentenceBean.getPropertyOfWords().get(i)
					+ "-" + subObjSentenceBean.getPropertyOfWords().get(i + 1);
			// 遍历model中的2-pos组合，与句子中的2-pos组合匹配，如果匹配成功则将其权重值加到总得分中
			while (it.hasNext()) {
				key = it.next().toString();
				value = (double) proPertyOfWordsMap.get(key);
				if (theTwoPos.equals(key)) {
					Subj += value;
					break;
				}
			}
		}

		// 消除句子长度对于总得分的影响
		Subj = Subj / (subObjSentenceBean.getPropertyOfWords().size() - 1);

		// 将总得分与阀值比较
		if (Subj >= threshodValue) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 读入模型文件
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void readModel() {
		String modelContent = FileOperation.readTxtFile(new File(
				"res/model_SubObj.txt"));
		String[] sentences = modelContent.split("\r\n");
		for (int i = 0; i < sentences.length; i++) {
			String[] attributeOfTwoBean = sentences[i].split("/");
			proPertyOfWordsMap.put(attributeOfTwoBean[0],
					Double.parseDouble(attributeOfTwoBean[1]));
		}
	}

}
