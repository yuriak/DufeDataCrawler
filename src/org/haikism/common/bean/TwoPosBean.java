package org.haikism.common.bean;

//用于存放某个2-pos组合的bean
public class TwoPosBean {
	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public double getChi_square() {
		return chi_square;
	}

	public void setChi_square(double chi_square) {
		this.chi_square = chi_square;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	String pattern;  //2-pos的模式，采用"词性-词性"的方式表示
	double chi_square; //卡方值
	double weight;//权重
	
	public TwoPosBean()
	{
		
	}
}
