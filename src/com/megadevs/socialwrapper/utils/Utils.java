package com.megadevs.socialwrapper.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Map;

public class Utils {
	
	/**
	 * Gets current date in a suitable format for Foursquare requests (YYYYMMDD).
	 * @return a string representing the current date
	 */
	public static String getCurrentDate() {
		Calendar c = Calendar.getInstance();
		
		// Calendar.MONTH starts from 0, so a little hack is needed
		String month;
		if (c.get(Calendar.MONTH) < 10)
			month = "0" + String.valueOf(c.get(Calendar.MONTH)+1);
		else
			month = String.valueOf(c.get(Calendar.MONTH)+1);
		
		return String.valueOf(c.get(Calendar.YEAR)) + month + String.valueOf(c.get(Calendar.DAY_OF_MONTH));
	}
	
	/**
	 * Read data from an HTTP post request.
	 * @param targetURL the target page
	 * @param urlParameters the post parameters
	 * @return HTTP response
	 * @throws IOException 
	 * @throws FakeConnectivityException 
	 * @throws IOException 
	 * @throws URISyntaxException 
	 * */
	public static HTTPResult executeHTTPUrlPost(String targetURL, HTTPPostParameters params, Map<String,String> cookies) throws IOException {
		HTTPResult ris = new HTTPResult();
		try{
			ClientHttpRequest client = new ClientHttpRequest(targetURL);

			//Set cookies
			if(cookies!=null){
				client.setCookies(cookies);
				//System.out.println("Set cookies to: "+cookies);
			}

			InputStream is;

			if(params == null)
				is = client.post();
			else if(params.getParamCount()>0){
				is = client.post(params.getParams()); 	
			}
			else 
				is = client.post();

			String data=readInputStreamAsString(is);
			
			//save result
			ris.setData(data);
			//System.out.println(ris.getData());
			ris.setHeader(client.getConnection().getHeaderFields());
			System.out.println("End network");
		}catch(IOException e){
			e.printStackTrace();
		}
		return ris;
	}

	/**
	 * Read an input stream into a string
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static String readInputStreamAsString(InputStream in) throws IOException {

		BufferedInputStream bis = new BufferedInputStream(in);
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		final byte[] buffer = new byte[512];

		int result = bis.read(buffer);
		while(result != -1) {
			//byte b = (byte)result;
			buf.write(buffer,0,result);
			result = bis.read(buffer);
		}        
		return buf.toString();
	}

	
}
