package org.haikism.textcluster;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;


public class Tokeniser implements ITokeniser {


	public Tokeniser() {
	}
	
	/**
	 * 采用ANSJ进行分词
	 */
	public List<String> partition(String input) {
		List<String> filter = new ArrayList<String>();
		List<Term> terms=ToAnalysis.parse(input);
		for (int i = 0; i < terms.size(); i++) {
			String[] termArray=terms.get(i).toString().split("/");
			boolean shouldStop;
			//过滤掉停用词
			if (termArray.length>1&&!StopWordsHandler.IsStopword(terms.get(i).toString()))
			{
				shouldStop=false;
				//过滤掉停用的词性
				for (int j = 0; j < StopWordsHandler.stopPropertyOfWords.length; j++) {
					if (StopWordsHandler.stopPropertyOfWords[j].equals(termArray[1])) {
						shouldStop=true;
					}
				}
				if (!shouldStop) {
					filter.add(termArray[0]);
				}
				
			}
		}
		return filter;
	}
}
