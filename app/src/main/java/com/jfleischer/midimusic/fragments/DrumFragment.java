package com.jfleischer.midimusic.fragments;

import java.util.ArrayList;
import java.util.List;
import com.jfleischer.midimusic.MainActivity;
import com.jfleischer.midimusic.MidiMusicConfig.PlayingMode;
import com.jfleischer.midimusic.R;
import com.jfleischer.midimusic.model.DrumSound;
import com.jfleischer.midimusic.util.HLog;
import com.jfleischer.midimusic.views.RecordingPane;
import com.jfleischer.midimusic.views.UsbConnection;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class DrumFragment extends Fragment{

	private ImageButton gridBtn;
	private ImageButton editBtn;
	private ImageButton connectBtn;
	private static Spinner invisibleSpinner;

	private UsbConnection usbConn;

	public static boolean isEditMode;
	private static DrumSound selectDrumSound;
	private static PlayingMode lastSelectedPlayingMode = PlayingMode.SINGLE_NOTE;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_drum, container, false);
		isEditMode = false;
		selectDrumSound = null;

		usbConn = rootView.findViewById(R.id.usb_connection_view);
		RecordingPane recordingPane = rootView.findViewById(R.id.recording_pane_view);
		recordingPane.init();
		gridBtn= rootView.findViewById(R.id.drum_grid_btn);
		editBtn= rootView.findViewById(R.id.drum_edit_btn);
		connectBtn = rootView.findViewById(R.id.drum_connect_btn);
		ImageButton keyBtn = rootView.findViewById(R.id.drum_key_btn);
		invisibleSpinner= rootView.findViewById(R.id.drum_invis_spinner);

		usbConn.updateUSBConn(MainActivity.midiInputDevice!=null);

		gridBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!MainActivity.config.kitIsShowing){
					FragmentController.getInstance().showDrumKit();
					gridBtn.setImageResource(R.drawable.grid);
				}else{
					FragmentController.getInstance().showDrumGrid();
					gridBtn.setImageResource(R.drawable.drum);
				}
				MainActivity.config.kitIsShowing = ! MainActivity.config.kitIsShowing;
			}
		});
		editBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isEditMode = !isEditMode;
				int vis;
				if(isEditMode){
					vis = View.VISIBLE;
					editBtn.setImageResource(R.drawable.no_wrench);
				}else{
					vis = View.GONE;
					editBtn.setImageResource(R.drawable.wrench);
					FragmentController.getInstance().hideNavBar();
				}

				if(!MainActivity.config.kitIsShowing){
					DrumGridFragment.changeEditMode();
				}else{
					DrumKitFragment.changeEditMode(vis);
				}
			}
		});
		connectBtn.setImageResource((MainActivity.config.playingMode == PlayingMode.DRUMS)?R.drawable.connected:R.drawable.connect);
		connectBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if(MainActivity.config.playingMode != PlayingMode.DRUMS){
					connectBtn.setImageResource(R.drawable.connected);
					lastSelectedPlayingMode = MainActivity.config.playingMode;
					HLog.i(getResources().getString(R.string.attach_drum));
					MainActivity.config.playingMode = PlayingMode.DRUMS;
				}else{
					connectBtn.setImageResource(R.drawable.connect);
					HLog.i(getResources().getString(R.string.detach_drum));
					MainActivity.config.playingMode = lastSelectedPlayingMode;
				}

			}
		});
		keyBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentController.getInstance().showInstrumentFragment();
			}
		});

		List<String> list = new ArrayList<>();
		for(DrumSound ds: MainActivity.config.allDrumSounds){
			list.add(ds.getName());
		}
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		invisibleSpinner.setAdapter(dataAdapter);
		invisibleSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if(selectDrumSound!=null){
					FragmentController.getInstance().hideNavBar();

					if(!MainActivity.config.kitIsShowing){
						DrumGridFragment.changeDrum(selectDrumSound, position);
					}else{
						DrumKitFragment.changeDrum(selectDrumSound, position);
					}
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});

		gridBtn.setImageResource((MainActivity.config.kitIsShowing?R.drawable.grid:R.drawable.drum));
		FragmentController.getInstance().setupDrumFragment();

		return rootView;
	}

	public static void deploySpinner(DrumSound drumSound){
		selectDrumSound = drumSound;
		invisibleSpinner.performClick();
	}

	public UsbConnection getUsbConn() {
		return usbConn;
	}

	@Override
	public void onResume() {
		super.onResume();
		FragmentController.getInstance().hideNavBar();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		RecordingPane.stopTimer();
	}
}
