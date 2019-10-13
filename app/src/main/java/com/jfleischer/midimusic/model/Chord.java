package com.jfleischer.midimusic.model;

import com.jfleischer.midimusic.MainActivity;
import com.jfleischer.midimusic.audio.MidiFile;
import com.jfleischer.midimusic.model.Note.NoteDuration;
import com.jfleischer.midimusic.model.Note.NoteName;

public class Chord {

	private final NoteName rootNote;
	private final ChordName chordName;
	private final NoteDuration duration;
	private final int instrument;
	private final int octave;

	public enum ChordName{

		M("Maj", new int[]{4,7}), min("min", new int[]{3,7}), power("5", new int[]{7}), octave("octave",new int[]{12}),
		six("6", new int[]{4,7,9}), min6("m6", new int[]{3,7,9}), M7("M7", new int[]{4,7,11}), m7("m7",new int[]{3,7,10}),
		dom7("7", new int[]{4,7,10}), dim("dim", new int[]{3,6}),aug("aug", new int[]{4,8}), sus2("sus2", new int[]{3,7}),
		sus4("sus4", new int[]{5,7}), sixNine("6/9", new int[]{4,9,14}), major_add9("M(add9)", new int[]{4,7,14}),
		minor_add9("m(add9)", new int[]{3,7,14}),m7b5("m7b5", new int[]{3,6,10});

		private final String name;
		private final int[] intervals;

		ChordName(String n, int[] i){
			name = n;
			intervals = i;
		}
		@Override
		public String toString(){
			return name;
		}
		public int[] getIntervals() {
			return intervals;
		}
	}

	public Chord(NoteName nn, ChordName cn, NoteDuration nd, int ints, int oct){
		rootNote = nn;
		chordName = cn;
		duration = nd;
		instrument = ints;
		octave = oct;
		updateChordFile();
	}

	public NoteName getRootNote() {
		return rootNote;
	}

	public ChordName getChordName() {
		return chordName;
	}

	private void updateChordFile(){
		String fileName = "chrd_"+rootNote.name()+chordName.name()+".mid";
		int midiValue= 12+(12*octave)+rootNote.ordinal();
		MidiFile.writeChordFile(midiValue, instrument, Note.DEFAULT_NOTE_VELOCITY, duration.getValue(), fileName, MainActivity.config.tempo.getTempoEvent(), chordName.intervals );
		//soundID = SoundManager.getInstance().addChordSoundPool(fileName);
	}

	public void playChord() {
		//SoundManager.getInstance().playChordPlayerSound(soundID);
	}

}
