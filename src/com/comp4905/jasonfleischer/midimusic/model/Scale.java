package com.comp4905.jasonfleischer.midimusic.model;

import com.comp4905.jasonfleischer.midimusic.MainActivity;
import com.comp4905.jasonfleischer.midimusic.R;

public enum Scale{

	None(MainActivity.getInstance().getResources().getString(R.string.none), new int[]{}),
	Major("Major", new int[]{2,4,5,7,9,11}),
	min("Natural minor", new int[]{2,3,5,7,8,10}),
	hmin("Harmonic minor", new int[]{2,3,5,7,8,11}),
	Mpent("Maj Pentatonic", new int[]{2,4,7,9}),
	mpent("min Pentatonic", new int[]{3,5,7,10}),
	blues("Blues",new int[]{3,5,6,7,10}),
	whole("Whole note", new int[]{2,4,6,8,10}),
	Mtri("Maj Triad", new int[]{4,7}),
	mtri("min Triad", new int[]{3,7});

	private String name;
	private int[] intervals;

	Scale(String n, int[] i){
		name = n;
		intervals = i;
	}
	public String toString(){
		return name;
	}
	public int[] getIntervals() {
		return intervals;
	}
}

