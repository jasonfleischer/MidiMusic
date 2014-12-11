package com.comp4905.jasonfleischer.midimusic.midi;

import com.comp4905.jasonfleischer.midimusic.MainActivity;
import com.comp4905.jasonfleischer.midimusic.model.Note;
import com.comp4905.jasonfleischer.midimusic.util.HLog;

public class MidiListener implements OnMidiInputEventListener{

	@Override
	public void onMidiNoteOn(MidiInputDevice sender, int cable, int channel,
			int note, int velocity) {
		for(Note n:MainActivity.config.allNotes){
			if(n.getMidiValue()==(note+(-1*MainActivity.config.usbModulation))){
				n.playNote();
				return;
			}
		}
		HLog.e("Note not found onMidiNoteOn "+note+": "+velocity+": "+ cable+": "+sender+"? "+channel);
	}

	// Others not used //

	@Override
	public void onMidiMiscellaneousFunctionCodes(MidiInputDevice sender,
			int cable, int byte1, int byte2, int byte3) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onMidiCableEvents(MidiInputDevice sender, int cable, int byte1,
			int byte2, int byte3) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onMidiSystemCommonMessage(MidiInputDevice sender, int cable,
			byte[] bytes) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onMidiSystemExclusive(MidiInputDevice sender, int cable,
			byte[] systemExclusive) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onMidiNoteOff(MidiInputDevice sender, int cable, int channel,
			int note, int velocity) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onMidiPolyphonicAftertouch(MidiInputDevice sender, int cable,
			int channel, int note, int pressure) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onMidiControlChange(MidiInputDevice sender, int cable,
			int channel, int function, int value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onMidiProgramChange(MidiInputDevice sender, int cable,
			int channel, int program) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onMidiChannelAftertouch(MidiInputDevice sender, int cable,
			int channel, int pressure) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onMidiPitchWheel(MidiInputDevice sender, int cable,
			int channel, int amount) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onMidiSingleByte(MidiInputDevice sender, int cable, int byte1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onMidiRPNReceived(MidiInputDevice sender, int cable,
			int channel, int function, int valueMSB, int valueLSB) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onMidiNRPNReceived(MidiInputDevice sender, int cable,
			int channel, int function, int valueMSB, int valueLSB) {
		// TODO Auto-generated method stub
	}
}
