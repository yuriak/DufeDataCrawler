package org.haikism.common.bean;

import java.util.Comparator;

public class FeatureWordsBean implements Comparable<FeatureWordsBean>{
	public String wordName;   //词语名称
	public int numInWordPool; //在情感词库里的序号
	public double sentimentValue;//情感倾向值
	public String getWordName() {
		return wordName;
	}
	public void setWordName(String wordName) {
		this.wordName = wordName;
	}
	public int getNumInWordPool() {
		return numInWordPool;
	}
	public void setNumInWordPool(int numInWordPool) {
		this.numInWordPool = numInWordPool;
	}
	public double getSentimentValue() {
		return sentimentValue;
	}
	public void setSentimentValue(double sentimentValue) {
		this.sentimentValue = sentimentValue;
	}
	
	@Override
	public String toString() {
		return "wordName:"+wordName+"  numInWordPool："+numInWordPool+"  sentimentValue:"+sentimentValue;
	}
	@Override
	public int compareTo(FeatureWordsBean o) {
		if (this.getNumInWordPool()==o.getNumInWordPool()) {
			return 0;
		}
		else if (this.getNumInWordPool()<o.getNumInWordPool()) {
			return -1;
		}
		else {
			return 1;
		}
			
	}
	
	
	
}
