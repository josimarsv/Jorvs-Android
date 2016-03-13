package com.automated.taxinow.driver.fragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.automated.taxinow.driver.R;
import com.automated.taxinow.driver.adapter.VehicalTypeListAdapter;
import com.automated.taxinow.driver.base.BaseRegisterFragment;
import com.automated.taxinow.driver.model.VehicalType;
import com.automated.taxinow.driver.parse.AsyncTaskCompleteListener;
import com.automated.taxinow.driver.parse.MultiPartRequester;
import com.automated.taxinow.driver.parse.ParseContent;
import com.automated.taxinow.driver.parse.VolleyHttpRequest;
import com.automated.taxinow.driver.utills.AndyConstants;
import com.automated.taxinow.driver.utills.AndyUtils;
import com.automated.taxinow.driver.utills.AppLog;
import com.automated.taxinow.driver.utills.PreferenceHelper;
import com.automated.taxinow.driver.widget.MyFontButton;
import com.automated.taxinow.driver.widget.MyFontEdittextView;
import com.automated.taxinow.driver.widget.MyFontTextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.Plus.PlusOptions;
import com.google.android.gms.plus.model.people.Person;
import com.soundcloud.android.crop.Crop;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.Permission.Type;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.entities.Profile.Properties;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnProfileListener;

/**
 * @author Kishan H Dhamat
 * 
 */
public class RegisterFragment extends BaseRegisterFragment implements
		OnClickListener, ConnectionCallbacks, OnConnectionFailedListener,
		AsyncTaskCompleteListener {
	private MyFontEdittextView etRegisterFname, etRegisterLName,
			etRegisterPassword, etRegisterEmail, etRegisterNumber,
			etRegisterAddress, etRegisterBio, etRegisterZipcode,
			etRegisterModel, etRegisterTaxiNo;
	private MyFontButton btnRegisterEmailInfo, btnRegisterModelInfo,
			btnRegisterTaxiNoInfo;
	private MyFontTextView tvCountryCode, tvPopupMsg;
	private ImageButton btnFb, btnGplus;
	private GridView gvTypes;
	private SlidingDrawer drawer;
	private ImageView ivProfile;
	private boolean mSignInClicked, mIntentInProgress;
	private ConnectionResult mConnectionResult;
	private GoogleApiClient mGoogleApiClient;
	private AQuery aQuery;
	private SimpleFacebook mSimpleFacebook;
	private ParseContent parseContent;
	private ArrayList<String> countryList;
	private String country;
	private Uri uri = null;
	private String profileImageFilePath, loginType = AndyConstants.MANUAL,
			socialId, profileImageData = null, socialProPicUrl;
	private Bitmap profilePicBitmap;
	private ImageOptions profileImageOptions;
	private SimpleFacebookConfiguration facebookConfiguration;
	private ArrayList<VehicalType> listType;
	private VehicalTypeListAdapter adapter;
	private PopupWindow registerInfoPopup;

	private final String TAG = "RegisterFragment";
	private static final int RC_SIGN_IN = 0;
	private int selectedTypePostion = -1;
	private RequestQueue requestQueue;

	Permission[] facebookPermission = new Permission[] { Permission.EMAIL };

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View registerFragmentView = inflater.inflate(R.layout.fragment_register, container, false);

		ivProfile = (ImageView) registerFragmentView
				.findViewById(R.id.ivRegisterProfile);
		etRegisterAddress = (MyFontEdittextView) registerFragmentView
				.findViewById(R.id.etRegisterAddress);
		etRegisterBio = (MyFontEdittextView) registerFragmentView
				.findViewById(R.id.etRegisterBio);
		etRegisterEmail = (MyFontEdittextView) registerFragmentView
				.findViewById(R.id.etRegisterEmail);
		etRegisterFname = (MyFontEdittextView) registerFragmentView
				.findViewById(R.id.etRegisterFName);
		etRegisterLName = (MyFontEdittextView) registerFragmentView
				.findViewById(R.id.etRegisterLName);
		etRegisterNumber = (MyFontEdittextView) registerFragmentView
				.findViewById(R.id.etRegisterNumber);
		etRegisterPassword = (MyFontEdittextView) registerFragmentView
				.findViewById(R.id.etRegisterPassword);
		etRegisterZipcode = (MyFontEdittextView) registerFragmentView
				.findViewById(R.id.etRegisterZipCode);
		etRegisterModel = (MyFontEdittextView) registerFragmentView
				.findViewById(R.id.etRegisterModel);
		etRegisterTaxiNo = (MyFontEdittextView) registerFragmentView
				.findViewById(R.id.etRegisterTaxiNo);

		tvCountryCode = (MyFontTextView) registerFragmentView
				.findViewById(R.id.tvRegisterCountryCode);
		btnRegisterEmailInfo = (MyFontButton) registerFragmentView
				.findViewById(R.id.btnRegisterEmailInfo);
		btnRegisterModelInfo = (MyFontButton) registerFragmentView
				.findViewById(R.id.btnRegisterModelInfo);
		btnRegisterTaxiNoInfo = (MyFontButton) registerFragmentView
				.findViewById(R.id.btnRegisterTaxiNoInfo);
		btnFb = (ImageButton) registerFragmentView
				.findViewById(R.id.btnRegisterFb);
		btnGplus = (ImageButton) registerFragmentView
				.findViewById(R.id.btnRegisterGplus);
		drawer = (SlidingDrawer) registerFragmentView.findViewById(R.id.drawer);

		gvTypes = (GridView) registerFragmentView.findViewById(R.id.gvTypes);
		tvCountryCode.setOnClickListener(this);
		ivProfile.setOnClickListener(this);
		btnRegisterEmailInfo.setOnClickListener(this);
		btnRegisterModelInfo.setOnClickListener(this);
		btnRegisterTaxiNoInfo.setOnClickListener(this);
		registerFragmentView.findViewById(R.id.btnRegisterFb)
				.setOnClickListener(this);
		registerFragmentView.findViewById(R.id.btnRegisterGplus)
				.setOnClickListener(this);
		registerFragmentView.findViewById(R.id.tvRegisterSubmit)
				.setOnClickListener(this);
		facebookConfiguration = new SimpleFacebookConfiguration.Builder()
		.setAppId(getResources().getString(R.string.app_id))
		.setNamespace(getResources().getString(R.string.app_name))
		.setPermissions(facebookPermission).build();
SimpleFacebook.setConfiguration(facebookConfiguration);
		return registerFragmentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerActivity.actionBar.show();
		registerActivity.setActionBarTitle(getResources().getString(
				R.string.text_register));
		registerActivity.setActionBarIcon(R.drawable.taxi);
		registerActivity.btnActionInfo.setVisibility(View.VISIBLE);
		registerActivity.btnActionInfo.setOnClickListener(this);
		parseContent = new ParseContent(registerActivity);

		// popup
		LayoutInflater inflate = LayoutInflater.from(registerActivity);
		RelativeLayout layout = (RelativeLayout) inflate.inflate(
				R.layout.popup_notification_window, null);
		tvPopupMsg = (MyFontTextView) layout.findViewById(R.id.tvPopupMsg);
		registerInfoPopup = new PopupWindow(layout, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);

		layout.setOnClickListener(this);
		registerInfoPopup.setBackgroundDrawable(new BitmapDrawable());
		registerInfoPopup.setOutsideTouchable(true);

		aQuery = new AQuery(registerActivity);
		profileImageOptions = new ImageOptions();
		profileImageOptions.fileCache = true;
		profileImageOptions.memCache = true;
		profileImageOptions.targetWidth = 200;
		profileImageOptions.fallback = R.drawable.user;

		listType = new ArrayList<VehicalType>();
		adapter = new VehicalTypeListAdapter(registerActivity, listType, this);
		gvTypes.setAdapter(adapter);
		gvTypes.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				for (int i = 0; i < listType.size(); i++)
					listType.get(i).isSelected = false;
				listType.get(position).isSelected = true;
				// onItemClick(position);
				selectedTypePostion = position;
				adapter.notifyDataSetChanged();
			}
		});

		countryList = parseContent.parseCountryCodes();
		for (int i = 0; i < countryList.size(); i++) {
			if (countryList.get(i).contains(country)) {
				tvCountryCode.setText((countryList.get(i).substring(0,
						countryList.get(i).indexOf(" "))));
			}
		}
		if (TextUtils.isEmpty(tvCountryCode.getText())) {
			tvCountryCode.setText((countryList.get(0).substring(0, countryList
					.get(0).indexOf(" "))));
		}
		getVehicalTypes();
		// facebook api initialization
		facebookConfiguration = new SimpleFacebookConfiguration.Builder()
				.setAppId(getResources().getString(R.string.app_id))
				.setNamespace(getResources().getString(R.string.app_name))
				.setPermissions(facebookPermission).build();
		SimpleFacebook.setConfiguration(facebookConfiguration);

		// Google plus api initialization
		Scope scope = new Scope(AndyConstants.GOOGLE_API_SCOPE_URL);
		mGoogleApiClient = new GoogleApiClient.Builder(registerActivity)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(Plus.API, PlusOptions.builder().build())
				.addScope(scope).build();

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		country = Locale.getDefault().getDisplayCountry();
		requestQueue = Volley.newRequestQueue(registerActivity);
	}

	@Override
	public void onClick(View v) {
		
		
		switch (v.getId()) {
		
		case R.id.btnRegisterFb:
			socialProPicUrl = null;
			if (!mSimpleFacebook.isLogin()) {
				registerActivity.setFbTag(AndyConstants.REGISTER_FRAGMENT_TAG);
				mSimpleFacebook.login(new OnLoginListener() {

					@Override
					public void onFail(String arg0) {
						Toast.makeText(
								registerActivity,
								getResources().getString(
										R.string.toast_facebook_login_failed),
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onException(Throwable arg0) {
					}

					@Override
					public void onThinking() {
					}

					@Override
					public void onNotAcceptingPermissions(Type arg0) {
						AppLog.Log("UBER",
								String.format(
										"You didn't accept %s permissions",
										arg0.name()));
					}

					@Override
					public void onLogin() {
						// Toast.makeText(registerActivity, "Facebook Success",
						// Toast.LENGTH_SHORT).show();
					}
				});
			} else {
				getFbProfile();
			}
			break;

		case R.id.ivRegisterProfile:
			showPictureDialog();
			break;

		case R.id.btnRegisterGplus:
			mSignInClicked = true;
			if (!mGoogleApiClient.isConnecting()) {
				AndyUtils.showCustomProgressDialog(registerActivity, "",
						getString(R.string.progress_getting_info), false);
				mGoogleApiClient.connect();
			}
			break;

		case R.id.tvRegisterSubmit:
			onRegisterButtonClick();
			break;

		case R.id.tvRegisterCountryCode:
			AlertDialog.Builder countryBuilder = new Builder(registerActivity);
			countryBuilder.setTitle(getResources().getString(
					R.string.dialog_title_country_codes));

			final String[] countryListArray = new String[countryList.size()];
			countryList.toArray(countryListArray);
			countryBuilder.setItems(countryListArray,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							tvCountryCode.setText(countryListArray[which]
									.substring(0, countryListArray[which]
											.indexOf(" ")));
						}
					}).show();
			break;
		case R.id.btnRegisterEmailInfo:
			openPopup(btnRegisterEmailInfo,
					getString(R.string.text_regemail_popup));
			break;

		case R.id.btnRegisterModelInfo:
			openPopup(btnRegisterModelInfo,
					getString(R.string.text_regmodelno_popup));
			break;

		case R.id.btnRegisterTaxiNoInfo:
			openPopup(btnRegisterTaxiNoInfo,
					getString(R.string.text_regtaxino_popup));
			break;

		case R.id.btnActionInfo:
			openPopup(registerActivity.btnActionInfo,
					getString(R.string.text_regaction_popup));
			break;

		default:
			break;
		}
	}

	public void openPopup(View view, String msg) {
		if (registerInfoPopup.isShowing())
			registerInfoPopup.dismiss();
		else {
			registerInfoPopup.showAsDropDown(view);
			tvPopupMsg.setText(msg);
		}

	}

	@SuppressWarnings("deprecation")
	private void onRegisterButtonClick() {
		if (etRegisterFname.getText().length() == 0) {
			AndyUtils.showToast(
					getResources().getString(R.string.error_empty_fname),
					registerActivity);
			return;
		} else if (etRegisterLName.getText().length() == 0) {
			AndyUtils.showToast(
					getResources().getString(R.string.error_empty_lname),
					registerActivity);
			return;
		} else if (etRegisterEmail.getText().length() == 0) {
			AndyUtils.showToast(
					getResources().getString(R.string.error_empty_email),
					registerActivity);
			return;
		} else if (!AndyUtils.eMailValidation(etRegisterEmail.getText()
				.toString())) {
			AndyUtils.showToast(
					getResources().getString(R.string.error_valid_email),
					registerActivity);
			return;
		} else if (etRegisterModel.getText().length() == 0) {
			AndyUtils.showToast(
					getResources().getString(R.string.error_empty_model),
					registerActivity);
			return;
		} else if (etRegisterTaxiNo.getText().length() == 0) {
			AndyUtils.showToast(
					getResources().getString(R.string.error_empty_taxi_no),
					registerActivity);
			return;

		} else if (etRegisterPassword.getVisibility() == View.VISIBLE) {
			if (etRegisterPassword.getText().length() == 0) {
				AndyUtils.showToast(
						getResources().getString(
								R.string.error_empty_reg_password),
						registerActivity);
				return;
			} else if (etRegisterPassword.getText().length() < 6) {
				AndyUtils
						.showToast(
								getResources().getString(
										R.string.error_valid_password),
								registerActivity);
				return;
			}
		}

		if (etRegisterPassword.getVisibility() == View.GONE) {
			if (!TextUtils.isEmpty(socialProPicUrl)) {
				profileImageData = null;
				profileImageData = aQuery.getCachedFile(socialProPicUrl)
						.getPath();
			}
		}

		if (etRegisterNumber.getText().length() == 0) {
			AndyUtils.showToast(
					getResources().getString(R.string.error_empty_number),
					registerActivity);
			return;
			// } else if (profileImageData == null ||
			// profileImageData.equals("")) {
			// AndyUtils.showToast(
			// getResources().getString(R.string.error_empty_image),
			// registerActivity);
			// return;
		} else if (selectedTypePostion == -1) {
			AndyUtils.showToast(
					getResources().getString(R.string.error_empty_type),
					registerActivity);
			drawer.open();
			return;
		} else {
			register(loginType, socialId);
		}
	}

	private void showPictureDialog() {
		AlertDialog.Builder pictureDialog = new AlertDialog.Builder(
				registerActivity);
		pictureDialog.setTitle(getResources().getString(
				R.string.dialog_chhose_photo));
		String[] pictureDialogItems = {
				getResources().getString(R.string.dialog_from_gallery),
				getResources().getString(R.string.dialog_from_camera) };

		pictureDialog.setItems(pictureDialogItems,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {

						case 0:
							choosePhotoFromGallary();
							break;

						case 1:
							takePhotoFromCamera();
							break;

						}
					}
				});
		pictureDialog.show();
	}

	private void resolveSignInError() {
		if (mConnectionResult.hasResolution()) {
			try {
				mIntentInProgress = true;
				registerActivity.startIntentSenderForResult(mConnectionResult
						.getResolution().getIntentSender(), RC_SIGN_IN, null,
						0, 0, 0, AndyConstants.REGISTER_FRAGMENT_TAG);
			} catch (SendIntentException e) {
				/*
				 * The intent was canceled before it was sent. Return to the
				 * default state and attempt to connect to get an updated
				 * ConnectionResult.
				 */
				mIntentInProgress = false;
				mGoogleApiClient.connect();
			}
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (!mIntentInProgress) {
			mConnectionResult = result;
			if (mSignInClicked) {
				resolveSignInError();
			}
		}

	}

	private void getFbProfile() {
		AndyUtils.showCustomProgressDialog(registerActivity, "",
				getString(R.string.text_getting_info_facebook), true);
		Profile.Properties properties = new Profile.Properties.Builder()
		.add(Properties.ID).add(Properties.FIRST_NAME)
		.add(Properties.GENDER).add(Properties.EMAIL)
		.add(Properties.LAST_NAME).add(Properties.BIRTHDAY)
		.add(Properties.EDUCATION).add(Properties.PICTURE).build();
		mSimpleFacebook.getProfile(properties,new OnProfileListener() {
			@Override
			public void onComplete(Profile profile) {
				AndyUtils.removeCustomProgressDialog();
				AppLog.Log("Uber", "My profile id = " + profile.getId());
				btnFb.setEnabled(false);
				btnGplus.setEnabled(false);
				etRegisterEmail.setText(profile.getEmail());
				etRegisterFname.setText(profile.getFirstName());
				etRegisterLName.setText(profile.getLastName());
				socialId = profile.getId();
				loginType = AndyConstants.SOCIAL_FACEBOOK;
				// etRegisterPassword.setEnabled(false);
				etRegisterPassword.setVisibility(View.GONE);

				if (!TextUtils.isEmpty(profile.getPicture())
						|| !profile.getPicture().equalsIgnoreCase("null")) {
					socialProPicUrl = profile.getPicture();
					aQuery.id(ivProfile).image(profile.getPicture(),
							profileImageOptions);
				} else {
					socialProPicUrl = null;
				}

			}
		});
	}

	public void onItemClick(int pos) {
		selectedTypePostion = pos;

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RC_SIGN_IN) {

			if (resultCode != registerActivity.RESULT_OK) {
				mSignInClicked = false;
			}

			mIntentInProgress = false;

			if (!mGoogleApiClient.isConnecting()) {
				mGoogleApiClient.connect();
			}
		} else if (requestCode == AndyConstants.CHOOSE_PHOTO) {
			if (data != null) {

				Uri contentURI = data.getData();
				registerActivity.setFbTag(AndyConstants.REGISTER_FRAGMENT_TAG);
				AppLog.Log(TAG, "Choose photo on activity result");
				beginCrop(contentURI);
				// profileImageData = getRealPathFromURI(contentURI);
				// aQuery.id(ivProfile).image(profileImageData);
			}

		} else if (requestCode == AndyConstants.TAKE_PHOTO) {

			if (uri != null) {
				profileImageFilePath = uri.getPath();
				registerActivity.setFbTag(AndyConstants.REGISTER_FRAGMENT_TAG);
				AppLog.Log(TAG, "Take photo on activity result");
				beginCrop(uri);
				// if (profileImageFilePath != null
				// && profileImageFilePath.length() > 0) {
				// File myFile = new File(profileImageFilePath);
				//
				// try {
				// if (profilePicBitmap != null)
				// profilePicBitmap.recycle();
				// profilePicBitmap = new ImageHelper().decodeFile(myFile);
				// } catch (OutOfMemoryError e) {
				// System.out.println("out of bound");
				// }
				// ivProfile.setImageBitmap(profilePicBitmap);
				// profileImageData = profileImageFilePath;
				// System.out.println(profileImageData);
				// } else {
				// Toast.makeText(
				// registerActivity,
				// registerActivity.getResources().getString(
				// R.string.toast_unable_to_selct_image),
				// Toast.LENGTH_LONG).show();
				// }

			} else {
				Toast.makeText(
						registerActivity,
						registerActivity.getResources().getString(
								R.string.toast_unable_to_selct_image),
						Toast.LENGTH_LONG).show();
			}
		} else if (requestCode == Crop.REQUEST_CROP) {
			AppLog.Log(TAG, "Crop photo on activity result");
			handleCrop(resultCode, data);
		} else {
			mSimpleFacebook.onActivityResult(registerActivity, requestCode,
					resultCode, data);
			if (mSimpleFacebook.isLogin()) {
				getFbProfile();
			} else {
				Toast.makeText(
						registerActivity,
						registerActivity.getResources().getString(
								R.string.toast_facebook_login_failed),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void choosePhotoFromGallary() {

		// Intent intent = new Intent();
		// intent.setType("image/*");
		// intent.setAction(Intent.ACTION_GET_CONTENT);
		// intent.addCategory(Intent.CATEGORY_OPENABLE);
		Intent galleryIntent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		registerActivity
				.startActivityForResult(galleryIntent,
						AndyConstants.CHOOSE_PHOTO,
						AndyConstants.REGISTER_FRAGMENT_TAG);

	}

	private void takePhotoFromCamera() {
		Calendar cal = Calendar.getInstance();
		File file = new File(Environment.getExternalStorageDirectory(),
				(cal.getTimeInMillis() + ".jpg"));

		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {

			file.delete();
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		uri = Uri.fromFile(file);
		Intent cameraIntent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		registerActivity.startActivityForResult(cameraIntent,
				AndyConstants.TAKE_PHOTO, AndyConstants.REGISTER_FRAGMENT_TAG);
	}

	@Override
	public void onConnected(Bundle arg0) {
		AndyUtils.removeCustomProgressDialog();
		String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
		Person currentPerson = Plus.PeopleApi
				.getCurrentPerson(mGoogleApiClient);

		String personName = currentPerson.getDisplayName();
		String personPhoto = currentPerson.getImage().toString();

		btnGplus.setEnabled(false);
		btnFb.setEnabled(false);
		etRegisterEmail.setText(email);
		if (personName.contains(" ")) {
			String[] split = personName.split(" ");
			etRegisterFname.setText(split[0]);
			etRegisterLName.setText(split[1]);
		} else {
			etRegisterFname.setText(personName);
		}

		// etRegisterPassword.setEnabled(false);
		etRegisterPassword.setVisibility(View.GONE);
		if (!TextUtils.isEmpty(personPhoto)
				|| !personPhoto.equalsIgnoreCase("null")) {
			//socialProPicUrl = personPhoto;
			aQuery.id(ivProfile).image(personPhoto, profileImageOptions);
		} else {
			socialProPicUrl = null;
		}

		socialId = currentPerson.getId();
		loginType = AndyConstants.SOCIAL_GOOGLE;
		// etRegisterPassword.setEnabled(false);
		etRegisterPassword.setVisibility(View.GONE);
	}

	private void register(String type, String id) {

		if (!AndyUtils.isNetworkAvailable(registerActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					registerActivity);
			return;
		}

		AndyUtils.showCustomProgressDialog(registerActivity, "", getResources()
				.getString(R.string.progress_dialog_register), false);

		if (type.equals(AndyConstants.MANUAL)) {
			AppLog.Log(TAG, "Simple Register method");
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(AndyConstants.URL, AndyConstants.ServiceType.REGISTER);
			map.put(AndyConstants.Params.FIRSTNAME, etRegisterFname.getText()
					.toString());
			map.put(AndyConstants.Params.LAST_NAME, etRegisterLName.getText()
					.toString());
			map.put(AndyConstants.Params.EMAIL, etRegisterEmail.getText()
					.toString());
			map.put(AndyConstants.Params.PASSWORD, etRegisterPassword.getText()
					.toString());

			if (!TextUtils.isEmpty(profileImageData)) {
				map.put(AndyConstants.Params.PICTURE, profileImageData);
			}
			map.put(AndyConstants.Params.PHONE, tvCountryCode.getText()
					.toString().trim()
					+ etRegisterNumber.getText().toString());
			map.put(AndyConstants.Params.BIO, etRegisterBio.getText()
					.toString());
			map.put(AndyConstants.Params.ADDRESS, etRegisterAddress.getText()
					.toString());
			map.put(AndyConstants.Params.STATE, "");
			map.put(AndyConstants.Params.COUNTRY, "");
			map.put(AndyConstants.Params.ZIPCODE, etRegisterZipcode.getText()
					.toString().trim());
			map.put(AndyConstants.Params.TYPE,
					String.valueOf(listType.get(selectedTypePostion).getId()));
			map.put(AndyConstants.Params.DEVICE_TYPE,
					AndyConstants.DEVICE_TYPE_ANDROID);
			map.put(AndyConstants.Params.DEVICE_TOKEN, new PreferenceHelper(
					registerActivity).getDeviceToken());
			map.put(AndyConstants.Params.TAXI_MODEL, etRegisterModel.getText()
					.toString().trim());
			map.put(AndyConstants.Params.TAXI_NUMBER, etRegisterTaxiNo
					.getText().toString().trim());
			map.put(AndyConstants.Params.LOGIN_BY, AndyConstants.MANUAL);
			new MultiPartRequester(registerActivity, map,
					AndyConstants.ServiceCode.REGISTER, this);
		} else {
			registerSoicial(id, type);
		}
	}

	private void registerSoicial(String id, String loginType) {
		AppLog.Log(TAG, "Register social method");
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.REGISTER);
		map.put(AndyConstants.Params.FIRSTNAME, etRegisterFname.getText()
				.toString());
		map.put(AndyConstants.Params.LAST_NAME, etRegisterLName.getText()
				.toString());
		map.put(AndyConstants.Params.ADDRESS, etRegisterAddress.getText()
				.toString());
		map.put(AndyConstants.Params.EMAIL, etRegisterEmail.getText()
				.toString());
		map.put(AndyConstants.Params.PHONE, tvCountryCode.getText().toString()
				.trim()
				+ etRegisterNumber.getText().toString());
		if (!TextUtils.isEmpty(profileImageData)) {
			map.put(AndyConstants.Params.PICTURE, profileImageData);
		}
		map.put(AndyConstants.Params.STATE, "");
		map.put(AndyConstants.Params.TYPE,
				String.valueOf(listType.get(selectedTypePostion).getId()));
		map.put(AndyConstants.Params.COUNTRY, "");
		map.put(AndyConstants.Params.BIO, etRegisterBio.getText().toString());
		map.put(AndyConstants.Params.DEVICE_TYPE,
				AndyConstants.DEVICE_TYPE_ANDROID);
		map.put(AndyConstants.Params.DEVICE_TOKEN, new PreferenceHelper(
				registerActivity).getDeviceToken());
		map.put(AndyConstants.Params.ZIPCODE, etRegisterZipcode.getText()
				.toString().trim());
		map.put(AndyConstants.Params.TAXI_MODEL, etRegisterModel.getText()
				.toString().trim());
		map.put(AndyConstants.Params.TAXI_NUMBER, etRegisterTaxiNo.getText()
				.toString().trim());
		map.put(AndyConstants.Params.SOCIAL_UNIQUE_ID, id);
		map.put(AndyConstants.Params.LOGIN_BY, loginType);
		new MultiPartRequester(registerActivity, map,
				AndyConstants.ServiceCode.REGISTER, this);

	}

	@Override
	public void onConnectionSuspended(int arg0) {
	}

	@Override
	public void onTaskCompleted(String response, int serviceCode) {
		AndyUtils.removeCustomProgressDialog();

		switch (serviceCode) {
		case AndyConstants.ServiceCode.GET_VEHICAL_TYPES:
			AppLog.Log(TAG, "Vehicle types  " + response);
			if (parseContent.isSuccess(response)) {
				parseContent.parseTypes(response, listType);
				adapter.notifyDataSetChanged();
			}
			AndyUtils.removeCustomProgressDialog();
			break;

		case AndyConstants.ServiceCode.REGISTER:
			AppLog.Log(TAG, "Register response :" + response);

			if (parseContent.isSuccess(response)) {
				AndyUtils.showToast(
						registerActivity.getResources().getString(
								R.string.toast_register_success),
						registerActivity);
				// parseContent.parseUserAndStoreToDb(response);
				// new PreferenceHelper(getActivity())
				// .putPassword(etRegisterPassword.getText().toString());
				// Intent intent = new Intent(registerActivity,
				// MapActivity.class);
				// startActivity(intent);
				registerActivity.addFragment(new LoginFragment(), false,
						AndyConstants.LOGIN_FRAGMENT_TAG, false);
				// registerActivity.finish();
			}
			break;

		default:
			break;
		}

	}

	private String getRealPathFromURI(Uri contentURI) {
		String result;
		Cursor cursor = registerActivity.getContentResolver().query(contentURI,
				null, null, null, null);

		if (cursor == null) { // Source is Dropbox or other similar local file
								// path
			result = contentURI.getPath();
		} else {
			cursor.moveToFirst();
			int idx = cursor
					.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
			result = cursor.getString(idx);
			cursor.close();
		}
		return result;
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		AndyUtils.removeCustomProgressDialog();
	}

	private void getVehicalTypes() {
		AndyUtils.showCustomProgressDialog(registerActivity, "", getResources()
				.getString(R.string.progress_getting_types), false);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.GET_VEHICAL_TYPES);
		AppLog.Log(TAG, AndyConstants.URL);
		// new HttpRequester(registerActivity, map,
		// AndyConstants.ServiceCode.GET_VEHICAL_TYPES, true, this);

		requestQueue.add(new VolleyHttpRequest(Method.GET, map,
				AndyConstants.ServiceCode.GET_VEHICAL_TYPES, this, this));
	}

	@Override
	public void onResume() {
		super.onResume();
		registerActivity.currentFragment = AndyConstants.REGISTER_FRAGMENT_TAG;
		mSimpleFacebook = SimpleFacebook.getInstance(registerActivity);
	}

	private void beginCrop(Uri source) {
		// Uri outputUri = Uri.fromFile(new File(registerActivity.getCacheDir(),
		// "cropped"));
		Uri outputUri = Uri.fromFile(new File(Environment
				.getExternalStorageDirectory(), (Calendar.getInstance()
				.getTimeInMillis() + ".jpg")));
		new Crop(source).output(outputUri).asSquare().start(registerActivity);
	}

	private void handleCrop(int resultCode, Intent result) {
		if (resultCode == registerActivity.RESULT_OK) {
			AppLog.Log(TAG, "Handle crop");
			profileImageData = getRealPathFromURI(Crop.getOutput(result));
			ivProfile.setImageURI(Crop.getOutput(result));
		} else if (resultCode == Crop.RESULT_ERROR) {
			Toast.makeText(registerActivity,
					Crop.getError(result).getMessage(), Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onErrorResponse(VolleyError error) {
		// TODO Auto-generated method stub

	}
}
