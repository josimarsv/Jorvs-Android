package com.automated.taxinow.driver.fragment;

import java.util.HashMap;

import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.automated.taxinow.driver.MapActivity;
import com.automated.taxinow.driver.R;
import com.automated.taxinow.driver.base.BaseRegisterFragment;
import com.automated.taxinow.driver.parse.AsyncTaskCompleteListener;
import com.automated.taxinow.driver.parse.ParseContent;
import com.automated.taxinow.driver.parse.VolleyHttpRequest;
import com.automated.taxinow.driver.utills.AndyConstants;
import com.automated.taxinow.driver.utills.AndyUtils;
import com.automated.taxinow.driver.utills.AppLog;
import com.automated.taxinow.driver.utills.PreferenceHelper;
import com.automated.taxinow.driver.widget.MyFontEdittextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.Plus.PlusOptions;
import com.google.android.gms.plus.model.people.Person;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.Permission.Type;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnProfileListener;

/**
 * @author Kishan H Dhamat
 * 
 */
public class LoginFragment extends BaseRegisterFragment implements
		OnClickListener, ConnectionCallbacks, OnConnectionFailedListener,
		AsyncTaskCompleteListener {
	private MyFontEdittextView etLoginEmail, etLoginPassword;
	private ImageButton btnFb, btnGplus, btnActionMenu;
	private ConnectionResult mConnectionResult;
	private GoogleApiClient mGoogleApiClient;
	private SimpleFacebook mSimpleFacebook;
	private SimpleFacebookConfiguration facebookConfiguration;
	private ParseContent parseContent;
	private boolean mSignInClicked, mIntentInProgress;
	private final String TAG = "LoginFragment";
	private static final int RC_SIGN_IN = 0;
	private RequestQueue requestQueue;

	Permission[] facebookPermissions = new Permission[] { Permission.EMAIL };

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View loginFragmentView = inflater.inflate(R.layout.fragment_login,
				container, false);
		requestQueue = Volley.newRequestQueue(registerActivity);
		etLoginEmail = (MyFontEdittextView) loginFragmentView
				.findViewById(R.id.etLoginEmail);
		etLoginPassword = (MyFontEdittextView) loginFragmentView
				.findViewById(R.id.etLoginPassword);
		btnFb = (ImageButton) loginFragmentView.findViewById(R.id.btnLoginFb);
		btnGplus = (ImageButton) loginFragmentView
				.findViewById(R.id.btnLoginGplus);
		btnActionMenu = (ImageButton) loginFragmentView
				.findViewById(R.id.btnActionMenu);

		loginFragmentView.findViewById(R.id.tvLoginForgetPassword)
				.setOnClickListener(this);
		loginFragmentView.findViewById(R.id.tvLoginSignin).setOnClickListener(
				this);
		loginFragmentView.findViewById(R.id.btnLoginFb)
				.setOnClickListener(this);
		loginFragmentView.findViewById(R.id.btnLoginGplus).setOnClickListener(
				this);

		return loginFragmentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerActivity.actionBar.show();
		registerActivity.setActionBarTitle(getResources().getString(
				R.string.text_signin));
		registerActivity.btnActionInfo.setVisibility(View.INVISIBLE);
		registerActivity.setActionBarIcon(R.drawable.taxi);
		parseContent = new ParseContent(registerActivity);
		// facebook api initialization
		facebookConfiguration = new SimpleFacebookConfiguration.Builder()
				.setAppId(getResources().getString(R.string.app_id))
				.setNamespace(getResources().getString(R.string.app_name))
				.setPermissions(facebookPermissions).build();
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

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLoginGplus:
			mSignInClicked = true;
			if (!mGoogleApiClient.isConnecting()) {
				mGoogleApiClient.connect();
			}
			break;
		case R.id.btnLoginFb:
			if (!mSimpleFacebook.isLogin()) {

				registerActivity.setFbTag(AndyConstants.LOGIN_FRAGMENT_TAG);
				mSimpleFacebook.login(new OnLoginListener() {

					@Override
					public void onFail(String arg0) {
						Toast.makeText(registerActivity, "fb login failed",
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
						Toast.makeText(registerActivity, "success",
								Toast.LENGTH_SHORT).show();
					}
				});
			} else {
				getFbProfile();
			}
			break;

		case R.id.tvLoginForgetPassword:
			registerActivity.addFragment(new ForgetPasswordFragment(), true,
					AndyConstants.FOREGETPASS_FRAGMENT_TAG, true);
			break;

		case R.id.tvLoginSignin:
			if (etLoginEmail.getText().length() == 0) {
				AndyUtils.showToast(
						getResources().getString(R.string.error_empty_email),
						registerActivity);
				return;
			} else if (!AndyUtils.eMailValidation(etLoginEmail.getText()
					.toString())) {
				AndyUtils.showToast(
						getResources().getString(R.string.error_valid_email),
						registerActivity);
				return;
			} else if (etLoginPassword.getText().length() == 0) {
				AndyUtils
						.showToast(
								getResources().getString(
										R.string.error_empty_password),
								registerActivity);
				return;
			} else {
				login();
			}

			break;

		default:
			break;
		}
	}

	private void login() {
		if (!AndyUtils.isNetworkAvailable(registerActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					registerActivity);
			return;
		}
		AndyUtils.showCustomProgressDialog(registerActivity, "", getResources()
				.getString(R.string.progress_dialog_sign_in), false);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.LOGIN);
		map.put(AndyConstants.Params.EMAIL, etLoginEmail.getText().toString());
		map.put(AndyConstants.Params.PASSWORD, etLoginPassword.getText()
				.toString());
		map.put(AndyConstants.Params.DEVICE_TYPE,
				AndyConstants.DEVICE_TYPE_ANDROID);
		map.put(AndyConstants.Params.DEVICE_TOKEN, new PreferenceHelper(
				registerActivity).getDeviceToken());
		map.put(AndyConstants.Params.LOGIN_BY, AndyConstants.MANUAL);
		// new HttpRequester(registerActivity, map,
		// AndyConstants.ServiceCode.LOGIN, this);
		requestQueue.add(new VolleyHttpRequest(Method.POST, map,
				AndyConstants.ServiceCode.LOGIN, this, this));

	}

	private void loginSocial(String id, String loginType) {
		if (!AndyUtils.isNetworkAvailable(registerActivity)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet),
					registerActivity);
			return;
		}

		AndyUtils.showCustomProgressDialog(registerActivity, "", getResources()
				.getString(R.string.progress_dialog_sign_in), false);

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.LOGIN);
		map.put(AndyConstants.Params.SOCIAL_UNIQUE_ID, id);
		map.put(AndyConstants.Params.DEVICE_TYPE,
				AndyConstants.DEVICE_TYPE_ANDROID);
		map.put(AndyConstants.Params.DEVICE_TOKEN, new PreferenceHelper(
				registerActivity).getDeviceToken());
		map.put(AndyConstants.Params.LOGIN_BY, loginType);
		// new HttpRequester(registerActivity, map,
		// AndyConstants.ServiceCode.LOGIN, this);

		requestQueue.add(new VolleyHttpRequest(Method.POST, map,
				AndyConstants.ServiceCode.LOGIN, this, this));
	}

	private void getFbProfile() {
		AndyUtils.showCustomProgressDialog(registerActivity, "",
				getString(R.string.text_getting_info_facebook), true);
		mSimpleFacebook.getProfile(new OnProfileListener() {
			@Override
			public void onComplete(Profile profile) {
				// AndyUtils.removeSimpleProgressDialog();
				Log.i("Uber", "My profile id = " + profile.getId());
				btnFb.setEnabled(false);
				btnGplus.setEnabled(false);
				AndyUtils.removeCustomProgressDialog();
				loginSocial(profile.getId(), AndyConstants.SOCIAL_FACEBOOK);
			}
		});
	}

	private void resolveSignInError() {
		if (mConnectionResult.hasResolution()) {
			try {
				mIntentInProgress = true;
				registerActivity.startIntentSenderForResult(mConnectionResult
						.getResolution().getIntentSender(), RC_SIGN_IN, null,
						0, 0, 0, AndyConstants.LOGIN_FRAGMENT_TAG);
			} catch (SendIntentException e) {
				// The intent was canceled before it was sent. Return to the
				// default
				// state and attempt to connect to get an updated
				// ConnectionResult.
				mIntentInProgress = false;
				mGoogleApiClient.connect();
			}
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (!mIntentInProgress) {
			// Store the ConnectionResult so that we can use it later when the
			// user clicks
			// 'sign-in'.
			mConnectionResult = result;

			if (mSignInClicked) {
				// The user has already clicked 'sign-in' so we attempt to
				// resolve all
				// errors until the user is signed in, or they cancel.
				resolveSignInError();
			}
		}

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
		} else {
			AppLog.Log("TAG", "on activity result facebook");
			mSimpleFacebook.onActivityResult(registerActivity, requestCode,
					resultCode, data);
			if (mSimpleFacebook.isLogin()) {
				getFbProfile();
			} else {
				Toast.makeText(
						registerActivity,
						getResources().getString(
								R.string.toast_facebook_login_failed),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onConnected(Bundle arg0) {
		// String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
		Person currentPerson = Plus.PeopleApi
				.getCurrentPerson(mGoogleApiClient);

		// String personName = currentPerson.getDisplayName();
		// String personPhoto = currentPerson.getImage().toString();
		// String personGooglePlusProfile = currentPerson.getUrl();
		// Toast.makeText(
		// registerActivity,
		// "email: " + email + "\nName:" + personName + "\n Profile URL:"
		// + personGooglePlusProfile + "\nPhoto:" + personPhoto
		// + "\nBirthday:" + currentPerson.getBirthday()
		// + "\n GENDER: " + currentPerson.getGender(),
		// Toast.LENGTH_LONG).show();
		btnGplus.setEnabled(false);
		btnFb.setEnabled(false);
		AndyUtils.removeCustomProgressDialog();
		loginSocial(currentPerson.getId(), AndyConstants.SOCIAL_GOOGLE);

	}

	@Override
	public void onConnectionSuspended(int arg0) {

	}

	@Override
	public void onStop() {
		super.onStop();
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		registerActivity.currentFragment = AndyConstants.LOGIN_FRAGMENT_TAG;
		mSimpleFacebook = SimpleFacebook.getInstance(registerActivity);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		AndyUtils.removeCustomProgressDialog();
	}

	@Override
	public void onTaskCompleted(String response, int serviceCode) {
		// TODO Auto-generated method stub
		AndyUtils.removeCustomProgressDialog();
		AppLog.Log(TAG, response);
		switch (serviceCode) {
		case AndyConstants.ServiceCode.LOGIN:
			if (!parseContent.isSuccess(response)) {
				return;
			}
			if (parseContent.isSuccessWithId(response)) {
				parseContent.parseUserAndStoreToDb(response);
				new PreferenceHelper(getActivity()).putPassword(etLoginPassword
						.getText().toString());
				startActivity(new Intent(registerActivity, MapActivity.class));
				registerActivity.finish();
			}
			break;
		default:
			break;
		}

	}

	@Override
	public void onErrorResponse(VolleyError error) {
		// TODO Auto-generated method stub

	}

}
