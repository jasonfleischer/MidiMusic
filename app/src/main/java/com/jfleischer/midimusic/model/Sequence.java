package com.jfleischer.midimusic.model;

import java.io.Serializable;

import com.jfleischer.midimusic.MainActivity;
import com.jfleischer.midimusic.R;

public class Sequence implements Serializable{

	public static final String[] tempoList = new String[]{
		MainActivity.getInstance().getResources().getString(R.string.seq_very_slow),
		MainActivity.getInstance().getResources().getString(R.string.seq_slow),
		MainActivity.getInstance().getResources().getString(R.string.seq_med),
		MainActivity.getInstance().getResources().getString(R.string.seq_fast),
		MainActivity.getInstance().getResources().getString(R.string.seq_very_fast)};

	private static final long serialVersionUID = -8801190481097252349L;
	private final String name;
	private int[] sequence;
	private final int length; // numberOfCol

	public Sequence(String n, int[] s, int l){
		name=n;
		sequence=s;
		length = l;
	}

	public String getName() {
		return name;
	}
	public int[] getSequence() {
		return sequence;
	}

	public void setInterval(int[] s) {
		sequence = s;
	}

	public int getLength() {
		return length;
	}
}
