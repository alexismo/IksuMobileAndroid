package com.alexismorin.iksu;

import java.util.ArrayList;
import java.util.Date;

import org.w3c.dom.Document;

public class IKSUSchedule{
	public Date lastRefreshed;
	public Document thePage;
	public ArrayList<IKSUActivity> activities = new ArrayList<IKSUActivity>();
	public ArrayList<String> dates;
	public int currentDateIndex;
	
	public IKSUSchedule(){
	}
	
	public int getNumDays(){
		return activities.size();
	}
	
	public IKSUActivity getActivityAtPosition(int pos){		
		return activities.get(pos);
	}
	
	public int getNumActivities(){
		return activities.size();
	}
}
