package org.haikism.common.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.List;

import org.haikism.common.bean.FeatureWordsBean;

/**
 * 用来创建、写入、读取文件的类
 * 
 * @author Haikism
 */
public class FileOperation {
	/**
	 * 创建文件
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean createFile(File fileName){
		boolean flag = false;
		try {
			if (!fileName.exists()) {
				fileName.createNewFile();
				flag = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

	/**
	 * 读TXT文件内容
	 * 
	 * @param fileName
	 */
	public static String readTxtFile(File fileName) {
		String result = "";
		InputStreamReader in = null;
		BufferedReader bufferedReader = null;
		try {
			in= new InputStreamReader(new FileInputStream(fileName),"UTF-8");
			bufferedReader = new BufferedReader(in);
			try {
				
				int lineNum=0;
				String read = null;
				while ((read = bufferedReader.readLine()) != null) {
					if (lineNum==0) {
						read=read.substring(1);
					}
					lineNum++;
					result = result + read + "\r\n";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
//		System.out.println("读取出来的文件内容是：" + "\r\n" + result);
		return result;
	}

	/**
	 * 写入文件内容
	 * 
	 * @param content 文件内容
	 * @param fileName 文件路径
	 * @param append 设置是否覆盖原来的内容，false不覆盖原来内容，true覆盖原来内容
	 * @return
	 * @throws Exception
	 */
	public static boolean writeTxtFile(String content, File fileName,boolean append) {
		RandomAccessFile mm = null;
		boolean flag = false;
		FileOutputStream o = null;
		try {
			o = new FileOutputStream(fileName,append);  //设置不覆盖原来内容
			o.write(content.getBytes("utf-8"));
			o.close();
			// mm=new RandomAccessFile(fileName,"rw");
			// mm.writeBytes(content);
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (mm != null) {
				try {
					mm.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return flag;
	}
	
	
	/**
	 * 写入一行符合LIBSVM格式要求的数据
	 * 每行的格式：Label 1:value 2:value …
	 * @param featureWordsBeans
	 * @param fileName
	 * @param isPositive 该条句子是否是积极的，true代表积极，false代表消极
	 */
	public static void writeFeatureWordsBeans(List<FeatureWordsBean> featureWordsBeans, File fileName,boolean isPositive) {
		String writeContent;
		if (isPositive) {
		    writeContent="1 ";
	    }
	    else {
		    writeContent="-1 ";
	    }
		
		double subSum=0;
		double ObjSum=0;
		for (int i = 0; i < featureWordsBeans.size(); i++) {
			if (featureWordsBeans.get(i).getSentimentValue()>0) {
				subSum+=featureWordsBeans.get(i).getSentimentValue();
			}
			else {
				ObjSum+=featureWordsBeans.get(i).getSentimentValue();
			}
		}
		if (!(subSum==0&&ObjSum==0)) {
			writeContent+="1:";
			writeContent+=subSum/(subSum-ObjSum);
			writeContent+=" ";
			
			writeContent+="2:";
			writeContent+=1-subSum/(subSum-ObjSum);
			writeContent+=" ";
		}
		else {
			writeContent+="1:0 ";
			
			writeContent+="2:0 ";
		}

		
		writeContent+="\r\n";//换行
		writeTxtFile(writeContent, fileName,true);

		
	}

	public static void contentToTxt(String filePath, String content) {
		String str = new String(); // 原有txt内容
		String s1 = new String();// 内容更新
		try {
			File f = new File(filePath);
			if (f.exists()) {
				System.out.print("文件存在");
			} else {
				System.out.print("文件不存在");
				f.createNewFile();// 不存在则创建
			}
			BufferedReader input = new BufferedReader(new FileReader(f));

			while ((str = input.readLine()) != null) {
				s1 += str + "\n";
			}
			System.out.println(s1);
			input.close();
			s1 += content;

			BufferedWriter output = new BufferedWriter(new FileWriter(f));
			output.write(s1);
			output.close();
		} catch (Exception e) {
			e.printStackTrace();

		}
	}
}
