package org.yuriak.weibo.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.Header;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.remote.html5.AddApplicationCache;
import org.yuriak.weibo.config.CommonValues;

import cn.edu.hfut.dmic.webcollector.weiboapi.WeiboCN;

public class WeiboAttentionUtil {
	public static void addAttention(String uid,String cookie) throws Exception {  
        // 创建默认的httpClient实例.    
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建httppost    
        HttpPost httppost = new HttpPost("http://m.weibo.cn/attentionDeal/addAttention");
        //伪造http头，伪装成浏览器
        httppost.addHeader("cookie", cookie);
        httppost.addHeader("Referer","http://m.weibo.cn/u/"+uid);
        httppost.addHeader("Origin","http://m.weibo.cn");
        httppost.addHeader("Host", "m.weibo.cn");
        httppost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.111 Safari/537.36");
        // 创建参数队列    
        List<BasicNameValuePair> formparams = new ArrayList<BasicNameValuePair>();  
        formparams.add(new BasicNameValuePair("uid", uid));
        UrlEncodedFormEntity uefEntity;  
        try {  
            uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");  
            httppost.setEntity(uefEntity);  
            System.out.println("executing request " + httppost.getURI());  
            CloseableHttpResponse response = httpclient.execute(httppost);  
            try {  
                HttpEntity entity = response.getEntity();  
                if (entity != null) {
                	//如果返回值里有ok:1就说明加关注成功
                    System.out.println("Response content: " + EntityUtils.toString(entity, "UTF-8"));  
                }
            } finally {  
                response.close();  
            }
        } catch (ClientProtocolException e) {  
            e.printStackTrace();  
        } catch (UnsupportedEncodingException e1) {  
            e1.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {  
            // 关闭连接,释放资源    
            try {  
                httpclient.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
    }
	
	public static void addAllAttention(final List<String> uids,final String cookie){
		//这里本来想用多线程然后配合线程等待来降低速度，后来发现普通速度可以不被封号，所以就没再动
		Thread thread=new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				for (int i = 0; i < uids.size(); i++) {
					try {
						addAttention(uids.get(i),cookie);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		thread.start();
	}
	
	public static void main(String[] args) throws IOException {
//		List<String> users=FileUtils.readLines(new File("data/users.txt"));
//		System.out.println(users.size());
//		addAllAttention(users);
	}
}
