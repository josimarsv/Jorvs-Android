package com.automated.taxinow.driver.fragment;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.androidquery.AQuery;
import com.automated.taxinow.driver.MapActivity;
import com.automated.taxinow.driver.R;
import com.automated.taxinow.driver.base.BaseMapFragment;
import com.automated.taxinow.driver.db.DBHelper;
import com.automated.taxinow.driver.locationupdate.LocationHelper;
import com.automated.taxinow.driver.locationupdate.LocationHelper.OnLocationReceived;
import com.automated.taxinow.driver.model.BeanRoute;
import com.automated.taxinow.driver.model.BeanStep;
import com.automated.taxinow.driver.model.RequestDetail;
import com.automated.taxinow.driver.parse.AsyncTaskCompleteListener;
import com.automated.taxinow.driver.parse.ParseContent;
import com.automated.taxinow.driver.parse.VolleyHttpRequest;
import com.automated.taxinow.driver.utills.AndyConstants;
import com.automated.taxinow.driver.utills.AndyUtils;
import com.automated.taxinow.driver.utills.AppLog;
import com.automated.taxinow.driver.utills.PreferenceHelper;
import com.automated.taxinow.driver.widget.MyFontTextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * @author Kishan H Dhamat
 * 
 */
public class JobFragment extends BaseMapFragment implements
		AsyncTaskCompleteListener, OnLocationReceived {
	private GoogleMap googleMap;
	private PolylineOptions lineOptions;
	private BeanRoute route;
	private ArrayList<LatLng> points;
	private MyFontTextView tvJobTime, tvJobDistance, tvJobStatus, tvClientName,
			tvDestinationAddress;
	private ImageView ivClientProfilePicture;
	private RatingBar tvClientRating;
	private ParseContent parseContent;

	private Location location;
	private LocationHelper locationHelper;
	private AQuery aQuery;
	private RequestDetail requestDetail;
	private Marker markerDriverLocation, markerClientLocation;
	private Timer elapsedTimer;
	private DBHelper dbHelper;
	private int jobStatus = 0;
	private String time, distance = "0";
	private final String TAG = "JobFragment";
	DecimalFormat decimalFormat;
	private BroadcastReceiver mReceiver;
	private MyFontTextView tvPaymentType;
	private BroadcastReceiver modeReceiver;
	private View jobFragmentView;
	private MapView mMapView;
	public static final long ELAPSED_TIME_SCHEDULE = 60 * 1000;
	private Bundle mBundle;
	private BroadcastReceiver destReceiver;
	private BeanRoute routeDest;
	private ArrayList<LatLng> pointsDest;
	private PolylineOptions lineOptionsDest;
	private Polyline polyLineDest;
	private Marker markerDestination;
	private RequestQueue requestQueue;
	private ImageButton btnNavigate;
	private boolean isAddMarker = false;
	private BeanRoute routeClient;
	private ArrayList<LatLng> pointsClient;
	private PolylineOptions lineOptionsClient;
	private Polyline polyLineClient;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		jobFragmentView = inflater.inflate(R.layout.fragment_job, container,
				false);
		try {
			MapsInitializer.initialize(getActivity());
		} catch (Exception e) {
		}

		preferenceHelper = new PreferenceHelper(getActivity());
		tvJobTime = (MyFontTextView) jobFragmentView
				.findViewById(R.id.tvJobTime);
		tvJobDistance = (MyFontTextView) jobFragmentView
				.findViewById(R.id.tvJobDistance);
		tvJobStatus = (MyFontTextView) jobFragmentView
				.findViewById(R.id.tvJobStatus);
		tvClientName = (MyFontTextView) jobFragmentView
				.findViewById(R.id.tvClientName);
		tvPaymentType = (MyFontTextView) jobFragmentView
				.findViewById(R.id.tvPaymentType);
		// tvClientPhoneNumber = (MyFontTextView) jobFragmentView
		// .findViewById(R.id.tvClientNumber);
		tvClientRating = (RatingBar) jobFragmentView
				.findViewById(R.id.tvClientRating);
		ivClientProfilePicture = (ImageView) jobFragmentView
				.findViewById(R.id.ivClientImage);
		btnNavigate = (ImageButton) jobFragmentView
				.findViewById(R.id.btnNavigate);
		tvDestinationAddress = (MyFontTextView) jobFragmentView
				.findViewById(R.id.tvDestinationAddress);

		tvJobStatus.setOnClickListener(this);
		if (preferenceHelper.isNavigate()) {
			btnNavigate.setVisibility(View.GONE);
		} else {
			btnNavigate.setVisibility(View.VISIBLE);
			btnNavigate.setOnClickListener(this);
		}
		jobFragmentView.findViewById(R.id.tvJobCallClient).setOnClickListener(
				this);

		setPaymentType();
		return jobFragmentView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBundle = savedInstanceState;
		requestQueue = Volley.newRequestQueue(mapActivity);
		// getDestinationAddress(preferenceHelper.getDestinationLatitude(),
		// preferenceHelper.getDestinationLongitude());

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		parseContent = new ParseContent(mapActivity);
		decimalFormat = new DecimalFormat("0.00");
		points = new ArrayList<LatLng>();
		aQuery = new AQuery(mapActivity);
		dbHelper = new DBHelper(mapActivity);
		jobStatus = getArguments().getInt(AndyConstants.JOB_STATUS,
				AndyConstants.IS_WALKER_ARRIVED);
		requestDetail = (RequestDetail) getArguments().getSerializable(
				AndyConstants.REQUEST_DETAIL);

		if (jobStatus == AndyConstants.IS_WALK_COMPLETED) {
			startElapsedTimer();
			getPathFromServer();
		}

		setClientDetails(requestDetail);
		setjobStatus(jobStatus);

		mMapView = (MapView) jobFragmentView.findViewById(R.id.jobMap);
		mMapView.onCreate(mBundle);
		setUpMap();

		locationHelper = new LocationHelper(getActivity());
		locationHelper.setLocationReceivedLister(this);
		locationHelper.onStart();

		// getDistance();
	}

	/**
	 * 
	 */
	private void getPathFromServer() {
		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_loading), false);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL,
				AndyConstants.ServiceType.PATH_REQUEST
						+ AndyConstants.Params.ID + "="
						+ preferenceHelper.getUserId() + "&"
						+ AndyConstants.Params.TOKEN + "="
						+ preferenceHelper.getSessionToken() + "&"
						+ AndyConstants.Params.REQUEST_ID + "="
						+ preferenceHelper.getRequestId());
		// new HttpRequester(mapActivity, map,
		// AndyConstants.ServiceCode.PATH_REQUEST, true, this);

		requestQueue.add(new VolleyHttpRequest(Method.GET, map,
				AndyConstants.ServiceCode.PATH_REQUEST, this, this));
	}

	private void setClientDetails(RequestDetail requestDetail) {
		tvClientName.setText(requestDetail.getClientName());
		// tvClientPhoneNumber.setText(requestDetail.getClientPhoneNumber());
		if (requestDetail.getClientRating() != 0) {
			tvClientRating.setRating(requestDetail.getClientRating());
		}
		if (TextUtils.isEmpty(requestDetail.getClientProfile())) {
			aQuery.id(ivClientProfilePicture).progress(R.id.pBar)
					.image(R.drawable.user);
		} else {
			aQuery.id(ivClientProfilePicture).progress(R.id.pBar)
					.image(requestDetail.getClientProfile());

		}

	}

	public void getDestinationAddress(String latitude, String longitude) {

		if (TextUtils.isEmpty(latitude) && TextUtils.isEmpty(longitude)) {
			tvClientRating.setVisibility(View.VISIBLE);
			preferenceHelper.putDestinationLatitude("");
			preferenceHelper.putDestinationLongitude("");
			tvDestinationAddress.setVisibility(View.GONE);
		} else {

			Geocoder geocoder;
			List<Address> addresses;
			geocoder = new Geocoder(mapActivity, Locale.getDefault());
			try {
				addresses = geocoder.getFromLocation(
						Double.parseDouble(latitude),
						Double.parseDouble(longitude), 1);
				if (addresses == null || addresses.size() == 0)
					return;
				String address = addresses.get(0).getAddressLine(0);
				String city = addresses.get(0).getLocality();
				String state = addresses.get(0).getAdminArea();
				String country = addresses.get(0).getCountryName();
				String postalCode = addresses.get(0).getPostalCode();
				// String knownName = addresses.get(0).getFeatureName();

				tvDestinationAddress.setVisibility(View.VISIBLE);
				tvClientRating.setVisibility(View.GONE);
				tvDestinationAddress.setText(" " + address + " " + city + "\n "
						+ state + " " + country + " " + postalCode);

				AppLog.Log("Address", " " + address + " " + city + "\n "
						+ state + " " + country + " " + postalCode);

			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		if (googleMap == null) {
			return;
		}
	}

	/**
	 * it is used for seeting text for jobstatus on textview
	 */
	private void setjobStatus(int jobStatus) {

		switch (jobStatus) {
		case AndyConstants.IS_WALKER_STARTED:
			tvJobStatus.setText(mapActivity.getResources().getString(
					R.string.text_walker_started));
			break;
		case AndyConstants.IS_WALKER_ARRIVED:
			tvJobStatus.setText(mapActivity.getResources().getString(
					R.string.text_walker_arrived));
			break;
		case AndyConstants.IS_WALK_STARTED:
			tvJobStatus.setText(mapActivity.getResources().getString(
					R.string.text_walk_started));
			break;
		case AndyConstants.IS_WALK_COMPLETED:
			tvJobStatus.setText(mapActivity.getResources().getString(
					R.string.text_walk_completed));
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tvJobStatus:
			switch (jobStatus) {
			case AndyConstants.IS_WALKER_STARTED:
				mapActivity.clearAll();
				walkerStarted();
				break;
			case AndyConstants.IS_WALKER_ARRIVED:
				mapActivity.clearAll();
				walkerArrived();
				break;
			case AndyConstants.IS_WALK_STARTED:
				mapActivity.clearAll();
				walkStarted();
				break;
			case AndyConstants.IS_WALK_COMPLETED:
				mapActivity.clearAll();
				walkCompleted();
				break;
			default:
				break;
			}

			break;
		case R.id.tvJobCallClient:
			if (!TextUtils.isEmpty(requestDetail.getClientPhoneNumber())) {
				Intent callIntent = new Intent(Intent.ACTION_CALL);
				callIntent.setData(Uri.parse("tel:"
						+ requestDetail.getClientPhoneNumber()));
				startActivity(callIntent);
			} else {
				Toast.makeText(
						mapActivity,
						mapActivity.getResources().getString(
								R.string.toast_number_not_found),
						Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.btnNavigate:
			if (markerClientLocation != null && markerDriverLocation != null) {
				preferenceHelper.putIsNavigate(true);
				animateCamera(markerDriverLocation.getPosition());
				v.setVisibility(View.GONE);
				drawPathToClient(markerDriverLocation.getPosition(),
						markerClientLocation.getPosition());
			} else {
				AndyUtils.showToast("Wating for location", getActivity());
			}
			break;
		default:
			break;
		}
	}

	private void animateCamera(LatLng latlng) {
		if (markerClientLocation != null) {
			try {
				Location dest = new Location("dest");
				dest.setLatitude(markerClientLocation.getPosition().latitude);
				dest.setLongitude(markerClientLocation.getPosition().longitude);

				CameraPosition cameraPosition = new CameraPosition.Builder()
						.target(latlng).bearing(location.bearingTo(dest))
						.zoom(googleMap.getCameraPosition().zoom).tilt(45)
						.build();
				googleMap.animateCamera(CameraUpdateFactory
						.newCameraPosition(cameraPosition));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void drawPathToClient(LatLng source, LatLng destination) {
		if (source == null || destination == null) {
			return;
		}
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL,
				"https://maps.googleapis.com/maps/api/directions/json?origin="
						+ source.latitude + "," + source.longitude
						+ "&destination=" + destination.latitude + ","
						+ destination.longitude + "&sensor=false&key="
						+ AndyConstants.DIRECTION_API_KEY);

		AppLog.Log("Navigation Path",
				"https://maps.googleapis.com/maps/api/directions/json?origin="
						+ source.latitude + "," + source.longitude
						+ "&destination=" + destination.latitude + ","
						+ destination.longitude + "&sensor=false&key="
						+ AndyConstants.DIRECTION_API_KEY);
		// new HttpRequester(mapActivity, map,
		// AndyConstants.ServiceCode.DRAW_PATH_CLIENT, true, this);
		requestQueue.add(new VolleyHttpRequest(Method.GET, map,
				AndyConstants.ServiceCode.DRAW_PATH_CLIENT, this, this));
	}

	/**
	 * send this when walk completed
	 */
	private void walkCompleted() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}

		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_send_request), false);

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.WALK_COMPLETED);
		map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
		map.put(AndyConstants.Params.REQUEST_ID,
				String.valueOf(preferenceHelper.getRequestId()));
		map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());
		map.put(AndyConstants.Params.LATITUDE,
				preferenceHelper.getWalkerLatitude());
		map.put(AndyConstants.Params.LONGITUDE,
				preferenceHelper.getWalkerLongitude());
		map.put(AndyConstants.Params.DISTANCE, preferenceHelper.getDistance()
				+ "");
		map.put(AndyConstants.Params.TIME, time);
		// new HttpRequester(mapActivity, map,
		// AndyConstants.ServiceCode.WALK_COMPLETED, this);

		requestQueue.add(new VolleyHttpRequest(Method.POST, map,
				AndyConstants.ServiceCode.WALK_COMPLETED, this, this));
	}

	/**
	 * send this when job started
	 */
	private void walkStarted() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}
		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_send_request), false);

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.WALK_STARTED);
		map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
		map.put(AndyConstants.Params.REQUEST_ID,
				String.valueOf(preferenceHelper.getRequestId()));
		map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());
		map.put(AndyConstants.Params.LATITUDE,
				preferenceHelper.getWalkerLatitude());
		map.put(AndyConstants.Params.LONGITUDE,
				preferenceHelper.getWalkerLongitude());
		// new HttpRequester(mapActivity, map,
		// AndyConstants.ServiceCode.WALK_STARTED, this);

		requestQueue.add(new VolleyHttpRequest(Method.POST, map,
				AndyConstants.ServiceCode.WALK_STARTED, this, this));
	}

	/**
	 * send this when walker arrived client's location
	 */
	private void walkerArrived() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}

		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_send_request), false);

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.WALK_ARRIVED);
		map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
		map.put(AndyConstants.Params.REQUEST_ID,
				String.valueOf(preferenceHelper.getRequestId()));
		map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());
		map.put(AndyConstants.Params.LATITUDE,
				preferenceHelper.getWalkerLatitude());
		map.put(AndyConstants.Params.LONGITUDE,
				preferenceHelper.getWalkerLongitude());
		// new HttpRequester(mapActivity, map,
		// AndyConstants.ServiceCode.WALKER_ARRIVED, this);

		requestQueue.add(new VolleyHttpRequest(Method.POST, map,
				AndyConstants.ServiceCode.WALKER_ARRIVED, this, this));
	}

	/**
	 * send this when walker started his/her run
	 */
	private void walkerStarted() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}

		AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
				.getString(R.string.progress_send_request), false);

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.WALKER_STARTED);
		map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
		map.put(AndyConstants.Params.REQUEST_ID,
				String.valueOf(preferenceHelper.getRequestId()));
		map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());
		map.put(AndyConstants.Params.LATITUDE,
				preferenceHelper.getWalkerLatitude());
		map.put(AndyConstants.Params.LONGITUDE,
				preferenceHelper.getWalkerLongitude());
		// new HttpRequester(mapActivity, map,
		// AndyConstants.ServiceCode.WALKER_STARTED, this);

		requestQueue.add(new VolleyHttpRequest(Method.POST, map,
				AndyConstants.ServiceCode.WALKER_STARTED, this, this));
	}

	private void setUpMap() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (googleMap == null) {
			googleMap = ((MapView) jobFragmentView.findViewById(R.id.jobMap))
					.getMap();
			initPreviousDrawPath();

			googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {

				// Use default InfoWindow frame

				@Override
				public View getInfoWindow(Marker marker) {
					View v = mapActivity.getLayoutInflater().inflate(
							R.layout.info_window_layout, null);

					((TextView) v).setText(marker.getTitle());
					return v;
				}

				// Defines the contents of the InfoWindow

				@Override
				public View getInfoContents(Marker marker) {

					// Getting view from the layout file info_window_layout View

					// Getting reference to the TextView to set title TextView

					// Returning the view containing InfoWindow contents return
					return null;

				}

			});

			googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {
				@Override
				public boolean onMarkerClick(Marker marker) {
					marker.showInfoWindow();
					return true;
				}
			});

			addMarker();

		}
	}

	// It will add marker on map of walker location
	private void addMarker() {
		if (googleMap == null) {
			setUpMap();
			return;
		}

	}

	public void onDestroyView() {
		SupportMapFragment f = (SupportMapFragment) getFragmentManager()
				.findFragmentById(R.id.jobMap);
		if (f != null) {
			try {
				getFragmentManager().beginTransaction().remove(f).commit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		googleMap = null;
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		mMapView.onDestroy();
		super.onDestroy();
		stopElapsedTimer();
		ubregisterCancelReceiver();
		ubregisterPaymentModeReceiver();
		unRegisterDestinationReceiver();
	}

	private void initPreviousDrawPath() {
		// points = dbHelper.getLocations();
		lineOptions = new PolylineOptions();
		lineOptions.addAll(points);
		lineOptions.width(15);
		lineOptions.geodesic(true);
		lineOptions.color(getResources().getColor(R.color.skyblue));
		googleMap.addPolyline(lineOptions);
		points.clear();
	}

	@Override
	public void onTaskCompleted(String response, int serviceCode) {
		AndyUtils.removeCustomProgressDialog();
		if (!this.isVisible())
			return;
		LatLng latlong;
		switch (serviceCode) {
		case AndyConstants.ServiceCode.WALKER_STARTED:
			AppLog.Log(TAG, "walker started response " + response);
			if (parseContent.isSuccess(response)) {
				jobStatus = AndyConstants.IS_WALKER_ARRIVED;
				setjobStatus(jobStatus);
			}

			break;
		case AndyConstants.ServiceCode.WALKER_ARRIVED:
			AppLog.Log(TAG, "walker arrived response " + response);
			if (parseContent.isSuccess(response)) {
				jobStatus = AndyConstants.IS_WALK_STARTED;
				setjobStatus(jobStatus);
			}
			break;
		case AndyConstants.ServiceCode.WALK_STARTED:
			AppLog.Log(TAG, "walk started response " + response);
			if (parseContent.isSuccess(response)) {
				preferenceHelper.putIsTripStart(true);
				jobStatus = AndyConstants.IS_WALK_COMPLETED;
				setjobStatus(jobStatus);
				// getDistance();
				preferenceHelper.putRequestTime(Calendar.getInstance()
						.getTimeInMillis());
				if (markerClientLocation != null) {
					markerClientLocation.setTitle(mapActivity.getResources()
							.getString(R.string.job_start_location));
				}
				startElapsedTimer();
			}

			break;
		case AndyConstants.ServiceCode.WALK_COMPLETED:
			AppLog.Log(TAG, "walk completed response " + response);
			if (parseContent.isSuccess(response)) {
				FeedbackFrament feedbackFrament = new FeedbackFrament();
				requestDetail = parseContent.parseRequestStatus(response);
				Bundle bundle = new Bundle();
				bundle.putSerializable(AndyConstants.REQUEST_DETAIL,
						requestDetail);

				// bundle.putString(
				// AndyConstants.Params.TIME,
				// time
				// + " "
				// + mapActivity.getResources().getString(
				// R.string.text_mins));
				// bundle.putString(
				// AndyConstants.Params.DISTANCE,
				// decimalFormat.format(preferenceHelper.getDistance())
				// // / (1000 * 1.62))
				// + " "
				// + mapActivity.getResources().getString(
				// R.string.text_miles));
				try {
					requestDetail.setAmount(new JSONObject(response)
							.getString("total"));
				} catch (JSONException e) {
					e.printStackTrace();
				}

				requestDetail.setTime(time);
				requestDetail.setDistance(" " + preferenceHelper.getDistance());
				requestDetail.setUnit(preferenceHelper.getUnit());
				feedbackFrament.setArguments(bundle);
				if (this.isVisible())
					mapActivity.addFragment(feedbackFrament, false,
							AndyConstants.FEEDBACK_FRAGMENT_TAG, true);
			}
			break;

		case AndyConstants.ServiceCode.GET_ROUTE:
			// if (parseContent.isSuccess(response)) {
			// jobStatus = AndyConstants.;
			// setjobStatus(jobStatus);
			// }
		case AndyConstants.ServiceCode.PATH_REQUEST:
			AppLog.Log(TAG, "Path request :" + response);
			if (parseContent.isSuccess(response)) {
				parseContent.parsePathRequest(response, points);
				initPreviousDrawPath();
			}
			break;
		case AndyConstants.ServiceCode.DRAW_PATH:
			if (!TextUtils.isEmpty(response)) {
				routeDest = new BeanRoute();
				parseContent.parseRoute(response, routeDest);

				final ArrayList<BeanStep> step = routeDest.getListStep();
				System.out.println("step size=====> " + step.size());
				pointsDest = new ArrayList<LatLng>();
				lineOptionsDest = new PolylineOptions();

				for (int i = 0; i < step.size(); i++) {
					List<LatLng> path = step.get(i).getListPoints();
					System.out.println("step =====> " + i + " and "
							+ path.size());
					pointsDest.addAll(path);
				}
				if (polyLineDest != null)
					polyLineDest.remove();
				lineOptionsDest.addAll(pointsDest);
				lineOptionsDest.width(15);
				lineOptionsDest.geodesic(true);
				lineOptionsDest.color(getResources().getColor(
						R.color.color_blue)); // #00008B rgb(0,0,139)

				if (lineOptionsDest != null && googleMap != null) {
					polyLineDest = googleMap.addPolyline(lineOptionsDest);
					boundLatLang();
					// LatLngBounds.Builder bld = new LatLngBounds.Builder();
					//
					// bld.include(markerClientLocation.getPosition());
					// bld.include(markerDestination.getPosition());
					// LatLngBounds latLngBounds = bld.build();
					// googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
					// latLngBounds, 15));
				}
			}
			break;

		case AndyConstants.ServiceCode.DRAW_PATH_CLIENT:
			AppLog.Log("JobFragment", "PATH Response : " + response);
			if (!TextUtils.isEmpty(response)) {
				routeClient = new BeanRoute();
				parseContent.parseRoute(response, routeClient);

				final ArrayList<BeanStep> step = routeClient.getListStep();
				pointsClient = new ArrayList<LatLng>();
				lineOptionsClient = new PolylineOptions();

				for (int i = 0; i < step.size(); i++) {
					List<LatLng> path = step.get(i).getListPoints();
					pointsClient.addAll(path);
				}
				if (polyLineClient != null)
					polyLineClient.remove();
				lineOptionsClient.addAll(pointsClient);
				lineOptionsClient.width(17);
				lineOptionsClient.color(Color.BLUE);

				if (lineOptionsClient != null && googleMap != null) {
					polyLineClient = googleMap.addPolyline(lineOptionsClient);
				}
			}

		default:
			break;
		}
	}

	/**
	 * 
	 */
	private void startElapsedTimer() {
		elapsedTimer = new Timer();
		elapsedTimer.scheduleAtFixedRate(new TimerRequestStatus(),
				AndyConstants.DELAY, ELAPSED_TIME_SCHEDULE);
	}

	private void stopElapsedTimer() {
		if (elapsedTimer != null) {
			elapsedTimer.cancel();
			elapsedTimer = null;
		}
	}

	private class TimerRequestStatus extends TimerTask {
		@Override
		public void run() {
			// isContinueRequest = false;
			AppLog.Log(TAG, "In elapsed time timer");
			mapActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (isVisible()) {
						if (preferenceHelper.getRequestTime() == AndyConstants.NO_TIME) {
							preferenceHelper.putRequestTime(System
									.currentTimeMillis());
						}
						time = String.valueOf((Calendar.getInstance()
								.getTimeInMillis() - preferenceHelper
								.getRequestTime())
								/ (1000 * 60));
						tvJobTime.setText(time
								+ " "
								+ mapActivity.getResources().getString(
										R.string.text_mins));
					}
				}
			});

		}
	}

	@Override
	public void onLocationReceived(LatLng latlong) {
		if (googleMap == null) {
			return;
		}

		if (markerDriverLocation != null && markerClientLocation != null) {

			if (preferenceHelper.isNavigate()) {
				drawPathToClient(markerDriverLocation.getPosition(),
						markerClientLocation.getPosition());
			}

		}
		getDestinationAddress(preferenceHelper.getDestinationLatitude(),
				preferenceHelper.getDestinationLongitude());
		if (markerClientLocation == null) {

			markerClientLocation = googleMap.addMarker(new MarkerOptions()
					.position(
							new LatLng(Double.parseDouble(requestDetail
									.getClientLatitude()), Double
									.parseDouble(requestDetail
											.getClientLongitude()))).icon(
							BitmapDescriptorFactory
									.fromResource(R.drawable.pin_client)));

			if (jobStatus == AndyConstants.IS_WALK_COMPLETED) {
				markerClientLocation.setTitle(mapActivity.getResources()
						.getString(R.string.job_start_location));
			} else {
				markerClientLocation.setTitle(mapActivity.getResources()
						.getString(R.string.client_location));
			}
			drawPath(markerClientLocation.getPosition(),
					preferenceHelper.getClientDestination());
		}
		if (latlong != null) {
			if (googleMap != null) {
				if (markerDriverLocation == null) {
					markerDriverLocation = googleMap
							.addMarker(new MarkerOptions()
									.position(
											new LatLng(latlong.latitude,
													latlong.longitude))
									.icon(BitmapDescriptorFactory
											.fromResource(R.drawable.pin_driver))
									.title(getResources().getString(
											R.string.my_location)));
					boundLatLang();
					// googleMap.animateCamera(CameraUpdateFactory
					// .newLatLngZoom(new LatLng(latlong.latitude,
					// latlong.longitude), 16));
				} else {
					markerDriverLocation.setPosition(new LatLng(
							latlong.latitude, latlong.longitude));
					if (jobStatus == AndyConstants.IS_WALK_COMPLETED) {
						drawTrip(new LatLng(latlong.latitude, latlong.longitude));

						// distance = decimalFormat.format(distanceMeter / (1000
						// * 1.62));

						// tvJobDistance.setText(decimalFormat
						// .format(preferenceHelper.getDistance()
						// / (1000 * 1.62))
						// + " "
						// + mapActivity.getResources().getString(
						// R.string.text_miles));

						tvJobDistance.setText(decimalFormat
								.format(preferenceHelper.getDistance()
								// / (1000 * 1.62))
								) + " " + preferenceHelper.getUnit());

					} else {
						tvJobDistance
								.setText("0 " + preferenceHelper.getUnit());

					}

				}
				// getDistance();
			}
		}

	}

	// private void getDistance() {
	// if (googleMap == null || markerDriverLocation == null) {
	// return;
	// }
	// if (jobStatus == AndyConstants.IS_WALK_COMPLETED) {
	//
	// ArrayList<LatLng> latLngs = dbHelper.getLocations();
	// int distanceMeter = 0;
	// if (latLngs.size() >= 2) {
	// for (int i = 0; i < latLngs.size() - 1; i++) {
	// Location location1 = new Location("");
	// Location location2 = new Location("");
	// location1.setLatitude(latLngs.get(i).latitude);
	// location1.setLongitude(latLngs.get(i).longitude);
	// location2.setLatitude(latLngs.get(i +
	// 1).latitude);googleMap.setInfoWindowAdapter(this);
	// location2.setLongitude(latLngs.get(i + 1).longitude);
	// distanceMeter = distanceMeter
	// + (int) location1.distanceTo(location2);
	//
	// }
	// }
	// // AndyUtils.showToast("Meter:" + distanceMeter, mapActivity);
	// DecimalFormat decimalFormat = new DecimalFormat("0.00");
	// distance = decimalFormat.format(distanceMeter / (1000 * 1.62));
	// tvJobDistance
	// .setText(distance
	// + " "
	// + mapActivity.getResources().getString(
	// R.string.text_miles));
	// // Location jobStartLocation = new Location("");
	// // Location currentLocation = new Location("");
	// // jobStartLocation.setLatitude(Double.parseDouble(requestDetail
	// // .getClientLatitude()));
	// // jobStartLocation.setLongitude(Double.parseDouble(requestDetail
	// // .getClientLongitude()));
	// // currentLocation
	// // .setLatitude(markerDriverLocation.getPosition().latitude);
	// // currentLocation
	// // .setLongitude(markerDriverLocation.getPosition().longitude);
	// // AppLog.Log(TAG, jobStartLocation.distanceTo(currentLocation)
	// // + " METERS ");
	// // int distanceMeter = (int) jobStartLocation
	// // .distanceTo(currentLocation);
	// // DecimalFormat decimalFormat = new DecimalFormat("0.0");
	// // distance = decimalFormat.format(distanceMeter / (1000 * 1.62));
	// // tvJobDistance
	// // .setText(distance
	// // + " "
	// // + mapActivity.getResources().getString(
	// // R.string.text_miles));
	// }
	// }

	private void drawTrip(LatLng latlng) {

		if (googleMap != null) {
			// setMarker(latlng);
			points.add(latlng);
			// dbHelper.addLocation(latlng);
			lineOptions = new PolylineOptions();
			lineOptions.addAll(points);
			lineOptions.width(15);
			lineOptions.geodesic(true);
			lineOptions.color(getResources().getColor(R.color.skyblue));

			googleMap.addPolyline(lineOptions);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
		registerCancelReceiver();
		registerPaymentModeReceiver();
		registerDestinationReceiver();
		if (isAddMarker && preferenceHelper.isNavigate()) {
			if (jobStatus == AndyConstants.IS_WALKER_ARRIVED) {
				drawPathToClient(markerDriverLocation.getPosition(),
						markerClientLocation.getPosition());
			}

		}

	}

	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	private void registerCancelReceiver() {
		IntentFilter intentFilter = new IntentFilter("CANCEL_REQUEST");
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				AppLog.Log("JobFragment", "CANCEL_REQUEST");
				stopElapsedTimer();
				mapActivity.startActivity(new Intent(mapActivity,
						MapActivity.class));
				mapActivity.finish();
			}
		};
		mapActivity.registerReceiver(mReceiver, intentFilter);
	}

	private void ubregisterCancelReceiver() {
		if (mReceiver != null) {
			mapActivity.unregisterReceiver(mReceiver);
		}
	}

	private void registerPaymentModeReceiver() {
		IntentFilter intentFilter = new IntentFilter("PAYMENT_MODE");
		modeReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				AppLog.Log("JobFragment", "PAYMENT_MODE");
				if (JobFragment.this.isVisible()) {
					setPaymentType();
				}
			}
		};
		mapActivity.registerReceiver(modeReceiver, intentFilter);
	}

	private void ubregisterPaymentModeReceiver() {
		if (modeReceiver != null) {
			mapActivity.unregisterReceiver(modeReceiver);
		}
	}

	private void unRegisterDestinationReceiver() {
		if (destReceiver != null) {
			mapActivity.unregisterReceiver(destReceiver);
		}
	}

	private void registerDestinationReceiver() {
		IntentFilter intentFilter = new IntentFilter("CLIENT_DESTINATION");
		destReceiver = new BroadcastReceiver() {
			private LatLng destLatLng;

			@Override
			public void onReceive(Context context, Intent intent) {
				AppLog.Log("JobFragment", "CLIENT_DESTINATION");
				destLatLng = preferenceHelper.getClientDestination();
				drawPath(markerClientLocation.getPosition(), destLatLng);
			}
		};
		mapActivity.registerReceiver(destReceiver, intentFilter);
	}

	private void setPaymentType() {
		if (preferenceHelper.getPaymentType() == AndyConstants.CASH)
			tvPaymentType.setText(getString(R.string.text_type_cash));
		else
			tvPaymentType.setText(getString(R.string.text_type_card));
	}

	private void drawPath(LatLng source, LatLng destination) {
		if (source == null || destination == null) {
			return;
		}
		if (destination.latitude != 0) {

			setDestinationMarker(destination);
			boundLatLang();
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(AndyConstants.URL,
					"http://maps.googleapis.com/maps/api/directions/json?origin="
							+ source.latitude + "," + source.longitude
							+ "&destination=" + destination.latitude + ","
							+ destination.longitude + "&sensor=false");
			// new HttpRequester(mapActivity, map,
			// AndyConstants.ServiceCode.DRAW_PATH, true, this);

			requestQueue.add(new VolleyHttpRequest(Method.GET, map,
					AndyConstants.ServiceCode.DRAW_PATH, this, this));
		}
	}

	private void setDestinationMarker(LatLng latLng) {
		if (latLng != null) {
			if (googleMap != null && this.isVisible()) {
				if (markerDestination == null) {
					MarkerOptions opt = new MarkerOptions();
					opt.position(latLng);
					opt.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.pin_client_destination));
					opt.title(getString(R.string.text_destination));
					markerDestination = googleMap.addMarker(opt);
				} else {
					markerDestination.setPosition(latLng);
				}
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
		if (location != null) {
			if (googleMap != null) {
				if (markerDriverLocation == null) {
					markerDriverLocation = googleMap
							.addMarker(new MarkerOptions()
									.position(
											new LatLng(location.getLatitude(),
													location.getLongitude()))
									.icon(BitmapDescriptorFactory
											.fromResource(R.drawable.pin_driver))
									.title(getResources().getString(
											R.string.my_location)));
					googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
							new LatLng(location.getLatitude(), location
									.getLongitude()), 16));
					googleMap
							.setOnCameraChangeListener(new OnCameraChangeListener() {

								@Override
								public void onCameraChange(CameraPosition arg0) {
									if (!isAddMarker) {
										isAddMarker = true;
										if (preferenceHelper.isNavigate())
											animateCamera(markerDriverLocation
													.getPosition());
									}
								}
							});
				} else {
					markerDriverLocation.setPosition(new LatLng(location
							.getLatitude(), location.getLongitude()));
				}
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

	private void boundLatLang() {

		try {
			if (markerDriverLocation != null && markerClientLocation != null
					&& markerDestination != null) {
				LatLngBounds.Builder bld = new LatLngBounds.Builder();
				bld.include(new LatLng(
						markerDriverLocation.getPosition().latitude,
						markerDriverLocation.getPosition().longitude));
				bld.include(new LatLng(
						markerClientLocation.getPosition().latitude,
						markerClientLocation.getPosition().longitude));
				bld.include(new LatLng(
						markerDestination.getPosition().latitude,
						markerDestination.getPosition().longitude));
				LatLngBounds latLngBounds = bld.build();

				googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
						latLngBounds, 50));
			} else if (markerDriverLocation != null
					&& markerClientLocation != null) {
				LatLngBounds.Builder bld = new LatLngBounds.Builder();
				bld.include(new LatLng(
						markerDriverLocation.getPosition().latitude,
						markerDriverLocation.getPosition().longitude));
				bld.include(new LatLng(
						markerClientLocation.getPosition().latitude,
						markerClientLocation.getPosition().longitude));
				LatLngBounds latLngBounds = bld.build();

				googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
						latLngBounds, 100));
			}
		} catch (Exception e) {
			e.printStackTrace();

		}

	}
}