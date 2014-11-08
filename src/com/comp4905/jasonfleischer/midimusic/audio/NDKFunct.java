package com.comp4905.jasonfleischer.midimusic.audio;

public class NDKFunct {
	
	// cd /Users/Fleischer/Documents/HCN/repos/MidiMusic
	// javah -jni -classpath bin/classes/ -d jni/ com.comp4905.jasonfleischer.midimusic.audio.NDKFunct
	
	public native static void writeSingleNoteFile(int midiValue, int instrument, int velocity, int noteDuration, String fileName, int tempoMpqn);
	public native static void writeChordFile(int midiValue, int instrument, int velocity, int noteDuration, String fileName, int tempoMpqn, int[] intervals);
	public native static void writeSequenceFile(int midiValue, int instrument, int velocity, String fileName, int tempoMpqn, int[] sequence);
	
    static {
        // as defined by LOCAL_MODULE in Android.mk
        System.loadLibrary("com_comp4905_jasonfleischer_midimusic_audio_NDKFunct");
    }
}
