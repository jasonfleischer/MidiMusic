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
	
	//private View[] allOtherKeyViews;
	//private static View whiteKeyToRight;
	
	public Key(View v, Note n, boolean isBlkKey, boolean isHLigt, View[] allOtherKeys, View toRight){
		view = v;
		note = n;
		isBlackKey = isBlkKey;
		isHighlighted = isHLigt;
		//allOtherKeyViews = allOtherKeys;
		//whiteKeyToRight = toRight;
		
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
		
		// set keyPress

		
		/*view.setOnTouchListener(new OnTouchListener() {		
			boolean keyPressed = false;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(note != null){
					
					float xPos = event.getX();
					float yPos = event.getY();
					 Rect r = new Rect();
			         view.getHitRect(r); 
			         if(!r.contains((int)xPos,(int)yPos)){
			        	 if(!keyPressed && whiteKeyToRight.getId()!=view.getId()){
			        		 Log.i(",","once"+whiteKeyToRight);
			        		 whiteKeyToRight.dispatchTouchEvent(event);
			        		 keyPressed = true;
			        		 //return false;
			        	 }
			        	/* for(View otherView: allOtherKeyViews){
			        		 Rect r2 = new Rect();
			        		 otherView.getHitRect(r);  
			        		 if({
			        			
			        			 otherView.dispatchTouchEvent(event);
			        		 }
					         //Log.i(",",""+xPos);
					         if(r2.contains((int)xPos,(int)yPos)){
					        	 HLog.e("other");
					        	 
					         }
			        	 }* /
			         }
					
					int action = event.getAction();
					if(action == MotionEvent.ACTION_DOWN){
						onPress();
						return true;
					}else if (action == MotionEvent.ACTION_UP){
						onRelease();
						return true;
					}
					return false;
				}else{
					//HLog.i("Note Unavailable");
					return false;
				}
					
			}
		});
		*/
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
