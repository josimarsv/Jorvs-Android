package com.automated.taxinow.driver.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View.OnClickListener;

import com.android.volley.Response.ErrorListener;
import com.automated.taxinow.driver.RegisterActivity;

/**
 * @author Kishan H Dhamat
 * 
 */
public abstract class BaseRegisterFragment extends Fragment implements
		OnClickListener, ErrorListener {
	protected RegisterActivity registerActivity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerActivity = (RegisterActivity) getActivity();
	}

	public void startActivityForResult(Intent intent, int requestCode,
			String fragmentTag) {
		registerActivity.startActivityForResult(intent, requestCode,
				fragmentTag);
	}

	@Override
	@Deprecated
	public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(intent, requestCode);
	}

}
