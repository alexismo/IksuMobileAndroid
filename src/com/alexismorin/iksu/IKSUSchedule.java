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
	
	public boolean isUserLogged = false;
	public boolean areCredentialsValid = false;
	public boolean containsLoggedUserData = false;
	public String username = "";
	public String password = "";
	
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
	
	public boolean hasCredentials(){
		if(!username.equals("") && !password.equals(""))
			return true;
		else
			return false;
	}
}
