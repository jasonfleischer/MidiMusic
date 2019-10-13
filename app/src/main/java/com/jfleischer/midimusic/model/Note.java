package com.jfleischer.midimusic.model;

import java.io.Serializable;

import com.jfleischer.midimusic.MainActivity;
import com.jfleischer.midimusic.R;
import com.jfleischer.midimusic.audio.MidiFile;
import com.jfleischer.midimusic.audio.SoundManager;
import com.jfleischer.midimusic.audio.SoundManager.SoundType;
import com.jfleischer.midimusic.fragments.FragmentController;
import com.jfleischer.midimusic.util.HLog;

public class Note implements Serializable{

	private static final long serialVersionUID = -1263171525588010652L;
	private final NoteName name;
	private final int octave;
	private int midiValue;
	private int soundID;
	private int chordSoundID;
	private int sequenceSoundID;

	public static final int DEFAULT_NOTE_VELOCITY = 127;
	private static final int MIN_MIDI_VALUE = 21;
	private static final int MAX_MIDI_VALUE = 108;

	public enum NoteName{
		C("C"), Db("Db/C#"), D("D"), Eb("Eb/D#"), E("E"), F("F"),
		Gb("Gb/F#"), G("G"), Ab("Ab/G#"), A("A"), Bb("Bb/A#"), B("B");

		private final String name;
		NoteName(String n){
			name = n;
		}

		@Override
		public String toString(){
			return name;
		}

		public static NoteName getNameName(String name){
			for(NoteName nn:NoteName.values()){
				if(nn.name.equals(name))
					return nn;
			}
			return null;


		}
	}

	public enum NoteDuration{
		Sixteenth(4, MainActivity.getInstance().getResources().getString(R.string.note_very_short)),
		Eighth(8, MainActivity.getInstance().getResources().getString(R.string.note_short)),
		Quarter(16, MainActivity.getInstance().getResources().getString(R.string.note_med)),
		Half(32, MainActivity.getInstance().getResources().getString(R.string.note_long)),
		Whole(64, MainActivity.getInstance().getResources().getString(R.string.note_very_long));

		private final int value;
		private final String name;
		NoteDuration(int v, String n){
			value = v;
			name = n;
		}
		public int getValue() {
			return value;
		}
		public String getName(){
			return name;
		}
	}

	public Note(int oct, NoteName n, int midiV){
		if(midiV<MIN_MIDI_VALUE||midiV>MAX_MIDI_VALUE){
			HLog.e("out of range midi:"+midiValue);
		}
		name = n;
		octave = oct;
		midiValue = midiV;
		soundID = -1;
		updateNoteFile();
	}

	public void playNote(){

		switch(MainActivity.config.playingMode){
		case CHORD:
			SoundManager.getInstance().playSound(chordSoundID, SoundType.CHORD);
			MainActivity.getInstance().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					FragmentController.getInstance().updateNotePressed(name.toString(), octave);
				}
			});
			break;
		case DRUMS:
			SoundManager.getInstance().playDrumSound(MainActivity.config.midiDrumSounds[name.ordinal()].getSoundID());
			MainActivity.getInstance().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					FragmentController.getInstance().updateNotePressed(MainActivity.config.midiDrumSounds[name.ordinal()].getName(), -1);
				}
			});
			break;
		case SEQUENCE:
			SoundManager.getInstance().playSound(sequenceSoundID, SoundType.SEQUENCE);
			MainActivity.getInstance().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					FragmentController.getInstance().updateNotePressed(name.toString(), octave);
				}
			});
			break;
		case SINGLE_NOTE:
			SoundManager.getInstance().playSound(soundID, SoundType.NOTE);
			MainActivity.getInstance().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					FragmentController.getInstance().updateNotePressed(name.toString(), octave);
				}
			});
			break;
		default:
			break;
		}
	}

	public static int getMidiValueFrom(NoteName noteName, int octave){
		return (octave*12 +noteName.ordinal())+12;
	}

	public void updateNoteFile(){

		String fileName;
		switch(MainActivity.config.playingMode){
		case CHORD:

			fileName = midiValue+"_chord.mid";
			/*NDKFunct.writeChordFile(midiValue, MainActivity.config.chordInstrument.getValue(),
						DEFAULT_NOTE_VELOCITY, MainActivity.config.noteDuration.getValue(),
						FileManager.getInstance().EXTERNAL_PATH+fileName, MainActivity.config.tempo.getMPQN()
						,MainActivity.config.chord.getIntervals()); */
			MidiFile.writeChordFile(midiValue, MainActivity.config.chordInstrument.getValue(),
					DEFAULT_NOTE_VELOCITY, MainActivity.config.noteDuration.getValue(),
					fileName, MainActivity.config.tempo.getTempoEvent(),
					MainActivity.config.chord.getIntervals());
			chordSoundID = SoundManager.getInstance().addSoundSoundPool(fileName);
			break;
		case SEQUENCE:
			fileName = midiValue+"_sequence.mid";
			/*NDKFunct.writeSequenceFile(midiValue, MainActivity.config.sequenceInstrument.getValue(),
						DEFAULT_NOTE_VELOCITY, FileManager.getInstance().EXTERNAL_PATH+fileName,
						MainActivity.config.tempo.getMPQN(),
						MainActivity.config.sequence.getSequence());*/

			MidiFile.writeSequenceFile(midiValue, MainActivity.config.sequenceInstrument.getValue(),
					DEFAULT_NOTE_VELOCITY, fileName,
					MainActivity.config.sequenceTempo.getTempoEvent(),
					MainActivity.config.sequence.getSequence());
			sequenceSoundID = SoundManager.getInstance().addSoundSoundPool(fileName);
			break;
		case SINGLE_NOTE:
			fileName = midiValue+".mid";
			MidiFile.writeSingleNoteFile(midiValue, MainActivity.config.singleNoteInstrument.getValue(),
					DEFAULT_NOTE_VELOCITY, MainActivity.config.noteDuration.getValue(),
					fileName, MainActivity.config.tempo.getTempoEvent());
			/*NDKFunct.writeSingleNoteFile(midiValue, MainActivity.config.singleNoteInstrument.getValue(),
						DEFAULT_NOTE_VELOCITY, MainActivity.config.noteDuration.getValue(),
						FileManager.getInstance().EXTERNAL_PATH+fileName, MainActivity.config.tempo.getMPQN());*/
			soundID = SoundManager.getInstance().addSoundSoundPool(fileName);
			break;
		default:
			break;
		}

	}

	public NoteName getName() {
		return name;
	}
	public int getOctave() {
		return octave;
	}
	public int getMidiValue() {
		return midiValue;
	}

	public void unLoad() {
		SoundManager.getInstance().unloadFromSoundPool(soundID);
		SoundManager.getInstance().unloadFromSoundPool(sequenceSoundID);
		SoundManager.getInstance().unloadFromSoundPool(chordSoundID);
	}
}
