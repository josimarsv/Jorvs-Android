package com.example.hybridtest;

import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;

import java.security.PublicKey;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends ActionBarActivity {

	private WebView mWebView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
/*		try
		   {((View)act.findViewById(android.R.id.title).getParent()).setVisibility(View.GONE);
		   }
		catch (Exception e) {}
		act.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		act.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		view.requestLayout();*/
		
		mWebView = (WebView) findViewById(R.id.activity_main_webview);
		WebSettings websettings = mWebView.getSettings();
		
		websettings.setJavaScriptEnabled(true);
		mWebView.loadUrl("https://teamtreehouse.com/");
		
		//mWebView.setWebViewClient(new WebViewClient()); // prevents the url open in browser
		mWebView.setWebViewClient(new MyAppWebViewClient(){
			
			@Override
			public void onPageFinished(WebView view, String url){
			//hide spash
			findViewById(R.id.splashLoading1).setVisibility(view.GONE);
			//show web view
			findViewById(R.id.activity_main_webview).setVisibility(view.VISIBLE);
			}
			
		
		});
		
	}

	
	@Override
	public void onBackPressed() {
		if(mWebView.canGoBack()){
			mWebView.goBack();
		}
		else{
			super.onBackPressed();
		}
		
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	private class MyAppWebViewClient extends WebViewClient{
		
	}
}
