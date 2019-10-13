package com.jfleischer.midimusic.fragments;

import com.jfleischer.midimusic.MainActivity;
import com.jfleischer.midimusic.R;
import com.jfleischer.midimusic.model.Scale;
import com.jfleischer.midimusic.views.GridElement;

import android.app.Fragment;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.GridLayout;

public class GridFragment extends Fragment{
	private GridElement[] ges;
	private final static String TAG = GridFragment.class.getSimpleName();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		GridLayout rootView = (GridLayout) inflater.inflate(R.layout.fragment_grid, container, false);

		int indexOfAllNotes = 3+MainActivity.config.key.ordinal();
		int[] scale = MainActivity.config.choosenScale.getIntervals();
		if(scale.length==0){
			scale = Scale.Major.getIntervals();
		}

		ges = new GridElement[rootView.getChildCount()];
		for(int i=0;i<rootView.getChildCount();i++){
			GridElement ge = (GridElement) rootView.getChildAt(i);
			ges[i]=ge;
			ge.init(MainActivity.config.allNotes[indexOfAllNotes]);
			for(int j: scale){
				if(i%13 == j || i%13 == 0 || (i+1)%13==0){
					ge.setHighlighted();
					break;
				}
			}
			if((i+1)%13!=0){
				indexOfAllNotes++;
			}
		}

		rootView.setOnTouchListener(new OnTouchListener() {

			private int lastPressedGrid = -1;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				v.performClick();
				float xPos = event.getX();
				float yPos = event.getY();

				if (event.getAction() == MotionEvent.ACTION_DOWN){
					lastPressedGrid = -1;
				}

				for(int i=0;i<ges.length;i++){
					GridElement ge = ges[i];
					Rect r = new Rect();
					ge.getHitRect(r);
					if(r.contains((int)xPos,(int)yPos)){
						if(lastPressedGrid != i){
							if(lastPressedGrid != -1)
								ges[lastPressedGrid].setDefaultResource();
							lastPressedGrid = i;
							ge.playNote();
							ge.setBackgroundResource(R.drawable.grid_element_pressed);
							break;
						}
					}
				}

				if (event.getAction() == MotionEvent.ACTION_UP){
					try{
						ges[lastPressedGrid].setDefaultResource();
					}catch(Exception ex){
					    ex.printStackTrace();
                        Log.e(TAG, ex.getMessage());
                    }
					lastPressedGrid = -1;
				}

				return true;
			}
		});

		return rootView;
	}
}
