package com.comp4905.jasonfleischer.midimusic.audio;

import java.io.*;
import java.util.*;

import com.comp4905.jasonfleischer.midimusic.util.FileManager;


public class MidiFile {
	
	// Note ** Modified Class
	// Reference: Kevin Boone
	// http://kevinboone.net/javamidi.html
	
	// Note lengths:
	//  We are working with 32 ticks to the crotchet. So
	//  all the other note lengths can be derived from this
	//  basic figure. Note that the longest note we can
	//  represent with this code is one tick short of a 
	//  two semibreves (i.e., 8 crotchets)

	// Standard MIDI file header, for one-track file
	private static final int header[] = new int[] {
	   0x4d, 0x54, 0x68, 0x64, // MThd
	   0x00, 0x00, 0x00, 0x06, // chunk size 6
	   0x00, 0x00, // single-track format
	   0x00, 0x01, // one track
	   0x00, 0x10, // 16 ticks per quarter
	   0x4d, 0x54, 0x72, 0x6B
	};
	
	// Standard footer
	private static final int footer[] = new int[]{
	   0x01, 0xFF, 0x2F, 0x00
	};


	// A MIDI event to set the key signature. This is irrelent to
	//  playback, but necessary for editing applications 
	private static final int keySigEvent[] = new int[] {
	   0x00, 0xFF, 0x59, 0x02,
	   0x00, // C
	   0x00  // major
	};

	// A MIDI event to set the time signature. This is irrelent to
	//  playback, but necessary for editing applications 
	private static final int timeSigEvent[] = new int[] {
	   0x00, 0xFF, 0x58, 0x04,
	   0x04, // numerator
	   0x02, // denominator (2^2==4, powers of 2)
	   0x10, // ticks per click (not used)
	   0x08  // 32nd notes per crotchet 
	};

	// The collection of events to play, in time order
	private Vector<int[]> playEvents;

	/** Construct a new MidiFile with an empty playback event list */
	public MidiFile() {
		playEvents = new Vector<int[]>();
	}


	/** Write the stored MIDI events to a file 
	 * @param tempo */
	private void writeToFile (String filename, int[] tempoEvent) {
		
		try{
			FileOutputStream fos = new FileOutputStream (filename);
			fos.write (intArrayToByteArray (header));
	
			// Calculate the amount of track data
			// _Do_ include the footer but _do not_ include the 
			// track header
		
			int size = tempoEvent.length + keySigEvent.length + timeSigEvent.length + footer.length;
		
			// size = 7 + 6 + 8 + 4 = 25 0x19
			
			for (int i = 0; i < playEvents.size(); i++)
				size += playEvents.elementAt(i).length;
	
			//size += 
			
			// Write out the track data size in big-endian format
			// Note that this math is only valid for up to 64k of data
			//  (but that's a lot of notes) 
			int high = size / 256;  
			int low = size - (high * 256);
			fos.write ((byte) 0); // 8bits
			fos.write ((byte) 0);
			fos.write ((byte) high);
			fos.write ((byte) low);
	
			// Write the standard metadata — tempo, etc
			// At present, tempo is stuck at crotchet=60 
			fos.write (intArrayToByteArray (tempoEvent));
			fos.write (intArrayToByteArray (keySigEvent));
			fos.write (intArrayToByteArray (timeSigEvent));
	
			// Write out the note, etc., events
			for (int i = 0; i < playEvents.size(); i++){
				fos.write (intArrayToByteArray (playEvents.elementAt(i)));
			}
	
			// Write the footer and close
			fos.write (intArrayToByteArray (footer));
			fos.close();
		}catch(Exception ex){  }
	}

	/** Convert an array of integers which are assumed to contain
	    unsigned bytes into an array of bytes */
	private static byte[] intArrayToByteArray (int[] ints) {
		int l = ints.length;
		byte[] out = new byte[ints.length];
		for (int i = 0; i < l; i++){
			out[i] = (byte) ints[i];
		}
		return out;
	}
	
	/** Store a note-on event */
	private void noteOn (int channel, int delta, int note, int velocity){
		int[] data = new int[4];
		data[0] = delta;
		data[1] = 0x90 | channel; // x9 = id channel = 0
		data[2] = note;
		data[3] = velocity;
		playEvents.add (data);
	}

	/** Store a note-off event */
	private void noteOff (int channel, int delta, int note){
		int[] data = new int[4];
		data[0] = delta;
		data[1] = 0x80 | channel;
		data[2] = note;
		data[3] = 0;
		playEvents.add (data);
	}

	/** Store a program-change event at current position */
	private void progChange (int channel, int prog){
		int[] data = new int[3];
		data[0] = 0;
		data[1] = 0xC0 | channel;
		data[2] = prog;
		playEvents.add (data);
	}

	/** Store a note-on event followed by a note-off event a note length
	    later. There is no delta value — the note is assumed to
	    follow the previous one with no gap. */
	public void noteOnOffNow (int channel, int duration, int note, int velocity){
		noteOn (channel, 0, note, velocity);
		noteOff (channel, duration, note);
	}

	public void noteSequenceFixedVelocity (int channel, int[] sequence, int velocity, int midiValue){
		boolean lastWasRest = false;
		int restDelta = 0;
		for (int i = 0; i < sequence.length; i += 2){
			int note = sequence[i]+midiValue;
			int duration = sequence[i + 1];
			if (sequence[i] == -99){ // This is a rest
				restDelta += duration;
				lastWasRest = true;
			}else{ // A note, not a rest
				if (lastWasRest){
					noteOn (channel, restDelta, note, velocity);
					noteOff (channel, duration, note);
				}else{
					noteOn (channel, 0, note, velocity);
					noteOff (channel, duration, note);
				}
				restDelta = 0;
				lastWasRest = false;
			}
		}
	}

	// Jason Fleischer
	
	// 88 notes on a keyboard: range note from A0 to C8
	// midi notes range from 21 to 108 where
	// (Note<octave> -> midiValue)
	// A0 -> 21
	// C1 -> 24  
	// C2 -> 36	 
	// C3 -> 48
	// C4 -> 60 middle C, A4 = 440Hz
	// C5 -> 72
	// C6 -> 84
	// C7 -> 96
	// C8 -> 108
	
	public static void writeSingleNoteFile(int midiValue, int instrument, int velocity, int noteDuration, String fileName, int[] tempo){
		int channel = 0x00;
		MidiFile mf = new MidiFile();
		mf.progChange(channel, instrument); // change instrument
		mf.noteOn(channel, 0, midiValue, velocity);
		mf.noteOff(channel, noteDuration, midiValue);
		mf.writeToFile(FileManager.getInstance().EXTERNAL_PATH+fileName,tempo);
	}
	
	public static void writeChordFile(int midiValue, int instrument, int velocity, int noteDuration, String fileName, int[] tempo, int[] intervals){
		int channel = 0x00;
		MidiFile mf = new MidiFile();
		mf.progChange(channel, instrument); // change instrument
		mf.noteOn(channel, 0, midiValue, velocity);
		
		for(int i=0; i<intervals.length;i++){
			mf.noteOn(channel, 0, midiValue+intervals[i], velocity);
		}
		mf.noteOff(channel, noteDuration, midiValue);
		for(int i=0; i<intervals.length;i++){
			mf.noteOff(channel, 0, midiValue+intervals[i]);
		}
		mf.writeToFile(FileManager.getInstance().EXTERNAL_PATH+fileName,tempo);
	}
	 
	public static void writeSequenceFile(int midiValue, int instrument, int velocity, String fileName, int[] tempo, int[] sequence){	
		int channel = 0x00;
		MidiFile mf = new MidiFile();
		mf.progChange(channel, instrument); // change instrument
		mf.noteSequenceFixedVelocity(channel, sequence, velocity, midiValue);
		mf.writeToFile(FileManager.getInstance().EXTERNAL_PATH+fileName, tempo);
	}
}