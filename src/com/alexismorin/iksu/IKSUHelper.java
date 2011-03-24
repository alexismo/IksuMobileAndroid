package com.alexismorin.iksu;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

public class IKSUHelper {
	private static final String iksuWsUrl = "http://alexismorin.com/iksuMobile/iksuMobile.php";
	private static final int HTTP_STATUS_OK = 200;
	private static byte[] buff = new byte[1024];
	
	@SuppressWarnings("serial")
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
	
	protected static synchronized String getXml() throws ApiException, SocketTimeoutException {
        HttpGet request = new HttpGet(iksuWsUrl);
        request.setHeader("User-Agent","IksuMobileAndroid");
        /*HttpParams httpParameters = new BasicHttpParams();
        
        int timeoutConnection = 5000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        int timeoutSocket = 5000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);*/
        HttpClient client = new DefaultHttpClient();
        
        try {
        	Log.i("GetXML", "Executing HTTP Request");
			HttpResponse response = client.execute(request);
			StatusLine status = response.getStatusLine();
			DefaultRedirectHandler redirectHandle = new DefaultRedirectHandler();
			HttpContext localHttpContext = new BasicHttpContext(); 
			
			if(status.getStatusCode() != HTTP_STATUS_OK){
				Log.i("GetXML", "HTTP STATUS error");
				throw new Exception("Invalid response from alexismorin.com" + 
                        status.toString());
			}
			
			if(redirectHandle.isRedirectRequested(response, localHttpContext)){
				Log.i("GetXML", "Redirrects");
				throw new Exception("Request redirrected from alexismorin.com");
			}
			
			HttpEntity entity = response.getEntity();
			InputStream ist = entity.getContent();
			
			ByteArrayOutputStream content = new ByteArrayOutputStream();
			int readcount = 0;
            while ((readcount = ist.read(buff)) != -1)
				content.write(buff, 0, readcount);
            
            //check if the content is longer than 0
            if(content.size() > 0)
            	return new String(content.toByteArray());
            else
            	throw new Exception("No content retrieved from web call.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;//if the try/catch fails
	}
	
	public static Document parseTheXml(String thePage) throws SAXException, IOException, ParserConfigurationException{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(thePage));
	        
	        return db.parse(is);
		//build dat DOM document!

	}
	
	public static ArrayList<String> getDatesArray(Document allDoc){
		NodeList nodes = allDoc.getElementsByTagName("day");
		ArrayList<String> datesArray = new ArrayList<String>();
		
		for(int i = 0; i < nodes.getLength(); i++){
			Element element = (Element) nodes.item(i);
			NodeList datesNodes = element.getElementsByTagName("date");
			
			Element line = (Element) datesNodes.item(0);
			
			datesArray.add(getCharacterDataFromElement(line).trim());//gotta trim...or you can't see shiat
		}
		
		return datesArray;
	}
	
	public static ArrayList<IKSUActivity> getScheduleFromPageForDay(Document allDoc, int dayIndex, String filter){
		ArrayList<IKSUActivity> activities = new ArrayList<IKSUActivity>();
		NodeList nodes = allDoc.getElementsByTagName("day");
		//only gets the day we need
		Element element = (Element) nodes.item(dayIndex);
		
		NodeList activitiesNodes = element.getElementsByTagName("activity");
		
		for(int j = 0; j < activitiesNodes.getLength(); j++){
			Element activity = (Element) activitiesNodes.item(j);
			
			NodeList actType = activity.getElementsByTagName("type");
			NodeList actTime = activity.getElementsByTagName("time");
			NodeList actName = activity.getElementsByTagName("name");
			NodeList actRoom = activity.getElementsByTagName("room");
			NodeList actInstructor = activity.getElementsByTagName("instructor");
			
			//TODO benchmark .startsWith() versus .equals() for performance
			if(getCharacterDataFromElement((Element) actType.item(0)).trim().startsWith(filter, 0))
			activities.add(new IKSUActivity(
					getCharacterDataFromElement(((Element) actName.item(0))).trim(), 
					getCharacterDataFromElement(((Element) actTime.item(0))).trim(), 
					dayIndex, 
					getCharacterDataFromElement(((Element) actInstructor.item(0))).trim(), 
					getCharacterDataFromElement(((Element) actRoom.item(0))).trim()));
		}
		
		return activities;
	}
	
	public static String getCharacterDataFromElement(Element e) {
	    Node child = e.getFirstChild();
	    if (child instanceof CharacterData) {
	       CharacterData cd = (CharacterData) child;
	       return cd.getData();
	    }
	    return "?";
	  }
	
}
