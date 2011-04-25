package com.alexismorin.iksu;

public class IKSUActivity {
	public String name;
	public String time;
	public int dateIndex;
	public String instructor;
	public String room;
	public String filterCode;
	
	public IKSUActivity(String newName, String newTime, int newDateIndex, String newInstructor, String newRoom, String newFilter){
		name = newName;
		time = newTime;
		dateIndex = newDateIndex;
		instructor = newInstructor;
		room = newRoom;
		filterCode = newFilter;
	}
}
