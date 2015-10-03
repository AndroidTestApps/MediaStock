
package com.example.mediastock.util;

import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class Utilities {

	/**
	 * It reads the content of the input stream.
     *
	 * @return the string from the stream
	 */
	public static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		
		return sb.toString();
	}


    public static String getDate(int day){
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -day);

        Date d = calendar.getTime();

        return ft.format(d);
    }

	public static String getLicenseKey(){
		String authString = "3bcee2a0f5bc8879f49f:3a24e63a54e171fe231ea2c777ea2dd21884c072";
		byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
		String authStringEnc = new String(authEncBytes);
	
		return authStringEnc;
	} 

}
