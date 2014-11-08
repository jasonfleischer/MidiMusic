package com.comp4905.jasonfleischer.midimusic.model;

import java.io.Serializable;

import com.comp4905.jasonfleischer.midimusic.MainActivity;
import com.comp4905.jasonfleischer.midimusic.MidiMusicConfig.PlayingMode;
import com.comp4905.jasonfleischer.midimusic.audio.MidiFile;
import com.comp4905.jasonfleischer.midimusic.audio.NDKFunct;
import com.comp4905.jasonfleischer.midimusic.audio.SoundManager;
import com.comp4905.jasonfleischer.midimusic.fragments.FragMentManager;
import com.comp4905.jasonfleischer.midimusic.util.FileManager;
import com.comp4905.jasonfleischer.midimusic.util.HLog;

public class Note implements Serializable{
	
	private static final long serialVersionUID = -1263171525588010652L;
	private NoteName name;
	private int octave;
	private int midiValue;
	private int soundID;
	
	public static final int DEFAULT_NOTE_VELOCITY = 127;
	private static final int MIN_MIDI_VALUE = 21;
	private static final int MAX_MIDI_VALUE = 108;
	
	public static enum NoteName{
		C("C"), Db("Db/C#"), D("D"), Eb("Eb/D#"), E("E"), F("F"), 
		Gb("Gb/F#"), G("G"), Ab("Ab/G#"), A("A"), Bb("Bb/A#"), B("B");
		
		private String name; 
		NoteName(String n){
			name = n;
		}
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
	
	public static enum NoteDuration{
		Sixteenth(4),Eighth(8),Quarter(16),Half(32),Whole(64);
		private int value;
		NoteDuration(int v){
			value = v;
		}
		public int getValue() {
			return value;
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
		if(MainActivity.config.playingMode != PlayingMode.DRUMS){
			SoundManager.getInstance().playSound(soundID);
			MainActivity.getInstance().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					FragMentManager.getInstance().updateNotePressed(name.toString(), octave);
				}
			});
		}else{
			SoundManager.getInstance().playDrumSound(MainActivity.config.midiDrumSounds[name.ordinal()].getSoundID());
		}
	}
	
	public static int getMidiValueFrom(NoteName noteName, int octave){
		return (octave*12 +noteName.ordinal())+12;
	}
	
	public void updateNoteFile(){
		if(soundID != -1){
			SoundManager.getInstance().unloadFromSoundPool(soundID);
		}
		String fileName = midiValue+".mid";
		switch(MainActivity.config.playingMode){
			case CHORD:
				
				NDKFunct.writeChordFile(midiValue, MainActivity.config.chordInstrument.getValue(), 
						DEFAULT_NOTE_VELOCITY, MainActivity.config.noteDuration.getValue(),
						FileManager.getInstance().EXTERNAL_PATH+fileName, MainActivity.config.tempo.getMpqn()
						,MainActivity.config.chord.getIntervals()); 
				/*MidiFile.writeChordFile(midiValue, MainActivity.config.chordInstrument.getValue(), 
						DEFAULT_NOTE_VELOCITY, MainActivity.config.noteDuration.getValue(),
						fileName, MainActivity.config.tempo.getTempoEvent(),
						MainActivity.config.chord.getIntervals());*/
				break;
			case SEQUENCE:
				//TODO NDK
				/*NDKFunct.writeSequenceFile(midiValue, MainActivity.config.sequenceInstrument.getValue(),
						DEFAULT_NOTE_VELOCITY, FileManager.getInstance().EXTERNAL_PATH+fileName, 
						MainActivity.config.tempo.getMpqn(),
						MainActivity.config.sequence.getSequence());*/
				
				MidiFile.writeSequenceFile(midiValue, MainActivity.config.sequenceInstrument.getValue(),
						DEFAULT_NOTE_VELOCITY, fileName, 
						MainActivity.config.sequenceTempo.getTempoEvent(),
						MainActivity.config.sequence.getSequence());
				break;
			case SINGLE_NOTE:
				MidiFile.writeSingleNoteFile(midiValue, MainActivity.config.singleNoteInstrument.getValue(), 
						DEFAULT_NOTE_VELOCITY, MainActivity.config.noteDuration.getValue(),
						fileName, MainActivity.config.tempo.getTempoEvent());
				/*NDKFunct.writeSingleNoteFile(midiValue, MainActivity.config.singleNoteInstrument.getValue(), 
						DEFAULT_NOTE_VELOCITY, MainActivity.config.noteDuration.getValue(),
						FileManager.getInstance().EXTERNAL_PATH+fileName, MainActivity.config.tempo.getMpqn());*/
				break;
			default:
				break;		
		}
		soundID = SoundManager.getInstance().addSoundSoundPool(fileName);
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
	}
}
