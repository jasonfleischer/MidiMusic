package com.jfleischer.midimusic.model;

import java.io.Serializable;

public class Tempo implements Serializable {

	private static final long serialVersionUID = -3326218759047829266L;
    private final String name;
	private final int bpm;  // beats per minute
	//private int micro_seconds_per_minute; // million u-sec per quarter note
	private final long msBetweenBeats;
    private int[] tempoEvent;

	// range 0 to 8355711 or 0x000000 to 0x7F7F7F
	// BPM = MICROSECONDS_PER_MINUTE / MPQN
	// MPQN = MICROSECONDS_PER_MINUTE / BPM
	// MICROSECONDS_PER_MINUTE = 60000000
	// example: BPM 100 MPQN 600000

	public Tempo(int bPM){

		bpm = bPM;
		msBetweenBeats = ((long)(60*1000)/(long)bpm); // b/s = b/m*1m/60s
		// b/ms = b/s*1s/1000ms
		if(bpm<46)
			name = "Grave";
		else if(bpm<51)
			name = "Lento";
		else if(bpm<56)
			name = "Largo";
		else if(bpm<61)
			name = "Larghetto";
		else if(bpm<73)
			name = "Adagio";
		else if(bpm<81)
			name = "Adagietto";
		else if(bpm<85)
			name = "Andantino";
		else if(bpm<91)
			name = "Andante";
		else if(bpm<97)
			name = "Andante moderato";
		else if(bpm<109)
			name = "Moderato";
		else if(bpm<113)
			name = "Allegro Moderato";
		else if(bpm<121)
			name = "Allegretto";
		else if(bpm<146)
			name = "Allegro";
		else if(bpm<161)
			name = "Vivacissimo";
		else if(bpm<167)
			name = "Allegrissimo";
		else if(bpm<201)
			name = "Presto";
		else
			name = "Prestissimo";

		createTempoEvent();
	}

	private void createTempoEvent(){

        int MICROSECONDS_PER_MINUTE = 60000000;
        int micro_seconds_per_minute = MICROSECONDS_PER_MINUTE / bpm; //  million u-sec per quarter note

		StringBuilder hexString = new StringBuilder(Integer.toHexString(micro_seconds_per_minute));
		while(hexString.length()<6){
			hexString.insert(0, "0"); // bpm 58 -208
		}
		String[] splitHexString = new String[3];
		splitHexString[0] = "0x"+hexString.substring(0, 2);
		splitHexString[1] = "0x"+hexString.substring(2, 4);
		splitHexString[2] = "0x"+hexString.substring(4, 6);
		tempoEvent = new int[]{ 0x00, 0xFF, 0x51, 0x03,
				Integer.decode(splitHexString[0]),Integer.decode(splitHexString[1]),Integer.decode(splitHexString[2])};

	}

	@Override
	public String toString(){
		return bpm + " - "+ name;
	}
	public String getName() {
		return name;
	}
	public int[] getTempoEvent() {
		return tempoEvent;
	}
	public int getBpm() {
		return bpm;
	}
	public long getMS() {
		return msBetweenBeats;
	}
}
