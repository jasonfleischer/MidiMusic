package com.comp4905.jasonfleischer.midimusic.views;

import com.comp4905.jasonfleischer.midimusic.MainActivity;
import com.comp4905.jasonfleischer.midimusic.R;
import com.comp4905.jasonfleischer.midimusic.MidiMusicConfig.PlayingMode;
import com.comp4905.jasonfleischer.midimusic.model.Note;
import com.comp4905.jasonfleischer.midimusic.util.HLog;

import android.view.View;

public class Key {

	private int defaultColor;
	private boolean isBlackKey;
	private boolean disabled;
	private boolean isHighlighted;

	private View view;
	private Note note;

	public Key(View v, Note n, boolean isBlkKey, boolean isHLigt, View[] allOtherKeys, View toRight){
		view = v;
		note = n;
		isBlackKey = isBlkKey;
		isHighlighted = isHLigt;

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
		if(isHighlighted){
			if(isBlackKey)
				defaultColor = R.drawable.key_highlighted_black;
			else
				defaultColor = R.drawable.key_highlighted_white;
			v.setBackgroundResource(defaultColor);
		}else{
			if(isBlackKey)
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
