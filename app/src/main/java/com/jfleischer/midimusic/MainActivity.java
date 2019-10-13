package com.jfleischer.midimusic;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import com.jfleischer.midimusic.audio.SoundManager;
import com.jfleischer.midimusic.fragments.FragmentController;
import com.jfleischer.midimusic.midi.MidiInputDevice;
import com.jfleischer.midimusic.midi.MidiListener;
import com.jfleischer.midimusic.model.DrumSound;
import com.jfleischer.midimusic.model.Note;
import com.jfleischer.midimusic.model.Note.NoteName;
import com.jfleischer.midimusic.util.FileManager;
import com.jfleischer.midimusic.util.HLog;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;


public class MainActivity extends AppCompatActivity {

    private static final String ACTION_USB_PERMISSION = MainActivity.class.getSimpleName();
    private static final int CHECK_USB_CONN_TIME_MS = 2500;
    private static Timer checkUsbDetachedTimer;

    private static MainActivity instance;
    private static UsbManager usbManager;
    public static MidiMusicConfig config = null;
    public static MidiInputDevice midiInputDevice;

    private static PendingIntent mPermissionIntent;

    public static Activity getInstance() { return instance; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if (savedInstanceState == null) {
            FragmentController.getInstance().init(getFragmentManager());
        }
        instance = this;

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);

        connectUSBDevice();

        new BuildModel().execute();
    }

    private class BuildModel extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            FragmentController.getInstance().showInstrumentFragment();

        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            FragmentController.getInstance().updateInitProgress(values[0]);
        }
        protected Void doInBackground(Void... params) {
            publishProgress(0);
            if(FileManager.getInstance().hasMusicConfigFile()){
                config = FileManager.getInstance().readMidiMusicConfig();
                //config = new MidiMusicConfig();
            }else{
                config = new MidiMusicConfig();
            }
            publishProgress(5);
            //populate drums Sounds
            for(int i=0;i< config.allDrumSounds.length;i++){
                publishProgress((int)(5+((45.0f/config.allDrumSounds.length)*i)));
                config.allDrumSounds[i].setSoundId();
            }
            publishProgress(50);
            // populate notes
            int i = 0;
            int oct = 0;
            int midiV = 21;
            config.setNotes(i++, oct, NoteName.A, midiV++);
            config.setNotes(i++, oct, NoteName.Bb, midiV++);
            config.setNotes(i++, oct, NoteName.B, midiV++);
            for(int j=0;j<7;j++){
                publishProgress(51+((50/7)*j));
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

    public static void connectUSBDevice(){
        if(midiInputDevice != null){
            HLog.i(MainActivity.getInstance().getResources().getString(R.string.usb_already_connected));
            return;
        }
        if(usbManager == null){
            usbManager = (UsbManager) instance.getSystemService(Context.USB_SERVICE);
        }

        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        if(deviceList.isEmpty()){
            HLog.i(MainActivity.getInstance().getResources().getString(R.string.no_usb_detected));
            FragmentController.getInstance().updateUSBConnection(false);
        }else{
            UsbDevice device = deviceIterator.next();
            usbManager.requestPermission(device, mPermissionIntent);
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            //call method to set up device communication
                            connectToKeyboard(device);
                        }
                    }
                    else {
                        HLog.i(getResources().getString(R.string.permission_denied_for_usb));
                    }
                }
            }
        }
    };

    private void connectToKeyboard(UsbDevice device){
        int vendorId = device.getVendorId();
        if(vendorId == 9319){ // keyboard

            //HLog.i("Keyboard detected");
            final UsbDeviceConnection conn = usbManager.openDevice(device);

            conn.controlTransfer(0x40, 0, 0, 0, null, 0, 0);//reset
            conn.controlTransfer(0x40, 0, 1, 0, null, 0, 0);//clear Rx
            conn.controlTransfer(0x40, 0, 2, 0, null, 0, 0);//clear Tx
            conn.controlTransfer(0x40, 0x03, 0x4138, 0, null, 0, 0);//baud rate 9600

            UsbEndpoint endPoint = null;
            final UsbInterface usbIf = device.getInterface(1);
            for(int i = 0; i < usbIf.getEndpointCount(); i++){ // 1
                if(usbIf.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK &&
                        usbIf.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN){
                    endPoint = usbIf.getEndpoint(i);
                }
            }
            if(endPoint == null){
                FragmentController.getInstance().updateUSBConnection(false);
                HLog.e("End point not set");
                return;
            }
            midiInputDevice = new MidiInputDevice(device, conn, usbIf, endPoint, new MidiListener());

            //setup device detached listener

            checkUsbDetachedTimer = new Timer();
            checkUsbDetachedTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if(usbManager.getDeviceList().isEmpty()){
                        FragmentController.getInstance().updateUSBConnection(false);
                        midiInputDevice.stop();
                        midiInputDevice = null;
                        stopCheckUsbDetachedTimer();
                    }
                }
            }, 0, CHECK_USB_CONN_TIME_MS);

        }else{
            HLog.i(getResources().getString(R.string.usb_not_supported));
        }
    }

	/*private void connectToGuitar(UsbDevice device){

		int vendorId = device.getVendorId();

		if(vendorId == 2235){ // guitar

			HLog.i("Guitar detected");

			UsbDeviceConnection conn = usbManager.openDevice(device);
			// need this??
			conn.controlTransfer(0x40, 0, 0, 0, null, 0, 0);//reset
			conn.controlTransfer(0x40, 0, 1, 0, null, 0, 0);//clear Rx
			conn.controlTransfer(0x40, 0, 2, 0, null, 0, 0);//clear Tx
			conn.controlTransfer(0x40, 0x03, 0x4138, 0, null, 0, 0);//baud rate 9600

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
				FragmentController.getInstance().updateUSBConnection(false);
				HLog.i("End point not set");
				return;
			}
			midiInputDevice = new MidiInputDevice(device, conn, usbIf, endPoint, new MidiListener());* /
		}
	}*/

    private static void stopCheckUsbDetachedTimer(){
        if(checkUsbDetachedTimer!=null){
            checkUsbDetachedTimer.purge();
            checkUsbDetachedTimer.cancel();
            checkUsbDetachedTimer = null;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(config != null)
            FileManager.getInstance().writeMidiMusicConfig(config);
    }

    @Override
    protected void onStop() {
        super.onStop();

        FragmentController.getInstance().updateUSBConnection(false);
        if(midiInputDevice!= null){
            midiInputDevice.stop();
            midiInputDevice = null;
        }
        usbManager = null;
        stopCheckUsbDetachedTimer();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbReceiver);

        for(Note n : config.allNotes){
            n.unLoad();
        }
        for(DrumSound d: config.allDrumSounds){
            SoundManager.getInstance().unloadDrumPool(d.getSoundID());
        }
        SoundManager.getInstance().unloadMetronome();
        //SoundManager.release();
    }
}
