
package com.example.mediastock.util;

import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.io.Reader;


public class Utilities {

	/**
	 * It reads the content of the input stream.
	 * @param rd
	 * @return
	 * @throws IOException
	 */
	public static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		
		return sb.toString();
	}


	public static String getLicenseKey(){
		String authString = "3bcee2a0f5bc8879f49f:3a24e63a54e171fe231ea2c777ea2dd21884c072";
		byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
		String authStringEnc = new String(authEncBytes);
	
		return authStringEnc;
	} 

}
