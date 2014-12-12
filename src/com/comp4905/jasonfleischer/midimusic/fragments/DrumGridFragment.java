package com.comp4905.jasonfleischer.midimusic.fragments;

import com.comp4905.jasonfleischer.midimusic.MainActivity;
import com.comp4905.jasonfleischer.midimusic.R;
import com.comp4905.jasonfleischer.midimusic.model.DrumSound;
import com.comp4905.jasonfleischer.midimusic.views.DrumPad;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

public class DrumGridFragment extends Fragment{
	
	private static GridLayout rootView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = (GridLayout) inflater.inflate(R.layout.fragment_drum_grid, container, false);
		
		for(int i =0;i<rootView.getChildCount();i++){
			DrumPad dp = (DrumPad) rootView.getChildAt(i);
			dp.init(MainActivity.config.gridDrumSounds[i], DrumFragment.isEditMode);
		}
		
		return rootView;
	}

	public static void changeEditMode(){
		for(int i =0;i<rootView.getChildCount();i++){
			DrumPad d = (DrumPad) rootView.getChildAt(i);
			d.setMode(DrumFragment.isEditMode);
		}
	}

	public static void changeDrum(DrumSound selectDrumSound, int position) {
		for(int i=0;i<MainActivity.config.gridDrumSounds.length;i++){
			if(MainActivity.config.gridDrumSounds[i].getFileName().equals(selectDrumSound.getFileName())){
				MainActivity.config.gridDrumSounds[i] = MainActivity.config.allDrumSounds[position];
				DrumPad d = (DrumPad) rootView.getChildAt(i);
				d.setSound(MainActivity.config.allDrumSounds[position]);
				break;
			}
		}
		
	}
}
