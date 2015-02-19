package com.stampur.android.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

public class Helper_Http {

    private static String imageUploadServer;

    public static void setImageUploadServer(String server) {
        imageUploadServer = server;
    }
	public static String get_string_from_url(String URL)
	{
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet getRequest = new HttpGet(URL);
		HttpResponse response;
		try
		{
			response = httpclient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
			{
				HttpEntity entity = response.getEntity();
				InputStream instream = entity.getContent();				
				String resultString= convertStreamToString(instream);
				instream.close();
				return resultString.trim();
			}
		}
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	public static JSONObject get_json_from_url(String url)
	{
		try
		{
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpGet getRequest = new HttpGet(url);
			HttpResponse response = (HttpResponse) httpclient.execute(getRequest);
			HttpEntity entity = response.getEntity();

			if (entity != null)
			{
				InputStream instream = entity.getContent();				
				String resultString= convertStreamToString(instream);
				instream.close();
				JSONObject jsonObjRecv = new JSONObject(resultString);
				
				return jsonObjRecv;
			} 
		}
		catch (Exception e){e.printStackTrace();}
		return null;
	}
	
	public static String post_json_to_url(String url, JSONObject jsonObjSend)
	{
		try
		{
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost(url);
			
			postRequest.setEntity(new StringEntity(jsonObjSend.toString()));
			postRequest.setHeader("Accept", "application/json");
			postRequest.setHeader("Content-type", "application/json");
			
			HttpResponse response = (HttpResponse) httpclient.execute(postRequest);
			
			HttpEntity entity = response.getEntity();

			if (entity != null)
			{
				InputStream instream = entity.getContent();				
				String resultString= convertStreamToString(instream);
				instream.close();
				System.out.println(resultString);
				
				return resultString;
			} 
		}
		catch (Exception e){e.printStackTrace(); return "";}
		return null;
	}
	
	public static String post_image_to_media(String pathname, byte [] image_buffer)
	{
		try
		{
			DefaultHttpClient httpclient = new DefaultHttpClient();
			
			HttpPost postRequest = new HttpPost(imageUploadServer);
			postRequest.setEntity(new ByteArrayEntity(image_buffer));
			postRequest.setHeader("X-File-Name", pathname);
			
			HttpResponse response = httpclient.execute(postRequest);
			HttpEntity entity = response.getEntity();

			if (entity != null)
			{
				InputStream instream = entity.getContent();				
				String resultString= convertStreamToString(instream);
				instream.close();
				
				return resultString;
			} 
		}
		catch (Exception e){e.printStackTrace(); return "";}
		return null;
	}

	private static String convertStreamToString(InputStream is)
	{
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}	
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

}
