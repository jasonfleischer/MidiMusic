package com.comp4905.jasonfleischer.midimusic.fragments;

import com.comp4905.jasonfleischer.midimusic.MainActivity;
import com.comp4905.jasonfleischer.midimusic.R;
import com.comp4905.jasonfleischer.midimusic.model.Note;
import com.comp4905.jasonfleischer.midimusic.views.Key;

import android.app.Fragment;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.TextView;

public class KeysFragment extends Fragment{

	private View[] keyViews;
	private View[] blackKeyViews;
	private View[] whiteKeyViews;

	private Key[] keys;
	private Key[] blackKeys;
	private Key[] whiteKeys;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_keys, container, false);
		((TextView) rootView.findViewById(R.id.key_text)).setText(MainActivity.config.key.toString()+MainActivity.config.octave);

		keyViews = new View[13];
		blackKeyViews = new View[5];
		whiteKeyViews = new View[8];
		keys = new Key[13];
		blackKeys = new Key[5];
		whiteKeys = new Key[8];
		Note[] notes = getKeyBoardNotes();

		int j=0,k=0;
		for(int i=0; i<keys.length; i++) {
			String viewID = "key_" + i;
			int resID = getResources().getIdentifier(viewID, "id", "com.comp4905.jasonfleischer.midimusic");


			boolean isHighlighted;
			int[] scaleIntervals = MainActivity.config.choosenScale.getIntervals();
			if(scaleIntervals.length == 0){
				isHighlighted = false;
			}else{
				if(i==0||i==12|| containsInterval(i, scaleIntervals))
					isHighlighted =true;
				else
					isHighlighted = false;
			}
			keyViews[i] = rootView.findViewById(resID);
			keys[i] = new Key(keyViews[i], notes[i], ((i==1||i==3||i==6||i==8||i==10)?true:false), isHighlighted, keyViews, keyViews[5]);

			//sort keys
			if(i==1||i==3||i==6||i==8||i==10){
				blackKeyViews[j] = keyViews[i];
				blackKeys[j] =keys[i];
				j++;
			}else{
				whiteKeyViews[k] = keyViews[i];
				whiteKeys[k] = keys[i];
				k++;
			}
		}


		rootView.setOnTouchListener(new OnTouchListener() {

			private int lastBlackKeyPressed = -1;
			private int lastWhiteKeyPressed = -1;
			boolean blackKeyPressed = false;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				float xPos = event.getX();
				float yPos = event.getY();

				if (event.getAction() == MotionEvent.ACTION_DOWN){
					lastBlackKeyPressed = -1;
					lastWhiteKeyPressed = -1;
					blackKeyPressed = false;
				}


				for(int i=0;i<blackKeyViews.length;i++){
					View kv = blackKeyViews[i];
					Rect r = new Rect();
					kv.getHitRect(r);
					if(r.contains((int)xPos,(int)yPos)){
						if(lastBlackKeyPressed != i){
							if(lastBlackKeyPressed!=-1)
								blackKeys[lastBlackKeyPressed].onRelease();
							lastBlackKeyPressed = i;
							blackKeys[i].onPress();
							blackKeyPressed = true;
							if(lastWhiteKeyPressed!=-1){
								whiteKeys[lastWhiteKeyPressed].onRelease();
							}

							break;
						}
					}
				}

				if(!blackKeyPressed){
					for(int i=0;i<whiteKeyViews.length;i++){
						View kv = whiteKeyViews[i];
						Rect r = new Rect();
						kv.getHitRect(r);
						if(r.contains((int)xPos,(int)yPos)){
							if(lastWhiteKeyPressed != i){
								if(lastWhiteKeyPressed!=-1)
									whiteKeys[lastWhiteKeyPressed].onRelease();
								lastWhiteKeyPressed = i;
								whiteKeys[i].onPress();
								blackKeyPressed = false; //??
										if(lastBlackKeyPressed!=-1){
											blackKeys[lastBlackKeyPressed].onRelease();
										}
								break;
							}
						}
					}
				}

				if (event.getAction() == MotionEvent.ACTION_UP){
					if(lastBlackKeyPressed!=-1){
						blackKeys[lastBlackKeyPressed].onRelease();
					}
					if(lastWhiteKeyPressed!=-1){
						whiteKeys[lastWhiteKeyPressed].onRelease();
					}
				}

				return true;
			}
		});

		return rootView;
	}

	private boolean containsInterval(int i, int[] scaleIntervals){
		for(int j=0;j<scaleIntervals.length;j++){
			if(scaleIntervals[j]==i){
				return true;
			}
		}
		return false;
	}

	private Note[] getKeyBoardNotes(){
		Note[] notes = new Note[13];
		Note[] allNotes = MainActivity.config.allNotes;
		for(int j=0;j<allNotes .length-13;j++){
			Note n = allNotes[j];
			if(n.getName().equals(MainActivity.config.key) && n.getOctave() == MainActivity.config.octave){
				notes = new Note[13];
				for(int i=0; i<13;i++){
					notes[i] = n;
					n = allNotes[++j];
				}
				break;
			}
		}
		return notes;
	}
}
