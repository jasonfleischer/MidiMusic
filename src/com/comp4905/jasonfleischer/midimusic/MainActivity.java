package com.comp4905.jasonfleischer.midimusic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import com.comp4905.jasonfleischer.midimusic.audio.SoundManager;
import com.comp4905.jasonfleischer.midimusic.fragments.FragMentManager;
import com.comp4905.jasonfleischer.midimusic.midi.MidiInputDevice;
import com.comp4905.jasonfleischer.midimusic.midi.MidiListener;
import com.comp4905.jasonfleischer.midimusic.model.DrumSound;
import com.comp4905.jasonfleischer.midimusic.model.Note;
import com.comp4905.jasonfleischer.midimusic.model.Note.NoteName;
import com.comp4905.jasonfleischer.midimusic.util.FileManager;
import com.comp4905.jasonfleischer.midimusic.util.HLog;
import com.comp4905.jasonfleischer.midimusic.R;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

	private static final String TAG= "mainActivity";
	private static final int CHECK_USB_CONN_TIME_MS = 2500;
	private static Timer checkUsbDetachedTimer;
	
	private static MainActivity instance;
	private static UsbManager usbManager; 
	public static MidiMusicConfig config = null; 
	public static MidiInputDevice midiInputDevice;
	
	
	public static Activity getInstance() { return instance; }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		if (savedInstanceState == null) {
			FragMentManager.getInstance().init(getFragmentManager());
		}
		instance = this;
		usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		new BuildModel().execute();
	}
	
	private class BuildModel extends AsyncTask<Void, Integer, Void> {
	    @Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			FragMentManager.getInstance().showInstrumentFragment();
		}
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			FragMentManager.getInstance().updateInitProgress(values[0]);
		}
		protected Void doInBackground(Void... params) {
			publishProgress(0);
			//if(FileManager.getInstance().hasMusicConfigFile()){
				//config = FileManager.getInstance().readMidiMusicConfig();
			//}else{
				config = new MidiMusicConfig();
			//}
				
			
				
			//populate drums Sounds
			for(int i=0;i< config.allDrumSounds.length;i++){
				config.allDrumSounds[i].setSoundId();
			}
			
			// populate notes
			int i = 0;
			int oct = 0;
			int midiV = 21;
			config.setNotes(i++, oct, NoteName.A, midiV++);
			config.setNotes(i++, oct, NoteName.Bb, midiV++);
			config.setNotes(i++, oct, NoteName.B, midiV++);
			for(int j=0;j<7;j++){
				publishProgress(j*16);
				oct++;
				for(NoteName n: NoteName.values()){
					config.setNotes(i++, oct, n, midiV++);
				}
			}
			config.setNotes(i++, ++oct, NoteName.C, midiV++);
			
			SoundManager.getInstance().initMetronome();
			publishProgress(100);
	 		return null;
		}
	}
	
	@Override
	protected void onResume() {
		Log.i(TAG, "onResume");
		super.onResume();
		connectUSBDevice();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "onPause");
    	if(midiInputDevice != null){
    		midiInputDevice.stop();
    		midiInputDevice = null;
    	}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "onStop");
		if(config != null)
			FileManager.getInstance().writeMidiMusicConfig(config);
	}
	
	public static void connectUSBDevice(){
		Log.i(TAG, "connectUSBDevice");

		HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		UsbDevice device = null;
		if(deviceList.isEmpty()){
			//HLog.i("No USB device connected");
			FragMentManager.getInstance().updateUSBConnection(false);
			return;
		}else{	
			device = deviceIterator.next();	 
			int vendorId = device.getVendorId();
			
			if(vendorId == 2235){ // guitar
				
				HLog.i("Guitar detected");
				
				
				UsbDeviceConnection conn = usbManager.openDevice(device);
				// need this??
				conn.controlTransfer(0x40, 0, 0, 0, null, 0, 0);//reset
				conn.controlTransfer(0x40, 0, 1, 0, null, 0, 0);//clear Rx
				conn.controlTransfer(0x40, 0, 2, 0, null, 0, 0);//clear Tx
				conn.controlTransfer(0x40, 0x03, 0x4138, 0, null, 0, 0);//baudrate 9600
				
				UsbEndpoint endPoint = null;
				HLog.i("num of int: "+device.getInterfaceCount());
				
				for(int i=0; i<device.getInterfaceCount(); i++){
					UsbInterface usbIf = device.getInterface(i);
					//HLog.i(i+"  num of ep: "+usbIf.getEndpointCount());
					for(int j=0; j<usbIf.getEndpointCount(); j++){
						UsbEndpoint tempEndPoint = usbIf.getEndpoint(j);
						//HLog.i(j+"  ep details: "+tempEndPoint.getType()+":"+tempEndPoint.getDirection()+":");
						
						if(tempEndPoint.getDirection() == UsbConstants.USB_DIR_IN){
							
							//tempEndPoint.getType() == UsbConstants.USB
							HLog.i("MATCH!!!! "+i+": "+j+" type: "+tempEndPoint.getType());
							//1:0
							//to
							//27:0
							
							//type 0 =  USB_ENDPOINT_XFER_CONTROL
							//type 3 = USB_ENDPOINT_XFER_INT
							
							//all type 1 exc
							//27 type 3
							//endPoint = tempEndPoint;
						}
					}
					
				}
				
				UsbInterface usbIf = device.getInterface(0);
				
				/*for(int i = 0; i < usbIf.getEndpointCount(); i++){ // 1
					if(usbIf.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK && 
						usbIf.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN){
						endPoint = usbIf.getEndpoint(i);
					}
				}
				if(endPoint == null){
					FragMentManager.getInstance().updateUSBConnection(false);
					HLog.i("End point not set");
					return;
				}
				midiInputDevice = new MidiInputDevice(device, conn, usbIf, endPoint, new MidiListener());*/
				
				
			}else if(vendorId == 9319){ // keyboard
				
				HLog.i("Keyboard detected");
				UsbDeviceConnection conn = usbManager.openDevice(device);
				// need this??
				conn.controlTransfer(0x40, 0, 0, 0, null, 0, 0);//reset
				conn.controlTransfer(0x40, 0, 1, 0, null, 0, 0);//clear Rx
				conn.controlTransfer(0x40, 0, 2, 0, null, 0, 0);//clear Tx
				conn.controlTransfer(0x40, 0x03, 0x4138, 0, null, 0, 0);//baudrate 9600

				UsbEndpoint endPoint = null;
				UsbInterface usbIf = device.getInterface(1);	
				for(int i = 0; i < usbIf.getEndpointCount(); i++){ // 1
					if(usbIf.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK && 
						usbIf.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN){
						endPoint = usbIf.getEndpoint(i);
					}
				}
				if(endPoint == null){
					FragMentManager.getInstance().updateUSBConnection(false);
					HLog.i("End point not set");
					return;
				}
				midiInputDevice = new MidiInputDevice(device, conn, usbIf, endPoint, new MidiListener());
				
				//setup device detached listener
				
				checkUsbDetachedTimer = new Timer();
				checkUsbDetachedTimer.scheduleAtFixedRate(new TimerTask() {
					
					@Override
					public void run() {
						if(usbManager.getDeviceList().isEmpty()){
				    		 HLog.i("USB device has been detached");
				    		 
				    		 FragMentManager.getInstance().updateUSBConnection(false);
				    		 midiInputDevice.stop();
				    		 midiInputDevice = null;
				    		 stopCheckUsbDetachedTimer();
				    	 }
					}
				}, 0, CHECK_USB_CONN_TIME_MS);
				
			}else{
				HLog.i("USB device not supported");
			}
		}
	}

	private static void stopCheckUsbDetachedTimer(){
		Log.i(TAG, "stopCheckUsbDetachedTimer");
		if(checkUsbDetachedTimer!=null){
			checkUsbDetachedTimer.purge();
			checkUsbDetachedTimer.cancel();
			checkUsbDetachedTimer = null;
		}
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy");
		for(Note n : config.allNotes){
			n.unLoad();
		}
		
		for(DrumSound d: config.allDrumSounds){
			SoundManager.getInstance().unloadDrumPool(d.getSoundID());
		}
		
		SoundManager.getInstance().unloadMetronome();
		stopCheckUsbDetachedTimer();
	}
}