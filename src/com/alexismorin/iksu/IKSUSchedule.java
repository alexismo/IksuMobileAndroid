package com.alexismorin.iksu;

import java.util.ArrayList;
import java.util.Date;

import org.w3c.dom.Document;

public class IKSUSchedule{
	public Date lastRefreshed;
	public Document thePage;
	public String typeFilter;
	public ArrayList<IKSUActivity> activities = new ArrayList<IKSUActivity>();
	public ArrayList<String> dates = new ArrayList<String>();
	public int currentDateIndex;
	public int activityFilterIndex = 0;
	
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
