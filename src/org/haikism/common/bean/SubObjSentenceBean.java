package org.haikism.common.bean;

import java.util.ArrayList;
import java.util.List;

//存放主观句分析中的经过预处理的每个句子的bean
public class SubObjSentenceBean {
	//存放该句话所有的词性
	List<String> propertyOfWords;
	public List<String> getPropertyOfWords() {
		return propertyOfWords;
	}

	public void setPropertyOfWords(List<String> propertyOfWords) {
		this.propertyOfWords = propertyOfWords;
	}

	//是否为主观句
	boolean isSubject;
	
	public boolean isSubject() {
		return isSubject;
	}

	public void setSubject(boolean isSubject) {
		this.isSubject = isSubject;
	}

	public SubObjSentenceBean()
	{
		propertyOfWords=new ArrayList<String>();
	}
	
}
