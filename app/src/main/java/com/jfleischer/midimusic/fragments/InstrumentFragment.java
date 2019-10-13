package com.jfleischer.midimusic.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jfleischer.midimusic.MainActivity;
import com.jfleischer.midimusic.MidiMusicConfig.PlayingMode;
import com.jfleischer.midimusic.R;
import com.jfleischer.midimusic.views.RecordingPane;
import com.jfleischer.midimusic.views.UsbConnection;

public class InstrumentFragment extends Fragment{

    private TextView noteTV;
	private ImageButton instrumentChangeBtn;
    private UsbConnection usbConn;

	private RecordingPane recordingPane;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


		View rootView = inflater.inflate(R.layout.fragment_instrument, container, false);
        TextView instrumentTV = rootView.findViewById(R.id.instrument);
		noteTV = rootView.findViewById(R.id.note);
		usbConn = rootView.findViewById(R.id.usb_connection_view);

		instrumentChangeBtn = rootView.findViewById(R.id.instrument_change_btn);
        ImageButton drumBtn = rootView.findViewById(R.id.drum_btn);
        ImageButton sequenceBtn = rootView.findViewById(R.id.sequence_btn);
        ImageButton consoleBtn = rootView.findViewById(R.id.console_btn);
        ImageButton notesBtn = rootView.findViewById(R.id.note_btn);
        ImageButton closeBtn = rootView.findViewById(R.id.close_btn);

		recordingPane = rootView.findViewById(R.id.recording_pane_view);
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
				FragmentController.getInstance().showConsoleFragment();
			}
		});

		usbConn.updateUSBConn(MainActivity.midiInputDevice!=null);

		instrumentChangeBtn.setImageResource(MainActivity.config.keysAreShowing?R.drawable.grid2:R.drawable.keys);
		instrumentChangeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(MainActivity.config.keysAreShowing){
					instrumentChangeBtn.setImageResource(R.drawable.keys);
					FragmentController.getInstance().showGrid();
				}else{
					instrumentChangeBtn.setImageResource(R.drawable.grid2);
					FragmentController.getInstance().showKeys();
				}
				MainActivity.config.keysAreShowing = ! MainActivity.config.keysAreShowing;
			}
		});
		drumBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentController.getInstance().showDrumFragment();
			}
		});
		sequenceBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentController.getInstance().showSequenceFragment();
			}
		});
		consoleBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentController.getInstance().showConsoleFragment();
			}
		});
		notesBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentController.getInstance().showChordFragment();
			}
		});
		closeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				recordingPane.clearTrack();
				MainActivity.getInstance().finish();
			}
		});

		FragmentController.getInstance().setupInstrumentFragment();

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
		FragmentController.getInstance().hideNavBar();
	}
}
