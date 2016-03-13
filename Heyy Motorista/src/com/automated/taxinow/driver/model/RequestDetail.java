/**
 * 
 */
package com.automated.taxinow.driver.model;

import java.io.Serializable;

/**
 * @author Kishan H Dhamat
 * 
 */
public class RequestDetail implements Serializable {
	private int requestId;
	private int timeLeft;
	private int jobStatus;
	private long startTime;
	private String time, distance, unit, amount, date, total, basePrice,
			distanceCost, timecost, referralBonus, promoBonus;
	private String clientName, clientProfile, clientLatitude, clientLongitude,
			clientPhoneNumber, destinationLatitude, destinationLongitude;

	public String getDestinationLatitude() {
		return destinationLatitude;
	}

	public void setDestinationLatitude(String destinationLatitude) {
		this.destinationLatitude = destinationLatitude;
	}

	public String getDestinationLongitude() {
		return destinationLongitude;
	}

	public void setDestinationLongitude(String destinationLongitude) {
		this.destinationLongitude = destinationLongitude;
	}

	private float clientRating;

	public String getReferralBonus() {
		return referralBonus;
	}

	public void setReferralBonus(String referralBonus) {
		this.referralBonus = referralBonus;
	}

	public String getPromoBonus() {
		return promoBonus;
	}

	public void setPromoBonus(String promoBonus) {
		this.promoBonus = promoBonus;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getBasePrice() {
		return basePrice;
	}

	public void setBasePrice(String basePrice) {
		this.basePrice = basePrice;
	}

	public String getDistanceCost() {
		return distanceCost;
	}

	public void setDistanceCost(String distanceCost) {
		this.distanceCost = distanceCost;
	}

	public String getTimecost() {
		return timecost;
	}

	public void setTimecost(String timecost) {
		this.timecost = timecost;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime
	 *            the startTime to set
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the jobStatus
	 */
	public int getJobStatus() {
		return jobStatus;
	}

	/**
	 * @param jobStatus
	 *            the jobStatus to set
	 */
	public void setJobStatus(int jobStatus) {
		this.jobStatus = jobStatus;
	}

	/**
	 * @return the requestId
	 */
	public int getRequestId() {
		return requestId;
	}

	/**
	 * @return the clientName
	 */
	public String getClientName() {
		return clientName;
	}

	/**
	 * @param clientName
	 *            the clientName to set
	 */
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	/**
	 * @return the clientProfile
	 */
	public String getClientProfile() {
		return clientProfile;
	}

	/**
	 * @param clientProfile
	 *            the clientProfile to set
	 */
	public void setClientProfile(String clientProfile) {
		this.clientProfile = clientProfile;
	}

	/**
	 * @return the clientRating
	 */
	public float getClientRating() {
		return clientRating;
	}

	/**
	 * @param clientRating
	 *            the clientRating to set
	 */
	public void setClientRating(float clientRating) {
		this.clientRating = clientRating;
	}

	/**
	 * @return the clientLatitude
	 */
	public String getClientLatitude() {
		return clientLatitude;
	}

	/**
	 * @param clientLatitude
	 *            the clientLatitude to set
	 */
	public void setClientLatitude(String clientLatitude) {
		this.clientLatitude = clientLatitude;
	}

	/**
	 * @return the clientLongitude
	 */
	public String getClientLongitude() {
		return clientLongitude;
	}

	/**
	 * @param clientLongitude
	 *            the clientLongitude to set
	 */
	public void setClientLongitude(String clientLongitude) {
		this.clientLongitude = clientLongitude;
	}

	/**
	 * @return the clientPhoneNumber
	 */
	public String getClientPhoneNumber() {
		return clientPhoneNumber;
	}

	/**
	 * @param clientPhoneNumber
	 *            the clientPhoneNumber to set
	 */
	public void setClientPhoneNumber(String clientPhoneNumber) {
		this.clientPhoneNumber = clientPhoneNumber;
	}

	/**
	 * @param requestId
	 *            the requestId to set
	 */
	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	/**
	 * @return the timeLeft
	 */
	public int getTimeLeft() {
		return timeLeft;
	}

	/**
	 * @param timeLeft
	 *            the timeLeft to set
	 */
	public void setTimeLeft(int timeLeft) {
		this.timeLeft = timeLeft;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

}
