package com.jfleischer.midimusic.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.jfleischer.midimusic.MainActivity;
import com.jfleischer.midimusic.MidiMusicConfig.PlayingMode;
import com.jfleischer.midimusic.R;
import com.jfleischer.midimusic.audio.SoundManager;
import com.jfleischer.midimusic.dialogs.LoadingDialogFragment;
import com.jfleischer.midimusic.model.Chord;
import com.jfleischer.midimusic.model.Chord.ChordName;
import com.jfleischer.midimusic.model.Instrument;
import com.jfleischer.midimusic.model.Note;
import com.jfleischer.midimusic.model.Tempo;
import com.jfleischer.midimusic.model.Note.NoteDuration;
import com.jfleischer.midimusic.model.Note.NoteName;
import com.jfleischer.midimusic.model.Scale;
import com.jfleischer.midimusic.model.Sequence;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ToggleButton;

public class ConsoleFragment extends Fragment{

	private ImageButton metronomeBtn;
	private Spinner keySpinner;
	private Spinner octaveSpinner;
	private Spinner instrumentSpinner;
	private Spinner scaleSpinner;
	private Spinner modulateSpinner;
	private Spinner chordSpinner;
	private Spinner durationSpinner;
	private Spinner durationSpinnerChord;
	private Spinner sequenceSpinner;
	private Spinner accentSpinner;
	private UsbConnection usbConn;
	private RadioGroup playingModeRG, spokenModeRG;
	private GridLayout instrumentCont;
	private LinearLayout playingModeCont;

	private int selectedPlayingMode;
	private HashMap<String, Integer> tempoMap;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		FragmentController.getInstance().hideNavBar();

		View rootView = inflater.inflate(R.layout.fragment_console, container, false);
		usbConn = rootView.findViewById(R.id.usb_connection_view);
		Button usbBtn = rootView.findViewById(R.id.usb_btn);
		ImageButton keyBtn = rootView.findViewById(R.id.console_key_btn);
		ToggleButton spokenBtn = rootView.findViewById(R.id.console_spoken_metronome_btn);
		metronomeBtn = rootView.findViewById(R.id.console_metronome_btn);
		keySpinner = rootView.findViewById(R.id.keySpinner);
		octaveSpinner = rootView.findViewById(R.id.octaveSpinner);
		instrumentSpinner = rootView.findViewById(R.id.instrumentSpinner);
		instrumentCont = rootView.findViewById(R.id.instrument_cont);
		scaleSpinner = rootView.findViewById(R.id.scaleSpinner);
		modulateSpinner = rootView.findViewById(R.id.modulateSpinner);
		playingModeRG = rootView.findViewById(R.id.playing_mode_radio_group);
		spokenModeRG = rootView.findViewById(R.id.console_spoken_options);
		playingModeCont = rootView.findViewById(R.id.playing_mode_cont);
		durationSpinner = rootView.findViewById(R.id.duration_spinner);
		durationSpinnerChord = rootView.findViewById(R.id.duration_spinner_chord);
		chordSpinner = rootView.findViewById(R.id.chord_spinner);
		sequenceSpinner = rootView.findViewById(R.id.sequence_spinner);
		Spinner sequenceTempoSpinner = rootView.findViewById(R.id.sequence_tempo_spinner);
		Spinner tempoSpinner = rootView.findViewById(R.id.consoleTempoSpinner);
		accentSpinner = rootView.findViewById(R.id.consoleAccentSpinner);

		usbConn.updateUSBConn(MainActivity.midiInputDevice!=null);

		usbBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MainActivity.connectUSBDevice();
			}
		});
		keyBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LoadingDialogFragment.getInstance().show(getResources().getString(R.string.updating_midi));
				new UpdateSelections().execute();
			}
		});

		metronomeBtn.setImageResource(SoundManager.isPlayingMetronome?R.drawable.stop:R.drawable.play);
		rootView.findViewById(R.id.console_metronome_btn_cont).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(SoundManager.isPlayingMetronome){
					metronomeBtn.setImageResource(R.drawable.play);
					SoundManager.getInstance().stopMetronome();
				}else{
					int radioButtonID = spokenModeRG.getCheckedRadioButtonId();
					int indexOfSpokenOption = spokenModeRG.indexOfChild(spokenModeRG.findViewById(radioButtonID));
					if(SoundManager.isMetronomeSpeakState && indexOfSpokenOption == 2 && MainActivity.config.tempo.getBpm() > 110){
						HLog.i(getResources().getString(R.string.try_smaller_tempo));
						return;
					}
					metronomeBtn.setImageResource(R.drawable.stop);
					SoundManager.getInstance().startMetronome(accentSpinner.getSelectedItemPosition(), indexOfSpokenOption);
				}
			}
		});

		spokenBtn.setChecked(SoundManager.isMetronomeSpeakState);
		spokenBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SoundManager.isMetronomeSpeakState = !SoundManager.isMetronomeSpeakState;
				if(SoundManager.isMetronomeSpeakState)
					spokenModeRG.setVisibility(View.VISIBLE);
				else
					spokenModeRG.setVisibility(View.GONE);
			}
		});
		if(SoundManager.isMetronomeSpeakState)
			spokenModeRG.setVisibility(View.VISIBLE);
		else
			spokenModeRG.setVisibility(View.GONE);

		List<String> list = new ArrayList<>();
		NoteName[] noteNames = NoteName.values();
		for(NoteName nn: noteNames){
			list.add(nn.toString());
		}
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		keySpinner.setAdapter(dataAdapter);
		keySpinner.setSelection(MainActivity.config.key.ordinal());

		List<Integer> intList = new ArrayList<>();
		for(int i=1;i<7;i++){
			intList.add(i);
		}
		ArrayAdapter<Integer> octDataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, intList);
		octDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		octaveSpinner.setAdapter(octDataAdapter);
		octaveSpinner.setSelection(MainActivity.config.octave-1);

		list = new ArrayList<>();
		int instrumentSelected = 0;
		for (int i=0;i<MainActivity.config.instruments.size();i++) {
			Instrument instrument = MainActivity.config.instruments.get(i);
			if(MainActivity.config.playingMode == PlayingMode.SINGLE_NOTE){
				if(instrument.getValue()==MainActivity.config.singleNoteInstrument.getValue())
					instrumentSelected = i;
			}else if(MainActivity.config.playingMode == PlayingMode.CHORD){
				if(instrument.getValue()==MainActivity.config.chordInstrument.getValue())
					instrumentSelected = i;
			}else if(MainActivity.config.playingMode == PlayingMode.SEQUENCE){
				if(instrument.getValue()==MainActivity.config.sequenceInstrument.getValue())
					instrumentSelected = i;
			}
			list.add(instrument.getName());
		}
		dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		instrumentSpinner.setAdapter(dataAdapter);
		instrumentSpinner.setSelection(instrumentSelected);
		instrumentSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if(PlayingMode.SINGLE_NOTE == PlayingMode.values()[selectedPlayingMode]){
					MainActivity.config.singleNoteInstrument = MainActivity.config.instruments.get(position);
				}else if(PlayingMode.CHORD == PlayingMode.values()[selectedPlayingMode]){
					MainActivity.config.chordInstrument = MainActivity.config.instruments.get(position);
				}else if(PlayingMode.SEQUENCE == PlayingMode.values()[selectedPlayingMode]){
					MainActivity.config.sequenceInstrument = MainActivity.config.instruments.get(position);
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {	}
		});

		list = new ArrayList<>();
		for(int i=0;i<Scale.values().length;i++){
			list.add(Scale.values()[i].toString());
		}
		dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		scaleSpinner.setAdapter(dataAdapter);
		scaleSpinner.setSelection(MainActivity.config.choosenScale.ordinal());

		intList = new ArrayList<>();
		for(int i=-12;i<13;i++){
			intList.add(i);
		}
		ArrayAdapter<Integer> modDataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, intList);
		modDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		modulateSpinner.setAdapter(modDataAdapter);
		modulateSpinner.setSelection(MainActivity.config.usbModulation+12);

		for(int i=0; i<playingModeRG.getChildCount(); i++){
			RadioButton rb = (RadioButton) playingModeRG.getChildAt(i);
			rb.setText(PlayingMode.values()[i].toString());
			if(i==MainActivity.config.playingMode.ordinal()){
				selectedPlayingMode =  i;
				rb.setChecked(true);
				playingModeCont.getChildAt(i+3).setVisibility(View.VISIBLE);
			}
			final int j= i;
			rb.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					selectedPlayingMode =  j;
					instrumentCont.setVisibility(View.VISIBLE);
					if(PlayingMode.SINGLE_NOTE == PlayingMode.values()[selectedPlayingMode]){ // may break if text file changed
						instrumentSpinner.setSelection(MainActivity.config.singleNoteInstrument.getValue());
					}else if(PlayingMode.CHORD == PlayingMode.values()[selectedPlayingMode]){
						instrumentSpinner.setSelection(MainActivity.config.chordInstrument.getValue());
					}else if(PlayingMode.SEQUENCE == PlayingMode.values()[selectedPlayingMode]){
						instrumentSpinner.setSelection(MainActivity.config.sequenceInstrument.getValue());
					}else{
						instrumentCont.setVisibility(View.GONE);
					}
					for(int i=0; i<playingModeRG.getChildCount(); i++){
						if(i == j)
							playingModeCont.getChildAt(i+3).setVisibility(View.VISIBLE);
						else
							playingModeCont.getChildAt(i+3).setVisibility(View.GONE);
					}
				}
			});
		}

		if(MainActivity.config.playingMode == PlayingMode.DRUMS)
			instrumentCont.setVisibility(View.GONE);

		list = new ArrayList<>();
		for (int i=0;i<NoteDuration.values().length;i++) {
			list.add(NoteDuration.values()[i].getName());
		}
		dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		durationSpinner.setAdapter(dataAdapter);
		durationSpinnerChord.setAdapter(dataAdapter);
		durationSpinner.setSelection(MainActivity.config.noteDuration.ordinal());
		durationSpinnerChord.setSelection(MainActivity.config.noteDuration.ordinal());


		list = new ArrayList<>();
		for (int i=0;i<Chord.ChordName.values().length;i++) {
			list.add(Chord.ChordName.values()[i].toString());
		}
		dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		chordSpinner.setAdapter(dataAdapter);
		chordSpinner.setSelection(MainActivity.config.chord.ordinal());

		list = new ArrayList<>();
		int seqSelected =0;
		for (int i=0;i<MainActivity.config.sequences.size();i++) {
			Sequence seq = MainActivity.config.sequences.get(i);
			if(seq.getName().equals(MainActivity.config.sequence.getName()))
				seqSelected = i;
			list.add(seq.getName());
		}
		dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sequenceSpinner.setAdapter(dataAdapter);
		sequenceSpinner.setSelection(seqSelected);


		tempoMap = new HashMap<>();
		tempoMap.put(Sequence.tempoList[0], 60);
		tempoMap.put(Sequence.tempoList[1], 78);
		tempoMap.put(Sequence.tempoList[2], 40);
		tempoMap.put(Sequence.tempoList[3], 180);
		tempoMap.put(Sequence.tempoList[4], 48);
		dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, Sequence.tempoList);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sequenceTempoSpinner.setAdapter(dataAdapter);
		sequenceTempoSpinner.setSelection(2);
		sequenceTempoSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

				ArrayList<Tempo> tempos = MainActivity.config.tempos;
				for(Tempo temp: tempos){
					if(temp.getBpm() == tempoMap.get(Sequence.tempoList[position])){
						MainActivity.config.sequenceTempo = temp;
					}
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});

		list = new ArrayList<>();
		for(Tempo t: MainActivity.config.tempos){
			list.add(String.valueOf(t.getBpm()));
		}
		dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tempoSpinner.setAdapter(dataAdapter);
		tempoSpinner.setSelection((MainActivity.config.tempo.getBpm()-40)/2);
		tempoSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				MainActivity.config.tempo = MainActivity.config.tempos.get(position);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});

		list = new ArrayList<>();
		list.add(getResources().getString(R.string.none));
		list.add(getResources().getString(R.string.two));
		list.add(getResources().getString(R.string.three));
		list.add(getResources().getString(R.string.four));
		dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		accentSpinner.setAdapter(dataAdapter);
		accentSpinner.setSelection(3);

		return rootView;
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
			MainActivity.config.key = NoteName.getNameName(String.valueOf(keySpinner.getSelectedItem()));
			MainActivity.config.octave = (int) octaveSpinner.getSelectedItem();
			MainActivity.config.playingMode = PlayingMode.values()[selectedPlayingMode];
			MainActivity.config.chord = ChordName.values()[chordSpinner.getSelectedItemPosition()];
			MainActivity.config.choosenScale = Scale.values()[scaleSpinner.getSelectedItemPosition()];

			if(MainActivity.config.playingMode ==PlayingMode.SINGLE_NOTE){
				MainActivity.config.singleNoteInstrument = MainActivity.config.instruments.get(instrumentSpinner.getSelectedItemPosition());
				MainActivity.config.noteDuration = NoteDuration.values()[durationSpinner.getSelectedItemPosition()];
			}else if(MainActivity.config.playingMode ==PlayingMode.CHORD){
				MainActivity.config.chordInstrument = MainActivity.config.instruments.get(instrumentSpinner.getSelectedItemPosition());
				MainActivity.config.noteDuration = NoteDuration.values()[durationSpinnerChord.getSelectedItemPosition()];
			}else if(MainActivity.config.playingMode  == PlayingMode.SEQUENCE){
				MainActivity.config.sequenceInstrument = MainActivity.config.instruments.get(instrumentSpinner.getSelectedItemPosition());
				MainActivity.config.sequence = MainActivity.config.sequences.get(sequenceSpinner.getSelectedItemPosition());
			}
			MainActivity.config.usbModulation = (int) modulateSpinner.getSelectedItem();


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

	public UsbConnection getUsbConn() {
		return usbConn;
	}
}
