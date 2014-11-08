package com.comp4905.jasonfleischer.midimusic.views;

import com.comp4905.jasonfleischer.midimusic.R;
import com.comp4905.jasonfleischer.midimusic.model.Note;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

public class GridElement extends FrameLayout{
	private Note note;
	private boolean highligthed;

	public GridElement(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public void init(Note n){
		note =n;
		highligthed=false;
	}
	public void playNote(){
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
