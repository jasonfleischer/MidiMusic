package com.comp4905.jasonfleischer.midimusic.fragments;

import com.comp4905.jasonfleischer.midimusic.MainActivity;
import com.comp4905.jasonfleischer.midimusic.R;
import com.comp4905.jasonfleischer.midimusic.audio.SoundManager;
import com.comp4905.jasonfleischer.midimusic.model.DrumSound;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;



public class DrumKitFragment extends Fragment{

	private static FrameLayout[] drums;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		RelativeLayout rootView = (RelativeLayout) inflater.inflate(R.layout.fragment_drum_kit, container, false);

		drums = new FrameLayout[15];
		for(int i=0; i<drums.length; i++) {
			String viewID = "drum_" + i;
			int resID = getResources().getIdentifier(viewID, "id", "com.comp4905.jasonfleischer.midimusic");
			drums[i] = (FrameLayout) rootView.findViewById(resID);
			final int k =i;
			drums[i].setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					DrumSound drumSound = MainActivity.config.kitDrumSounds[k];
					if(DrumFragment.isEditMode){
						DrumFragment.deploySpinner(drumSound);
					}else{
						SoundManager.getInstance().playDrumSound(drumSound.getSoundID());
					}
				}
			});
		}

		changeEditMode((DrumFragment.isEditMode?View.VISIBLE:View.GONE));

		return rootView;
	}

	public static void changeEditMode(int vis){
		for(FrameLayout f: drums){
			f.getChildAt(0).setVisibility(vis);
		}
	}

	public static void changeDrum(DrumSound selectDrumSound, int position) {
		for(int i=0;i<MainActivity.config.gridDrumSounds.length;i++){
			if(MainActivity.config.kitDrumSounds[i].getFileName().equals(selectDrumSound.getFileName())){
				MainActivity.config.kitDrumSounds[i] = MainActivity.config.allDrumSounds[position];
				break;
			}
		}
	}
}
