package org.yuriak.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.bcel.generic.NEW;

public class MyLog {
	public static void INFO(String string){
		System.out.println(new SimpleDateFormat("YYYY-M-d H:m:s").format(new Date()).toString()+" > "+string);
	}
	
//	public static void main(String[] args) {
//		INFO("asd");
//	}
}
