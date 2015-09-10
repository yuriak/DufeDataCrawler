package org.yuriak.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.common.bean.RelationPairBean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ltpCloudUtil {
	
	public static ArrayList<RelationPairBean> analysis(String text) throws Exception{
			ArrayList<RelationPairBean> relationPairBeans=new ArrayList<>();
			ArrayList<WordBean> wordBeans=get(text);
			for (WordBean wordBean : wordBeans) {
				RelationPairBean rBean=new RelationPairBean();
				if (wordBean.getParent()==-1) {
					continue;
				}
				if (wordBean.getRelation().equals("ADV")||wordBean.getRelation().equals("CMP")||wordBean.getRelation().equals("VOB")) {
					rBean.setFirstId(wordBean.getId());
					rBean.setFirstPos(wordBean.getPos());
					rBean.setFirstWordName(wordBean.getName());
					rBean.setRelation(wordBean.getRelation());
					rBean.setSentiValue(0);
					for (WordBean wordBean2 : wordBeans) {
						if (wordBean2.getId()==wordBean.getParent()) {
							rBean.setSecondId(wordBean2.getId());
							rBean.setSecondPos(wordBean2.getPos());
							rBean.setSecondWordName(wordBean2.getName());
						}
					}
					relationPairBeans.add(rBean);
				}else {
					continue;
				}
			}
			return relationPairBeans;
	}
	
	 private static ArrayList<WordBean> get(String text) throws Exception {
	       
	        String api_key = "w3P7b4V6ELLIozEEDMhG8LZCrZ8ciPGAyV7N2wyc";
	        String pattern = "all";
	        String format  = "json";
	        text = URLEncoder.encode(text, "utf-8");
	        URL url     = new URL("http://ltpapi.voicecloud.cn/analysis/?"
	                              + "api_key=" + api_key + "&"
	                              + "text="    + text    + "&"
	                              + "format="  + format  + "&"
	                              + "pattern=" + pattern);
	        URLConnection conn = url.openConnection();
	        conn.connect();

	        BufferedReader innet = new BufferedReader(new InputStreamReader(
	                                conn.getInputStream(),
	                                "utf-8"));
	        String line;
	        StringBuilder sBuilder=new StringBuilder();
	        while ((line = innet.readLine())!= null) {
	        	sBuilder.append(line);
	        }
	        innet.close();
	        ArrayList<WordBean> wList=new ArrayList<>();
			JSONArray a1=new JSONArray(sBuilder.toString());
			for (int i = 0; i < a1.length(); i++) {
				JSONArray a2=a1.getJSONArray(i);
				for (int j = 0; j < a2.length(); j++) {
					JSONArray a3=a2.getJSONArray(j);
					for (int k = 0; k < a3.length(); k++) {
						JSONObject wordObj=a3.getJSONObject(k);
						WordBean word=new WordBean();
						word.setId(wordObj.getInt("id"));
						word.setName(wordObj.getString("cont"));
						word.setPos(wordObj.getString("pos"));
						word.setParent(wordObj.getInt("parent"));
						word.setRelation(wordObj.getString("relate"));
						wList.add(word);
					}
				}
			}
			return wList;
	    }
	 
	 public static void main(String[] args) {
		try {
			ArrayList<RelationPairBean> relationPairBeans=analysis("她很是不漂亮");
			for (RelationPairBean relationPairBean : relationPairBeans) {
				System.out.println(relationPairBean.getFirstWordName()+relationPairBean.getSecondWordName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static class WordBean{
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getPos() {
			return pos;
		}
		public void setPos(String pos) {
			this.pos = pos;
		}
		public int getParent() {
			return parent;
		}
		public void setParent(int parent) {
			this.parent = parent;
		}
		public String getRelation() {
			return relation;
		}
		public void setRelation(String relation) {
			this.relation = relation;
		}
		private int id;
		private String name;
		private String pos;
		private int parent;
		private String relation;
	}
}
