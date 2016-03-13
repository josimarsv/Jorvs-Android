/**
 * 
 */
package com.automated.taxinow.driver;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.automated.taxinow.driver.adapter.DrawerAdapter;
import com.automated.taxinow.driver.base.ActionBarBaseActivitiy;
import com.automated.taxinow.driver.model.ApplicationPages;
import com.automated.taxinow.driver.parse.AsyncTaskCompleteListener;
import com.automated.taxinow.driver.parse.ParseContent;
import com.automated.taxinow.driver.parse.VolleyHttpRequest;
import com.automated.taxinow.driver.utills.AndyConstants;
import com.automated.taxinow.driver.utills.AndyUtils;
import com.automated.taxinow.driver.utills.AppLog;

/**
 * @author Kishan H Dhamat
 * 
 */
public class HelpActivity extends ActionBarBaseActivitiy implements
		OnItemClickListener, AsyncTaskCompleteListener {

	private ParseContent parseContent;
	private ListView lvHelpMenu;
	private ArrayList<ApplicationPages> listMenu;
	private DrawerAdapter adapter;
	private ImageView tvNoHistory;
	private RequestQueue requestQueue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		lvHelpMenu = (ListView) findViewById(R.id.lvHelpMenu);
		lvHelpMenu.setOnItemClickListener(this);
		tvNoHistory = (ImageView) findViewById(R.id.ivEmptyView);
		listMenu = new ArrayList<ApplicationPages>();
		adapter = new DrawerAdapter(this, listMenu);
		lvHelpMenu.setAdapter(adapter);
		parseContent = new ParseContent(this);
		requestQueue = Volley.newRequestQueue(this);
		getHelpMenus();
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

	private void getHelpMenus() {
		if (!AndyUtils.isNetworkAvailable(this)) {
			AndyUtils.showToast(
					getResources().getString(R.string.dialog_no_inter_message),
					this);
			return;
		}
		AndyUtils.showCustomProgressDialog(this, "",
				getResources().getString(R.string.progress_please_wait), false);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.APPLICATION_PAGES
				+ "?user_type=1");

		// new HttpRequester(this, map,
		// AndyConstants.ServiceCode.APPLICATION_PAGES, true, this);

		requestQueue.add(new VolleyHttpRequest(Method.GET, map,
				AndyConstants.ServiceCode.APPLICATION_PAGES, this, this));
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		Intent intent = new Intent(this, MenuDescActivity.class);
		intent.putExtra(AndyConstants.Params.TITLE, listMenu.get(position)
				.getTitle());
		intent.putExtra(AndyConstants.Params.CONTENT, listMenu.get(position)
				.getData());
		startActivity(intent);
	}

	@Override
	public void onTaskCompleted(String response, int serviceCode) {
		AndyUtils.removeCustomProgressDialog();
		switch (serviceCode) {
		case AndyConstants.ServiceCode.APPLICATION_PAGES:
			AppLog.Log("HelpActivity", "" + response);
			listMenu.clear();
			parseContent.parsePages(listMenu, response);
			if (listMenu.size() > 0) {
				lvHelpMenu.setVisibility(View.VISIBLE);
				tvNoHistory.setVisibility(View.GONE);
			} else {
				lvHelpMenu.setVisibility(View.GONE);
				tvNoHistory.setVisibility(View.VISIBLE);
			}
			adapter.notifyDataSetChanged();
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
