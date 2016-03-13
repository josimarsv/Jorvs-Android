package com.automated.taxinow.driver.utills;

public class AndyConstants {

	public static final String DIRECTION_API_KEY = "AIzaSyDLQZQAARIyVD75Cd3Ri-DX93nHoWPhFxI";
	/*public static final String DIRECTION_API_KEY = "AIzaSyDDkGuI3VGsmIIk8jsxgGOKFFtKuN5MD4A"; new*/
	
	// fragment constants
	public static final String MAIN_FRAGMENT_TAG = "mainFragment";
	public static final String LOGIN_FRAGMENT_TAG = "loginFragment";
	public static final String REGISTER_FRAGMENT_TAG = "registerFragment";
	public static final String CLIENT_REQUEST_TAG = "clientRequestFragment";
	public static final String FEEDBACK_FRAGMENT_TAG = "feedbackFragment";
	public static final String JOB_FRGAMENT_TAG = "jobDoneFragment";
	public static final String ARRIVED_FRAGMENT_TAG = "arrivedFragment";
	public static final String FOREGETPASS_FRAGMENT_TAG = "FoegetPassFragment";

	public static final int CHOOSE_PHOTO = 112;
	public static final int TAKE_PHOTO = 113;

	public static final String PREF_NAME = "taxinowprovider";
	public static final String URL = "url";
	public static final String DEVICE_TYPE_ANDROID = "android";
	public static final String SOCIAL_FACEBOOK = "facebook";
	public static final String SOCIAL_GOOGLE = "google";
	public static final String MANUAL = "manual";
	public static final String GOOGLE_API_SCOPE_URL = "https://www.googleapis.com/auth/plus.login";
	public static final long DELAY = 0;
	public static final long TIME_SCHEDULE = 10 * 1000;
	public static final long DELAY_OFFLINE = 15 * 60 * 1000;
	public static final long TIME_SCHEDULE_OFFLINE = 15 * 60 * 1000;

	public static final int IS_ASSIGNED = 0;
	public static final int IS_WALKER_STARTED = 1;
	public static final int IS_WALKER_ARRIVED = 2;
	public static final int IS_WALK_STARTED = 3;
	public static final int IS_WALK_COMPLETED = 4;
	public static final int IS_DOG_RATED = 5;

	public static final String JOB_STATUS = "jobstatus";
	public static final String REQUEST_DETAIL = "requestDetails";

	// error code
	public static final int INVALID_TOKEN = 406;
	public static final int REQUEST_ID_NOT_FOUND = 408;

	// no request
	public static final int NO_REQUEST = -1;
	public static final int NO_TIME = -1;
	public static final String NEW_REQUEST = "new_request";
	public static final int NOTIFICATION_ID = 0;
	// payment mode
	public static final int CASH = 1;
	public static final int CREDIT = 0;

	// web service url constants
	public class ServiceType {

		// Local server for Taxi Now Makeover version
		//private static final String HOST_URL = "http://laudore.com/";
		private static final String HOST_URL = "http://heyycar.azurewebsites.net/";
		//private static final String HOST_URL = "http://taxi.jayzay.com/";


		private static final String BASE_URL = HOST_URL + "provider/";
		public static final String LOGIN = BASE_URL + "login";
		public static final String REGISTER = BASE_URL + "register";
		public static final String GET_ALL_REQUESTS = BASE_URL + "getrequests?";
		public static final String RESPOND_REQUESTS = BASE_URL
				+ "respondrequest";
		public static final String UPDATE_PROVIDER_LOCATION = BASE_URL
				+ "location";
		public static final String CHECK_REQUEST_STATUS = BASE_URL
				+ "getrequest?";
		public static final String REQUEST_IN_PROGRESS = BASE_URL
				+ "requestinprogress?";
		public static final String WALKER_STARTED = BASE_URL
				+ "requestwalkerstarted";
		public static final String WALK_ARRIVED = BASE_URL
				+ "requestwalkerarrived";
		public static final String WALK_STARTED = BASE_URL
				+ "requestwalkstarted";
		public static final String WALK_COMPLETED = BASE_URL
				+ "requestwalkcompleted";
		public static final String RATING = BASE_URL + "rating";
		public static final String UPDATE_PROFILE = BASE_URL + "update";
		public static final String HISTORY = BASE_URL + "history?";
		public static final String PATH_REQUEST = BASE_URL + "requestpath?";
		public static final String REQUEST_LOCATION_UPDATE = HOST_URL
				+ "request/location";
		public static final String CHECK_STATE = BASE_URL + "checkstate?";
		public static final String TOGGLE_STATE = BASE_URL + "togglestate";
		public static final String FORGET_PASSWORD = HOST_URL
				+ "application/forgot-password";
		public static final String GET_VEHICAL_TYPES = HOST_URL
				+ "application/types";
		public static final String APPLICATION_PAGES = HOST_URL
				+ "application/pages";
		public static final String LOGOUT = BASE_URL + "logout";
	}

	public class ServiceCode {
		public static final int REGISTER = 1;
		public static final int LOGIN = 2;
		public static final int GET_ALL_REQUEST = 3;
		public static final int RESPOND_REQUEST = 4;
		public static final int CHECK_REQUEST_STATUS = 5;
		public static final int REQUEST_IN_PROGRESS = 6;
		public static final int WALKER_STARTED = 7;
		public static final int WALKER_ARRIVED = 8;
		public static final int WALK_STARTED = 9;
		public static final int WALK_COMPLETED = 10;
		public static final int RATING = 11;
		public static final int GET_ROUTE = 12;
		public static final int APPLICATION_PAGES = 13;
		public static final int UPDATE_PROFILE = 14;
		public static final int GET_VEHICAL_TYPES = 16;
		public static final int FORGET_PASSWORD = 17;
		public static final int HISTORY = 18;
		public static final int CHECK_STATE = 19;
		public static final int TOGGLE_STATE = 20;
		public static final int PATH_REQUEST = 21;
		public static final int DRAW_PATH_ROAD = 22;
		public static final int DRAW_PATH = 23;
		public static final int LOGOUT = 24;
		public static final int DRAW_PATH_CLIENT = 25;
	}

	// webservice key constants
	public class Params {
		public static final String EMAIL = "email";
		public static final String PASSWORD = "password";
		public static final String FIRSTNAME = "first_name";
		public static final String LAST_NAME = "last_name";
		public static final String PHONE = "phone";
		public static final String DEVICE_TOKEN = "device_token";
		public static final String DEVICE_TYPE = "device_type";
		public static final String BIO = "bio";
		public static final String ADDRESS = "address";
		public static final String STATE = "state";
		public static final String COUNTRY = "country";
		public static final String ZIPCODE = "zipcode";
		public static final String TAXI_MODEL = "car_model";
		public static final String TAXI_NUMBER = "car_number";
		public static final String LOGIN_BY = "login_by";
		public static final String SOCIAL_UNIQUE_ID = "social_unique_id";
		public static final String PICTURE = "picture";
		public static final String ID = "id";
		public static final String TOKEN = "token";
		public static final String REQUEST_ID = "request_id";
		public static final String ACCEPTED = "accepted";
		public static final String LATITUDE = "latitude";
		public static final String LONGITUDE = "longitude";
		public static final String DISTANCE = "distance";
		public static final String BEARING = "bearing";
		public static final String COMMENT = "comment";
		public static final String RATING = "rating";
		public static final String INCOMING_REQUESTS = "incoming_requests";
		public static final String TIME_LEFT_TO_RESPOND = "time_left_to_respond";
		public static final String REQUEST = "request";
		public static final String REQUESTS = "requests";
		public static final String REQUEST_DATA = "request_data";
		public static final String NAME = "name";
		public static final String NUM_RATING = "num_rating";
		public static final String OWNER = "owner";
		public static final String WALKER = "walker";
		public static final String UNIQUE_ID = "unique_id";
		public static final String TITLE = "title";
		public static final String CONTENT = "content";
		public static final String INFORMATIONS = "informations";
		public static final String IS_ACTIVE = "is_active";
		public static final String ICON = "icon";
		public static final String TYPE = "type";
		public static final String DISTANCE_COST = "distance_cost";
		public static final String TIME_COST = "time_cost";
		public static final String TOTAL = "total";
		public static final String IS_PAID = "is_paid";
		public static final String TIME = "time";
		public static final String DATE = "date";
		public static final String LOCATION_DATA = "locationdata";
		public static final String START_TIME = "start_time";
		public static final String PAYMENT_TYPE = "payment_type";
		public static final String IS_APPROVED = "is_approved";
		public static final String OLD_PASSWORD = "old_password";
		public static final String NEW_PASSWORD = "new_password";
		public static final String CAR_NUMBER = "car_number";
		public static final String CAR_MODEL = "car_model";
		public static final String REFERRAL_BONUS = "referral_bonus";
		public static final String PROMO_BONUS = "promo_bonus";
		public static final String UNIT = "unit";
		public static final String DESTINATION_LATITUDE = "dest_latitude";
		public static final String DESTINATION_LONGITUDE = "dest_longitude";

	}
}
