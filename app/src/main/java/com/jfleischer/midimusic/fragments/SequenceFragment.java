package com.jfleischer.midimusic.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.jfleischer.midimusic.MainActivity;
import com.jfleischer.midimusic.MidiMusicConfig.PlayingMode;
import com.jfleischer.midimusic.R;
import com.jfleischer.midimusic.audio.MidiFile;
import com.jfleischer.midimusic.audio.SoundManager;
import com.jfleischer.midimusic.dialogs.LoadingDialogFragment;
import com.jfleischer.midimusic.model.Instrument;
import com.jfleischer.midimusic.model.Note;
import com.jfleischer.midimusic.model.Note.NoteDuration;
import com.jfleischer.midimusic.model.Note.NoteName;
import com.jfleischer.midimusic.model.Sequence;
import com.jfleischer.midimusic.model.Tempo;
import com.jfleischer.midimusic.util.HLog;
import com.jfleischer.midimusic.views.UsbConnection;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class SequenceFragment extends Fragment{

	private TextView sequenceNameTv, middleTv;
	private UsbConnection usbConn;
	private ImageButton loopBtn;
	private ImageButton connectBtn;
	private ImageButton expandBtn;
	private LinearLayout verticalMarkers;
	private GridLayout gridLayout, configGridLayout;
	private Spinner sequenceSpinner;
	private Spinner columnsSpinner;
	private ArrayList<SequenceElement> sequenceElements;
	private static boolean isLooping = false;
	private static final int NUM_OF_ROWS = 25;
	private static final String[] VERTICAL_MARKERS = new String[]{"-","-","m3","M3","-","-","5","-","6","-","-","Oct"};
	private HashMap<String, Integer> tempoMap;

	//Config
	private static PlayingMode lastSelectedPlayingMode = PlayingMode.SINGLE_NOTE;
	private static NoteName key = NoteName.C;
	private static int octave = 2;
	private static int numberOfCol;
	private static NoteDuration noteDuration = NoteDuration.Eighth;


	private class SequenceElement{
		private final FrameLayout view;
		private boolean colored;
		private boolean selected;

		private final Integer id;
		private final int interval;
		private final int column;
		private final int defaultDrawable;
		private NoteDuration noteDuration;

		private final ArrayList<Integer> ids; // other associated fms

		private static final int selectedDrawable = R.drawable.sequence_shape_selected;
		private static final int coloredDrawable = R.drawable.sequence_shape_colored;

		private SequenceElement(int i, int c, FrameLayout v, int idz){
			id = idz;
			interval=i;
			column=c;
			view=v;
			ids = new ArrayList<>();
			if(interval==0 || interval ==-12|| interval == 12){
				defaultDrawable = R.drawable.sequence_shape_highlight;
			}else{
				defaultDrawable = R.drawable.sequence_shape;
			}
			view.setBackgroundResource(defaultDrawable);
			noteDuration = null;
			selected = false;
			colored = false;
		}

		private void update(NoteDuration nd){
			int iterations = nd.getValue()/4;
			if(noteDuration!=null)
				iterations = Math.max(nd.getValue()/4, noteDuration.getValue()/4);

			if(nd.getValue()/4>numberOfCol-column){
				HLog.i(nd.toString() +" "+getResources().getString(R.string.note_too_large));
				return;
			}
			boolean isAdding = true;

			for(int i=0;i<iterations;i++){
				SequenceElement element = getSeqElement(interval, column+i);

				if(i==0){
					if(selected){
						isAdding = false;
						ids.remove(id);
						if(ids.isEmpty())
							unSelect();
						else
							color();
					}else {
						select(nd);
						ids.add(id);

						for(int j=-12;j<=12;j++){
							if(j!=interval){

								SequenceElement otherSeqElementInCol = getSeqElement(j, column) ;
								NoteDuration otherNd = otherSeqElementInCol.getNoteDuration();
								if(otherNd!=null && otherSeqElementInCol.selected){
									otherSeqElementInCol.update(otherNd);
								}
							}
						}
					}
				}else{
					if(isAdding){
						if(!element.selected){  //unselected
							element.color();
						}
						element.ids.add(id);
					}else{ // removing
						element.ids.remove(id);
						if(element.colored){
							if(element.ids.isEmpty())
								element.unSelect();
						}
					}
				}
			}
		}

		private SequenceElement getSeqElement(int interval, int col){
			int row = (-1*interval+12)%24;
			if (interval == -12 ) row = 24;
			return sequenceElements.get(row*numberOfCol+col);
		}

		private void unSelect() {
			view.setBackgroundResource(defaultDrawable);
			noteDuration = null;
			selected = false;
			colored = false;
		}

		private void select(NoteDuration nd){
			view.setBackgroundResource(selectedDrawable);
			noteDuration = nd;
			selected = true;
			colored = false;
		}
		private void color(){
			view.setBackgroundResource(coloredDrawable);
			noteDuration = null;
			selected = false;
			colored = true;
		}

		NoteDuration getNoteDuration() {
			return noteDuration;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_sequence, container, false);
		FragmentController.getInstance().hideNavBar();

		// defaults

		numberOfCol = MainActivity.config.sequence.getLength();
		sequenceNameTv = rootView.findViewById(R.id.sequence_name);
		usbConn = rootView.findViewById(R.id.usb_connection_view);

		ImageButton addBtn = rootView.findViewById(R.id.sequence_add_btn);
		ImageButton deleteBtn = rootView.findViewById(R.id.sequence_delete_btn);
		ImageButton playBtn = rootView.findViewById(R.id.sequence_play_btn);
		loopBtn = rootView.findViewById(R.id.sequence_loop_btn);
		connectBtn = rootView.findViewById(R.id.sequence_connect_btn);
		expandBtn = rootView.findViewById(R.id.sequence_expand_btn);
		ImageButton keyBtn = rootView.findViewById(R.id.sequence_key_btn);

		sequenceSpinner = rootView.findViewById(R.id.seqSeqSpinner);
		Spinner tempoSpinner = rootView.findViewById(R.id.seqTempoSpinner);
		Spinner keySpinner = rootView.findViewById(R.id.seqKeySpinner);
		Spinner octaveSpinner = rootView.findViewById(R.id.seqOctaveSpinner);
		Spinner instrumentSpinner = rootView.findViewById(R.id.seqInstrumentSpinner);
		Spinner durationSpinner = rootView.findViewById(R.id.seqDurationSpinner);
		columnsSpinner = rootView.findViewById(R.id.seqColSpinner);

		gridLayout = rootView.findViewById(R.id.sequence_grid_layout);
		configGridLayout = rootView.findViewById(R.id.seqGridLayout);
		verticalMarkers = rootView.findViewById(R.id.seq_vertical_markers);

		sequenceNameTv.setText(MainActivity.config.sequence.getName());
		usbConn.updateUSBConn(MainActivity.midiInputDevice!=null);

		populateMarkers();
		gridLayout.setRowCount(NUM_OF_ROWS);
		loadSequence();


		//BUTTONS
		addBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Sequence seq = MainActivity.config.addSequence(new int[]{0,  NoteDuration.Quarter.getValue()}, 16);
				if(seq == null){
					HLog.i(getResources().getString(R.string.create_sequence_limit));
					return;
				}
				MainActivity.config.sequence  = seq;
				sequenceNameTv.setText(MainActivity.config.sequence.getName());
				loadSequence();
				HLog.i(getResources().getString(R.string.new_sequence));
				reloadSequenceSpinner();
			}
		});
		deleteBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(MainActivity.config.removeSequence()){
					sequenceNameTv.setText(MainActivity.config.sequence.getName());
					HLog.i(getResources().getString(R.string.sequence_deleted));
					numberOfCol = MainActivity.config.sequence.getLength();
					loadSequence();
					columnsSpinner.setSelection((numberOfCol/16)-1);

					reloadSequenceSpinner();
				}else{
					HLog.i(getResources().getString(R.string.cannot_delete_last_sequence));
				}
			}
		});
		playBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createSequenceFile();
				SoundManager.getInstance().playSequence(false);
			}
		});
		//TODO remove
		loopBtn.setImageResource((isLooping?R.drawable.stop:R.drawable.loop));
		loopBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isLooping){
					SoundManager.getInstance().stopLoop();
					loopBtn.setImageResource(R.drawable.loop);
				}else{
					createSequenceFile();
					SoundManager.getInstance().playSequence(true);
					loopBtn.setImageResource(R.drawable.stop);
				}
				isLooping = !isLooping;
			}
		});
		connectBtn.setImageResource((MainActivity.config.playingMode == PlayingMode.SEQUENCE)?R.drawable.connected:R.drawable.connect);
		connectBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if(MainActivity.config.playingMode != PlayingMode.SEQUENCE){
					connectBtn.setImageResource(R.drawable.connected);
					lastSelectedPlayingMode = MainActivity.config.playingMode;
					MainActivity.config.playingMode = PlayingMode.SEQUENCE;
					HLog.i(getResources().getString(R.string.attach_sequence));
				}else{
					connectBtn.setImageResource(R.drawable.connect);
					MainActivity.config.playingMode = lastSelectedPlayingMode;
					HLog.i(getResources().getString(R.string.detach_sequence));
				}
				if(MainActivity.midiInputDevice != null){
					new UpdateSelections().execute();
				}
			}
		});
		expandBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(configGridLayout.getVisibility() == View.VISIBLE){
					expandBtn.setImageResource(R.drawable.expand);
					configGridLayout.setVisibility(View.GONE);
					FragmentController.getInstance().hideNavBar();
				}else{
					expandBtn.setImageResource(R.drawable.collapse);
					configGridLayout.setVisibility(View.VISIBLE);
					FragmentController.getInstance().hideNavBar();
				}
			}
		});
		keyBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MainActivity.config.sequence.setInterval(createSequenceFile());
				LoadingDialogFragment.getInstance().show(getResources().getString(R.string.updating_midi));
				new UpdateSelections().execute();
			}
		});

		//SPINNERS
		List<String> list = new ArrayList<>();
		for(NoteName nn: NoteName.values()){
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
				setMiddleText();
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});

		tempoMap = new HashMap<>();
		tempoMap.put(Sequence.tempoList[0], 60);
		tempoMap.put(Sequence.tempoList[1], 78);
		tempoMap.put(Sequence.tempoList[2], 40);
		tempoMap.put(Sequence.tempoList[3], 180);
		tempoMap.put(Sequence.tempoList[4], 48);
		dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, Sequence.tempoList);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tempoSpinner.setAdapter(dataAdapter);
		tempoSpinner.setSelection(2);
		tempoSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
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
				setMiddleText();
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});

		list = new ArrayList<>();
		int instrumentSelected = 0;
		for (int i=0;i<MainActivity.config.instruments.size();i++) {
			Instrument instrument = MainActivity.config.instruments.get(i);
			if(instrument.getValue()==MainActivity.config.sequenceInstrument.getValue())
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
				MainActivity.config.sequenceInstrument = MainActivity.config.instruments.get(position);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});

		list = new ArrayList<>();
		for (int i=0;i<NoteDuration.values().length;i++) {
			list.add(NoteDuration.values()[i].toString());
		}
		dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		durationSpinner.setAdapter(dataAdapter);
		durationSpinner.setSelection(noteDuration.ordinal());
		durationSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				noteDuration = NoteDuration.values()[position];
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});

		intList = new ArrayList<>();
		for(int i=16;i<16*5;i=i+16){
			intList.add(i);
		}
		ArrayAdapter<Integer> colDataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, intList);
		colDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		columnsSpinner.setAdapter(colDataAdapter);
		columnsSpinner.setSelection((numberOfCol/16)-1);
		columnsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				for(SequenceElement seq : sequenceElements){
					if(seq.column>(position+1)*16-1)
						seq.unSelect();
				}
				MainActivity.config.sequence.setInterval(createSequenceFile());
				numberOfCol = (position+1)*16;
				loadSequence();
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});

		reloadSequenceSpinner();
		sequenceSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

				MainActivity.config.sequence.setInterval(createSequenceFile());//save

				MainActivity.config.sequence = MainActivity.config.sequences.get(position);
				numberOfCol = MainActivity.config.sequence.getLength();
				loadSequence();
				columnsSpinner.setSelection((numberOfCol/16)-1);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});

		return rootView;
	}

	private int[] createSequenceFile(){
		ArrayList<Integer> seq = new ArrayList<>();
		for(int c=0;c<numberOfCol;c++){
			boolean colSet = false;
			for(int i=0;i<NUM_OF_ROWS;i++){
				SequenceElement info = sequenceElements.get(i*numberOfCol+c);
				if(info.selected){
					seq.add(info.interval);
					seq.add(info.noteDuration.getValue());
					colSet = true;
					break;
				}
			}
			if(!colSet){
				seq.add(-99);
				seq.add(NoteDuration.Sixteenth.getValue());
			}
		}
		int[] finalSeq = new int[seq.size()];

		for(int j = 0; j<seq.size(); j++){
			finalSeq[j] = seq.get(j);
		}

		MidiFile.writeSequenceFile(Note.getMidiValueFrom(key, octave), MainActivity.config.sequenceInstrument.getValue(), Note.DEFAULT_NOTE_VELOCITY, "sequence.mid", MainActivity.config.sequenceTempo.getTempoEvent(), finalSeq);

		return finalSeq;
	}

	private void reloadSequenceSpinner(){
		ArrayList<String> list = new ArrayList<>();
		int seqSelected =0;
		for (int i=0;i<MainActivity.config.sequences.size();i++) {
			Sequence seq = MainActivity.config.sequences.get(i);
			if(seq.getName().equals(MainActivity.config.sequence.getName()))
				seqSelected = i;
			list.add(seq.getName());
		}
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sequenceSpinner.setAdapter(dataAdapter);
		sequenceSpinner.setSelection(seqSelected);
	}

	private void loadSequence(){

		if(gridLayout.getChildCount()>0){
			gridLayout.removeAllViews();
		}

		gridLayout.setColumnCount(numberOfCol);

		sequenceElements = new ArrayList<>();
		int l=0;
		for(int i=12;i>=-12;i--){
			for(int c=0;c<numberOfCol;c++){
				FrameLayout fm = new FrameLayout(getActivity());
				final SequenceElement info = new SequenceElement(i, c, fm, l++);
				sequenceElements.add(info);
				fm.setLayoutParams(new LayoutParams(62, 62));
				fm.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						info.update(noteDuration);
					}
				});
				gridLayout.addView(fm);
			}
		}

		// load
		boolean lastWasRest = false;
		int interval = 0, col = 0, j = 0;
		for(int i: MainActivity.config.sequence.getSequence()){
			if(j%2==0){
				if(i == -99){
					lastWasRest = true;
				}else{
					lastWasRest = false;
					interval = i;
				}
			}else{
				NoteDuration nd = NoteDuration.Eighth;
				for(NoteDuration n: NoteDuration.values()){
					if(n.getValue()==i)
						nd = n;
				}
				if(!lastWasRest){
					int row = (-1*interval+12)%24;
					if (interval == -12 ) row = 24;


					SequenceElement refSeq = sequenceElements.get(row*numberOfCol+col);
					refSeq.update(nd);
				}
				col += 1;
			}
			j++;
		}
	}

	private void populateMarkers(){
		if(verticalMarkers.getChildCount()>0){
			verticalMarkers.removeAllViews();
		}

		for(int i = VERTICAL_MARKERS.length-1; i>=0; i--){
			TextView tv = new TextView(getActivity());
			tv.setHeight(62);
			tv.setWidth(60);
			tv.setGravity(Gravity.CENTER);
			tv.setText(VERTICAL_MARKERS[i]);
			verticalMarkers.addView(tv);
		}
		middleTv = new TextView(getActivity());
		middleTv.setHeight(62);
		middleTv.setWidth(60);
		middleTv.setGravity(Gravity.CENTER);
		setMiddleText();
		verticalMarkers.addView(middleTv);
		for (String verticalMarker : VERTICAL_MARKERS) {
			TextView tv = new TextView(getActivity());
			tv.setHeight(62);
			tv.setWidth(60);
			tv.setGravity(Gravity.CENTER);
			tv.setText(verticalMarker);
			verticalMarkers.addView(tv);
		}
	}
	private void setMiddleText(){
		String text = key.toString();
		if(text.contains("/"))
			text = text.substring(0, text.indexOf('/'))+octave;
		else
			text=text+octave;
		middleTv.setText(text);
	}

	public UsbConnection getUsbConn() {
		return usbConn;
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
