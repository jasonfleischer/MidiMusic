package com.jfleischer.midimusic.model;

import java.util.ArrayList;

import com.jfleischer.midimusic.audio.SoundManager.SoundType;

public class Track {
	private final Long startTime;
	private Long dubStartTime;

	private Long delayBeforeNextLoop;
	private final ArrayList<Long> times;
	private final ArrayList<Integer> soundIds;
	private final ArrayList<SoundType> soundTypes;

	private final Tempo tempo;
	private int numberOfBeats;

	private final ArrayList<Long> timesDub;
	private final ArrayList<Integer> soundIdsDub;
	private final ArrayList<SoundType> soundTypesDub;

	public Track(Tempo t){

		tempo = t;
		numberOfBeats=0;
		startTime = System.nanoTime();
		times = new ArrayList<>();
		soundIds = new ArrayList<>();
		soundTypes = new ArrayList<>();
		timesDub = new ArrayList<>();
		soundIdsDub = new ArrayList<>();
		soundTypesDub = new ArrayList<>();
		dubStartTime = null;
	}

	public void add(SoundType st, long l, int soundID) {
		if(dubStartTime == null){
			times.add(l);
			soundIds.add(soundID);
			soundTypes.add(st);
		}else{ // dubbing
			timesDub.add(l);
			soundIdsDub.add(soundID);
			soundTypesDub.add(st);
		}
	}

	public void normalizeTime(){
		if(dubStartTime == null){

			delayBeforeNextLoop = System.nanoTime();

			delayBeforeNextLoop = (delayBeforeNextLoop- startTime) / 1000000;
			numberOfBeats = Math.round(delayBeforeNextLoop/tempo.getMS()) + 1;
			delayBeforeNextLoop = numberOfBeats * tempo.getMS();

			if(times.isEmpty())
				return;
			for(int i =0;i<times.size();i++){
				times.set(i, (times.get(i)- startTime) / 1000000);
			}


		}else{ // merge dub track

			for(int i =0;i<timesDub.size();i++){
				timesDub.set(i, (timesDub.get(i)- dubStartTime) / 1000000);
			}

			if(times.isEmpty()){
				times.addAll(timesDub);
				soundIds.addAll(soundIdsDub);
				soundTypes.addAll(soundTypesDub);
			}else{

				final int size2 = times.size();
				for(int m=0; m<timesDub.size(); m++){
					for(int k=0, l=1; l<size2;k++,l++){
						if(k==0 && timesDub.get(m) <= times.get(k)){ //first element
							times.add(0, timesDub.get(m));
							soundIds.add(0, soundIdsDub.get(m));
							soundTypes.add(0, soundTypesDub.get(m));
							break;
						}
						else if(timesDub.get(m) > times.get(k) && timesDub.get(m) <= times.get(l)){
							times.add(l, timesDub.get(m));
							soundIds.add(l, soundIdsDub.get(m));
							soundTypes.add(l, soundTypesDub.get(m));
							break;
						}
						else if(l==timesDub.size()-1&& timesDub.get(m) >= times.get(l)){
							times.add(times.size(), timesDub.get(m));
							soundIds.add(soundIds.size(), soundIdsDub.get(m));
							soundTypes.add(soundTypes.size(), soundTypesDub.get(m));
							break;
						}
					}
				}
			}
			timesDub.clear();
			soundIdsDub.clear();
			soundTypesDub.clear();
		}
	}

	public ArrayList<Long> getTimes() {
		return times;
	}

	public ArrayList<Integer> getSoundIds() {
		return soundIds;
	}

	public Long getDelayBeforeNextLoop() {
		return delayBeforeNextLoop;
	}

	public ArrayList<SoundType> getSoundTypes() {
		return soundTypes;
	}

	public void setDubStartTime() {
		dubStartTime =  System.nanoTime();
	}

	public int getNumberOfBeats() {
		return numberOfBeats;
	}
}
