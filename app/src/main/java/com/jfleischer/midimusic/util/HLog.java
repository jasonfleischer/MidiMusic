package com.jfleischer.midimusic.util;

import com.jfleischer.midimusic.MainActivity;

import android.util.Log;
import android.widget.Toast;

public class HLog {

	private final static String TAG = "MidiMusic";

	public static void i(final String s){
		MainActivity.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Log.i(TAG, s);
				Toast.makeText(MainActivity.getInstance(), s, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public static void e(final String s){
		MainActivity.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Log.e(TAG, "ERROR:"+s);
			}
		});
	}
}
