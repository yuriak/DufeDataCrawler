package org.haikism.common.bean;

/**
 * 用来存放情感词及其情感倾向值的bean
 * @author Haikism
 *
 */
public class SentiWordsBean {
	//情感词的名称
	public String wordsName;
	//情感倾向值
	public int sentiValue;
	
	public String getWordsName() {
		return wordsName;
	}

	public void setWordsName(String wordsName) {
		this.wordsName = wordsName;
	}

	public int getSentiValue() {
		return sentiValue;
	}

	public void setSentiValue(int sentiValue) {
		this.sentiValue = sentiValue;
	}

	public SentiWordsBean()
	{
		
	}
	
}
