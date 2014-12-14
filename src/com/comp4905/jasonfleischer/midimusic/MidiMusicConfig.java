package com.comp4905.jasonfleischer.midimusic;

import java.io.Serializable;
import java.util.ArrayList;

import com.comp4905.jasonfleischer.midimusic.model.Chord;
import com.comp4905.jasonfleischer.midimusic.model.DrumSound;
import com.comp4905.jasonfleischer.midimusic.model.Instrument;
import com.comp4905.jasonfleischer.midimusic.model.Note;
import com.comp4905.jasonfleischer.midimusic.model.Scale;
import com.comp4905.jasonfleischer.midimusic.model.Sequence;
import com.comp4905.jasonfleischer.midimusic.model.Chord.ChordName;
import com.comp4905.jasonfleischer.midimusic.model.Note.NoteDuration;
import com.comp4905.jasonfleischer.midimusic.model.Note.NoteName;
import com.comp4905.jasonfleischer.midimusic.model.Tempo;
import com.comp4905.jasonfleischer.midimusic.util.FileManager;

public class MidiMusicConfig implements Serializable{

	private static final long serialVersionUID = 6431197269116457775L;

	//Global Variables
	public NoteName key;
	public int octave;
	public Instrument singleNoteInstrument;
	public Instrument chordInstrument;
	public Instrument sequenceInstrument;
	public Scale choosenScale;
	public int usbModulation;
	public Note[] allNotes;
	public ArrayList<Instrument> instruments;
	public PlayingMode playingMode;
	public ChordName chord;
	public NoteDuration noteDuration;
	public ArrayList<Sequence> sequences;
	public Sequence sequence;
	public ArrayList<Tempo> tempos;
	public Tempo tempo;
	public Tempo sequenceTempo;
	public static int seqNum;

	public DrumSound[] allDrumSounds;
	public DrumSound[] gridDrumSounds;
	public DrumSound[] kitDrumSounds;
	public DrumSound[] midiDrumSounds;

	public boolean kitIsShowing;
	public boolean keysAreShowing; // for instrument fragment

	public static enum PlayingMode{
		SINGLE_NOTE(MainActivity.getInstance().getResources().getString(R.string.playing_mode_single_note)),
		CHORD(MainActivity.getInstance().getResources().getString(R.string.playing_mode_chord)),
		SEQUENCE(MainActivity.getInstance().getResources().getString(R.string.playing_mode_sequence)),
		DRUMS(MainActivity.getInstance().getResources().getString(R.string.playing_mode_drums));
		private String name;
		PlayingMode(String n){
			name = n;
		}
		public String toString(){
			return name;
		}
	}

	public MidiMusicConfig(){
		// set defaults settings
		key = NoteName.C;
		octave = 4;
		playingMode = PlayingMode.SINGLE_NOTE;
		choosenScale = Scale.None;
		chord = Chord.ChordName.M;
		noteDuration = NoteDuration.Quarter;
		sequences = new ArrayList<Sequence>();
		int [] seq1 = new int[]{ // -99 for rests
				0,  NoteDuration.Eighth.getValue(),
				7,  NoteDuration.Eighth.getValue(),
				12, NoteDuration.Eighth.getValue(),
				7,  NoteDuration.Eighth.getValue()
		};
		int [] seq2= new int[]{
				0,  NoteDuration.Eighth.getValue(),
				4,  NoteDuration.Sixteenth.getValue(),
				7, NoteDuration.Sixteenth.getValue(),
				4,  NoteDuration.Sixteenth.getValue()
		};
		int [] seq3= new int[]{
				0,  NoteDuration.Eighth.getValue(),
				12,  NoteDuration.Eighth.getValue(),
				0, NoteDuration.Eighth.getValue(),
				10,  NoteDuration.Eighth.getValue()
		};
		seqNum = 2;
		sequence = new Sequence(MainActivity.getInstance().getResources().getString(R.string.playing_mode_sequence)+" 1", seq1, 16);
		sequences.add(sequence);
		addSequence(seq2, 32);
		addSequence(seq3, 48);

		instruments = new ArrayList<Instrument>();
		FileManager.getInstance().loadInstrumentsFromAssets(instruments); //make instruments

		singleNoteInstrument = instruments.get(0); 	//Piano
		chordInstrument = instruments.get(0); 		//Piano
		sequenceInstrument = instruments.get(0); 	//Piano
		usbModulation = 0;
		allNotes = new Note[88]; // 88 keys on a keyboard

		tempos = new ArrayList<Tempo>();
		for(int i=40; i<210; i=i+2){
			tempos.add(new Tempo(i));
		}
		tempo = tempos.get(10);
		sequenceTempo = tempos.get(0);

		kitIsShowing = false;
		keysAreShowing = true;

		createDrumSounds();
	}

	private void createDrumSounds(){

		String[] fileNames = FileManager.getInstance().getDrumFileNames();
		allDrumSounds = new DrumSound[fileNames.length];
		gridDrumSounds = new DrumSound[25];
		kitDrumSounds = new DrumSound[15];
		midiDrumSounds = new DrumSound[12];


		for(int i=0;i< fileNames.length;i++){
			DrumSound ds = new DrumSound(fileNames[i]);
			allDrumSounds[i] = ds;
		}

		gridDrumSounds[0] = allDrumSounds[14];//crashes
		gridDrumSounds[1] = allDrumSounds[15];
		gridDrumSounds[2] = allDrumSounds[16];
		gridDrumSounds[3] = allDrumSounds[17];
		gridDrumSounds[4] = allDrumSounds[18];
		gridDrumSounds[5] = allDrumSounds[60];//toms
		gridDrumSounds[6] = allDrumSounds[63];
		gridDrumSounds[7] = allDrumSounds[61];
		gridDrumSounds[8] = allDrumSounds[21];
		gridDrumSounds[9] = allDrumSounds[22];
		gridDrumSounds[10] = allDrumSounds[30];//hihat
		gridDrumSounds[11] = allDrumSounds[25];
		gridDrumSounds[12] = allDrumSounds[49];//snare
		gridDrumSounds[13] = allDrumSounds[43];
		gridDrumSounds[14] = allDrumSounds[44];
		gridDrumSounds[15] = allDrumSounds[70];
		gridDrumSounds[16] = allDrumSounds[48];
		gridDrumSounds[17] = allDrumSounds[32];//kick
		gridDrumSounds[18] = allDrumSounds[0];
		gridDrumSounds[19] = allDrumSounds[24];
		gridDrumSounds[20] = allDrumSounds[52];//stereo fx
		gridDrumSounds[21] = allDrumSounds[53];
		gridDrumSounds[22] = allDrumSounds[54];
		gridDrumSounds[23] = allDrumSounds[55];
		gridDrumSounds[24] = allDrumSounds[23];

		kitDrumSounds[0] = allDrumSounds[11];
		kitDrumSounds[1] = allDrumSounds[14];
		kitDrumSounds[2] = allDrumSounds[15];
		kitDrumSounds[3] = allDrumSounds[16];
		kitDrumSounds[4] = allDrumSounds[17];
		kitDrumSounds[5] = allDrumSounds[44];
		kitDrumSounds[6] = allDrumSounds[25];
		kitDrumSounds[7] = allDrumSounds[63];//tom
		kitDrumSounds[8] = allDrumSounds[61];//tom
		kitDrumSounds[9] = allDrumSounds[18];
		kitDrumSounds[10] = allDrumSounds[70];
		kitDrumSounds[11] = allDrumSounds[49];//snare
		kitDrumSounds[12] = allDrumSounds[35];//kick
		kitDrumSounds[13] = allDrumSounds[21];//tom
		kitDrumSounds[14] = allDrumSounds[22];

		midiDrumSounds[0] = allDrumSounds[35];
		midiDrumSounds[1] = allDrumSounds[22];
		midiDrumSounds[2] = allDrumSounds[21];
		midiDrumSounds[3] = allDrumSounds[61];
		midiDrumSounds[4] = allDrumSounds[63];
		midiDrumSounds[5] = allDrumSounds[49];//snare
		midiDrumSounds[6] = allDrumSounds[25];
		midiDrumSounds[7] = allDrumSounds[10];
		midiDrumSounds[8] = allDrumSounds[14];
		midiDrumSounds[9] = allDrumSounds[15];
		midiDrumSounds[10] = allDrumSounds[16];
		midiDrumSounds[11] = allDrumSounds[17];
	}

	void setNotes(int index, int oct, NoteName name, int midiV){
		allNotes[index] = new Note(oct, name, midiV);
	}

	public Sequence addSequence(int[] seq, int length){
		if(sequences.size()<30){
			Sequence sequ = new Sequence(MainActivity.getInstance().getResources().getString(R.string.playing_mode_sequence)
					+" "+seqNum++, seq, length);
			sequences.add(sequ);
			return sequ;
		}
		return null;
	}
	public boolean removeSequence(){
		if(sequences.size()>1){
			sequences.remove(sequence);
			sequence = sequences.get(sequences.size()-1);
			return true;
		}
		return false;
	}
}
