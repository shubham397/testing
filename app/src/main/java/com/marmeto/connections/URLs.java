package com.marmeto.connections;

/**
 * Store the various URLs that can be called for functions of the T&T
 * 
 * @author Dan
 * 
 */
public class URLs {

	private String task;

	//String localTestURL = "http://10.1.10.98:8443";
	String testURL = "http://54.84.102.16:10444";
	//String localTestURL = "http://54.84.102.16:10445";
	String localTestURL = "http://54.84.102.16:10444";

	private final String emailURLLocalTest = localTestURL +"/email";
	private final String sendReportURLLocalTest =localTestURL +"/shipcases/add";
	private final String settingsURLLocalTest = localTestURL +"/settings";
	private final String getShipmentURLLocalTest = localTestURL +"/shipments";
	private final String updateShipmentURLLocalTest =localTestURL +"/shipments/add";
	private final String getEmailsURLLocalTest = localTestURL +"/email";
 
	private final String emailURLTest = testURL+"/email";
	private final String sendReportURLTest = testURL+"/shipcases/add";
	private final String settingsURLTest = testURL+"/settings";
	private final String getShipmentURLTest = testURL+"/shipments";
	private final String updateShipmentURLTest = testURL+"/shipments/add";
	private final String getEmailsURLTest = testURL+"/email";

	private final String emailURLProd = "http://54.84.102.16:10444/email";
	private final String sendReportURLProd = "http://54.84.102.16:10444/shipcases/add";
	private final String settingsURLProd = "http://54.84.102.16:10444/settings";
	private final String getShipmentURLProd = "http://54.84.102.16:10444/shipments";
	private final String updateShipmentURLProd = "http://54.84.102.16:10444/shipments/add";
	private final String getEmailsURLProd = "http://54.84.102.16:10444/email";

	/**
	 * Create a URL for the given task
	 */
	public URLs(String task) {
		this.task = task;
	}

	/**
	 * Return the proper url given the task.
	 * 
	 * @return
	 */
	public String getURL(String version) {
		String URL = "";
		String[] split = version.split(" ");
		String environment = "";
		if (split.length > 1) {
			environment = split[1];
		}

		if (task.equals("updateEmail")) {
			if (environment.equals("UAT")) {
				URL = emailURLTest;
			} else {
				if (environment.equals("PROD")) {
					URL = emailURLProd;
				}else{
					URL = emailURLLocalTest;
				}
			}
			return URL;
		}

		if (task.equals("getEmails")) {
			if (environment.equals("UAT")) {
				URL = getEmailsURLTest;
			} else {
				if (environment.equals("PROD")) {
					URL = getEmailsURLProd;
				}else{
					URL = getEmailsURLLocalTest;
				}
			}
			return URL;
		}

		if (task.equals("sendReport")) {
			if (environment.equals("UAT")) {
				URL = sendReportURLTest;
			} else {
				if (environment.equals("PROD")) {
					URL = sendReportURLProd;
				}else{
					URL = sendReportURLLocalTest;
				}
			}
			return URL;
		}

		if (task.equals("updateSettings")) {
			if (environment.equals("UAT")) {
				URL = settingsURLTest;
			} else {
				if (environment.equals("PROD")) {
					URL = settingsURLProd;
				}else{
					URL = settingsURLTest;
				}
			}
			return URL;
		}

		if (task.equals("getShipment")) {
			if (environment.equals("UAT")) {
				URL = getShipmentURLTest;
			} else {
				if (environment.equals("PROD")) {
					URL = getShipmentURLProd;
				}else{
					URL = getShipmentURLLocalTest;
				}
			}
			return URL;
		}
		if (task.equals("sendRxCReport")) {
			if (environment.equals("UAT")) {
				URL = updateShipmentURLTest;
			} else {
				if (environment.equals("PROD")) {
					URL = updateShipmentURLProd;
				}else{
					URL = updateShipmentURLLocalTest;
				}

			}
			return URL;
		}
		return URL;

	}

}
