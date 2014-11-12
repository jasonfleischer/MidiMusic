package com.comp4905.jasonfleischer.midimusic.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import com.comp4905.jasonfleischer.midimusic.MainActivity;
import com.comp4905.jasonfleischer.midimusic.MidiMusicConfig;
import com.comp4905.jasonfleischer.midimusic.model.Instrument;

@SuppressLint("SdCardPath")
public class FileManager {
	
	private static final FileManager instance = new FileManager();
	private final AssetManager assets;
	private final String DIR = "MidiMusic";
	private final String SAVED_CONFIG_FILENAME ="MidiMusic.ser";
	//public final String EXTERNAL_PATH =  "/sdcard/"+DIR+"/";
	public final String INTERNAL_PATH = MainActivity.getInstance().getFilesDir().getPath();
	
	public static FileManager getInstance(){
		return instance;
	}
	
	private FileManager(){
		assets = MainActivity.getInstance().getAssets();
		File path = new File(INTERNAL_PATH);
        if(!path.exists()) {
            path.mkdirs();
        }
	}
	
	public void loadInstrumentsFromAssets(ArrayList<Instrument> instruments){
		
 		try{		
			InputStream is = assets.open("instruments.txt");
			InputStreamReader isr = new  InputStreamReader(is); 
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
			   int value = Integer.valueOf(line.substring(0, line.indexOf(" ")));
			   String name = line.substring(line.indexOf(" ")+1, line.length() );
			   instruments.add( new Instrument(value, name));
			}
			is.close();
			isr.close();
			br.close();
		}catch(Exception ex){ 
			ex.printStackTrace();
		}
	}
	
	public String[] getMetronomeSoundsFromAssets(){
		try {
			return assets.list("metronome");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String[] getDrumFileNames(){
		try {
			return assets.list("drums");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public AssetFileDescriptor getAFD(String fileName){
		try {
			return assets.openFd(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean hasMusicConfigFile(){
		return (new File(INTERNAL_PATH+ SAVED_CONFIG_FILENAME)).exists();
	}
	public void writeMidiMusicConfig(final MidiMusicConfig object){	
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(INTERNAL_PATH+ SAVED_CONFIG_FILENAME));
					try{
					   out.writeObject(object);
					   ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
					   byte[] buf = bos.toByteArray(); 
					   out.write(buf); 
					} finally{
						out.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
		}).start();
	}
	public MidiMusicConfig readMidiMusicConfig(){	
		try {
			FileInputStream in = new FileInputStream(INTERNAL_PATH+ SAVED_CONFIG_FILENAME);
			ObjectInputStream reader = new ObjectInputStream(in);
			MidiMusicConfig result = (MidiMusicConfig) reader.readObject();
			reader.close();
			in.close();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("FileMan", ""+e+e.getMessage()+e.getLocalizedMessage());
			HLog.e("Problem reading saved configurations");
			return new MidiMusicConfig();
		}
	}
}
