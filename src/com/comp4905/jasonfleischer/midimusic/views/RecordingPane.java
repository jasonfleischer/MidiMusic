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

	private static TextView bpmTextView, timeTextView, statusTextView;
	private static ImageButton recordBtn, loopBtn, deleteTrackBtn;

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
			timeTextView.setText(masterTrack.getNumberOfBeats()+" "+getResources().getString(R.string.beats));
		}else{
			timeTextView.setText("");
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
					HLog.i(getResources().getString(R.string.cannot_record_while_looping));
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


							isRecording = true;
							SoundManager.getInstance().startMetronome(0, 0);

							if(masterTrack == null){
								HLog.i(getResources().getString(R.string.recording_started));
								masterTrack = new Track(tempo);
								MainActivity.getInstance().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										recordBtn.setImageResource(R.drawable.stop);
										statusTextView.setText(getResources().getString(R.string.recording_track));
									}
								});
							}else {

								HLog.i(getResources().getString(R.string.dubbing_started));
								SoundManager.getInstance().playTrack(masterTrack, false);
								masterTrack.setDubStartTime();
								MainActivity.getInstance().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										recordBtn.setImageResource(R.drawable.stop);
										statusTextView.setText(getResources().getString(R.string.dubbing_track));
									}
								});
							}
						}
					}, delay);



				}else{
					stopTimer();
					SoundManager.getInstance().stopMetronome();
					isRecording = false;
					recordBtn.setImageResource(R.drawable.record);

					if(masterTrack == null){
						HLog.i(getResources().getString(R.string.dubbing_stopped));
						statusTextView.setText(getResources().getString(R.string.dubbing_stopped));

					}else{
						SoundManager.getInstance().stopTrack();
						HLog.i(getResources().getString(R.string.recording_stopped));
						statusTextView.setText(getResources().getString(R.string.recording_stopped));
					}
					masterTrack.normalizeTime();
					timeTextView.setText(masterTrack.getNumberOfBeats()+" "+getResources().getString(R.string.beats));

				}
			}
		});

		loopBtn.setImageResource((isLooping?R.drawable.stop:R.drawable.loop));
		loopBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!isLooping){//start loop
					if(masterTrack ==null || masterTrack.getDelayBeforeNextLoop()==null || masterTrack.getSoundIds().isEmpty()){
						HLog.i(getResources().getString(R.string.cannot_loop_uncomplete));
						return;
					}

					isLooping = true;
					SoundManager.getInstance().playTrack(masterTrack, true);
					statusTextView.setText(getResources().getString(R.string.track_playing));
					loopBtn.setImageResource(R.drawable.stop);
				}else{//stop loop
					statusTextView.setText(getResources().getString(R.string.track_stopped));
					SoundManager.getInstance().stopTrack();
					isLooping = false;
					loopBtn.setImageResource(R.drawable.loop);
				}
			}
		});

		deleteTrackBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clearTrack();
				HLog.i(getResources().getString(R.string.clear_recording));
			}
		});
	}

	public void clearTrack(){
		SoundManager.getInstance().stopTrack();
		stopTimer();
		isLooping = false;
		isRecording = false;
		loopBtn.setImageResource(R.drawable.loop);
		recordBtn.setImageResource(R.drawable.record);
		masterTrack = null;
		statusTextView.setText(getResources().getString(R.string.track_cleared));
		timeTextView.setText(" ");
	}
	
	private void updateStatus(){
		if(masterTrack == null){
			statusTextView.setText(getResources().getString(R.string.empty_track));
		}else if(isRecording){
			statusTextView.setText(getResources().getString(R.string.recording_track));
		}else if(isLooping){
			statusTextView.setText(getResources().getString(R.string.track_playing));
		}else{
			statusTextView.setText(getResources().getString(R.string.track_ready));
		}
	}

	public static void stopDub(){
		MainActivity.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				statusTextView.setText(MainActivity.getInstance().getResources().getString(R.string.dubbing_stopped));
				recordBtn.setImageResource(R.drawable.record);
			}
		});
		SoundManager.getInstance().stopMetronome();
		masterTrack.normalizeTime();
		isRecording = false;
	}

	public static void stopTimer(){
		if(countInTimer != null){
			countInTimer.purge();
			countInTimer.cancel();
			countInTimer = null;
		}
	}
}