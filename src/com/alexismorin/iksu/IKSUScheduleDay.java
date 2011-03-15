package com.alexismorin.iksu;

import java.util.ArrayList;

public class IKSUScheduleDay {
	public String day;
	public ArrayList<IKSUActivity> activities;

	public IKSUScheduleDay(String day, ArrayList<IKSUActivity> newActivities){
		activities = newActivities;
	}
}