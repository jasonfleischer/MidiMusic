package com.jfleischer.midimusic.model;

import com.jfleischer.midimusic.MainActivity;
import com.jfleischer.midimusic.R;

public enum Scale{

	None(MainActivity.getInstance().getResources().getString(R.string.none), new int[]{}),
	Major("Major", new int[]{2,4,5,7,9,11}),
	min("Natural minor", new int[]{2,3,5,7,8,10}),
	harmonic_min("Harmonic minor", new int[]{2,3,5,7,8,11}),
	major_pent("Maj Pentatonic", new int[]{2,4,7,9}),
	minor_pent("min Pentatonic", new int[]{3,5,7,10}),
	blues("Blues",new int[]{3,5,6,7,10}),
	whole("Whole note", new int[]{2,4,6,8,10}),
	major_tri("Maj Triad", new int[]{4,7}),
	minor_tri("min Triad", new int[]{3,7});

	private final String name;
	private final int[] intervals;

	Scale(String n, int[] i){
		name = n;
		intervals = i;
	}
	@Override
	public String toString(){
		return name;
	}
	public int[] getIntervals() {
		return intervals;
	}
}

