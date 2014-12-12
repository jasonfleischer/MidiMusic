package com.comp4905.jasonfleischer.midimusic.fragments;

import java.util.ArrayList;
import java.util.List;
import com.comp4905.jasonfleischer.midimusic.MainActivity;
import com.comp4905.jasonfleischer.midimusic.MidiMusicConfig.PlayingMode;
import com.comp4905.jasonfleischer.midimusic.R;
import com.comp4905.jasonfleischer.midimusic.model.DrumSound;
import com.comp4905.jasonfleischer.midimusic.util.HLog;
import com.comp4905.jasonfleischer.midimusic.views.RecordingPane;
import com.comp4905.jasonfleischer.midimusic.views.UsbConnection;

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

	private ImageButton gridBtn, editBtn, connectBtn, keyBtn;
	private RecordingPane recordingPane;
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

		usbConn = (UsbConnection) rootView.findViewById(R.id.usb_connection_view);
		recordingPane = (RecordingPane) rootView.findViewById(R.id.recording_pane_view);
		recordingPane.init();
		gridBtn= (ImageButton) rootView.findViewById(R.id.drum_grid_btn);
		editBtn= (ImageButton) rootView.findViewById(R.id.drum_edit_btn);
		connectBtn = (ImageButton) rootView.findViewById(R.id.drum_connect_btn);
		keyBtn = (ImageButton) rootView.findViewById(R.id.drum_key_btn);
		invisibleSpinner= (Spinner) rootView.findViewById(R.id.drum_invis_spinner);

		usbConn.updateUSBConn(MainActivity.midiInputDevice!=null);

		gridBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!MainActivity.config.kitIsShowing){
					FragMentManager.getInstance().showDrumKit();
					gridBtn.setImageResource(R.drawable.grid);
				}else{
					FragMentManager.getInstance().showDrumGrid();
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
					FragMentManager.getInstance().hideNavBar();
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
				FragMentManager.getInstance().showInstrumentFragment();
			}
		});

		List<String> list = new ArrayList<String>();
		for(DrumSound ds: MainActivity.config.allDrumSounds){
			list.add(ds.getName());
		}
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		invisibleSpinner.setAdapter(dataAdapter);
		invisibleSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if(selectDrumSound!=null){
					FragMentManager.getInstance().hideNavBar();

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
		FragMentManager.getInstance().setupDrumFragment();

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
		FragMentManager.getInstance().hideNavBar();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		RecordingPane.stopTimer();
	}
}
