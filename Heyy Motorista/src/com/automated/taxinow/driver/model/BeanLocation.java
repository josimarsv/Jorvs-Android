package com.automated.taxinow.driver.model;

import android.location.Location;

public class BeanLocation {
	private static Location location;

	public static Location getLocation() {
		return location;
	}

	public static void setLocation(Location location) {
		BeanLocation.location = location;
	}

}
