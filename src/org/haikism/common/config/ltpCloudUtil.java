package org.haikism.common.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.haikism.*;
import org.yuriak.common.util.StringUtil;
import org.common.bean.RelationPairBean;

public class ltpCloudUtil {
	
	public static ArrayList<RelationPairBean> analysis(String text) throws Exception{
			ArrayList<RelationPairBean> relationPairBeans=new ArrayList<>();
			text=text.trim().replaceAll("🐦", "");
			text=StringUtil.StringFilter(text);
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
	        String result=soupSend("http://ltpapi.voicecloud.cn/analysis/", "w3P7b4V6ELLIozEEDMhG8LZCrZ8ciPGAyV7N2wyc", text);
	        ArrayList<WordBean> wList=new ArrayList<>();
			try {
				JSONArray a1=new JSONArray(result);
				if (a1==null||a1.length()==0) {
					return wList;
				}
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
			} catch (Exception e) {
				return wList;
			}
			
	    }
	 
	 public static void main(String[] args) throws Exception {
//		 ArrayList<RelationPairBean> r=analysis("强烈建议非士兵考学的军校毕业学员，毕业分配到部队时，首先必须接受为期两年的当兵锻炼，身份就是普通的士兵，两年后根据最后军事技能、体能测试、各级评价等各项考核的综合成绩，按照优胜劣汰的原则来分配每个人的具体工作，没有士兵经历的军官，即使学历在高，也是个不称职的军官！不称职的军官，在基层部队这个只认强者的特殊环境里，根本没有生存的权利！");
//		 for (RelationPairBean string : r) {
//			System.out.println(string.getFirstWordName()+string.getSecondWordName());
//		}
//		 System.out.println(soupSend("http://127.0.0.1/header.php", "w3P7b4V6ELLIozEEDMhG8LZCrZ8ciPGAyV7N2wyc", URLEncoder.encode("我是中国人", "utf-8")));
//		 System.out.println(Jsoup.connect("http://127.0.0.1/header.php").data("api_key", "w3P7b4V6ELLIozEEDMhG8LZCrZ8ciPGAyV7N2wyc").method(Method.POST).execute().body());
//		 System.out.println(soupSend("", "", ""));
//		 Content-Type: application/json
//		 System.out.println(soupSend("http://ltpapi.voicecloud.cn/analysis/", "w3P7b4V6ELLIozEEDMhG8LZCrZ8ciPGAyV7N2wyc", "我是中国人"));
		 
//		 ArrayList<RelationPairBean> relationPairBeans=analysis("本来不是问题的问题");
//		 for (RelationPairBean relationPairBean : relationPairBeans) {
//			System.out.println(relationPairBean.getFirstWordName()+"|"+relationPairBean.getSecondWordName()+"|"+relationPairBean.getRelation());
//		}
	 }
	 
	 public static String crawl(String url,String param){
		 return null;
	 }
	 
	 public static String soupSend(String url, String apiKey,String text) throws Exception{
		 try {
			 return Jsoup.connect(url).data("api_key", apiKey).data("format", "json").data("pattern", "all").data("text", URLEncoder.encode(text, "utf-8")).method(Method.POST).ignoreContentType(true).timeout(3000).execute().body();
		} catch (Exception e) {
			System.out.println("ltp-cloud connection timed out,retrying");
//			e.printStackTrace();
			return null;
		}
		 
	 }
	 
	 public static String sendPost(String url, String param) {
	        PrintWriter out = null;
	        BufferedReader in = null;
	        String result = "";
	        try {
	            URL realUrl = new URL(url);
	            // 打开和URL之间的连接
	            URLConnection conn = realUrl.openConnection();
	            // 设置通用的请求属性
	            conn.setRequestProperty("accept", "*/*");
	            conn.setRequestProperty("connection", "Keep-Alive");
	            conn.setRequestProperty("user-agent",
	                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
	            // 发送POST请求必须设置如下两行
	            conn.setDoOutput(true);
	            conn.setDoInput(true);
	            // 获取URLConnection对象对应的输出流
	            out = new PrintWriter(conn.getOutputStream());
	            // 发送请求参数
	            out.print(param);
	            // flush输出流的缓冲
	            out.flush();
	            // 定义BufferedReader输入流来读取URL的响应
	            in = new BufferedReader(
	                    new InputStreamReader(conn.getInputStream()));
	            String line;
	            while ((line = in.readLine()) != null) {
	                result += line;
	            }
	        } catch (Exception e) {
	            System.out.println("发送 POST 请求出现异常！"+e);
	            e.printStackTrace();
	        }
	        //使用finally块来关闭输出流、输入流
	        finally{
	            try{
	                if(out!=null){
	                    out.close();
	                }
	                if(in!=null){
	                    in.close();
	                }
	            }
	            catch(IOException ex){
	                ex.printStackTrace();
	            }
	        }
	        return result;
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
