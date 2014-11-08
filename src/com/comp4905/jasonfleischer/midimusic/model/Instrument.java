package com.comp4905.jasonfleischer.midimusic.model;

import java.io.Serializable;

public class Instrument implements Serializable{
	
	private static final long serialVersionUID = -8851509295480162565L;
	private int value;
	private String name;
	
	public Instrument(int v, String n){
		value=v;
		name=n;
	}
	
	public int getValue() {
		return value;
	}
	public String getName() {
		return name;
	}
}
