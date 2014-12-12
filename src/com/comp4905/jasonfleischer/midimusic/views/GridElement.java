package com.comp4905.jasonfleischer.midimusic.views;

import com.comp4905.jasonfleischer.midimusic.MainActivity;
import com.comp4905.jasonfleischer.midimusic.R;
import com.comp4905.jasonfleischer.midimusic.MidiMusicConfig.PlayingMode;
import com.comp4905.jasonfleischer.midimusic.model.Note;
import com.comp4905.jasonfleischer.midimusic.util.HLog;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

public class GridElement extends FrameLayout{
	private Note note;
	private boolean highligthed;
	private boolean disabled;

	public GridElement(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public void init(Note n){
		note =n;
		highligthed=false;
		disabled = false;
		if(MainActivity.config.playingMode ==PlayingMode.SINGLE_NOTE){
			if(!MainActivity.config.singleNoteInstrument.inRange(note.getMidiValue())){
				disabled = true;
				setAlpha(0.5f);
			}
		}else if(MainActivity.config.playingMode ==PlayingMode.CHORD){
			if(!MainActivity.config.chordInstrument.inRange(note.getMidiValue())){
				disabled = true;
				setAlpha(0.5f);
			}
		}else if(MainActivity.config.playingMode == PlayingMode.SEQUENCE){
			if(!MainActivity.config.sequenceInstrument.inRange(note.getMidiValue())){
				disabled = true;
				setAlpha(0.5f);
			}
		}
	}

	public void playNote(){
		if(disabled){
			HLog.i(getResources().getString(R.string.out_of_range));
			return;
		}
		note.playNote();
	}
	public void setHighlighted(){
		highligthed=true;
		((TextView) findViewById(R.id.grid_element_label)).setText(note.getName().name());
		setBackgroundResource(R.drawable.grid_element_highlighted);
	}

	public void setDefaultResource(){
		if(highligthed)
			setBackgroundResource(R.drawable.grid_element_highlighted);
		else
			setBackgroundResource(R.drawable.grid_element);
	}
}
