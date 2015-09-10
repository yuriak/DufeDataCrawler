package org.haikism.common.config;
public class Keyword implements Comparable<Keyword> {
    private String name;
    private double score;
    private double idf;
    private int freq;

    public Keyword(String name, int docFreq, int weight) {
        this.name = name;
        this.idf = Math.log(10000 + 10000.0 / (docFreq + 1));
        this.score = idf * weight;
        freq++;
    }
    public void updateWeight(int weight) {
        this.score += weight * idf;
        freq++;
    }
    public int compareTo(Keyword o) {
        if (this.score < o.score) {
            return 1;
        } else {
            return -1;
        }

    }
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        if (obj instanceof Keyword) {
            Keyword k = (Keyword) obj;
            return k.name.equals(name);
        } else {
            return false;
        }
    }
    public String toString() {
        return name;
    }
    public double getScore(){
    	return score;
    }
    public int getFreq() {
        return freq;
    }

}