package com.comp4905.jasonfleischer.midimusic.model;

import java.io.Serializable;

public class Tempo implements Serializable {

	private static final long serialVersionUID = -3326218759047829266L;
	private static int MICROSECONDS_PER_MINUTE = 60000000;
	private String name;
	private int bpm;  // beats per minute
	private int mpqn; // million usec per quarter note
	private long msBetweenBeats;
	private int tempoEvent[];

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

		mpqn = MICROSECONDS_PER_MINUTE / bpm; //  million usec per quarter note

		String hexStrg = Integer.toHexString(mpqn);
		while(hexStrg.length()<6){
			hexStrg = "0"+hexStrg; // bpm 58 -208
		}
		String[] slitHexStrg = new String[3];
		slitHexStrg[0] = "0x"+hexStrg.substring(0, 2);
		slitHexStrg[1] = "0x"+hexStrg.substring(2, 4);
		slitHexStrg[2] = "0x"+hexStrg.substring(4, 6);
		tempoEvent = new int[]{ 0x00, 0xFF, 0x51, 0x03,
				Integer.decode(slitHexStrg[0]),Integer.decode(slitHexStrg[1]),Integer.decode(slitHexStrg[2])};

		//Log.i("",bpm+": "+mpqn+" "+slitHexStrg[0]+","+slitHexStrg[1]+","+slitHexStrg[2]);
	}

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
	public int getMpqn() {
		return mpqn;
	}
}
