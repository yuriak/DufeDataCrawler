package org.haikism.common.bean;

/**
 * 修饰词在前，中心词（情感词）在后
 * @author Haikism
 */
public class RelationPairBean {
	
	
	public int getFirstId() {
		return firstId;
	}
	public void setFirstId(int firstId) {
		this.firstId = firstId;
	}
	public String getFirstWordName() {
		return firstWordName;
	}
	public void setFirstWordName(String firstWordName) {
		this.firstWordName = firstWordName;
	}
	public String getFirstPos() {
		return firstPos;
	}
	public void setFirstPos(String firstPos) {
		this.firstPos = firstPos;
	}
	public int getSecondId() {
		return secondId;
	}
	public void setSecondId(int secondId) {
		this.secondId = secondId;
	}
	public String getSecondWordName() {
		return secondWordName;
	}
	public void setSecondWordName(String secondWordName) {
		this.secondWordName = secondWordName;
	}
	public String getSecondPos() {
		return secondPos;
	}
	public void setSecondPos(String secondPos) {
		this.secondPos = secondPos;
	}
	public String getRelation() {
		return relation;
	}
	public void setRelation(String relation) {
		this.relation = relation;
	}
	public double getSentiValue() {
		return sentiValue;
	}
	public void setSentiValue(double sentiValue) {
		this.sentiValue = sentiValue;
	}
	//首个词语的Id
	public int firstId;
	//首个词语的名称
	public String firstWordName;
	//首个词语的pos(词性)
	public String firstPos;
	
	//首个词语的Id
	public int secondId;
	//首个词语的名称
	public String secondWordName;
	//首个词语的pos(词性)
	public String secondPos;
	
	//两个词的关系，只需要ADV，CMP，VOB
	public String relation;
	//其中的情感词的情感值
	public double sentiValue;
	
	
}
