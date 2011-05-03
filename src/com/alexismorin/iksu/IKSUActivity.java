package com.alexismorin.iksu;

public class IKSUActivity {
	public String name;
	public String time;
	public int dateIndex;
	public String instructor;
	public String room;
	public String filterCode;
	
	//when logged
	public String spaces;
	public String booklink;
	public String reservationId;
	
	
	public IKSUActivity(String newName, String newTime, int newDateIndex, String newInstructor, String newRoom, String newFilter){
		name = newName;
		time = newTime;
		dateIndex = newDateIndex;
		instructor = newInstructor;
		room = newRoom;
		filterCode = newFilter;
	}
	
	public IKSUActivity(String newName, 
			String newTime, 
			int newDateIndex, 
			String newInstructor, 
			String newRoom, 
			String newFilter,
			String newSpaces,
			String newBooklink,
			String newReservationId){
		name = newName;
		time = newTime;
		dateIndex = newDateIndex;
		instructor = newInstructor;
		room = newRoom;
		filterCode = newFilter;
		spaces = newSpaces;
		booklink = newBooklink;
		reservationId = newReservationId;
	}
}
