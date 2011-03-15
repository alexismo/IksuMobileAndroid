package com.alexismorin.iksu;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.xml.sax.SAXException;

import org.htmlcleaner.*;

import android.util.Log;

public class IksuSiteHelper {
	
	public static final String IKSU_SCHEDULE_URL =
		"http://bokning.iksu.se/index.php?func=list";
	public static final String XPATH_IKSU_DATE = "//td[@class='datarow_bold']";
	public static final String XPATH_IKSU_DAY = "//table[@id='tid_$']";//dolla bill sign is for replacing
	public static final String XPATH_IKSU_ALL = "//td[@id='content']/div/div";
	public static final String XPATH_IKSU_ACTIVITY_TIME = "//td[@class='sort_col2_nl']";
	public static final String XPATH_IKSU_ACTIVITY_NAME = "//td[@class='sort_col3_nl']";
	public static final String XPATH_IKSU_ACTIVITY_ROOM = "//td[@class='sort_col4_nl']";
	public static final String XPATH_IKSU_ACTIVITY_INSTRUCTOR = "//td[@class='sort_col5_nl']";
	
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
	
	public static Object[] performXPathSelectorOnWebPage(String sXpath, String webpage) 
		throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, XPatherException{
		
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
	}
	
	public static ArrayList<String> getDatesArray(Object[] allNode) throws XPatherException{
		
		ArrayList<String> dates = new ArrayList<String>();
		
		for(Object nodeObj: allNode){
			
			Object[] result = ((TagNode)nodeObj).evaluateXPath(IksuSiteHelper.XPATH_IKSU_DATE);
			
			for(Object singleNode : result){
				dates.add(((TagNode)singleNode).getText().toString());
			}
		}
		
		return dates;
	}
	
	public static ArrayList<String> getActivitiesArray(Object[] allNode, int dayIndex) throws XPatherException{
		
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
	}
	
	public static ArrayList<IKSUActivity> getDayArray(Object[] allNode, int dayIndex) throws XPatherException{
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<String> time = new ArrayList<String>();
		ArrayList<String> instructor = new ArrayList<String>();
		ArrayList<String> room = new ArrayList<String>();
		
		ArrayList<IKSUActivity> activitiesList = new ArrayList<IKSUActivity>();
		
		Log.i("Message", "Getting Day "+ Integer.toString(dayIndex) + " for "+ allNode.length +" nodes");
		
		String xpathPrefix = IksuSiteHelper.XPATH_IKSU_DAY.replace("$", Integer.toString(dayIndex));
		
		String[] xpaths = {IksuSiteHelper.XPATH_IKSU_ACTIVITY_NAME,
		IksuSiteHelper.XPATH_IKSU_ACTIVITY_TIME,
		IksuSiteHelper.XPATH_IKSU_ACTIVITY_INSTRUCTOR,
		IksuSiteHelper.XPATH_IKSU_ACTIVITY_ROOM};
		
		names = xPathOnNode(allNode, xpathPrefix + xpaths[0]);
		time = xPathOnNode(allNode, xpathPrefix + xpaths[1]);
		instructor = xPathOnNode(allNode, xpathPrefix + xpaths[2]);
		room= xPathOnNode(allNode, xpathPrefix + xpaths[3]);
		
		if(names.size() == time.size() && time.size() == instructor.size() && instructor.size() == room.size()){
			for(int x = 0; x < names.size(); x++){
				if(names.get(x).length() > 0 && time.get(x).length() > 0){
					//Log.i("IKSUACt",names.get(x));
					activitiesList.add(new IKSUActivity(names.get(x), time.get(x), dayIndex, instructor.get(x), room.get(x)));
				}
			}
		}else{
			Log.e("IKSUActivityError","Activities were improperly parsed.");
		}
		
		return activitiesList;
	}
	
	public static ArrayList<IKSUActivity> getScheduleFromPageForDay(Object[] allNode, int day) throws XPatherException{
		ArrayList<String> scheduleDates = new ArrayList<String>();
		
		if(allNode.length > 0){
			scheduleDates = IksuSiteHelper.getDatesArray(allNode);
			Log.i("Schedule", "Has "+scheduleDates.size()+" days.");
		}
		else
			Log.e("DayError", "Page was improperly parsed");
		/*ArrayList<IKSUActivity> scheduleDays = new ArrayList<IKSUActivity>();
		
		for(int x = 0; x < scheduleDates.size();x++){
			scheduleDays.add(
						new IKSUScheduleDay(scheduleDates.get(x), IksuSiteHelper.getDayArray(allNode, x))
					);
		}
		*/
		
		//return scheduleDays;
		if(scheduleDates.size() > 0)
			return IksuSiteHelper.getDayArray(allNode, day);
		else
			return new ArrayList<IKSUActivity>();
	}
	
	private static ArrayList<String> xPathOnNode(Object[] allNode, String xpath) throws XPatherException{
		ArrayList<String> stringedResult = new ArrayList<String>();
		
		for(Object nodeObj: allNode){
			
			//only get the day you need
			Object[] result = ((TagNode)nodeObj).evaluateXPath(xpath);
			
			for(Object singleNode : result){
				stringedResult.add(((TagNode)singleNode).getText().toString());
			}
		}
		
		return stringedResult;
	}
}
