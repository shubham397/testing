package com.marmeto.global;

import java.util.LinkedList;

public class AssignSettings {
	
	public LinkedList<String> destinations = getDestinations();
	public int shippingcaseLimit = getLimit();
	
	
	public void applySettings() {
		//check if there is a network connection
		
		//if connected, pull in settings
		//get shipping case limit
		
	}
	
	public LinkedList<String> getDestinations(){
		 LinkedList<String> result = new  LinkedList<String>();
		return result;
	}
	
	public int getLimit(){
		int result = 50;
		
		return result;
	}

}
