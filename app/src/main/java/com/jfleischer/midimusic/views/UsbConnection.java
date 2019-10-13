package com.jfleischer.midimusic.views;

import com.jfleischer.midimusic.MainActivity;
import com.jfleischer.midimusic.R;
import com.jfleischer.midimusic.fragments.FragmentController;

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
		final ImageView led = findViewById(R.id.led);
		final TextView usbConnTV = findViewById(R.id.usb_connection);
		MainActivity.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(connected){
					usbConnTV.setText(R.string.usb_conn);
					led.setBackgroundResource(R.drawable.ledgreen);
				}else{
					usbConnTV.setText(R.string.usb_disconnect);
					led.setBackgroundResource(R.drawable.ledred);
				}

			}
		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		performClick();
		FragmentController.getInstance().showConsoleFragment();
		return super.onTouchEvent(event);
	}
}
