package com.marmeto.database;

import java.util.List;

public class Airbills {

	 
	private long id;
	private String tntLabels;
	private String dhlLabel;
	private String timestamp;
	private String destination;
	private String comments;
	private String task;
	
	 
	
	public long getId() {
		return id;
	} 
	
	public void setId(long id) {
		this.id = id;
	}

	public String getTNTLabels() {
		return tntLabels;
	}
	
	public String getTask() {
		return task;
	}
	
	public String getTimestamp() {
		return timestamp;
	}

	public void setTNTLabels(String tntLabels) {
		this.tntLabels = tntLabels;
	}
	
	public void setTask(String task) {
		this.task = task;
	}

	public String getDHLabel() {
		return dhlLabel;
	}

	public void setMPACodes(String dhlLabel) {
		this.dhlLabel = dhlLabel;
	}
	
	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}
	
	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return dhlLabel;
	}
 

}
