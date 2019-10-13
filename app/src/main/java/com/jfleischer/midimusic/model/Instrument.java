package com.jfleischer.midimusic.model;

import java.io.Serializable;

public class Instrument implements Serializable{

	private static final long serialVersionUID = -8851509295480162565L;
	private final int value;
	private final String name;
	private final int minValue; // for note range
	private final int maxValue;

	public Instrument(int v, String n, int min, int max){
		value=v;
		name=n;
		minValue=min;
		maxValue=max;
	}

	public int getValue() {
		return value;
	}
	public String getName() {
		return name;
	}
	public boolean inRange(int midiValue){
		return midiValue>=minValue && midiValue<=maxValue;
	}
}
