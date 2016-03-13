package com.automated.taxinow.driver;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.callback.ImageOptions;
import com.automated.taxinow.driver.base.ActionBarBaseActivitiy;
import com.automated.taxinow.driver.db.DBHelper;
import com.automated.taxinow.driver.model.User;
import com.automated.taxinow.driver.parse.AsyncTaskCompleteListener;
import com.automated.taxinow.driver.parse.MultiPartRequester;
import com.automated.taxinow.driver.parse.ParseContent;
import com.automated.taxinow.driver.utills.AndyConstants;
import com.automated.taxinow.driver.utills.AndyUtils;
import com.automated.taxinow.driver.utills.AppLog;
import com.automated.taxinow.driver.utills.PreferenceHelper;
import com.automated.taxinow.driver.widget.MyFontButton;
import com.automated.taxinow.driver.widget.MyFontEdittextView;
import com.automated.taxinow.driver.widget.MyFontTextView;
import com.soundcloud.android.crop.Crop;

/**
 * @author Kishan H Dhamat
 * 
 */
public class ProfileActivity extends ActionBarBaseActivitiy implements
		OnClickListener, AsyncTaskCompleteListener {
	private MyFontEdittextView etProfileFname, etProfileLName, etProfileEmail,
			etProfileNumber, etProfileAddress, etProfileBio, etProfileZipcode,
			etProfileCurrentPassword, etProfileNewPassword,
			etProfileRetypePassword, etProfileModel, etProfileVehicleNo;
	private ImageView ivProfile, btnActionMenu;
	private MyFontButton tvProfileSubmit, btnProfileModelInfo,
			btnProfileEmailInfo, btnProfileVehicleNoInfo;
	private MyFontTextView tvPopupMsg;
	private DBHelper dbHelper;
	private Uri uri = null;
	private AQuery aQuery;
	private String profileImageData, profileImageFilePath, loginType;
	private Bitmap profilePicBitmap;
	private PreferenceHelper preferenceHelper;
	private ImageOptions imageOptions;
	private final String TAG = "profileActivity";
	private ParseContent parseContent;
	private PopupWindow registerInfoPopup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		// getSupportActionBar().setTitle(getString(R.string.text_profile));
		// getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// getSupportActionBar().setHomeButtonEnabled(true);
		// findViewById(R.id.llProfileSocial).setVisibility(View.GONE);
		// findViewById(R.id.etProfilePassword).setVisibility(View.GONE);
		// findViewById(R.id.tvProfileSubmit).setVisibility(View.GONE);
		findViewById(R.id.tvProfileCountryCode).setVisibility(View.GONE);
		// findViewById(R.id.llseprateView).setVisibility(View.GONE);
		etProfileFname = (MyFontEdittextView) findViewById(R.id.etProfileFName);
		etProfileLName = (MyFontEdittextView) findViewById(R.id.etProfileLName);
		etProfileEmail = (MyFontEdittextView) findViewById(R.id.etProfileEmail);
		etProfileCurrentPassword = (MyFontEdittextView) findViewById(R.id.etProfileCurrentPassword);
		etProfileNewPassword = (MyFontEdittextView) findViewById(R.id.etProfileNewPassword);
		etProfileRetypePassword = (MyFontEdittextView) findViewById(R.id.etProfileRetypePassword);
		etProfileNumber = (MyFontEdittextView) findViewById(R.id.etProfileNumber);
		etProfileBio = (MyFontEdittextView) findViewById(R.id.etProfileBio);
		etProfileAddress = (MyFontEdittextView) findViewById(R.id.etProfileAddress);
		etProfileZipcode = (MyFontEdittextView) findViewById(R.id.etProfileZipCode);
		etProfileModel = (MyFontEdittextView) findViewById(R.id.etProfileModel);
		etProfileVehicleNo = (MyFontEdittextView) findViewById(R.id.etProfileVehicleNo);
		tvProfileSubmit = (MyFontButton) findViewById(R.id.tvProfileSubmit);
		ivProfile = (ImageView) findViewById(R.id.ivProfileProfile);
		btnProfileEmailInfo = (MyFontButton) findViewById(R.id.btnProfileEmailInfo);
		btnProfileModelInfo = (MyFontButton) findViewById(R.id.btnProfileModelInfo);
		btnProfileVehicleNoInfo = (MyFontButton) findViewById(R.id.btnProfileVehicleNoInfo);
		btnActionMenu = (ImageButton) findViewById(R.id.btnActionMenu);
		btnActionMenu.setVisibility(View.VISIBLE);
		setActionBarTitle(getString(R.string.text_profile));
		setActionBarIcon(R.drawable.nav_profile);
		ivProfile.setOnClickListener(this);
		tvProfileSubmit.setOnClickListener(this);
		btnProfileEmailInfo.setOnClickListener(this);
		btnProfileModelInfo.setOnClickListener(this);
		btnProfileVehicleNoInfo.setOnClickListener(this);
		tvProfileSubmit.setText(getResources().getString(
				R.string.text_edit_profile));
		parseContent = new ParseContent(this);
		preferenceHelper = new PreferenceHelper(this);
		// socialId = preferenceHelper.getSocialId();
		loginType = preferenceHelper.getLoginBy();
		if (loginType.equals(AndyConstants.MANUAL)) {
			etProfileCurrentPassword.setVisibility(View.VISIBLE);
			// etProfileCurrentPassword.setText(preferenceHelper.getPassword());
		}

		// popup
		LayoutInflater inflate = LayoutInflater.from(this);
		RelativeLayout layout = (RelativeLayout) inflate.inflate(
				R.layout.popup_notification_window, null);
		tvPopupMsg = (MyFontTextView) layout.findViewById(R.id.tvPopupMsg);
		registerInfoPopup = new PopupWindow(layout, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);

		layout.setOnClickListener(this);
		registerInfoPopup.setBackgroundDrawable(new BitmapDrawable());
		registerInfoPopup.setOutsideTouchable(true);
		aQuery = new AQuery(this);
		disableViews();
		imageOptions = new ImageOptions();
		imageOptions.memCache = true;
		imageOptions.fileCache = true;
		imageOptions.targetWidth = 200;
		imageOptions.fallback = R.drawable.user;
		setData();

	}

	private void disableViews() {
		etProfileFname.setEnabled(false);
		etProfileLName.setEnabled(false);
		etProfileEmail.setEnabled(false);
		etProfileNumber.setEnabled(false);
		etProfileBio.setEnabled(false);
		etProfileAddress.setEnabled(false);
		etProfileZipcode.setEnabled(false);
		etProfileCurrentPassword.setEnabled(false);
		etProfileNewPassword.setEnabled(false);
		etProfileRetypePassword.setEnabled(false);
		etProfileModel.setEnabled(false);
		etProfileVehicleNo.setEnabled(false);
		ivProfile.setEnabled(false);
	}

	private void enableViews() {
		etProfileFname.setEnabled(true);
		etProfileLName.setEnabled(true);
		// etProfileEmail.setEnabled(true);
		etProfileNumber.setEnabled(true);
		etProfileBio.setEnabled(true);
		etProfileAddress.setEnabled(true);
		etProfileZipcode.setEnabled(true);
		etProfileCurrentPassword.setEnabled(true);
		etProfileNewPassword.setEnabled(true);
		etProfileRetypePassword.setEnabled(true);
		ivProfile.setEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void setData() {
		dbHelper = new DBHelper(getApplicationContext());
		final User user = dbHelper.getUser();

		aQuery.id(ivProfile).progress(R.id.pBar).image(user.getPicture(), imageOptions);
		etProfileFname.setText(user.getFname());
		etProfileLName.setText(user.getLname());
		etProfileEmail.setText(user.getEmail());
		etProfileNumber.setText(user.getContact());
		etProfileBio.setText(user.getBio());
		etProfileAddress.setText(user.getAddress());
		etProfileZipcode.setText(user.getZipcode());
		etProfileModel.setText(user.getCarModel());
		etProfileVehicleNo.setText(user.getCarNumber());
		if (user != null && !TextUtils.isEmpty(user.getPicture())) {
			aQuery.id(R.id.ivProfileProfile).image(user.getPicture(), true,
					true, 200, 0, new BitmapAjaxCallback() {
						@Override
						public void callback(String url, ImageView iv,
								Bitmap bm, AjaxStatus status) {
							File f=aQuery.getCachedFile(user.getPicture());
							if (f!=null && !TextUtils.isEmpty(f.getAbsolutePath())) {
								AppLog.Log(TAG, "URL FROM AQUERY::" + url);
								profileImageData = f.getAbsolutePath();
								AppLog.Log(TAG, "URL path FROM AQUERY::" + url);
							}
							iv.setImageBitmap(bm);
						}
					});
		}
	}

	private void onUpdateButtonClick() {
		if (etProfileFname.getText().length() == 0) {
			AndyUtils.showToast(
					getResources().getString(R.string.error_empty_fname), this);
			return;
		} else if (etProfileLName.getText().length() == 0) {
			AndyUtils.showToast(
					getResources().getString(R.string.error_empty_lname), this);
			return;
		} else if (etProfileEmail.getText().length() == 0) {
			AndyUtils.showToast(
					getResources().getString(R.string.error_empty_email), this);
			return;
		} else if (!AndyUtils.eMailValidation(etProfileEmail.getText()
				.toString())) {
			AndyUtils.showToast(
					getResources().getString(R.string.error_valid_email), this);
			return;

		} else if (etProfileCurrentPassword.getVisibility() == View.VISIBLE) {
			if (!TextUtils.isEmpty(etProfileNewPassword.getText())) {
				if (etProfileNewPassword.getText().length() < 6) {
					AndyUtils.showToast(
							getResources().getString(
									R.string.error_valid_password), this);
					return;
				} else if (TextUtils
						.isEmpty(etProfileCurrentPassword.getText())) {
					AndyUtils.showToast(
							getResources().getString(
									R.string.error_empty_password), this);
					return;
				} else if (etProfileCurrentPassword.getText().length() < 6) {
					AndyUtils.showToast(
							getResources().getString(
									R.string.error_valid_password), this);
					return;
				} else if (TextUtils.isEmpty(etProfileRetypePassword.getText())) {
					AndyUtils.showToast(
							getResources().getString(
									R.string.error_empty_retypepassword), this);
					return;
				} else if (!etProfileRetypePassword.getText().toString()
						.equals(etProfileNewPassword.getText().toString())) {
					AndyUtils.showToast(
							getResources().getString(
									R.string.error_mismatch_password), this);
					return;
				}
			} else if (etProfileCurrentPassword.getVisibility() == View.INVISIBLE) {
				etProfileRetypePassword.setVisibility(View.INVISIBLE);
				etProfileRetypePassword.setVisibility(View.INVISIBLE);
			}
			// if (etProfileCurrentPassword.getText().length() > 0) {
			// AndyUtils
			// .showToast(
			// getResources().getString(
			// R.string.error_empty_password), this);
			// return;
			// } else if (etProfileCurrentPassword.getText().length() < 6) {
			// AndyUtils
			// .showToast(
			// getResources().getString(
			// R.string.error_valid_password), this);
			// return;
			// }
		}

		// if (etProfileCurrentPassword.getVisibility() == View.VISIBLE) {
		// if (!TextUtils.isEmpty(profileImageData)) {
		// profileImageData = null;
		// profileImageData = aQuery.getCachedFile(profileImageData)
		// .getPath();
		// }
		// }

		if (etProfileNumber.getText().length() == 0) {
			AndyUtils
					.showToast(
							getResources().getString(
									R.string.error_empty_number), this);
			return;
		} else if (profileImageData == null || profileImageData.equals("")) {
			AndyUtils.showToast(
					getResources().getString(R.string.error_empty_image), this);
			return;
		} else {
			updateSimpleProfile(loginType);
		}
	}

	private void updateSimpleProfile(String type) {

		if (!AndyUtils.isNetworkAvailable(this)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet), this);
			return;
		}

		AndyUtils.showCustomProgressDialog(this, "",
				getResources().getString(R.string.progress_update_profile),
				false);

		if (type.equals(AndyConstants.MANUAL)) {
			AppLog.Log(TAG, "Simple Profile update method");
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(AndyConstants.URL, AndyConstants.ServiceType.UPDATE_PROFILE);
			map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
			map.put(AndyConstants.Params.TOKEN,
					preferenceHelper.getSessionToken());
			map.put(AndyConstants.Params.FIRSTNAME, etProfileFname.getText()
					.toString());
			map.put(AndyConstants.Params.LAST_NAME, etProfileLName.getText()
					.toString());
			map.put(AndyConstants.Params.EMAIL, etProfileEmail.getText()
					.toString());
			map.put(AndyConstants.Params.OLD_PASSWORD, etProfileCurrentPassword
					.getText().toString());
			map.put(AndyConstants.Params.NEW_PASSWORD, etProfileNewPassword
					.getText().toString());
			map.put(AndyConstants.Params.PICTURE, profileImageData);
			map.put(AndyConstants.Params.PHONE, etProfileNumber.getText()
					.toString());
			map.put(AndyConstants.Params.BIO, etProfileBio.getText().toString());
			map.put(AndyConstants.Params.ADDRESS, etProfileAddress.getText()
					.toString());
			map.put(AndyConstants.Params.STATE, "");
			map.put(AndyConstants.Params.COUNTRY, "");
			map.put(AndyConstants.Params.ZIPCODE, etProfileZipcode.getText()
					.toString().trim());
			new MultiPartRequester(this, map,
					AndyConstants.ServiceCode.UPDATE_PROFILE, this);
		} else {
			updateSocialProfile(type);
		}
	}

	private void updateSocialProfile(String loginType) {
		AppLog.Log(TAG, "profile social update  method");
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.UPDATE_PROFILE);
		map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
		map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());
		map.put(AndyConstants.Params.FIRSTNAME, etProfileFname.getText()
				.toString());
		map.put(AndyConstants.Params.LAST_NAME, etProfileLName.getText()
				.toString());
		map.put(AndyConstants.Params.ADDRESS, etProfileAddress.getText()
				.toString());
		map.put(AndyConstants.Params.EMAIL, etProfileEmail.getText().toString());
		map.put(AndyConstants.Params.PHONE, etProfileNumber.getText()
				.toString());
		map.put(AndyConstants.Params.PICTURE, profileImageData);
		map.put(AndyConstants.Params.STATE, "");
		map.put(AndyConstants.Params.COUNTRY, "");
		map.put(AndyConstants.Params.BIO, etProfileBio.getText().toString());
		map.put(AndyConstants.Params.ZIPCODE, etProfileZipcode.getText()
				.toString().trim());
		new MultiPartRequester(this, map,
				AndyConstants.ServiceCode.UPDATE_PROFILE, this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tvProfileSubmit:
			if (tvProfileSubmit
					.getText()
					.toString()
					.equals(getResources()
							.getString(R.string.text_edit_profile))) {
				enableViews();
				etProfileFname.requestFocus();
				tvProfileSubmit.setText(getResources().getString(
						R.string.text_update_profile));
			} else {
				onUpdateButtonClick();
			}
			break;
		case R.id.ivProfileProfile:
			showPictureDialog();
			break;

		case R.id.btnActionNotification:
			onBackPressed();
			overridePendingTransition(R.anim.slide_in_left,
					R.anim.slide_out_right);
			break;

		case R.id.btnProfileEmailInfo:
			openPopup(btnProfileEmailInfo,
					getString(R.string.text_profile_popup));
			break;

		case R.id.btnProfileModelInfo:
			openPopup(btnProfileModelInfo,
					getString(R.string.text_profile_popup));
			break;

		case R.id.btnProfileVehicleNoInfo:
			openPopup(btnProfileVehicleNoInfo,
					getString(R.string.text_profile_popup));
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

	private void showPictureDialog() {
		AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
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

	private void choosePhotoFromGallary() {

		// Intent intent = new Intent();
		// intent.setType("image/*");
		// intent.setAction(Intent.ACTION_GET_CONTENT);
		// intent.addCategory(Intent.CATEGORY_OPENABLE);
		Intent galleryIntent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

		startActivityForResult(galleryIntent, AndyConstants.CHOOSE_PHOTO);

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
		startActivityForResult(cameraIntent, AndyConstants.TAKE_PHOTO);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		// if (requestCode == AndyConstants.CHOOSE_PHOTO) {
		// if (data != null) {
		//
		// Uri contentURI = data.getData();
		// profileImageData = getRealPathFromURI(contentURI);
		// aQuery.id(ivProfile).image(profileImageData, imageOptions);
		// }
		//
		// } else if (requestCode == AndyConstants.TAKE_PHOTO) {
		//
		// if (uri != null) {
		// profileImageFilePath = uri.getPath();
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
		// this,
		// getResources().getString(
		// R.string.toast_unable_to_selct_image),
		// Toast.LENGTH_LONG).show();
		// }
		//
		// } else {
		// Toast.makeText(
		// this,
		// getResources().getString(
		// R.string.toast_unable_to_selct_image),
		// Toast.LENGTH_LONG).show();
		// }
		// }
		if (requestCode == AndyConstants.CHOOSE_PHOTO) {
			if (data != null) {

				Uri contentURI = data.getData();
				AppLog.Log(TAG, "Choose photo on activity result");
				beginCrop(contentURI);
				// profileImageData = getRealPathFromURI(contentURI);
				// aQuery.id(ivProfile).image(profileImageData);
			}

		} else if (requestCode == AndyConstants.TAKE_PHOTO) {

			if (uri != null) {
				profileImageFilePath = uri.getPath();
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
						this,
						this.getResources().getString(
								R.string.toast_unable_to_selct_image),
						Toast.LENGTH_LONG).show();
			}
		} else if (requestCode == Crop.REQUEST_CROP) {
			AppLog.Log(TAG, "Crop photo on activity result");
			handleCrop(resultCode, data);
		}
	}

	private String getRealPathFromURI(Uri contentURI) {
		String result;
		Cursor cursor = getContentResolver().query(contentURI, null, null,
				null, null);

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
	public void onTaskCompleted(String response, int serviceCode) {
		AndyUtils.removeCustomProgressDialog();
		AppLog.Log(TAG, response);
		switch (serviceCode) {
		case AndyConstants.ServiceCode.UPDATE_PROFILE:
			if (!parseContent.isSuccess(response)) {
				return;
			}
			if (parseContent.isSuccessWithId(response)) {
				AndyUtils.showToast(
						getResources().getString(
								R.string.toast_update_profile_success), this);
				new DBHelper(this).deleteUser();
				parseContent.parseUserAndStoreToDb(response);
				new PreferenceHelper(this).putPassword(etProfileCurrentPassword
						.getText().toString());
				onBackPressed();
			}

			break;

		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AndyUtils.removeCustomProgressDialog();
	}

	private void beginCrop(Uri source) {
		// Uri outputUri = Uri.fromFile(new File(registerActivity.getCacheDir(),
		// "cropped"));
		Uri outputUri = Uri.fromFile(new File(Environment
				.getExternalStorageDirectory(), (Calendar.getInstance()
				.getTimeInMillis() + ".jpg")));
		new Crop(source).output(outputUri).asSquare().start(this);
	}

	private void handleCrop(int resultCode, Intent result) {
		if (resultCode == RESULT_OK) {
			AppLog.Log(TAG, "Handle crop");
			profileImageData = getRealPathFromURI(Crop.getOutput(result));
			ivProfile.setImageURI(Crop.getOutput(result));
		} else if (resultCode == Crop.RESULT_ERROR) {
			Toast.makeText(this, Crop.getError(result).getMessage(),
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onErrorResponse(VolleyError error) {
		// TODO Auto-generated method stub

	}
}
