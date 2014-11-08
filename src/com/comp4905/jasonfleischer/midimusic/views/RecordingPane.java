package com.comp4905.jasonfleischer.midimusic.views;

import java.util.Timer;
import java.util.TimerTask;

import com.comp4905.jasonfleischer.midimusic.MainActivity;
import com.comp4905.jasonfleischer.midimusic.R;
import com.comp4905.jasonfleischer.midimusic.audio.SoundManager;
import com.comp4905.jasonfleischer.midimusic.model.Tempo;
import com.comp4905.jasonfleischer.midimusic.model.Track;
import com.comp4905.jasonfleischer.midimusic.util.HLog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RecordingPane extends LinearLayout{

	private TextView bpmTextView, timeTextView, statusTextView;
	private ImageButton recordBtn, loopBtn, deleteTrackBtn;
	
	private volatile static boolean isLooping= false;
	public volatile static boolean isRecording = false;
	public volatile static Track masterTrack;
	private static Timer countInTimer;
	
	
	private Tempo tempo;
	
	public RecordingPane(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void init(){
	
		bpmTextView = (TextView) findViewById(R.id.recording_bpm);
		tempo = MainActivity.config.tempo;
		bpmTextView.setText(tempo.getBpm()+" BPM");
		
		
		timeTextView=  (TextView) findViewById(R.id.recording_time);
		if(masterTrack!=null){
			timeTextView.setText(masterTrack.getNumberOfBeats()+" beats");
		}else{
			timeTextView.setText("-");
		}
		
		statusTextView= (TextView) findViewById(R.id.recording_status);
		recordBtn = (ImageButton) findViewById(R.id.recording_rec_btn);
		loopBtn= (ImageButton) findViewById(R.id.recording_loop_btn);
		deleteTrackBtn = (ImageButton) findViewById(R.id.recording_delete_track_btn);
		updateStatus();
		
		recordBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if(isLooping){
					HLog.i("Cannot record while track is looping");
					return;
				}
				
				if(!isRecording){	
					//count in
					stopTimer();
					long delay = SoundManager.getInstance().playCountIn();
					countInTimer = new Timer();
					countInTimer.schedule(new TimerTask(){

						@Override
						public void run() {
							MainActivity.getInstance().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									recordBtn.setImageResource(R.drawable.stop);
									statusTextView.setText("Recording");
								}
							});
							
							isRecording = true;
							if(masterTrack == null){
								HLog.i("Recording Started");
								
								masterTrack = new Track(tempo);
							}else {
								HLog.i("Dubbing Started");
								
								SoundManager.getInstance().playTrack(masterTrack);
								masterTrack.setDubStartTime();
							}
						}
					}, delay);
					
					
					
				}else{
					stopTimer();
					isRecording = false;
					recordBtn.setImageResource(R.drawable.record);
					HLog.i("Recording Stopped");
					statusTextView.setText("Recording stopped");
					if(masterTrack != null){
						SoundManager.getInstance().stopTrack();
					}
					masterTrack.normalizeTime();
					timeTextView.setText(masterTrack.getNumberOfBeats()+" beats");
					
				}
			}
		});
		
		loopBtn.setImageResource((isLooping?R.drawable.stop:R.drawable.loop));
		loopBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				
				if(!isLooping){
					if(masterTrack ==null || masterTrack.getDelayBeforeNextLoop()==null){
						HLog.i("Cannot loop empty or uncomplete recording");
						return;
					}
					//long delay = SoundManager.getInstance().playCountIn();
					
					//countInTimer = new Timer();
					//countInTimer.schedule(new TimerTask(){

						//@Override
						//public void run() {
							//MainActivity.getInstance().runOnUiThread(new Runnable() {
								//@Override
								//public void run() {
									loopBtn.setImageResource(R.drawable.stop);
								//}
							//});
							isLooping = true;
							SoundManager.getInstance().playTrack(masterTrack);
						//}
					//}, delay);
						
					statusTextView.setText("Track playing");
					
				}else{
					
					statusTextView.setText("Track stopped");
					SoundManager.getInstance().stopTrack();
					isLooping = false;
					loopBtn.setImageResource(R.drawable.loop);
				}
				//isLooping=!isLooping;
			}
		});
		
		deleteTrackBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SoundManager.getInstance().stopTrack();
				stopTimer();
				isLooping = false;
				isRecording = false;
				loopBtn.setImageResource(R.drawable.loop);
				recordBtn.setImageResource(R.drawable.record);
				masterTrack = null;
				HLog.i("Cleared recording");
				statusTextView.setText("Track Cleared");
				timeTextView.setText("-");
			}
		});
	}

	
	private void updateStatus(){
		if(masterTrack == null){
			statusTextView.setText("No Track");
		}else if(isRecording){
			statusTextView.setText("Recording");
		}else if(isLooping){
			statusTextView.setText("Track playing");
		}else{
			statusTextView.setText("Track ready");
		}
		
	}
	
	public static void stopTimer(){
		if(countInTimer != null){
			countInTimer.purge();
			countInTimer.cancel();
			countInTimer = null;
		}
	}
}
