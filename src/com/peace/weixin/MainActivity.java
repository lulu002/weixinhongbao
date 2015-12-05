package com.peace.weixin;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;

public class MainActivity extends Activity {
	private final Intent mAccessibleIntent = new Intent(
			Settings.ACTION_ACCESSIBILITY_SETTINGS);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		hideSystemTitle();
		setContentView(R.layout.activity_main);
	}

	public void onButtonClicked(View view) {
		startActivity(mAccessibleIntent);
	}

	protected void onResume() {
		/** * 设置为竖屏 */
		if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		super.onResume();
	}

	/***** 隐藏标题 ****/
	protected void hideSystemTitle() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}
}
