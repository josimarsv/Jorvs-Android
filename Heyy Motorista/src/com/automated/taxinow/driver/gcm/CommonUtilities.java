/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.automated.taxinow.driver.gcm;

import android.content.Context;
import android.content.Intent;

public final class CommonUtilities {

	// public static final String SENDER_ID = "545172754837";

	public static final String SENDER_ID = "470787885801";
	/*public static final String SENDER_ID = "475639683794"; new*/
	public static final String TAG = "GCMDemo";
	public static final String DISPLAY_MESSAGE_ACTION = "com.uberdriverforx.DISPLAY_MESSAGE";
	public static final String DISPLAY_MESSAGE_REGISTER = "com.uberdriverforx.REGISTER_GCM";
	public static final String EXTRA_MESSAGE = "message";
	public static final String RESULT = "result";
	public static final String REGID = "regid";

	public static void displayMessage(Context context, String message) {
		Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
		intent.putExtra(EXTRA_MESSAGE, message);
		context.sendBroadcast(intent);
	}
}
