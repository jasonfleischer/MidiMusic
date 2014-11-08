package com.comp4905.jasonfleischer.midimusic.model;

import java.io.Serializable;

public class Sequence implements Serializable{

	private static final long serialVersionUID = -8801190481097252349L;
	private String name;
	private int[] sequence;
	private int length; // numberOfCol

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
