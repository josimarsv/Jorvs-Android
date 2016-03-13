/**
 * 
 */
package com.automated.taxinow.driver.fragment;

import java.util.HashMap;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.automated.taxinow.driver.R;
import com.automated.taxinow.driver.base.BaseRegisterFragment;
import com.automated.taxinow.driver.parse.AsyncTaskCompleteListener;
import com.automated.taxinow.driver.parse.ParseContent;
import com.automated.taxinow.driver.parse.VolleyHttpRequest;
import com.automated.taxinow.driver.utills.AndyConstants;
import com.automated.taxinow.driver.utills.AndyUtils;
import com.automated.taxinow.driver.utills.AppLog;
import com.automated.taxinow.driver.widget.MyFontEdittextView;

/**
 * @author Kishan H Dhamat
 * 
 */
public class ForgetPasswordFragment extends BaseRegisterFragment implements
		AsyncTaskCompleteListener {
	private MyFontEdittextView etEmail;
	private RequestQueue requestQueue;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestQueue = Volley.newRequestQueue(registerActivity);
		View forgetView = inflater.inflate(R.layout.fragment_forgetpassword,
				container, false);
		etEmail = (MyFontEdittextView) forgetView
				.findViewById(R.id.etForgetEmail);
		forgetView.findViewById(R.id.tvForgetSubmit).setOnClickListener(this);
		return forgetView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		registerActivity.setActionBarTitle(getResources().getString(
				R.string.text_forget_password));
		etEmail.requestFocus();
		showKeyboard(etEmail);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.tvForgetSubmit:
			if (etEmail.getText().length() == 0) {
				AndyUtils.showToast(
						getResources().getString(R.string.error_empty_email),
						registerActivity);
				return;
			} else if (!AndyUtils.eMailValidation(etEmail.getText().toString())) {
				AndyUtils.showToast(
						getResources().getString(R.string.error_valid_email),
						registerActivity);
				return;
			} else {
				if (!AndyUtils.isNetworkAvailable(registerActivity)) {
					AndyUtils.showToast(
							getResources()
									.getString(R.string.toast_no_internet),
							registerActivity);
					return;
				}
				forgetPassowrd();
			}
			break;
		case R.id.btnActionNotification:
			// OnBackPressed();
			break;

		default:
			break;
		}
	}

	private void forgetPassowrd() {

		AndyUtils.showCustomProgressDialog(registerActivity, "",
				getString(R.string.progress_loading), false);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.FORGET_PASSWORD);
		map.put(AndyConstants.Params.TYPE, 1 + "");
		map.put(AndyConstants.Params.EMAIL, etEmail.getText().toString());
		// new HttpRequester(registerActivity, map,
		// AndyConstants.ServiceCode.FORGET_PASSWORD, this);

		requestQueue.add(new VolleyHttpRequest(Method.POST, map,
				AndyConstants.ServiceCode.FORGET_PASSWORD, this, this));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.uberdriverforx.parse.AsyncTaskCompleteListener#onTaskCompleted(java
	 * .lang.String, int)
	 */
	@Override
	public void onTaskCompleted(String response, int serviceCode) {
		// TODO Auto-generated method stub
		AndyUtils.removeCustomProgressDialog();
		switch (serviceCode) {
		case AndyConstants.ServiceCode.FORGET_PASSWORD:
			AppLog.Log("TAG", "forget res:" + response);
			if (new ParseContent(registerActivity).isSuccess(response)) {
				AndyUtils.showToast(
						getResources().getString(
								R.string.toast_forget_password_success),
						registerActivity);
			}
			break;

		default:
			break;
		}

	}

	public void showKeyboard(View v) {
		InputMethodManager inputManager = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);

		// check if no view has focus:
		// View view = activity.getCurrentFocus();
		// if (view != null) {
		inputManager.showSoftInput(v, InputMethodManager.SHOW_FORCED);
		// }
	}

	@Override
	public void onErrorResponse(VolleyError error) {
		// TODO Auto-generated method stub
		AppLog.Log("TAG", error.getMessage());
	}

}
