package com.comp4905.jasonfleischer.midimusic.views;

import com.comp4905.jasonfleischer.midimusic.MainActivity;
import com.comp4905.jasonfleischer.midimusic.R;
import com.comp4905.jasonfleischer.midimusic.fragments.FragMentManager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class UsbConnection extends LinearLayout{

	public UsbConnection(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void updateUSBConn(final boolean connected){
		final ImageView led = (ImageView) findViewById(R.id.led);
		final TextView usbConnTV = (TextView) findViewById(R.id.usb_connection);
		MainActivity.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(connected){
					usbConnTV.setText(R.string.usb_conn);
					led.setBackgroundResource(R.drawable.ledgreen);
				}else{
					usbConnTV.setText(R.string.usb_disconn );
					led.setBackgroundResource(R.drawable.ledred);
				}

			}
		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		FragMentManager.getInstance().showConsoleFragment();
		return super.onTouchEvent(event);
	}
}
