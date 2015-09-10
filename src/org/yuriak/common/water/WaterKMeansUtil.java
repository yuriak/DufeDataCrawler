package org.yuriak.common.water;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.collections.FastArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.runners.Parameterized.Parameters;


public class WaterKMeansUtil {
	public String serverUrl;
	public String fileUrl;
	public String sourceFrame;
	public String destinationFrame;
	public String kmeansID;
	public String predictionID;
	/**
	 * 
	 * @param serverUrl 服务器地址	http://<domain|IP>:[port]
	 */
	public WaterKMeansUtil(String serverUrl){
		this.serverUrl=serverUrl;
	}
	
	public static void main(String[] args) throws IOException {
		WaterKMeansUtil kUtil=new WaterKMeansUtil("http://127.0.0.1:54321");
//		kUtil.importFiles("D:\\java\\J2EE\\DufeDataCrawler\\data\\km.csv");
//		System.out.println(kUtil.parse(kUtil.sourceFrame, -1, "CSV", 44, false));
//		System.out.println(kUtil.parse("[\"nfs:\\D:\\java\\J2EE\\DufeDataCrawler\\data\\km.csv\"]", -1, "CSV", 44, false));
//		System.out.println(kUtil.buildKMeansModel("km.hex", 0, "", true, 3, 100, "Furthest", false, true));
//		System.out.println(kUtil.predict(kUtil.kmeansID, kUtil.destinationFrame));
//		for (int data : kUtil.getData("prediction-2956a6ce-e90a-455e-861b-9664a2ab6406")) {
//			System.out.println(data);
//		}
	}
	
	public String importFiles(String fileUrl) throws IOException{
		String response=WaterHttpUtil.get(serverUrl+"/3/ImportFiles?path="+fileUrl);
		JSONObject resObject=new JSONObject(response);
		if (resObject.getJSONArray("fails").length()==0) {
			JSONArray frameArray=resObject.getJSONArray("destination_frames");
			sourceFrame="[\""+frameArray.getString(0)+"\"]";
			return sourceFrame;
		}else {
			return "error";
		}
	}
	
	/**
	 * 
	 * @param frameName frame的名称,一般是:[\"文件路径\"]
	 * @param checkHeader 第一行是否有列名，0：自动；1:有；-1:没有
	 * @param parseType 解析器：AUTO;ARFF;XLS;XLSX;CSV;SVMLight
	 * @param separator 分隔符：逗号:44; \t:09 space:32
	 * @param singleQuotes 单引号，false就行
	 * @return 目标框架名称
	 * @throws IOException 
	 */
	public String parse(String sourceFrame,int checkHeader,String parseType,int separator,boolean singleQuotes) throws IOException{
		HashMap<String, String> params=new HashMap<>();
		params.put("source_frames", sourceFrame);
		params.put("check_header", checkHeader+"");
		params.put("parse_type", parseType);
		params.put("separator", separator+"");
		params.put("single_quotes", singleQuotes+"");
		String response=WaterHttpUtil.post(serverUrl+"/3/ParseSetup", params);
		System.out.println(response);
		JSONObject resObject=new JSONObject(response);
		destinationFrame=resObject.getString("destination_frame");
		params.clear();
		params.put("destination_frame", resObject.get("destination_frame").toString());
		params.put("source_frames", this.sourceFrame);
		params.put("parse_type", resObject.get("parse_type").toString());
		params.put("separator", resObject.get("separator").toString());
		params.put("number_columns", resObject.get("number_columns").toString());
		params.put("single_quotes", resObject.get("single_quotes").toString());
		params.put("column_names", resObject.get("column_names").toString());
		params.put("column_types", resObject.get("column_types").toString());
		params.put("check_header", resObject.get("check_header").toString());
		params.put("delete_on_done", "true");
		params.put("chunk_size", resObject.get("chunk_size").toString());
		response=WaterHttpUtil.post(serverUrl+"/3/Parse", params);
		System.out.println(response);
		resObject=new JSONObject(response);
		String key=resObject.getJSONObject("job").getJSONObject("key").get("name").toString();
		destinationFrame=resObject.getJSONObject("destination_frame").getString("name");
		return destinationFrame;
	}
	
	
	/**
	 * 
	 * @param training_frame
	 * @param nfolds
	 * @param ignored_columns
	 * @param ignore_const_cols
	 * @param k
	 * @param max_iterations
	 * @param init Random;PlusPlus;*Furthest;User
	 * @param score_each_iteration
	 * @param standardize
	 * @return
	 * @throws IOException
	 */
	public String buildKMeansModel(
			String training_frame,
			int nfolds,
			String ignored_columns,
			boolean ignore_const_cols,
			int k,
			int max_iterations,
			String init,
			boolean score_each_iteration,
			boolean standardize
			) throws IOException{
		HashMap<String, String> params=new HashMap<>();
		kmeansID="kmeans-"+UUID.randomUUID();
		params.put("model_id", kmeansID);
		params.put("training_frame", training_frame);
		params.put("nfolds", nfolds+"");
		params.put("ignored_columns", ignored_columns);
		params.put("ignore_const_cols", ignore_const_cols+"");
		params.put("k", k+"");
		params.put("max_iterations", max_iterations+"");
		params.put("init", init);
		params.put("score_each_iteration", score_each_iteration+"");
		params.put("standardize", standardize+"");
		String response=WaterHttpUtil.post(serverUrl+"/3/ModelBuilders/kmeans", params);
		System.out.println(response);
		return kmeansID;
	}
	
	/**
	 * 
	 * @param modelID
	 * @param frameName
	 * @return
	 * @throws IOException
	 */
	public String predict(String modelID,String frameName) throws IOException{
		HashMap<String, String> params=new HashMap<>();
		predictionID="prediction-"+UUID.randomUUID();
		params.put("predictions_frame", predictionID);
		String response=WaterHttpUtil.post(serverUrl+"/3/Predictions/models/"+modelID+"/frames/"+frameName, params);
		System.out.println(response);
		return predictionID;
	}
	
	/**
	 * 
	 * @param predictionID
	 * @return
	 * @throws IOException
	 */
	public int[] getData(String predictionID) throws IOException{
		String response=WaterHttpUtil.get(serverUrl+"/3/Frames/"+predictionID+"/summary");
		System.out.println(response);
		JSONObject resObject=new JSONObject(response);
		JSONArray dataArray=resObject.getJSONArray("frames").getJSONObject(0).getJSONArray("columns").getJSONObject(0).getJSONArray("data");
		int data[]=new int[dataArray.length()];
		for (int i = 0; i < dataArray.length(); i++) {
			data[i]=dataArray.getInt(i);
		}
		return data;
	}
}
