package com.automated.taxinow.driver.parse;

public interface AsyncTaskCompleteListener {
	void onTaskCompleted(String response, int serviceCode);

}
