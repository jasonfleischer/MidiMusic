package com.comp4905.jasonfleischer.midimusic.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.comp4905.jasonfleischer.midimusic.MainActivity;
import com.comp4905.jasonfleischer.midimusic.MidiMusicConfig.PlayingMode;
import com.comp4905.jasonfleischer.midimusic.R;
import com.comp4905.jasonfleischer.midimusic.views.RecordingPane;
import com.comp4905.jasonfleischer.midimusic.views.UsbConnection;

public class InstrumentFragment extends Fragment{
	
	private TextView instrumentTV, noteTV;
	private ImageButton instrumentChangeBtn, drumBtn, sequenceBtn, consoleBtn, notesBtn, closeBtn;
	private UsbConnection usbConn;
	
	private RecordingPane recordingPane;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		
		View rootView = inflater.inflate(R.layout.fragment_instrument, container, false);
		instrumentTV = (TextView) rootView.findViewById(R.id.instrument);
		noteTV = (TextView) rootView.findViewById(R.id.note);
		usbConn = (UsbConnection) rootView.findViewById(R.id.usb_connection_view);
		
		instrumentChangeBtn = (ImageButton) rootView.findViewById(R.id.instrument_change_btn);
		drumBtn = (ImageButton) rootView.findViewById(R.id.drum_btn);
		sequenceBtn = (ImageButton) rootView.findViewById(R.id.sequence_btn);
		consoleBtn = (ImageButton) rootView.findViewById(R.id.console_btn);
		notesBtn = (ImageButton) rootView.findViewById(R.id.note_btn);
		closeBtn = (ImageButton) rootView.findViewById(R.id.close_btn);
		
		recordingPane = (RecordingPane) rootView.findViewById(R.id.recording_pane_view);
		recordingPane.init();
		
		if(MainActivity.config.playingMode ==PlayingMode.SINGLE_NOTE){
			instrumentTV.setText(MainActivity.config.singleNoteInstrument.getName());
		}else if(MainActivity.config.playingMode ==PlayingMode.CHORD){
			instrumentTV.setText(MainActivity.config.chordInstrument.getName());
		}else if(MainActivity.config.playingMode  == PlayingMode.SEQUENCE){
			instrumentTV.setText(MainActivity.config.sequenceInstrument.getName());
		}else{
			instrumentTV.setText("Drums");
		}
		instrumentTV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragMentManager.getInstance().showConsoleFragment();
			}
		});
		
		usbConn.updateUSBConn(MainActivity.midiInputDevice!=null);
		
		instrumentChangeBtn.setImageResource(MainActivity.config.keysAreShowing?R.drawable.grid2:R.drawable.keys);
		instrumentChangeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(MainActivity.config.keysAreShowing){
					instrumentChangeBtn.setImageResource(R.drawable.keys);
					FragMentManager.getInstance().showGrid();
				}else{
					instrumentChangeBtn.setImageResource(R.drawable.grid2);
					FragMentManager.getInstance().showKeys();
				}
				MainActivity.config.keysAreShowing = ! MainActivity.config.keysAreShowing;
			}
		});
		drumBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragMentManager.getInstance().showDrumFragment();
			}
		});
		sequenceBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragMentManager.getInstance().showSequenceFragment();
			}
		});
		consoleBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragMentManager.getInstance().showConsoleFragment();
			}
		});
		notesBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragMentManager.getInstance().showChordFragment();
			}
		});
		closeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MainActivity.getInstance().finish();
			}
		});
		
		FragMentManager.getInstance().setupInstrumentFragment();
		
		return rootView;
	}
	
	public void setNotePressed(String s, int octave){
		if(octave != -1){
			if(s.length()==1)
				noteTV.setText(s+octave);
			else
				noteTV.setText(s+" "+octave);
		}else{
			noteTV.setText(s);
		}
	}
	
	public UsbConnection getUsbConn() {
		return usbConn;
	}

	@Override
	public void onResume() {
		super.onResume();
		FragMentManager.getInstance().hideNavBar();
	}
}
