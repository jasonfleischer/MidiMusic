package com.comp4905.jasonfleischer.midimusic.fragments;

import java.util.ArrayList;
import java.util.List;

import com.comp4905.jasonfleischer.midimusic.MainActivity;
import com.comp4905.jasonfleischer.midimusic.MidiMusicConfig.PlayingMode;
import com.comp4905.jasonfleischer.midimusic.R;
import com.comp4905.jasonfleischer.midimusic.dialogs.LoadingDialogFragment;
import com.comp4905.jasonfleischer.midimusic.model.Chord;
import com.comp4905.jasonfleischer.midimusic.model.Instrument;
import com.comp4905.jasonfleischer.midimusic.model.Note;
import com.comp4905.jasonfleischer.midimusic.model.Chord.ChordName;
import com.comp4905.jasonfleischer.midimusic.model.Note.NoteDuration;
import com.comp4905.jasonfleischer.midimusic.model.Note.NoteName;
import com.comp4905.jasonfleischer.midimusic.model.Scale;
import com.comp4905.jasonfleischer.midimusic.util.HLog;
import com.comp4905.jasonfleischer.midimusic.views.UsbConnection;

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
	private Spinner keySpinner, octaveSpinner, instrumentSpinner, durationSpinner;

	private LinearLayout[] chordContainers;
	private static ArrayList<ArrayList<Chord>> chords;

	private static final Scale scale = Scale.Major;

	private static PlayingMode lastSelectedPlayingMode;
	private static NoteName key = NoteName.C;
	private static NoteDuration duration = NoteDuration.Quarter;
	private static int octave = 3;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_chord, container, false);

		usbConn = (UsbConnection) rootView.findViewById(R.id.usb_connection_view);
		keyBtn = (ImageButton) rootView.findViewById(R.id.chord_key_btn);
		connectBtn = (ImageButton) rootView.findViewById(R.id.chord_connect_btn);
		keySpinner = (Spinner) rootView.findViewById(R.id.chord_key_spinner);
		octaveSpinner = (Spinner) rootView.findViewById(R.id.chord_octave_spinner);
		durationSpinner = (Spinner) rootView.findViewById(R.id.chord_duration_spinner);
		instrumentSpinner = (Spinner) rootView.findViewById(R.id.chord_instrument_spinner);

		chordContainers = new LinearLayout[7];
		for(int i=0;i<7;i++){
			String viewID = "ll_" + i;
			int resID = getResources().getIdentifier(viewID, "id", "com.comp4905.jasonfleischer.midimusic");
			chordContainers[i] = (LinearLayout) rootView.findViewById(resID);
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

		List<String> list = new ArrayList<String>();
		NoteName[] noteNames = NoteName.values();
		for(NoteName nn: noteNames){
			list.add(nn.toString());
		}
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, list);
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

		List<Integer> intList = new ArrayList<Integer>();
		for(int i=1;i<7;i++){
			intList.add(i);
		}
		ArrayAdapter<Integer> octDataAdapter = new ArrayAdapter<Integer>(getActivity(),android.R.layout.simple_spinner_item, intList);
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

		list = new ArrayList<String>();
		int instrumentSelected = 0;
		for (int i=0;i<MainActivity.config.instruments.size();i++) {
			Instrument instrum = MainActivity.config.instruments.get(i);
			if(instrum.getValue()==MainActivity.config.chordInstrument.getValue())
				instrumentSelected = i;
			list.add(instrum.getName());
		}
		dataAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, list);
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

		list = new ArrayList<String>();
		for (int i=0;i<NoteDuration.values().length;i++) {
			NoteDuration noteDuration = NoteDuration.values()[i];
			list.add(noteDuration.toString());
		}
		dataAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, list);
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

		FragMentManager.getInstance().hideNavBar();//.showNavBar();

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

			int instrmnt =MainActivity.config.chordInstrument.getValue();
			chords = new ArrayList<ArrayList<Chord>>(7);

			LinearLayout[] textViews = new LinearLayout[7];;

			for(int i=0;i<7;i++){
				//publishProgress(16*i);
				ArrayList<Chord> chordList = new ArrayList<Chord>();
				if(i==0){
					chordList.add(new Chord(key, ChordName.M, duration, instrmnt, octave));
					chordList.add(new Chord(key, ChordName.power, duration, instrmnt, octave));
					chordList.add(new Chord(key, ChordName.sus2, duration, instrmnt, octave));
					chordList.add(new Chord(key, ChordName.sus4, duration, instrmnt, octave));
					chordList.add(new Chord(key, ChordName.six, duration, instrmnt, octave));
					chordList.add(new Chord(key, ChordName.sixNine, duration, instrmnt, octave));
					chordList.add(new Chord(key, ChordName.M7, duration, instrmnt, octave));
					chordList.add(new Chord(key, ChordName.Madd9, duration, instrmnt, octave));
				}else if(i==1){
					int index= ((key.ordinal()+scale.getIntervals()[i-1])%12);
					chordList.add(new Chord(NoteName.values()[index], ChordName.min, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.power, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.sus2, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.sus4, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.min6, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.m7, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.madd9, duration, instrmnt, octave));
				}else if(i==2){
					int index= ((key.ordinal()+scale.getIntervals()[i-1])%12);
					chordList.add(new Chord(NoteName.values()[index], ChordName.min, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.power, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.sus4, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.m7, duration, instrmnt, octave));
				}else if(i==3){
					int index= ((key.ordinal()+scale.getIntervals()[i-1])%12);
					chordList.add(new Chord(NoteName.values()[index], ChordName.M, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.power, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.sus2, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.six, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.sixNine, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.M7, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.Madd9, duration, instrmnt, octave));
				}else if(i==4){
					int index= ((key.ordinal()+scale.getIntervals()[i-1])%12);
					chordList.add(new Chord(NoteName.values()[index], ChordName.M, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.power, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.sus2, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.sus4, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.six, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.sixNine, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.dom7, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.Madd9, duration, instrmnt, octave));
				}else if(i==5){
					int index= ((key.ordinal()+scale.getIntervals()[i-1])%12);
					chordList.add(new Chord(NoteName.values()[index], ChordName.min, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.power, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.sus2, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.sus4, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.m7, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.madd9, duration, instrmnt, octave));
				}else if(i==6){
					int index= ((key.ordinal()+scale.getIntervals()[i-1])%12);
					chordList.add(new Chord(NoteName.values()[index], ChordName.dim, duration, instrmnt, octave));
					chordList.add(new Chord(NoteName.values()[index], ChordName.m7b5, duration, instrmnt, octave));
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
			FragMentManager.getInstance().showInstrumentFragment();
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
