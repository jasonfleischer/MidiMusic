package com.comp4905.jasonfleischer.midimusic.audio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.comp4905.jasonfleischer.midimusic.MainActivity;
import com.comp4905.jasonfleischer.midimusic.model.Track;
import com.comp4905.jasonfleischer.midimusic.util.FileManager;
import com.comp4905.jasonfleischer.midimusic.util.HLog;
import com.comp4905.jasonfleischer.midimusic.views.RecordingPane;

import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;

public class SoundManager {
	
	private static final SoundManager instance = new SoundManager();
	private static SoundPool metronomePool, soundPool, drumSoundPool, chordSoundPool/*, dynamicSoundPool*/, sequenceSoundPool;
	//private MediaPlayer mediaPlayer;
	private Timer timer, metronomeTimer;
	
	private int lastSequenceId;
	
	public static boolean isPlayingMetronome = false;
	public static boolean isMetronomeSpeakState = false; 
	
	static private HashMap<String, Integer> metronomeSoundMap;
	//static private int metronomeLow, metronomeHigh;
	
	public enum SoundType{
		NOTE, CHORD, DRUM;
	}
	
	private SoundManager() {
		
		soundPool = new SoundPool(25, AudioManager.STREAM_DTMF, 0);
		metronomePool = new SoundPool(2, AudioManager.STREAM_DTMF, 0);
		drumSoundPool = new SoundPool(25, AudioManager.STREAM_DTMF, 0);
		
		chordSoundPool = new SoundPool(15, AudioManager.STREAM_DTMF, 0);
		sequenceSoundPool = new SoundPool(3, AudioManager.STREAM_DTMF, 0);
		
		//mediaPlayer = new MediaPlayer();
		//mAudioManager = (AudioManager) MainActivity.getInstance().getSystemService(MainActivity.getInstance().AUDIO_SERVICE);
		//streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		//streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	}
	
	public static SoundManager getInstance(){
		return instance;
	}
	
	
	
	/*public void createAndPlayNoteDynamically(int midiValue, int velocity){
		if(dynamicSoundPool ==null){
			dynamicSoundPool = new SoundPool(88, AudioManager.STREAM_MUSIC, 0);
		}
		String fileName = "dyn_"+midiValue+".mid";
		MidiFile.writeNoteFile(midiValue, velocity, fileName, MainActivity.config.tempo.getTempoEvent());
		final int id = dynamicSoundPool.load(FileManager.getInstance().EXTERNAL_PATH+fileName, 1);
		dynamicSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				dynamicSoundPool.play(id, 1, 1, 0, 0, 1);
			}
		});
	}*/
	
	//KEY POOL
	
	public int addSoundSoundPool(String fileName){
		return soundPool.load(FileManager.getInstance().EXTERNAL_PATH+fileName, 1);
	}
	public void playSound(int soundId) {	
		soundPool.play(soundId, 1, 1, 0, 0, 1);
		if(RecordingPane.isRecording){
			RecordingPane.masterTrack.add(SoundType.NOTE, System.nanoTime(), soundId);
		}
	}
	
	/*public void playSingleNoteSound(int soundId){
		soundPool.play(soundId, 1, 1, 0, 0, 1);
		if(RecordingPane.isRecording){
			RecordingPane.masterTrack.add(SoundType.NOTE, System.nanoTime(), soundId);
		}
	}
	public void playChordSound(int soundId){
		soundPool.play(soundId, 1, 1, 0, 0, 1);
		if(RecordingPane.isRecording){
			RecordingPane.masterTrack.add(SoundType.NOTE, System.nanoTime(), soundId);
		}
	}
	public void playSequenceSound(int soundId){
		soundPool.play(soundId, 1, 1, 0, 0, 1);
		if(RecordingPane.isRecording){
			RecordingPane.masterTrack.add(SoundType.NOTE, System.nanoTime(), soundId);
		}
	}*/
	
	
	public void unloadFromSoundPool(int soundID){
		soundPool.unload(soundID);
	}
	
	//DRUM POOL
	
	public int addSoundToDrumSoundPool(String fileName){
		return drumSoundPool.load(FileManager.getInstance().getAFD("drums/"+fileName), 1);
	}
	public void playDrumSound(int soundId) {
		drumSoundPool.play(soundId, 0.8f, 0.8f, 0, 0, 1);
		if(RecordingPane.isRecording){
			RecordingPane.masterTrack.add(SoundType.DRUM, System.nanoTime(), soundId);
		}
	}
	public void unloadDrumPool(int soundIDs){
		drumSoundPool.unload(soundIDs);
	}
	
	public long playCountIn(){
		final long tempoTime = MainActivity.config.tempo.getMS();
		stopMetronome();
		metronomeTimer = new Timer(); 
		
		metronomePool.play(metronomeSoundMap.get("four.mp3"), 1, 1, 0, 0, 1);
		String[] fileNames =  new String[]{"three.mp3", "two.mp3", "one.mp3"};
		for(int i=0; i<3;i++){
			metronomeTimer.schedule(new MetronomeTimerTimer(fileNames[i]), tempoTime*(i+1));
		}
		return tempoTime*4;
	}
	
	//Track
	public void playTrack(final Track t, final boolean repeat){
		stopTimer(timer);
		timer = new Timer(); 
		
		TimerTask tt = new TimerTask() {
			@Override
			public void run() {
				ArrayList<Integer> ids = t.getSoundIds();
				ArrayList<Long> times = t.getTimes();
				ArrayList<SoundType> st = t.getSoundTypes();
				
				for(int i=0; i<ids.size();i++){
					if(st.get(i) == SoundType.DRUM)
						timer.schedule(new TrackTimerTimer(ids.get(i), drumSoundPool), times.get(i));
					else if(st.get(i) == SoundType.CHORD)
						timer.schedule(new TrackTimerTimer(ids.get(i), chordSoundPool), times.get(i));
					else
						timer.schedule(new TrackTimerTimer(ids.get(i), soundPool), times.get(i));
				}
				if(!repeat){
					timer.schedule(new TimerTask() {
						
						@Override
						public void run() {
							RecordingPane.stopDub();
						}
					}, t.getDelayBeforeNextLoop());
				}
			}
		};
		
		if(t==null||t.getSoundIds().isEmpty()){
			HLog.e("playTrack null");
			return;
		}
		
		if (repeat)
			timer.scheduleAtFixedRate(tt, 0, t.getDelayBeforeNextLoop());
		else
			timer.schedule(tt, 0);
	}
	
	public void stopTrack(){
		stopTimer(timer);
		stopTimer(metronomeTimer);
	}
	
	private class TrackTimerTimer extends TimerTask{
		private int soundId;
		private SoundPool sundPool;
		private TrackTimerTimer(Integer integer, SoundPool sp) {
			soundId = integer;
			sundPool =sp;
		}
		@Override
		public void run() {
			sundPool.play(soundId, 1, 1, 0, 0, 1);
		}
	}

	private void stopTimer(Timer timer) {
		if(timer !=null){
			timer.purge();
			timer.cancel();
			timer = null;
		}
	}
	
	//CHORD POOL
	
	public void playChordSound(int soundId) {
		chordSoundPool.play(soundId, 1, 1, 0, 0, 1);
	}
	public void unloadChordSound(int soundId){
		chordSoundPool.unload(soundId);
	}
	public int addChordSoundPool(String fileName) {
		return chordSoundPool.load(FileManager.getInstance().EXTERNAL_PATH+fileName, 1);
	}
	
	//SEQUENCE

	public void playSequence(final boolean loop) {
		
		//String fileName = "dyn_"+midiValue+".mid";
		//MidiFile.writeNoteFile(midiValue, velocity, fileName, MainActivity.config.tempo.getTempoEvent());
		lastSequenceId = sequenceSoundPool.load(FileManager.getInstance().EXTERNAL_PATH+"sequence.mid", 1);
		sequenceSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				sequenceSoundPool.play(lastSequenceId, 1, 1, 0, (loop?-1:0), 1);
			}
		});
		
		/*try {
			mediaPlayer.reset();
			mediaPlayer.setDataSource(FileManager.getInstance().EXTERNAL_PATH+"sequence.mid");
			mediaPlayer.setLooping(loop);
			mediaPlayer.prepare();		                
			mediaPlayer.start();
		} catch (Exception e) {
			HLog.e("Media Player Failure");
			e.printStackTrace();
		}*/
	}
	
	public void stopLoop(){
		//sequenceSoundPool.
		sequenceSoundPool.stop(lastSequenceId);
		//sequenceSoundPool.setLoop(lastSequence, 0);
		//mediaPlayer.reset();
	}
	
	/*public void releaseMediaPlayer(){
	   /*if (mediaPlayer != null) {
		   mediaPlayer.reset();
           mediaPlayer.release();
           mediaPlayer = null;
       }* /
	}*/

	// Metronome
	
	private class MetronomeTimerTimer extends TimerTask{
		private String fileName;
		public MetronomeTimerTimer(String string) {
			fileName = string;
		}

		@Override
		public void run() {
			metronomePool.play(metronomeSoundMap.get(fileName), 1, 1, 0, 0, 1);
		}
	}
	
	public void initMetronome(){ // onCreate	
		metronomeSoundMap = new HashMap<String, Integer>(); 
		for (String fileName: FileManager.getInstance().getMetronomeSoundsFromAssets()){
			metronomeSoundMap.put(fileName, metronomePool.load(FileManager.getInstance().getAFD("metronome/"+fileName), 1));
		}
	}
	
	public void unloadMetronome(){ // onDestroy
		for (String fileName: FileManager.getInstance().getMetronomeSoundsFromAssets()){
			metronomePool.unload(metronomeSoundMap.get(fileName));
		}
		stopMetronome();
	}
	
	public void stopMetronome() {
		isPlayingMetronome = false;
		stopTimer(metronomeTimer);;
	}
	public void startMetronome(final int accent, final int indexOfSpokenOption) {
		// accent == 0->none, 1->"2" ,2->"3" or 3->"4"
		// indexOfSpokenOption 0-> -, 1-> &, 2-> e & a
		
		final long time = MainActivity.config.tempo.getMS();
		final long halfTime = time/2;
		final long quarterTime = time/4;
		TimerTask tt;
		if(isMetronomeSpeakState){
			tt = new TimerTask() {
				@Override
				public void run() {
					
					metronomePool.play(metronomeSoundMap.get("one.mp3"), 1, 1, 0, 0, 1);
					if(indexOfSpokenOption == 1){
						metronomeTimer.schedule(new MetronomeTimerTimer("n.mp3"), halfTime);
					}
					if(indexOfSpokenOption == 2){
						metronomeTimer.schedule(new MetronomeTimerTimer("e.mp3"), quarterTime);
						metronomeTimer.schedule(new MetronomeTimerTimer("n.mp3"), halfTime);
						metronomeTimer.schedule(new MetronomeTimerTimer("a.mp3"), halfTime+quarterTime);
					}
					String[] fileNames =  new String[]{"two.mp3", "three.mp3", "four.mp3"};
					for(int i=0; i<accent;i++){
						long timeSurplus = time*(i+1);
						metronomeTimer.schedule(new MetronomeTimerTimer(fileNames[i]), timeSurplus);
						if(indexOfSpokenOption == 1){				
							metronomeTimer.schedule(new MetronomeTimerTimer("n.mp3"), timeSurplus+halfTime);
						}
						if(indexOfSpokenOption == 2){
							metronomeTimer.schedule(new MetronomeTimerTimer("e.mp3"), timeSurplus+quarterTime);
							metronomeTimer.schedule(new MetronomeTimerTimer("n.mp3"), timeSurplus+halfTime);
							metronomeTimer.schedule(new MetronomeTimerTimer("a.mp3"), timeSurplus+halfTime+quarterTime);
						}
					}
				}
			};
		}else{
			tt = new TimerTask() {
				@Override
				public void run() {				
					metronomePool.play(metronomeSoundMap.get("Low.wav"), 1, 1, 0, 0, 1);
					for(int i=0; i<accent;i++){
						metronomeTimer.schedule(new MetronomeTimerTimer("High.wav"), time*(i+1));
					}
				}
			};
		}
		metronomeTimer = new Timer(); 
		isPlayingMetronome = true;
		metronomeTimer.schedule(tt, 0, time*(accent+1));
	}
}
