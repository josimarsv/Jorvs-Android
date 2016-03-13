/**
 * 
 */
package com.automated.taxinow.driver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.automated.taxinow.driver.adapter.HistoryAdapter;
import com.automated.taxinow.driver.base.ActionBarBaseActivitiy;
import com.automated.taxinow.driver.model.History;
import com.automated.taxinow.driver.parse.AsyncTaskCompleteListener;
import com.automated.taxinow.driver.parse.ParseContent;
import com.automated.taxinow.driver.parse.VolleyHttpRequest;
import com.automated.taxinow.driver.utills.AndyConstants;
import com.automated.taxinow.driver.utills.AndyUtils;
import com.automated.taxinow.driver.utills.AppLog;
import com.automated.taxinow.driver.utills.PreferenceHelper;
import com.hb.views.PinnedSectionListView;

/**
 * @author Kishan H Dhamat
 * 
 */
public class HistoryActivity extends ActionBarBaseActivitiy implements
		OnItemClickListener, AsyncTaskCompleteListener {

	private HistoryAdapter historyAdapter;
	private ArrayList<History> historyList;
	private PreferenceHelper preferenceHelper;
	private ParseContent parseContent;
	private ImageView tvEmptyHistory;
	private TreeSet<Integer> mSeparatorsSet = new TreeSet<Integer>();
	private PinnedSectionListView lvHistory;
	private ArrayList<Date> dateList = new ArrayList<Date>();
	private ArrayList<History> historyListOrg;
	private RequestQueue requestQueue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		// getSupportActionBar().setTitle(getString(R.string.text_history));
		// getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// getSupportActionBar().setHomeButtonEnabled(true);

		requestQueue = Volley.newRequestQueue(this);
		lvHistory = (PinnedSectionListView) findViewById(R.id.lvHistory);
		tvEmptyHistory = (ImageView) findViewById(R.id.tvHistoryEmpty);
		lvHistory.setOnItemClickListener(this);
		historyList = new ArrayList<History>();
		preferenceHelper = new PreferenceHelper(this);
		dateList = new ArrayList<Date>();
		parseContent = new ParseContent(this);
		historyListOrg = new ArrayList<History>();
		setActionBarTitle(getString(R.string.text_history));
		setActionBarIcon(R.drawable.ub__nav_history);

		getHistory();
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

	private void getHistory() {
		if (!AndyUtils.isNetworkAvailable(this)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet), this);
			return;
		}
		AndyUtils.showCustomProgressDialog(this, "",
				getResources().getString(R.string.progress_getting_history),
				false);
		AppLog.Log("Histoy", "UserId : " + preferenceHelper.getUserId()
				+ " Tocken : " + preferenceHelper.getSessionToken());
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL,
				AndyConstants.ServiceType.HISTORY + AndyConstants.Params.ID
						+ "=" + preferenceHelper.getUserId() + "&"
						+ AndyConstants.Params.TOKEN + "="
						+ preferenceHelper.getSessionToken());
		// new HttpRequester(this, map, AndyConstants.ServiceCode.HISTORY, true,
		// this);

		requestQueue.add(new VolleyHttpRequest(Method.GET, map,
				AndyConstants.ServiceCode.HISTORY, this, this));
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		if (mSeparatorsSet.contains(position))
			return;

		History history = historyListOrg.get(position);
		// final Dialog mDialog = new Dialog(this,
		// android.R.style.Theme_Translucent_NoTitleBar);
		// mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//
		// mDialog.getWindow().setBackgroundDrawable(
		// new ColorDrawable(android.graphics.Color.TRANSPARENT));
		// mDialog.setContentView(R.layout.dialog_bill_layout);
		// DecimalFormat decimalFormat = new DecimalFormat("000.00");
		// DecimalFormat perHourFormat = new DecimalFormat("0.0");
		// History history = historyList.get(position);
		// String basePrice = String.valueOf(decimalFormat.format(Double
		// .parseDouble(history.getBasePrice())));
		// String totalCost = String.valueOf(decimalFormat.format(Double
		// .parseDouble(history.getTotal())));
		// String distanceCost = String.valueOf(decimalFormat.format(Double
		// .parseDouble(history.getDistanceCost())));
		// String timeCost = String.valueOf(decimalFormat.format(Double
		// .parseDouble(history.getTimecost())));
		// ((TextView) mDialog.findViewById(R.id.tvBillDistancePerMile))
		// .setText(String.valueOf(perHourFormat.format((Double
		// .parseDouble(history.getDistanceCost()) / Double
		// .parseDouble(history.getDistance()))))
		// + getResources().getString(R.string.text_cost_per_mile));
		// ((TextView) mDialog.findViewById(R.id.tvBillTimePerHour))
		// .setText(String.valueOf(perHourFormat.format((Double
		// .parseDouble(history.getTimecost()) / Double
		// .parseDouble(history.getTime()))))
		// + getResources().getString(R.string.text_cost_per_hour));
		// ((TextView) mDialog.findViewById(R.id.tvbase1)).setText(String
		// .valueOf(basePrice.charAt(0)));
		// ((TextView) mDialog.findViewById(R.id.tvbase2)).setText(String
		// .valueOf(basePrice.charAt(1)));
		// ((TextView) mDialog.findViewById(R.id.tvbase3)).setText(String
		// .valueOf(basePrice.charAt(2)));
		// ((TextView) mDialog.findViewById(R.id.tvBaseP1)).setText(String
		// .valueOf(basePrice.charAt(4)));
		// ((TextView) mDialog.findViewById(R.id.tvBaseP2)).setText(String
		// .valueOf(basePrice.charAt(5)));
		// ((TextView) mDialog.findViewById(R.id.tvDis1)).setText(String
		// .valueOf(distanceCost.charAt(0)));
		// ((TextView) mDialog.findViewById(R.id.tvDis2)).setText(String
		// .valueOf(distanceCost.charAt(1)));
		// ((TextView) mDialog.findViewById(R.id.tvDis3)).setText(String
		// .valueOf(distanceCost.charAt(2)));
		// ((TextView) mDialog.findViewById(R.id.tvDisP1)).setText(String
		// .valueOf(distanceCost.charAt(4)));
		// ((TextView) mDialog.findViewById(R.id.tvDisP2)).setText(String
		// .valueOf(distanceCost.charAt(5)));
		// ((TextView) mDialog.findViewById(R.id.tvTime1)).setText(String
		// .valueOf(timeCost.charAt(0)));
		// ((TextView) mDialog.findViewById(R.id.tvTime2)).setText(String
		// .valueOf(timeCost.charAt(1)));
		// ((TextView) mDialog.findViewById(R.id.tvTime3)).setText(String
		// .valueOf(timeCost.charAt(2)));
		// ((TextView) mDialog.findViewById(R.id.tvTimeP1)).setText(String
		// .valueOf(timeCost.charAt(4)));
		// ((TextView) mDialog.findViewById(R.id.tvTimeP2)).setText(String
		// .valueOf(timeCost.charAt(5)));
		// ((TextView) mDialog.findViewById(R.id.tvTotal1)).setText(String
		// .valueOf(totalCost.charAt(0)));
		// ((TextView) mDialog.findViewById(R.id.tvTotal2)).setText(String
		// .valueOf(totalCost.charAt(1)));
		// ((TextView) mDialog.findViewById(R.id.tvTotal3)).setText(String
		// .valueOf(totalCost.charAt(2)));
		// ((TextView) mDialog.findViewById(R.id.tvTotalP1)).setText(String
		// .valueOf(totalCost.charAt(4)));
		// ((TextView) mDialog.findViewById(R.id.tvTotalP2)).setText(String
		// .valueOf(totalCost.charAt(5)));
		// mDialog.findViewById(R.id.btnBillDialogClose).setOnClickListener(
		// new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// mDialog.dismiss();
		// }
		// });
		// mDialog.setCancelable(true);
		// mDialog.show();

		showBillDialog(history.getBasePrice(), history.getTotal(),
				history.getDistanceCost(), history.getTimecost(),
				history.getDistance(), history.getTime(),
				history.getReferralBonus(), history.getPromoBonus(),
				getString(R.string.text_close));

	}

	@Override
	public void onTaskCompleted(String response, int serviceCode) {
		AndyUtils.removeCustomProgressDialog();
		switch (serviceCode) {
		case AndyConstants.ServiceCode.HISTORY:
			AppLog.Log("TAG", "History Response :" + response);
			if (!parseContent.isSuccess(response)) {
				return;
			}
			historyListOrg.clear();
			historyList.clear();
			dateList.clear();
			parseContent.parseHistory(response, historyList);

			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				final Calendar cal = Calendar.getInstance();

				parseContent.parseHistory(response, historyList);

				Collections.sort(historyList, new Comparator<History>() {
					@Override
					public int compare(History o1, History o2) {

						SimpleDateFormat dateFormat = new SimpleDateFormat(
								"yyyy-MM-dd hh:mm:ss");
						try {
							// date1 = dateFormat.parse(o1.getDate());
							// date2 = dateFormat.parse(o2.getDate());

							String firstStrDate = o1.getDate();
							String secondStrDate = o2.getDate();

							Log.i("firstStrDate 1", "" + firstStrDate);
							Log.i("secondStrDate 2", "" + secondStrDate);
							Date date2 = dateFormat.parse(secondStrDate);
							Date date1 = dateFormat.parse(firstStrDate);
							Log.i("Date 1", "" + date1);
							Log.i("Date 2", "" + date2);
							int value = date2.compareTo(date1);
							Log.i("Value", "" + value);
							return value;
						} catch (ParseException e) {
							e.printStackTrace();
						}
						return 0;
					}
				});
				HashSet<Date> listToSet = new HashSet<Date>();

				for (int i = 0; i < historyList.size(); i++) {
					AppLog.Log("date", historyList.get(i).getDate() + "");
					if (listToSet.add(sdf.parse(historyList.get(i).getDate()))) {
						dateList.add(sdf.parse(historyList.get(i).getDate()));
					}

				}

				for (int i = 0; i < dateList.size(); i++) {

					cal.setTime(dateList.get(i));
					History item = new History();
					item.setDate(sdf.format(dateList.get(i)));
					historyListOrg.add(item);

					mSeparatorsSet.add(historyListOrg.size() - 1);
					for (int j = 0; j < historyList.size(); j++) {
						Calendar messageTime = Calendar.getInstance();
						messageTime.setTime(sdf.parse(historyList.get(j)
								.getDate()));
						if (cal.getTime().compareTo(messageTime.getTime()) == 0) {
							historyListOrg.add(historyList.get(j));
						}
					}
				}
				if (historyList.size() > 0) {
					lvHistory.setVisibility(View.VISIBLE);
					tvEmptyHistory.setVisibility(View.GONE);

				} else {
					lvHistory.setVisibility(View.GONE);
					tvEmptyHistory.setVisibility(View.VISIBLE);
				}
				historyAdapter = new HistoryAdapter(this, historyListOrg,
						mSeparatorsSet);
				lvHistory.setAdapter(historyAdapter);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnActionNotification:
			onBackPressed();
			overridePendingTransition(R.anim.slide_in_left,
					R.anim.slide_out_right);
			break;

		default:
			break;
		}
	}

	@Override
	public void onErrorResponse(VolleyError error) {
		// TODO Auto-generated method stub
		AppLog.Log("TAG", error.getMessage());

	}
}
