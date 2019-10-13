package com.jfleischer.midimusic.fragments;

import java.util.ArrayList;
import java.util.List;

import com.jfleischer.midimusic.MainActivity;
import com.jfleischer.midimusic.MidiMusicConfig.PlayingMode;
import com.jfleischer.midimusic.R;
import com.jfleischer.midimusic.dialogs.LoadingDialogFragment;
import com.jfleischer.midimusic.model.Chord;
import com.jfleischer.midimusic.model.Instrument;
import com.jfleischer.midimusic.model.Note;
import com.jfleischer.midimusic.model.Chord.ChordName;
import com.jfleischer.midimusic.model.Note.NoteDuration;
import com.jfleischer.midimusic.model.Note.NoteName;
import com.jfleischer.midimusic.model.Scale;
import com.jfleischer.midimusic.util.HLog;
import com.jfleischer.midimusic.views.UsbConnection;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

// Excluded from project

public class ChordFragment extends Fragment{

	private UsbConnection usbConn;
	private ImageButton connectBtn, keyBtn;

	private LinearLayout[] chordContainers;

	private static final Scale scale = Scale.Major;

	private static PlayingMode lastSelectedPlayingMode;
	private static NoteName key = NoteName.C;
	private static NoteDuration duration = NoteDuration.Quarter;
	private static int octave = 3;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_chord, container, false);

		usbConn = rootView.findViewById(R.id.usb_connection_view);
		keyBtn = rootView.findViewById(R.id.chord_key_btn);
		connectBtn = rootView.findViewById(R.id.chord_connect_btn);
		Spinner keySpinner = rootView.findViewById(R.id.chord_key_spinner);
		Spinner octaveSpinner = rootView.findViewById(R.id.chord_octave_spinner);
		Spinner durationSpinner = rootView.findViewById(R.id.chord_duration_spinner);
		Spinner instrumentSpinner = rootView.findViewById(R.id.chord_instrument_spinner);

		chordContainers = new LinearLayout[7];
		for(int i=0;i<7;i++){
			String viewID = "ll_" + i;
			int resID = getResources().getIdentifier(viewID, "id", "com.jfleischer.midimusic");
			chordContainers[i] = rootView.findViewById(resID);
		}

		usbConn.updateUSBConn(MainActivity.midiInputDevice!=null);
		connectBtn.setImageResource((MainActivity.config.playingMode == PlayingMode.CHORD)?R.drawable.connected:R.drawable.connect);
		connectBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if(MainActivity.config.playingMode != PlayingMode.CHORD){
					connectBtn.setImageResource(R.drawable.connected);
					lastSelectedPlayingMode = MainActivity.config.playingMode;
					HLog.i(getResources().getString(R.string.attach_chord));
					MainActivity.config.playingMode = PlayingMode.CHORD;
				}else{
					connectBtn.setImageResource(R.drawable.connect);
					HLog.i(getResources().getString(R.string.detach_chords));
					MainActivity.config.playingMode = lastSelectedPlayingMode;
				}

			}
		});

		List<String> list = new ArrayList<>();
		NoteName[] noteNames = NoteName.values();
		for(NoteName nn: noteNames){
			list.add(nn.toString());
		}
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		keySpinner.setAdapter(dataAdapter);
		keySpinner.setSelection(key.ordinal());
		keySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				key = NoteName.values()[position];
				updateView();
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});

		List<Integer> intList = new ArrayList<>();
		for(int i=1;i<7;i++){
			intList.add(i);
		}
		ArrayAdapter<Integer> octDataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, intList);
		octDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		octaveSpinner.setAdapter(octDataAdapter);
		octaveSpinner.setSelection(octave-1);
		octaveSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				octave = position+1;
				updateView();
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});

		list = new ArrayList<>();
		int instrumentSelected = 0;
		for (int i=0;i<MainActivity.config.instruments.size();i++) {
			Instrument instrument = MainActivity.config.instruments.get(i);
			if(instrument.getValue()==MainActivity.config.chordInstrument.getValue())
				instrumentSelected = i;
			list.add(instrument.getName());
		}
		dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		instrumentSpinner.setAdapter(dataAdapter);
		instrumentSpinner.setSelection(instrumentSelected);
		instrumentSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				MainActivity.config.chordInstrument = MainActivity.config.instruments.get(position);
				updateView();
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }

		});

		list = new ArrayList<>();
		for (int i=0;i<NoteDuration.values().length;i++) {
			NoteDuration noteDuration = NoteDuration.values()[i];
			list.add(noteDuration.toString());
		}
		dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		durationSpinner.setAdapter(dataAdapter);
		durationSpinner.setSelection(duration.ordinal());
		durationSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				duration = NoteDuration.values()[position];
				updateView();
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }

		});

		FragmentController.getInstance().hideNavBar();//.showNavBar();

		updateView();

		return rootView;
	}

	private void updateView(){
		LoadingDialogFragment.getInstance().show(getResources().getString(R.string.loading));
		new UpdateChords().execute();
	}

	public UsbConnection getUsbConn() {
		return usbConn;
	}

	private class UpdateChords extends AsyncTask<Void, Void, LinearLayout[]> {
		@Override
		protected void onPostExecute(LinearLayout[] result) {
			super.onPostExecute(result);
			for(int i=0;i<7;i++){
				chordContainers[i].removeAllViews();
				chordContainers[i].addView(result[i]);
			}

			keyBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					LoadingDialogFragment.getInstance().show(getResources().getString(R.string.updating_midi));
					new UpdateSelections().execute();
				}
			});
			LoadingDialogFragment.getInstance().dismiss();
		}
		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}
		protected LinearLayout[] doInBackground(Void... params) {

			int instrument = MainActivity.config.chordInstrument.getValue();
			ArrayList<ArrayList<Chord>> chords = new ArrayList<>(7);

			LinearLayout[] textViews = new LinearLayout[7];

			for(int i=0;i<7;i++){
				//publishProgress(16*i);
				ArrayList<Chord> chordList = new ArrayList<>();
				if(i==0){
					chordList.add(new Chord(key, ChordName.M, duration, instrument, octave));
					chordList.add(new Chord(key, ChordName.power, duration, instrument, octave));
					chordList.add(new Chord(key, ChordName.sus2, duration, instrument, octave));
					chordList.add(new Chord(key, ChordName.sus4, duration, instrument, octave));
					chordList.add(new Chord(key, ChordName.six, duration, instrument, octave));
					chordList.add(new Chord(key, ChordName.sixNine, duration, instrument, octave));
					chordList.add(new Chord(key, ChordName.M7, duration, instrument, octave));
					chordList.add(new Chord(key, ChordName.major_add9, duration, instrument, octave));
				}else if(i==1){
					int index= ((key.ordinal()+scale.getIntervals()[i-1])%12);
					chordList.add(new Chord(NoteName.values()[index], ChordName.min, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.power, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.sus2, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.sus4, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.min6, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.m7, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.minor_add9, duration, instrument, octave));
				}else if(i==2){
					int index= ((key.ordinal()+scale.getIntervals()[i-1])%12);
					chordList.add(new Chord(NoteName.values()[index], ChordName.min, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.power, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.sus4, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.m7, duration, instrument, octave));
				}else if(i==3){
					int index= ((key.ordinal()+scale.getIntervals()[i-1])%12);
					chordList.add(new Chord(NoteName.values()[index], ChordName.M, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.power, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.sus2, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.six, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.sixNine, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.M7, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.major_add9, duration, instrument, octave));
				}else if(i==4){
					int index= ((key.ordinal()+scale.getIntervals()[i-1])%12);
					chordList.add(new Chord(NoteName.values()[index], ChordName.M, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.power, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.sus2, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.sus4, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.six, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.sixNine, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.dom7, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.major_add9, duration, instrument, octave));
				}else if(i==5){
					int index= ((key.ordinal()+scale.getIntervals()[i-1])%12);
					chordList.add(new Chord(NoteName.values()[index], ChordName.min, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.power, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.sus2, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.sus4, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.m7, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.minor_add9, duration, instrument, octave));
				}else if(i==6){
					int index= ((key.ordinal()+scale.getIntervals()[i-1])%12);
					chordList.add(new Chord(NoteName.values()[index], ChordName.dim, duration, instrument, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.m7b5, duration, instrument, octave));
				}
				chords.add(chordList);

				LinearLayout ll = new LinearLayout(MainActivity.getInstance());
				ll.setOrientation(LinearLayout.VERTICAL);
				for(int j =0;j<chordList.size();j++){
					final Chord c = chordList.get(j);
					final TextView tv = new TextView(MainActivity.getInstance());
					tv.setTextSize(20f);
					tv.setPadding(10, 15, 10, 15);
					tv.setText(c.getRootNote().toString()+" "+c.getChordName().toString());
					tv.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							c.playChord();
						}
					});
					ll.addView(tv);
				}
				textViews[i] = ll;
			}
			//publishProgress(100);
			return textViews;
		}
	}

	private class UpdateSelections extends AsyncTask<Void, Integer, Void> {
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			LoadingDialogFragment.getInstance().dismiss();
			FragmentController.getInstance().showInstrumentFragment();
		}
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			LoadingDialogFragment.getInstance().updateProgress(values[0]);
		}
		protected Void doInBackground(Void... params) {
			// update all notes
			for(int i=0; i<MainActivity.config.allNotes.length;i++){
				Note n = MainActivity.config.allNotes[i];
				n.updateNoteFile();
				int percent = (int) (100*((double)i/MainActivity.config.allNotes.length));
				publishProgress(percent);
			}
			publishProgress(100);
			return null;
		}
	}
}
