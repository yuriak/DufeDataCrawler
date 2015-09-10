package org.yuriak.common.util;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.sql.*;

public class TimeUtil {

	public static String convertLongToDateString(String time){
		long unixLong=Long.parseLong(time)*1000;
		String date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(unixLong));
		return date;
	}
	
	public static Timestamp convertStringToTimeStamp(String time) throws Exception{
		Format f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = (Date) f.parseObject(time);
        Timestamp ts = new Timestamp(d.getTime());
        return ts;
	}
	
	public static void main(String[] args) throws Exception {
		Timestamp t1=convertStringToTimeStamp("2015-05-20 19:16:30");
		Timestamp t2=convertStringToTimeStamp("2015-05-20 19:30:25");
		
	}
	
	public static int getIntervalDays(Date startday, Date endday) {
		if (startday.after(endday)) {
			Date cal = startday;
			startday = endday;
			endday = cal;
		}
		long sl = startday.getTime();
		long el = endday.getTime();
		long ei = el - sl;
		return (int) (ei / (1000 * 60 * 60 * 24));
	}
	
	public static int getDaysBetween(Calendar d1, Calendar d2) {
		if (d1.after(d2)) {
			java.util.Calendar swap = d1;
			d1 = d2;
			d2 = swap;
		}
		int days = d2.get(Calendar.DAY_OF_YEAR) - d1.get(Calendar.DAY_OF_YEAR);
		int y2 = d2.get(Calendar.YEAR);
		if (d1.get(Calendar.YEAR) != y2) {
			d1 = (Calendar) d1.clone();
			do {
				days += d1.getActualMaximum(Calendar.DAY_OF_YEAR);// 得到当年的实际天数
				d1.add(Calendar.YEAR, 1);
			} while (d1.get(Calendar.YEAR) != y2);
		}
		return days;
	}
}
