package com.example.hybridtest;

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MyAppWebViewClient extends WebViewClient {
	
	public boolean shouldOverrideUriLoading(WebView view, String url){
		
		if(Uri.parse(url).getHost().endsWith("teamtreehouse.com")){
			return false;
		}
		
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		view.getContext().startActivity(intent);
		return true;
	}

}
