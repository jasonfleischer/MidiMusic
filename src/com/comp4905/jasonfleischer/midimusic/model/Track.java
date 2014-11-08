package com.comp4905.jasonfleischer.midimusic.model;

import java.util.ArrayList;

import android.util.Log;

import com.comp4905.jasonfleischer.midimusic.audio.SoundManager.SoundType;

public class Track {
	private Long startTime;
	private Long dubStartTime;
	
	private Long delayBeforeNextLoop;
	private ArrayList<Long> times;
	private ArrayList<Integer> soundIds;
	private ArrayList<SoundType> soundTypes;
	
	private Tempo tempo;
	private int numberOfBeats;
	
	private ArrayList<Long> timesDub;
	private ArrayList<Integer> soundIdsDub;
	private ArrayList<SoundType> soundTypesDub;
	
	public Track(Tempo t){
		
		tempo = t;
		numberOfBeats=0;
		
		startTime = System.nanoTime();
		times = new ArrayList<Long>();
		soundIds = new ArrayList<Integer>();
		soundTypes = new ArrayList<SoundType>();
		timesDub = new ArrayList<Long>();
		soundIdsDub = new ArrayList<Integer>();
		soundTypesDub = new ArrayList<SoundType>();
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
			
			Log.i("","tempo.getBpm()"+ tempo.getMS() +" vs "+delayBeforeNextLoop);
			numberOfBeats = Math.round(delayBeforeNextLoop/tempo.getMS());
			//delayBeforeNextLoop = (long) (numberOfBeats * tempo.getMS());
			
			if(times.isEmpty())
				return;
			for(int i =0;i<times.size();i++){
				times.set(i, (times.get(i)- startTime) / (long)1000000);
			}
			delayBeforeNextLoop = (delayBeforeNextLoop- startTime) / (long)1000000; 
		}else{ // merge dub track
			
			
			
			
			for(int i =0;i<timesDub.size();i++){
				timesDub.set(i, (timesDub.get(i)- dubStartTime) / (long)1000000);
			}
			long dubEndTime = (System.nanoTime() -dubStartTime) /(long)1000000;
			
			Log.e("", "Merge Dub Track");
			Log.i("", "timesDub"+ timesDub);
			Log.i("", "times"+times);
			Log.i("", "soundIdsDub"+soundIdsDub);
			Log.i("", "soundIds"+soundIds);
			Log.i("", "soundTypesDub"+soundTypesDub);
			Log.i("", "soundTypes"+ soundTypes);
			
			if(delayBeforeNextLoop>dubEndTime){
				Log.e("", "delayBeforeNextLoop<dubEndTime");
				
				int numberOfRepeats = (int) Math.floor(delayBeforeNextLoop/dubEndTime);
				
				final int size = timesDub.size();
				for(int j=0;j<numberOfRepeats;j++){		
					for(int i=0;i<size;i++){
						timesDub.add(timesDub.get(i)+dubEndTime*(j+1));
						soundIdsDub.add(soundIdsDub.get(i));
						soundTypesDub.add(soundTypesDub.get(i));
					}
				}
				
				delayBeforeNextLoop=numberOfRepeats*dubEndTime;
				
				Log.e("", "numberOfRepeats"+numberOfRepeats);
				Log.i("", "timesDub"+ timesDub);
				Log.i("", "times"+times);
				Log.i("", "soundIdsDub"+soundIdsDub);
				Log.i("", "soundIds"+soundIds);
				Log.i("", "soundTypesDub"+soundTypesDub);
				Log.i("", "soundTypes"+ soundTypes);
				
				final int size2 = timesDub.size();
				for(int m=0; m<times.size(); m++){
					for(int k=0, l=1; l<size2;k++,l++){	
						
						if(k==0 && times.get(m) < timesDub.get(k)){ //first element 
							timesDub.add(0, times.get(m));
							soundIdsDub.add(0, soundIds.get(m));
							soundTypesDub.add(0, soundTypes.get(m));
							break;
						}
						else if(times.get(m) >= timesDub.get(k) && times.get(m) < timesDub.get(l)){
							timesDub.add(l, times.get(m));
							soundIdsDub.add(l, soundIds.get(m));
							soundTypesDub.add(l, soundTypes.get(m));
							break;
						}		
						else if(l==times.size()-1&& times.get(m) > timesDub.get(l)){
							timesDub.add(timesDub.size(), times.get(m));
							soundIdsDub.add(soundIdsDub.size(), soundIds.get(m));
							soundTypesDub.add(soundTypesDub.size(), soundTypes.get(m));
							break;
						}
							
					}
				}
				
				times = timesDub;
				soundIds = soundIdsDub;
				soundTypes = soundTypesDub;
				
			}else{
				Log.e("", "delayBeforeNextLoop<dubEndTime");
				
				
				
				int numberOfRepeats = (int) Math.floor(dubEndTime/delayBeforeNextLoop);
				final int size = times.size();
				for(int j=0;j<numberOfRepeats;j++){			
					for(int i =0;i<size;i++){
						times.add(times.get(i)+delayBeforeNextLoop*(j+1));
						soundIds.add(soundIds.get(i));
						soundTypes.add(soundTypes.get(i));
					}
				}
				
				delayBeforeNextLoop=numberOfRepeats*delayBeforeNextLoop;
				
				Log.e("", "numberOfRepeats"+numberOfRepeats);
				Log.i("", "timesDub"+ timesDub);
				Log.i("", "times"+times);
				Log.i("", "soundIdsDub"+soundIdsDub);
				Log.i("", "soundIds"+soundIds);
				Log.i("", "soundTypesDub"+soundTypesDub);
				Log.i("", "soundTypes"+ soundTypes);
				
				final int size2 = times.size();
				for(int m=0; m<timesDub.size(); m++){
					for(int k=0, l=1; l<size2;k++,l++){	
						
						if(k==0 && timesDub.get(m) < times.get(k)){ //first element 
							times.add(0, timesDub.get(m));
							soundIds.add(0, soundIdsDub.get(m));
							soundTypes.add(0, soundTypesDub.get(m));
							break;
						}
						else if(timesDub.get(m) >= times.get(k) && timesDub.get(m) < times.get(l)){
							times.add(l, timesDub.get(m));
							soundIds.add(l, soundIdsDub.get(m));
							soundTypes.add(l, soundTypesDub.get(m));
							break;
						}	
						else if(l==timesDub.size()-1&& timesDub.get(m) > times.get(l)){
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
			dubStartTime = null;
			
			Log.e("", "Merge Dub Track");
			Log.i("", "timesDub"+ timesDub);
			Log.i("", "times"+times);
			Log.i("", "soundIdsDub"+soundIdsDub);
			Log.i("", "soundIds"+soundIds);
			Log.i("", "soundTypesDub"+soundTypesDub);
			Log.i("", "soundTypes"+ soundTypes);
			
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
		dubStartTime =  System.nanoTime();;
	}

	public int getNumberOfBeats() {
		return numberOfBeats;
	}

}
