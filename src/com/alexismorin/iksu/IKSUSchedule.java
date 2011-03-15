package com.alexismorin.iksu;

import java.util.ArrayList;
import java.util.Date;

public class IKSUSchedule{
	public Date lastRefreshed;
	public Object[] thePage;
	public ArrayList<IKSUActivity> activities = new ArrayList<IKSUActivity>();
	public ArrayList<String> dates;
	
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
