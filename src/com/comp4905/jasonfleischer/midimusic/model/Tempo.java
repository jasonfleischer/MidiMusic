package com.comp4905.jasonfleischer.midimusic.model;

import java.io.Serializable;

import android.util.Log;

public class Tempo implements Serializable {

	private static final long serialVersionUID = -3326218759047829266L;
	private static int MICROSECONDS_PER_MINUTE = 60000000;
	private String name;
	private int bpm; // beats per minute
	private int mpqn; //  million usec per quarter note
	private long msBetweenBeats;
	private int tempoEvent[];
	
	// range 0 to 8355711 or 0x000000 to 0x7F7F7F
	// BPM = MICROSECONDS_PER_MINUTE / MPQN
	// MPQN = MICROSECONDS_PER_MINUTE / BPM
	// MICROSECONDS_PER_MINUTE = 60000000
	// example: BPM 100 MPQN 600000	
	
	public Tempo(int bPM){
		
		
		//40 - 70
		//50 - ~220
		//60 -  40
		//70 - 40
		//80 -50
		//90 -52
		//100 -57
		//110 -62
		//120-67
		//130-72
		//140-81
		//150-83
		//160-100
		//170-102
		//180 -103
		
		
		bpm = bPM;
		msBetweenBeats = ((long)(60*1000)/(long)bpm); // b/s = b/m*1m/60s
											// b/ms = b/s*1s/1000ms 
		
		/*Grave – very slow (25–45 BPM)
		Lento – slowly (45–50 BPM)
		Largo – broadly (50–55 BPM)
		Larghetto – rather broadly (55–60 BPM)
		Adagio – slow and stately (literally, "at ease") (60–72 BPM)
		Adagietto – slower than andante (72–80 BPM)
		Andantino – slightly slower than andante (although in some cases it can be taken to mean slightly faster than andante) (80–84 BPM)
		Andante – at a walking pace (84–90 BPM)
		Andante moderato – between andante and moderato (thus the name andante moderato) (90–96 BPM)
			Marcia moderato – moderately, in the manner of a march[4][5] (83–85 BPM)
		Moderato – moderately (96–108 BPM)
		Allegro Moderato - moderately fast (108-112 BPM)
		Allegretto – close to but not quite allegro (112–120 BPM)
		Allegro – fast, quickly, and bright (120–144 BPM) (molto allegro is slightly faster than allegro, but always in its range)
			Vivace – lively and fast (132–144 BPM)
		Vivacissimo – very fast and lively (144–160 BPM)
			Allegrissimo (or Allegro Vivace) – very fast (145–167 BPM)
		Presto – extremely fast (168–200 BPM)
		Prestissimo – even faster than Presto (200 BPM and over)*/
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
	
		Log.i("",bpm+": "+mpqn+" "+slitHexStrg[0]+","+slitHexStrg[1]+","+slitHexStrg[2]);
	}
	
	public String toString(){
		return bpm + " - "+ name;
	}

	// getters
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
