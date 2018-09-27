package com.marmeto.database;

public class EmailAddress {

	private long id;
	private String address; 

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getAddress() {
		return address;
	}

	public void setFlag(String address) {
		this.address = address;
	}
 
	 
	@Override
	public String toString() {
		return address;
	}

}
