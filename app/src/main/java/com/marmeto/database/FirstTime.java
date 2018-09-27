package com.marmeto.database;

public class FirstTime {

	private long id;
	private String flag; 

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String username) {
		this.flag = username;
	}
 
	 
	@Override
	public String toString() {
		return flag;
	}

}
