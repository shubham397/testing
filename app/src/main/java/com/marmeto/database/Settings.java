package com.marmeto.database;

import android.app.Application;
import android.util.Log;

public class Settings extends Application{

	String TAG = "Settings";
	private long id;
	private String caseLimit;
	private String locations;
	private String version;
	private String PCI_ADMIN;
	private String RXC_Admin;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCaseLimit() {
		return caseLimit;
	}

	public void setCaseLimit(String caseLimit) {
		this.caseLimit = caseLimit;
	}
	
	public String getPCIAdminPass() {
		return PCI_ADMIN;
	}

	public void setPCIAdminPass(String PCI_ADMIN) {
		this.PCI_ADMIN = PCI_ADMIN;
	}
	
	public String getRxCAdminPass() {
		return RXC_Admin;
	}

	public void setRxCAdminPass(String RXC_Admin) {
		this.RXC_Admin = RXC_Admin;
	}
	
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		Log.d(TAG, "Setting Version: "+version);
		this.version = version;
	}

	public String getLocations() {
		return locations;
	}

	public void setLocations(String locations) {
		this.locations = locations;
	}
 

}
