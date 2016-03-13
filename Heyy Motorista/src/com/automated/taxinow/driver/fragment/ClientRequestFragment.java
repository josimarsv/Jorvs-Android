package com.automated.taxinow.driver.fragment;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.androidquery.AQuery;
import com.automated.taxinow.driver.R;
import com.automated.taxinow.driver.base.BaseMapFragment;
import com.automated.taxinow.driver.locationupdate.LocationHelper;
import com.automated.taxinow.driver.locationupdate.LocationHelper.OnLocationReceived;
import com.automated.taxinow.driver.model.RequestDetail;
import com.automated.taxinow.driver.parse.AsyncTaskCompleteListener;
import com.automated.taxinow.driver.parse.VolleyHttpRequest;
import com.automated.taxinow.driver.utills.AndyConstants;
import com.automated.taxinow.driver.utills.AndyUtils;
import com.automated.taxinow.driver.utills.AppLog;
import com.automated.taxinow.driver.widget.MyFontButton;
import com.automated.taxinow.driver.widget.MyFontTextView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * @author Kishan H Dhamat
 * 
 */

public class ClientRequestFragment extends BaseMapFragment implements
		AsyncTaskCompleteListener, OnLocationReceived {
	private GoogleMap mMap;
	private final String TAG = "ClientRequestFragment";
	private LinearLayout llAcceptReject;
	private View llUserDetailView;
	// private ProgressBar pbTimeLeft;
	private MyFontButton btnClientAccept, btnClientReject,
			btnClientReqRemainTime;
	// private RelativeLayout rlTimeLeft;
	private boolean isContinueRequest, isAccepted = false;
	private  Timer timer;
	private static SeekbarTimer seekbarTimer;
	private RequestDetail requestDetail;
	private Marker markerDriverLocation, markerClientLocation;

	private Location location;
	private LocationHelper locationHelper;
	private MyFontTextView tvClientName;// , tvClientPhoneNumber;
	private RatingBar tvClientRating;
	private ImageView ivClientProfilePicture;
	private AQuery aQuery;
	private newRequestReciever requestReciever;
	private View clientRequestView;
	private MapView mMapView;
	private Bundle mBundle;
	private MyFontTextView tvApprovedClose;
	private boolean isApprovedCheck = true, loaded = false;
	private Dialog mDialog;
	private SoundPool soundPool;
	private int soundid;
	private RequestQueue requestQueue;

	private Button btnGoOffline;
	private RelativeLayout relMap;
	private LinearLayout linearOffline;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		clientRequestView = inflater.inflate(R.layout.fragment_client_requests,
				container, false);
		try {
			MapsInitializer.initialize(getActivity());
		} catch (Exception e) {
		}

		llAcceptReject = (LinearLayout) clientRequestView
				.findViewById(R.id.llAcceptReject);
		llUserDetailView = (View) clientRequestView
				.findViewById(R.id.clientDetailView);
		btnClientAccept = (MyFontButton) clientRequestView
				.findViewById(R.id.btnClientAccept);
		btnClientReject = (MyFontButton) clientRequestView
				.findViewById(R.id.btnClientReject);
		linearOffline = (LinearLayout) clientRequestView
				.findViewById(R.id.linearOffline);
		// pbTimeLeft = (ProgressBar) clientRequestView
		// .findViewById(R.id.pbClientReqTime);
		// rlTimeLeft = (RelativeLayout) clientRequestView
		// .findViewById(R.id.rlClientReqTimeLeft);
		btnClientReqRemainTime = (MyFontButton) clientRequestView
				.findViewById(R.id.btnClientReqRemainTime);
		tvClientName = (MyFontTextView) clientRequestView
				.findViewById(R.id.tvClientName);
		// tvClientPhoneNumber = (MyFontTextView) clientRequestView
		// .findViewById(R.id.tvClientNumber);

		tvClientRating = (RatingBar) clientRequestView
				.findViewById(R.id.tvClientRating);

		ivClientProfilePicture = (ImageView) clientRequestView
				.findViewById(R.id.ivClientImage);

		btnClientAccept.setOnClickListener(this);
		btnClientReject.setOnClickListener(this);
		clientRequestView.findViewById(R.id.btnMyLocation).setOnClickListener(
				this);
		btnGoOffline = (Button) clientRequestView.findViewById(R.id.btnOffline);
		relMap = (RelativeLayout) clientRequestView.findViewById(R.id.relMap);
		linearOffline.setVisibility(View.GONE);
		relMap.setVisibility(View.VISIBLE);
		btnGoOffline.setOnClickListener(this);
		btnGoOffline.setSelected(true);
		return clientRequestView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		aQuery = new AQuery(mapActivity);
		mMapView = (MapView) clientRequestView.findViewById(R.id.clientReqMap);
		mMapView.onCreate(mBundle);

		setUpMap();
		locationHelper = new LocationHelper(getActivity());
		locationHelper.setLocationReceivedLister(this);
		locationHelper.onStart();
		checkState();

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBundle = savedInstanceState;
		requestQueue = Volley.newRequestQueue(mapActivity);
		IntentFilter filter = new IntentFilter(AndyConstants.NEW_REQUEST);
		requestReciever = new newRequestReciever();
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
				requestReciever, filter);
		soundPool = new SoundPool(5, AudioManager.STREAM_ALARM, 100);
		soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId,
					int status) {
				loaded = true;
			}
		});
		soundid = soundPool.load(mapActivity, R.raw.beep, 1);
	}

	private void addMarker() {
		if (mMap == null) {
			setUpMap();
			return;
		}

	}

	public void showLocationOffDialog() {

		AlertDialog.Builder gpsBuilder = new AlertDialog.Builder(mapActivity);
		gpsBuilder.setCancelable(false);
		gpsBuilder
				.setTitle(getString(R.string.dialog_no_location_service_title))
				.setMessage(getString(R.string.dialog_no_location_service))
				.setPositiveButton(
						getString(R.string.dialog_enable_location_service),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// continue with delete
								dialog.dismiss();
								Intent viewIntent = new Intent(
										Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivity(viewIntent);

							}
						})

				.setNegativeButton(getString(R.string.dialog_exit),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// do nothing
								dialog.dismiss();
								mapActivity.finish();
							}
						});
		gpsBuilder.create();
		gpsBuilder.show();
	}

	private void setUpMap() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			mMap = ((MapView) clientRequestView.findViewById(R.id.clientReqMap))
					.getMap();
			mMap.getUiSettings().setZoomControlsEnabled(false);
			mMap.setMyLocationEnabled(false);
			mMap.getUiSettings().setMyLocationButtonEnabled(false);

			mMap.setInfoWindowAdapter(new InfoWindowAdapter() {
				@Override
				public View getInfoWindow(Marker marker) {
					View v = mapActivity.getLayoutInflater().inflate(
							R.layout.info_window_layout, null);

					((TextView) v).setText(marker.getTitle());
					return v;
				}

				@Override
				public View getInfoContents(Marker marker) {

					// Getting view from the layout file info_window_layout View

					// Getting reference to the TextView to set title TextView

					// Returning the view containing InfoWindow contents return
					return null;

				}

			});

			mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
				@Override
				public boolean onMarkerClick(Marker marker) {
					marker.showInfoWindow();
					return true;
				}
			});
			addMarker();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnClientAccept:
			mapActivity.clearAll();
			isAccepted = true;
			cancelSeekbarTimer();
			respondRequest(1);
			
			break;
		case R.id.btnClientReject:
			mapActivity.clearAll();
			isAccepted = false;
			cancelSeekbarTimer();
			respondRequest(0);
			soundPool.stop(soundid);
			break;
		case R.id.btnMyLocation:
			// Location loc = mMap.getMyLocation();
			// if (loc != null) {
			// LatLng latLang = new LatLng(loc.getLatitude(),
			// loc.getLongitude());
			// mMap.animateCamera(CameraUpdateFactory.newLatLng(latLang));
			// }
			LatLng latLng = new LatLng(location.getLatitude(),
					location.getLongitude());
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
					latLng, 18);
			mMap.animateCamera(cameraUpdate);
			break;
		case R.id.btnOffline:
			mapActivity.clearAll();
			changeState();
			break;
		default:
			break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
		if (btnGoOffline.isSelected()) {
			if (preferenceHelper.getRequestId() == AndyConstants.NO_REQUEST) {
				startCheckingUpcomingRequests();
			}
		}
		mapActivity.setActionBarTitle(getString(R.string.app_name));

	}

	@Override
	public void onPause() {
		super.onPause();
		if (preferenceHelper.getRequestId() == AndyConstants.NO_REQUEST) {
			stopCheckingUpcomingRequests();
		}
		mMapView.onPause();
	}

	@Override
	public void onDestroy() {
		mMapView.onDestroy();
		super.onDestroy();
		stopCheckingUpcomingRequests();
		cancelSeekbarTimer();
		AndyUtils.removeCustomProgressDialog();
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
				requestReciever);

	}

	// public void openApprovedDialog() {
	// mDialog = new Dialog(mapActivity,
	// android.R.style.Theme_Translucent_NoTitleBar);
	// mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	//
	// mDialog.getWindow().setBackgroundDrawable(
	// new ColorDrawable(android.graphics.Color.TRANSPARENT));
	// mDialog.setContentView(R.layout.provider_approve_dialog);
	// mDialog.setCancelable(false);
	// tvApprovedClose = (MyFontTextView) mDialog
	// .findViewById(R.id.tvApprovedClose);
	// tvApprovedClose.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// // TODO Auto-generated method stub
	// mDialog.dismiss();
	// mapActivity.finish();
	// }
	// });
	// mDialog.show();
	// }

	@Override
	public void onTaskCompleted(String response, int serviceCode) {

		switch (serviceCode) {
		case AndyConstants.ServiceCode.GET_ALL_REQUEST:
			AndyUtils.removeCustomProgressDialog();
			AppLog.Log(TAG, "getAllRequests Response :" + response);
			if (!parseContent.parseIsApproved(response)) {
				if (isApprovedCheck) {
					mapActivity.openApprovedDialog();
					isApprovedCheck = false;
					return;
				}
			} else if (mDialog != null && mDialog.isShowing()) {
				mDialog.dismiss();
				isApprovedCheck = true;
			}
			if (!parseContent.isSuccess(response)) {
				return;
			}
			requestDetail = parseContent.parseAllRequests(response);
			if (requestDetail != null && mMap != null) {
				try {
					stopCheckingUpcomingRequests();
					// startTimerForRespondRequest(requestDetail.getTimeLeft());
					setComponentVisible();

					// pbTimeLeft.setMax(requestDetail.getTimeLeft());
					btnClientReqRemainTime.setText(""
							+ requestDetail.getTimeLeft());
					// pbTimeLeft.setProgress(requestDetail.getTimeLeft());
					tvClientName.setText(requestDetail.getClientName());
					// tvClientPhoneNumber.setText(requestDetail
					// .getClientPhoneNumber());
					if (requestDetail.getClientRating() != 0) {
						tvClientRating.setRating(requestDetail
								.getClientRating());
						Log.i("Rating", "" + requestDetail.getClientRating());
					}
					if (TextUtils.isEmpty(requestDetail.getClientProfile())) {
						aQuery.id(ivClientProfilePicture).progress(R.id.pBar)
								.image(R.drawable.user);
					} else {
						aQuery.id(ivClientProfilePicture).progress(R.id.pBar)
								.image(requestDetail.getClientProfile());
					}

				/*	markerClientLocation = null;
					markerClientLocation = mMap.addMarker(new MarkerOptions()
							.position(
									new LatLng(Double.parseDouble(requestDetail
											.getClientLatitude()), Double
											.parseDouble(requestDetail
													.getClientLongitude())))
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.pin_client))
							.title(mapActivity.getResources().getString(
									R.string.client_location)));*/
					
					if (markerClientLocation == null) {
						markerClientLocation = mMap
								.addMarker(new MarkerOptions()
										.position(
												new LatLng(
														Double.parseDouble(requestDetail
																.getClientLatitude()),
														Double.parseDouble(requestDetail
																.getClientLongitude())))
										.icon(BitmapDescriptorFactory
												.fromResource(R.drawable.pin_client))
										.title(mapActivity
												.getResources()
												.getString(
														R.string.client_location)));
					} else {
						markerClientLocation.setPosition(new LatLng(
								Double.parseDouble(requestDetail
										.getClientLatitude()), Double
										.parseDouble(requestDetail
												.getClientLongitude())));
					}
					
					
					if (seekbarTimer == null) {
						seekbarTimer = new SeekbarTimer(
								requestDetail.getTimeLeft() * 1000, 1000);
						seekbarTimer.start();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;
		case AndyConstants.ServiceCode.CHECK_STATE:
		case AndyConstants.ServiceCode.TOGGLE_STATE:
			AndyUtils.removeCustomProgressDialog();
			preferenceHelper.putIsActive(false);
			preferenceHelper.putDriverOffline(false);
			if (!parseContent.isSuccess(response)) {
				return;
			}
			AppLog.Log("TAG", "toggle state:" + response);
			if (parseContent.parseAvaibilty(response)) {
				updateButtonUi(true);
				if (preferenceHelper.getRequestId() == AndyConstants.NO_REQUEST) {
					startCheckingUpcomingRequests();
				}

			} else {
				stopCheckingUpcomingRequests();
				updateButtonUi(false);
			}

			break;
		case AndyConstants.ServiceCode.RESPOND_REQUEST:
			AppLog.Log(TAG, "respond Request Response :" + response);
			removeNotification();
			AndyUtils.removeCustomProgressDialog();
			if (parseContent.isSuccess(response)) {
				if (isAccepted) {
					preferenceHelper.putRequestId(requestDetail.getRequestId());
					JobFragment jobFragment = new JobFragment();
					Bundle bundle = new Bundle();
					bundle.putInt(AndyConstants.JOB_STATUS,
							AndyConstants.IS_WALKER_STARTED);
					bundle.putSerializable(AndyConstants.REQUEST_DETAIL,
							requestDetail);
					jobFragment.setArguments(bundle);
					if (this.isVisible())
						mapActivity.addFragment(jobFragment, false,
								AndyConstants.JOB_FRGAMENT_TAG, true);
					
				} else {
					//cancelSeekbarTimer();
					setComponentInvisible();
					if (markerClientLocation != null
							&& markerClientLocation.isVisible()) {
						markerClientLocation.remove();
						markerClientLocation=null;
					}
					preferenceHelper.putRequestId(AndyConstants.NO_REQUEST);
					startCheckingUpcomingRequests();
				}
				cancelSeekbarTimer();
			}

			// else {
			// AndyUtils.showToast(
			// mapActivity.getResources().getString(
			// R.string.toast_unable_respond_request),
			// mapActivity);
			// }
			break;

		default:
			break;

		}
	}

	private class SeekbarTimer extends CountDownTimer {

		public SeekbarTimer(long startTime, long interval) {
			super(startTime, interval);
			// pbTimeLeft.setProgressDrawable(getResources().getDrawable(
			// R.drawable.customprogress));
			/*soundPool = new SoundPool(5, AudioManager.STREAM_ALARM, 100);
			soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
				@Override
				public void onLoadComplete(SoundPool soundPool, int sampleId,
						int status) {
					loaded = true;
				}
			});
			soundid = soundPool.load(mapActivity, R.raw.beep, 1);*/
		}

		@Override
		public void onFinish() {
			if (seekbarTimer == null) {
				return;
			}
			AndyUtils.showToast(
					mapActivity.getResources().getString(
							R.string.toast_time_over), mapActivity);
			setComponentInvisible();
			preferenceHelper.clearRequestData();
			if (markerClientLocation != null
					&& markerClientLocation.isVisible()) {
				markerClientLocation.remove();
				markerClientLocation=null;
			}
			removeNotification();
			startCheckingUpcomingRequests();
			this.cancel();
			seekbarTimer = null;

		}

		@Override
		public void onTick(long millisUntilFinished) {
			int time = (int) (millisUntilFinished / 1000);

			if (!isVisible()) {
				return;
			}
			if (preferenceHelper.getSoundAvailability()) {
				if (time <= 15) {
					AppLog.Log("ClientRequest Timer Beep", "Beep started");
					if (loaded) {
						soundPool.play(soundid, 1, 1, 0, 0, 1);
					}

				}
			}

			btnClientReqRemainTime.setText("" + time);
			// pbTimeLeft.setProgress(time);
			// if (time <= 5) {
			// pbTimeLeft.setProgressDrawable(getResources().getDrawable(
			// R.drawable.customprogressred));
			// }

		}
	}

	// if status = 1 then accept if 0 then reject
	private void respondRequest(int status) {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}

		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_respond_request), false);

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.RESPOND_REQUESTS);
		map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
		map.put(AndyConstants.Params.REQUEST_ID,
				String.valueOf(requestDetail.getRequestId()));
		map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());
		map.put(AndyConstants.Params.ACCEPTED, String.valueOf(status));
		// new HttpRequester(mapActivity, map,
		// AndyConstants.ServiceCode.RESPOND_REQUEST, this);

		requestQueue.add(new VolleyHttpRequest(Method.POST, map,
				AndyConstants.ServiceCode.RESPOND_REQUEST, this, this));
	}

	public void checkRequestStatus() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}
		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_dialog_request), false);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL,
				AndyConstants.ServiceType.CHECK_REQUEST_STATUS
						+ AndyConstants.Params.ID + "="
						+ preferenceHelper.getUserId() + "&"
						+ AndyConstants.Params.TOKEN + "="
						+ preferenceHelper.getSessionToken() + "&"
						+ AndyConstants.Params.REQUEST_ID + "="
						+ preferenceHelper.getRequestId());
		// new HttpRequester(mapActivity, map,
		// AndyConstants.ServiceCode.CHECK_REQUEST_STATUS, true, this);

		requestQueue.add(new VolleyHttpRequest(Method.POST, map,
				AndyConstants.ServiceCode.CHECK_REQUEST_STATUS, this, this));
	}

	public void getAllRequests() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			return;
		}

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL,
				AndyConstants.ServiceType.GET_ALL_REQUESTS
						+ AndyConstants.Params.ID + "="
						+ preferenceHelper.getUserId() + "&"
						+ AndyConstants.Params.TOKEN + "="
						+ preferenceHelper.getSessionToken());

		// new HttpRequester(mapActivity, map,
		// AndyConstants.ServiceCode.GET_ALL_REQUEST, true, this);

		requestQueue.add(new VolleyHttpRequest(Method.GET, map,
				AndyConstants.ServiceCode.GET_ALL_REQUEST, this, this));
	}

	private class TimerRequestStatus extends TimerTask {
		@Override
		public void run() {
			if (isContinueRequest) {
				getAllRequests();
			}
		}
	}

	private void startCheckingUpcomingRequests() {
		AppLog.Log(TAG, "start checking upcomingRequests");
		isContinueRequest = true;
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerRequestStatus(),
				AndyConstants.DELAY, AndyConstants.TIME_SCHEDULE);
	}

	private void stopCheckingUpcomingRequests() {
		AppLog.Log(TAG, "stop checking upcomingRequests");
		isContinueRequest = false;
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	private void removeNotification() {
		try {
			NotificationManager manager = (NotificationManager) mapActivity
					.getSystemService(mapActivity.NOTIFICATION_SERVICE);
			manager.cancel(AndyConstants.NOTIFICATION_ID);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onLocationReceived(LatLng latlong) {
		if (latlong != null) {
			if (mMap != null) {
				if (markerDriverLocation == null) {
					markerDriverLocation = mMap.addMarker(new MarkerOptions()
							.position(
									new LatLng(latlong.latitude,
											latlong.longitude))
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.pin_driver))
							.title(mapActivity.getResources().getString(
									R.string.my_location)));
					mMap.animateCamera(CameraUpdateFactory
							.newLatLngZoom(new LatLng(latlong.latitude,
									latlong.longitude), 10));
				} else {
					markerDriverLocation.setPosition(new LatLng(
							latlong.latitude, latlong.longitude));
				}
			}
		}
	}

	public void setComponentVisible() {
		btnGoOffline.setVisibility(View.GONE);
		llAcceptReject.setVisibility(View.VISIBLE);
		btnClientReqRemainTime.setVisibility(View.VISIBLE);
		// rlTimeLeft.setVisibility(View.VISIBLE);
		llUserDetailView.setVisibility(View.VISIBLE);
	}

	public void setComponentInvisible() {
		btnGoOffline.setVisibility(View.VISIBLE);
		llAcceptReject.setVisibility(View.GONE);
		btnClientReqRemainTime.setVisibility(View.GONE);
		// rlTimeLeft.setVisibility(View.GONE);
		llUserDetailView.setVisibility(View.GONE);
	}

	public void cancelSeekbarTimer() {
		if (seekbarTimer != null) {
			seekbarTimer.cancel();
			seekbarTimer = null;
			soundPool.stop(soundid);
		}
	}

	public void onDestroyView() {
		SupportMapFragment f = (SupportMapFragment) getFragmentManager()
				.findFragmentById(R.id.clientReqMap);
		if (f != null) {
			try {
				getFragmentManager().beginTransaction().remove(f).commit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		mMap = null;
		super.onDestroyView();
	}

	private class newRequestReciever extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String response = intent.getStringExtra(AndyConstants.NEW_REQUEST);
			AppLog.Log(TAG, "FROM BROAD CAST-->" + response);
			try {
				JSONObject jsonObject = new JSONObject(response);
				if (jsonObject.getInt(AndyConstants.Params.UNIQUE_ID) == 1) {
					requestDetail = parseContent.parseNotification(response);
					if (requestDetail != null) {
						stopCheckingUpcomingRequests();
						// startTimerForRespondRequest(requestDetail.getTimeLeft());

						setComponentVisible();
						// pbTimeLeft.setMax(requestDetail.getTimeLeft());
						btnClientReqRemainTime.setText(""
								+ requestDetail.getTimeLeft());
						// pbTimeLeft.setProgress(requestDetail.getTimeLeft());
						tvClientName.setText(requestDetail.getClientName());
						// tvClientPhoneNumber.setText(requestDetail
						// .getClientPhoneNumber());
						if (requestDetail.getClientRating() != 0) {
							tvClientRating.setRating(requestDetail
									.getClientRating());
							Log.i("Rating",
									"" + requestDetail.getClientRating());
						}
						if (TextUtils.isEmpty(requestDetail.getClientProfile())) {
							aQuery.id(ivClientProfilePicture)
									.progress(R.id.pBar).image(R.drawable.user);
						} else {
							aQuery.id(ivClientProfilePicture)
									.progress(R.id.pBar)
									.image(requestDetail.getClientProfile());
						}
/*
						markerClientLocation = null;
						markerClientLocation = mMap
								.addMarker(new MarkerOptions()
										.position(
												new LatLng(
														Double.parseDouble(requestDetail
																.getClientLatitude()),
														Double.parseDouble(requestDetail
																.getClientLongitude())))
										.icon(BitmapDescriptorFactory
												.fromResource(R.drawable.pin_client))
										.title(mapActivity
												.getResources()
												.getString(
														R.string.client_location)));*/
						
						if (markerClientLocation == null) {
							markerClientLocation = mMap
									.addMarker(new MarkerOptions()
											.position(
													new LatLng(
															Double.parseDouble(requestDetail
																	.getClientLatitude()),
															Double.parseDouble(requestDetail
																	.getClientLongitude())))
											.icon(BitmapDescriptorFactory
													.fromResource(R.drawable.pin_client))
											.title(mapActivity
													.getResources()
													.getString(
															R.string.client_location)));
						} else {
							markerClientLocation.setPosition(new LatLng(
									Double.parseDouble(requestDetail
											.getClientLatitude()), Double
											.parseDouble(requestDetail
													.getClientLongitude())));
						}
						if (seekbarTimer == null) {
							seekbarTimer = new SeekbarTimer(
									requestDetail.getTimeLeft() * 1000, 1000);
							seekbarTimer.start();
						}
						AppLog.Log(TAG, "From broad cast recieved request");
					}
				} else {
					setComponentInvisible();
					preferenceHelper.clearRequestData();
					if (markerClientLocation != null
							&& markerClientLocation.isVisible()) {
						markerClientLocation.remove();
						markerClientLocation=null;
					}
					cancelSeekbarTimer();
					removeNotification();
					startCheckingUpcomingRequests();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onErrorResponse(VolleyError error) {
		// TODO Auto-generated method stub
		AppLog.Log("TAG", error.getMessage());
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
		this.location = location;
		if (location != null) {
			if (mMap != null) {
				if (markerDriverLocation == null) {
					markerDriverLocation = mMap.addMarker(new MarkerOptions()
							.position(
									new LatLng(location.getLatitude(), location
											.getLongitude()))
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.pin_driver))
							.title(getResources().getString(
									R.string.my_location)));
					mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
							new LatLng(location.getLatitude(), location
									.getLongitude()), 12));
				} else {
					markerDriverLocation.setPosition(new LatLng(location
							.getLatitude(), location.getLongitude()));
				}
			}
		} else {
			showLocationOffDialog();
		}
	}

	private void updateButtonUi(boolean state) {
		btnGoOffline.setSelected(state);
		if (btnGoOffline.isSelected()) {
			btnGoOffline.setText(getString(R.string.text_go_offline));
			linearOffline.setVisibility(View.GONE);
			relMap.setVisibility(View.VISIBLE);
		} else {
			btnGoOffline.setText(getString(R.string.text_go_online));
			linearOffline.setVisibility(View.VISIBLE);
			relMap.setVisibility(View.GONE);
		}
	}

	private void checkState() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}
//		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
//				.getString(R.string.progress_getting_avaibility), false);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL,
				AndyConstants.ServiceType.CHECK_STATE + AndyConstants.Params.ID
						+ "=" + preferenceHelper.getUserId() + "&"
						+ AndyConstants.Params.TOKEN + "="
						+ preferenceHelper.getSessionToken());
		// new HttpRequester(this, map, AndyConstants.ServiceCode.CHECK_STATE,
		// true, this);

		requestQueue.add(new VolleyHttpRequest(Method.GET, map,
				AndyConstants.ServiceCode.CHECK_STATE, this, this));
	}

	private void changeState() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}

		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_changing_avaibilty), false);

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.TOGGLE_STATE);
		map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
		map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());

		// new HttpRequester(this, map, AndyConstants.ServiceCode.TOGGLE_STATE,
		// this);

		requestQueue.add(new VolleyHttpRequest(Method.POST, map,
				AndyConstants.ServiceCode.TOGGLE_STATE, this, this));

	}
}