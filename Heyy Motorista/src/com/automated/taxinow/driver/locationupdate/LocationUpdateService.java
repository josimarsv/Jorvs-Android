package com.automated.taxinow.driver.locationupdate;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import com.automated.taxinow.driver.MapActivity;
import com.automated.taxinow.driver.MyReceiver;
import com.automated.taxinow.driver.R;
import com.automated.taxinow.driver.SettingActivity;
import com.automated.taxinow.driver.locationupdate.LocationHelper.OnLocationReceived;
import com.automated.taxinow.driver.utills.AndyConstants;
import com.automated.taxinow.driver.utills.AndyUtils;
import com.automated.taxinow.driver.utills.AppLog;
import com.automated.taxinow.driver.utills.PreferenceHelper;
import com.google.android.gms.maps.model.LatLng;

public class LocationUpdateService extends IntentService implements
		OnLocationReceived {
	private PreferenceHelper preferenceHelper;
	private LocationHelper locationHelper;
	private String id, token, latitude, longitude;
	private static Timer timer;
	private LatLng latlngPrevious, latlngCurrent;
	private static boolean isNoRequest;
	private float bearing;

	public LocationUpdateService() {
		this("MySendLocationService");
	}

	public LocationUpdateService(String name) {
		super("MySendLocationService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		locationHelper = new LocationHelper(this);
		locationHelper.setLocationReceivedLister(this);
		locationHelper.onStart();
		preferenceHelper = new PreferenceHelper(getApplicationContext());
		id = preferenceHelper.getUserId();
		token = preferenceHelper.getSessionToken();
		// if (driverId.equals("")) {
		// driverId = getDriverID();
		// }
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();

	}

	{

		if (timer == null) {
			timer = new Timer();
			timer.scheduleAtFixedRate(new TimerRequestStatus(),
					AndyConstants.DELAY_OFFLINE,
					AndyConstants.TIME_SCHEDULE_OFFLINE);

		}

	}

	private class TimerRequestStatus extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			if (latlngCurrent != null && latlngPrevious != null) {

				Location locationCurrent = new Location("");
				Location locationPrevious = new Location("");
				locationCurrent.setLatitude(latlngCurrent.latitude);
				locationCurrent.setLongitude(latlngCurrent.longitude);
				locationPrevious.setLatitude(latlngPrevious.latitude);
				locationPrevious.setLongitude(latlngPrevious.longitude);
				if (locationCurrent.distanceTo(locationPrevious) <= 20) {
					latlngPrevious = new LatLng(latlngCurrent.latitude,
							latlngCurrent.longitude);
					AppLog.Log("Check isActive State",
							"" + preferenceHelper.getIsActive());
					if (isNoRequest && preferenceHelper.getIsActive()) {
						generateNotification();

					}

				}
			}

		}

	}

	public void generateNotification() {
		Intent offlineIntent = new Intent(this, MyReceiver.class);
		offlineIntent.setAction("Go Offline");

		Intent cancelIntent = new Intent(this, MyReceiver.class);
		cancelIntent.setAction("Go Offline");

		Intent mainIntent = new Intent(this, SettingActivity.class);

		PendingIntent mainPIntent = PendingIntent.getActivity(this, 0,
				mainIntent, 0);

		PendingIntent pIntent = PendingIntent.getBroadcast(this, 0,
				offlineIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent pCancelIntent = PendingIntent.getBroadcast(this, 0,
				cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		Notification noti = new Notification.Builder(this)
				.setContentTitle(getResources().getString(R.string.app_name))
				.setContentText("Do you want to go offline?")
				.setSmallIcon(R.drawable.ic_launcher).setAutoCancel(true)
				.setContentIntent(mainPIntent)
				.addAction(0, "Go Offline", pIntent)
				.addAction(0, "Cancel", pCancelIntent).build();

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		noti.flags |= Notification.FLAG_AUTO_CANCEL;

		notificationManager.notify(0, noti);

	}

	@Override
	public void onDestroy() {
		if (locationHelper != null) {
			locationHelper.onStop();
		}
		super.onDestroy();
	}

	@Override
	public void onLocationReceived(LatLng latlong) {

	}

	private class UploadDataToServer extends AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				HttpParams myParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(myParams, 100000);
				HttpConnectionParams.setSoTimeout(myParams, 100000);
				HttpClient httpClient = new DefaultHttpClient(myParams);
				ResponseHandler<String> res = new BasicResponseHandler();
				HttpPost postMethod = new HttpPost(
						AndyConstants.ServiceType.UPDATE_PROVIDER_LOCATION);
				// HttpRequest httpRequest = new HttpRequest();
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.ID, id));
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.TOKEN, token));
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.LATITUDE, latitude));
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.LONGITUDE, longitude));
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.BEARING, bearing + ""));
				preferenceHelper.putLatitude(Double.parseDouble(latitude));
				preferenceHelper.putLongitude(Double.parseDouble(longitude));

				postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				String response = httpClient.execute(postMethod, res);
				// String response = httpRequest.postData(
				// AndyConstants.ServiceType.UPDATE_PROVIDER_LOCATION,
				// nameValuePairs);
				AppLog.Log("TAG", "location send Response:::" + response);

				JSONObject jsonObject = new JSONObject(response);
				if (jsonObject.getBoolean("success")) {
					if (jsonObject.getString("is_active").equals("1"))
						preferenceHelper.putIsActive(true);
					else
						preferenceHelper.putIsActive(false);
				}

				return response;
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			stopSelf();
		}
	}

	private class UploadTripLocationData extends
			AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				HttpParams myParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(myParams, 100000);
				HttpConnectionParams.setSoTimeout(myParams, 100000);
				HttpClient httpClient = new DefaultHttpClient(myParams);
				ResponseHandler<String> res = new BasicResponseHandler();
				HttpPost postMethod = new HttpPost(
						AndyConstants.ServiceType.REQUEST_LOCATION_UPDATE);
				// HttpRequest httpRequest = new HttpRequest();
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.ID, id));
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.TOKEN, token));
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.LATITUDE, latitude));
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.LONGITUDE, longitude));
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.BEARING, bearing + ""));

				// nameValuePairs.add(new BasicNameValuePair(
				// AndyConstants.Params.DISTANCE, 0 + ""));
				nameValuePairs.add(new BasicNameValuePair(
						AndyConstants.Params.REQUEST_ID, preferenceHelper
								.getRequestId() + ""));
				double dist;
				AppLog.Log("id", id);
				AppLog.Log("token", token);
				AppLog.Log("latitude", latitude);
				AppLog.Log("longitude", longitude);
				//AppLog.Log("BEARING", bearing+"");
				AppLog.Log("request_id", "" + preferenceHelper.getRequestId()+"");

				dist = AndyUtils.distance(preferenceHelper.getLatitude(),
						preferenceHelper.getLongitude(),
						Double.parseDouble(latitude),
						Double.parseDouble(longitude), 'K');

				AppLog.Log("50 meter vadu distance", "" + dist);

				postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				String response = httpClient.execute(postMethod, res);
				// String response = httpRequest.postData(
				// AndyConstants.ServiceType.UPDATE_PROVIDER_LOCATION,
				// nameValuePairs);
				AppLog.Log("TAG", "request location send Response:::"
						+ response);
				JSONObject jsonObject = new JSONObject(response);
				if (jsonObject.getBoolean("success")) {
					preferenceHelper.putDistance(Float.parseFloat(jsonObject
							.getString(AndyConstants.Params.DISTANCE)));

					preferenceHelper.putUnit(jsonObject
							.getString(AndyConstants.Params.UNIT));

					preferenceHelper
							.putDestinationLatitude(jsonObject
									.getString(AndyConstants.Params.DESTINATION_LATITUDE));
					preferenceHelper
							.putDestinationLongitude(jsonObject
									.getString(AndyConstants.Params.DESTINATION_LONGITUDE));

				} else {
					preferenceHelper.putUnit(jsonObject
							.getString(AndyConstants.Params.UNIT));

					preferenceHelper
							.putDestinationLatitude(jsonObject
									.getString(AndyConstants.Params.DESTINATION_LATITUDE));
					preferenceHelper
							.putDestinationLongitude(jsonObject
									.getString(AndyConstants.Params.DESTINATION_LONGITUDE));
				}
				if (!jsonObject.getBoolean("success"))
					if (jsonObject.getInt("is_cancelled") == 1) {
						preferenceHelper.clearRequestData();
						Intent i = new Intent(LocationUpdateService.this,
								MapActivity.class);
						i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(i);
					}

				return response;
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			stopSelf();
			// Change by hardik bhalodi
			locationHelper.onStop();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.automated.taxinow.driver.locationupdate.LocationHelper.OnLocationReceived
	 * #onLocationReceived(android.location.Location)
	 */
	@Override
	public void onLocationReceived(Location location) {
		// TODO Auto-generated method stub
		LatLng latlong = locationHelper.getLatLng(location);
		AppLog.Log("TAG", "onLocationReceived Lat : " + latlong.latitude
				+ " , long : " + latlong.longitude);
		if (latlong != null) {
			preferenceHelper
					.putWalkerLatitude(String.valueOf(latlong.latitude));
			preferenceHelper.putWalkerLongitude(String
					.valueOf(latlong.longitude));
			latlngCurrent = new LatLng(latlong.latitude, latlong.longitude);
			if (latlngPrevious == null) {
				latlngPrevious = new LatLng(latlong.latitude, latlong.longitude);
			}
		}
		if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(token)
				&& latlong != null) {

			latitude = String.valueOf(latlong.latitude);
			longitude = String.valueOf(latlong.longitude);
			bearing = location.getBearing();
			if (!AndyUtils.isNetworkAvailable(getApplicationContext())) {
				// AndyUtils.showToast(
				// getResources().getString(R.string.toast_no_internet),
				// getApplicationContext());
				return;
			}

			if (preferenceHelper.getRequestId() == AndyConstants.NO_REQUEST) {

				isNoRequest = true;
				new UploadDataToServer().execute();
			} else {
				isNoRequest = false;
				new UploadTripLocationData().execute();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.automated.taxinow.driver.locationupdate.LocationHelper.OnLocationReceived
	 * #onConntected(android.os.Bundle)
	 */
	@Override
	public void onConntected(Bundle bundle) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.automated.taxinow.driver.locationupdate.LocationHelper.OnLocationReceived
	 * #onConntected(android.location.Location)
	 */
	@Override
	public void onConntected(Location location) {
		// TODO Auto-generated method stub

	}
}
