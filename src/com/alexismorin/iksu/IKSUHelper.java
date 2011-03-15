package com.alexismorin.iksu;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class IKSUHelper {
	private static final String iksuWsUrl = "http://alexismorin.com/iksuMobile/iksuMobile.php";
	private static final int HTTP_STATUS_OK = 200;
	private static byte[] buff = new byte[1024];
	
	public static class ApiException extends Exception {
        public ApiException (String msg)
        {
            super (msg);
        }

        public ApiException (String msg, Throwable thr)
        {
            super (msg, thr);
        }
    }
	
	protected static synchronized String downloadFromServer(String param) throws ApiException {
		HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(iksuWsUrl + param);
        try {
			HttpResponse response = client.execute(request);
			StatusLine status = response.getStatusLine();
			if(status.getStatusCode() != HTTP_STATUS_OK)
				throw new ApiException("Invalid response from search.twitter.com" + 
                        status.toString());
			HttpEntity entity = response.getEntity();
			InputStream ist = entity.getContent();
			
			ByteArrayOutputStream content = new ByteArrayOutputStream();
			int readcount = 0;
            while ((readcount = ist.read(buff)) != -1)
				content.write(buff, 0, readcount);
			
			return new String (content.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
			throw new ApiException("Problem accessing the Webservice.");
		}
	}
}
