package com.jfleischer.midimusic.views;

import com.jfleischer.midimusic.MainActivity;
import com.jfleischer.midimusic.R;
import com.jfleischer.midimusic.MidiMusicConfig.PlayingMode;
import com.jfleischer.midimusic.model.Note;
import com.jfleischer.midimusic.util.HLog;

import android.view.View;

public class Key {

	private final int defaultColor;
    private boolean disabled;

    private final View view;
	private final Note note;

	public Key(View v, Note n, boolean isBlkKey, boolean isHLight){
		view = v;
		note = n;

        disabled = false;
		if(MainActivity.config.playingMode ==PlayingMode.SINGLE_NOTE){
			if(!MainActivity.config.singleNoteInstrument.inRange(note.getMidiValue())){
				disabled = true;
			}
		}else if(MainActivity.config.playingMode ==PlayingMode.CHORD){
			if(!MainActivity.config.chordInstrument.inRange(note.getMidiValue())){
				disabled = true;
			}
		}else if(MainActivity.config.playingMode  == PlayingMode.SEQUENCE){
			if(!MainActivity.config.sequenceInstrument.inRange(note.getMidiValue())){
				disabled = true;
			}
		}

		if(disabled){
			v.setAlpha(0.5f);
		}
		if(isHLight){
			if(isBlkKey)
				defaultColor = R.drawable.key_highlighted_black;
			else
				defaultColor = R.drawable.key_highlighted_white;
			v.setBackgroundResource(defaultColor);
		}else{
			if(isBlkKey)
				defaultColor = R.drawable.key_black;
			else
				defaultColor = R.drawable.key_white_key;
		}
	}

	public void onPress(){
		if(disabled){
			HLog.i(MainActivity.getInstance().getResources().getString(R.string.out_of_range));
			return;
		}
		note.playNote();
		view.setBackgroundResource(R.drawable.key_yellow);
	}

	public void onRelease(){
		view.setBackgroundResource(defaultColor);
	}
}
