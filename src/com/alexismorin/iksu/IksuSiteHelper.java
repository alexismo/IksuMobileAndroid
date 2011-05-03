package com.alexismorin.iksu;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

public class IksuSiteHelper {
	
	/*public static final String IKSU_SCHEDULE_URL =
		"http://bokning.iksu.se/index.php?func=list";
	public static final String XPATH_IKSU_DATE = "//td[@class='datarow_bold']";
	public static final String XPATH_IKSU_DAY = "//table[@id='tid_$']";//dolla bill sign is for replacing
	public static final String XPATH_IKSU_ALL = "//td[@id='content']/div/div";
	public static final String XPATH_IKSU_ACTIVITY_TIME = "//td[@class='sort_col2_nl']";
	public static final String XPATH_IKSU_ACTIVITY_NAME = "//td[@class='sort_col3_nl']";
	public static final String XPATH_IKSU_ACTIVITY_ROOM = "//td[@class='sort_col4_nl']";
	public static final String XPATH_IKSU_ACTIVITY_INSTRUCTOR = "//td[@class='sort_col5_nl']";
	*/
	
	private static final int HTTP_STATUS_OK = 200;
	
	protected static synchronized InputStream getAWebPage(URL url) 
	throws ClientProtocolException, IOException{
		HttpClient httpClient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		HttpGet httpGet = new HttpGet(url.toString());
		try{
			//do the request
			HttpResponse response = httpClient.execute(httpGet, localContext);
			StatusLine status = response.getStatusLine();
			if(status.getStatusCode() != HTTP_STATUS_OK){
				throw new IOException("Invalid response from the IKSU server! " + status.toString());
			}
			//InputStream ist = response.getEntity().getContent();
			
			return response.getEntity().getContent();
		}catch(Exception e){
			e.printStackTrace();
			throw new ClientProtocolException("Protocol Exception! "+e.toString());
		}
	}
	
	/*public static Object[] performXPathSelectorOnWebPage(String sXpath, String webpage) 
		throws ParserConfigurationException, SAXException, IOException, XPatherException{
		
		//clean the html in the InputStream
		HtmlCleaner pageParser = new HtmlCleaner();
		CleanerProperties props = pageParser.getProperties();
		props.setAllowHtmlInsideAttributes(true);
		props.setRecognizeUnicodeChars(true);
		props.setOmitComments(true);
		
		//clean the webpage specified
		TagNode pageNode = pageParser.clean(new URL(webpage), "ISO-8859-1");
		//use the xpath provided on it
		Object[] nodes = pageNode.evaluateXPath(sXpath);
		
		return nodes;
	}*/
	
	/*public static ArrayList<String> getDatesArray(Object[] allNode) throws XPatherException{
		
		ArrayList<String> dates = new ArrayList<String>();
		
		for(Object nodeObj: allNode){
			
			Object[] result = ((TagNode)nodeObj).evaluateXPath(IksuSiteHelper.XPATH_IKSU_DATE);
			
			for(Object singleNode : result){
				dates.add(((TagNode)singleNode).getText().toString());
			}
		}
		
		return dates;
	}*/
	
	/*public static ArrayList<String> getActivitiesArray(Object[] allNode, int dayIndex) throws XPatherException{
		
		ArrayList<String> activities = new ArrayList<String>();
		
		for(Object nodeObj: allNode){
			
			Object[] result = ((TagNode)nodeObj).evaluateXPath(
					IksuSiteHelper.XPATH_IKSU_DAY.replace("$", Integer.toString(dayIndex)) + 
					IksuSiteHelper.XPATH_IKSU_ACTIVITY_NAME);
			
			for(Object singleNode : result){
				activities.add(((TagNode)singleNode).getText().toString());
				Log.i("activity", ((TagNode)singleNode).getText().toString());
			}
		}
		
		return activities;
	}*/
}
