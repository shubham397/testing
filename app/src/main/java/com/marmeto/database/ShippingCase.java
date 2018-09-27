package com.marmeto.database;

import java.util.List;

public class ShippingCase {

	 
	private long id;
	private String tntLabel;
	private String mpaCodes;
	private String timestamp;
	private String task;
	
	 
	
	public long getId() {
		return id;
	} 
	
	public void setId(long id) {
		this.id = id;
	}

	public String getTNTLabel() {
		return tntLabel;
	}
	
	public String getTask() {
		return task;
	}
	
	public String getTimestamp() {
		return timestamp;
	}

	public void setTNTLabel(String tntLabel) {
		this.tntLabel = tntLabel;
	}
	
	public void setTask(String task) {
		this.task = task;
	}

	public String getMPACodes() {
		return mpaCodes;
	}

	public void setMPACodes(String mpaCodes) {
		this.mpaCodes = mpaCodes;
	}

	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return tntLabel;
	}
 

}
