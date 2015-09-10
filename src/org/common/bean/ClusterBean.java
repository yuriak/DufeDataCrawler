package org.common.bean;

import java.util.ArrayList;
public class ClusterBean {
	public ArrayList<String> getKeywords() {
		return keywords;
	}
	public void setKeywords(ArrayList<String> keywords) {
		this.keywords = keywords;
	}
	public ArrayList<String> getSentences() {
		return sentences;
	}
	public void setSentences(ArrayList<String> sentences) {
		this.sentences = sentences;
	}
	private ArrayList<String> keywords;
	private ArrayList<String> sentences;
}
