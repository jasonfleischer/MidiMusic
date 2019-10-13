package com.jfleischer.midimusic.fragments;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build;
import android.view.View;
import com.jfleischer.midimusic.MainActivity;
import com.jfleischer.midimusic.R;

public class FragmentController {
	private static final FragmentController instance = new FragmentController();
	private FragmentManager fragmentManager;

	private FragmentController() {  }
	public static FragmentController getInstance(){
		return instance;
	}

	public void init(FragmentManager f){
		fragmentManager =f;
		fragmentManager.beginTransaction().add(R.id.fragment_container, new InitFragment()).commit();
	}

	private void replace(Fragment newFragment, int containerId){
		fragmentManager.executePendingTransactions();
		fragmentManager.beginTransaction()/*.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)*/.replace(containerId, newFragment).commit();
	}

	void showDrumFragment(){
		replace(new DrumFragment(), R.id.fragment_container);
	}

	public void showInstrumentFragment(){
		replace(new InstrumentFragment(), R.id.fragment_container);
	}

	public void showConsoleFragment(){
		replace(new ConsoleFragment(), R.id.fragment_container);
	}

	void showChordFragment(){
		replace(new ChordFragment(), R.id.fragment_container);
	}

	void showSequenceFragment() {
		replace(new SequenceFragment(), R.id.fragment_container);
	}


	void setupInstrumentFragment(){
		fragmentManager.beginTransaction().add(R.id.instrument_fragment,(MainActivity.config.keysAreShowing?new KeysFragment():new GridFragment())).commit();
	}
	void showKeys(){
		replace(new KeysFragment(), R.id.instrument_fragment);
	}
	void showGrid() {
		replace(new GridFragment(), R.id.instrument_fragment);
	}

	void setupDrumFragment(){
		fragmentManager.beginTransaction().add(R.id.drum_fragment,(MainActivity.config.kitIsShowing?new DrumKitFragment():new DrumGridFragment())).commit();
	}
	void showDrumKit(){
		replace(new DrumKitFragment(), R.id.drum_fragment);
	}
	void showDrumGrid() {
		replace(new DrumGridFragment(), R.id.drum_fragment);
	}

	public void updateUSBConnection(boolean connected){

		Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container); // prevents duplicates
		if (fragment != null){
			if(fragment.isAdded()){
				if(fragment instanceof InstrumentFragment){
					((InstrumentFragment)fragment).getUsbConn().updateUSBConn(connected);
				}else if (fragment instanceof ConsoleFragment){
					((ConsoleFragment)fragment).getUsbConn().updateUSBConn(connected);
				}else if (fragment instanceof DrumFragment){
					((DrumFragment)fragment).getUsbConn().updateUSBConn(connected);
				}else if (fragment instanceof ChordFragment){
					((ChordFragment)fragment).getUsbConn().updateUSBConn(connected);
				}else if (fragment instanceof SequenceFragment){
					((SequenceFragment)fragment).getUsbConn().updateUSBConn(connected);
				}
			}
		}
	}

	public void updateNotePressed(String note, int octave){
		Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container); // prevents duplicates
		if (fragment != null){
			if(fragment.isAdded() && fragment instanceof InstrumentFragment)
				((InstrumentFragment)fragment).setNotePressed(note, octave);
		}
	}

	public void updateInitProgress(int percent){
		Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container); // prevents duplicates
		if (fragment != null){
			if(fragment.isAdded() && fragment instanceof InitFragment){
				InitFragment f = ((InitFragment) fragmentManager.findFragmentById(R.id.fragment_container));
				f.updateProgress(percent);
			}
		}

	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	void hideNavBar(){
		MainActivity.getInstance().getWindow().getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
	}

// --Commented out by Inspection START (2019-10-12 22:46):
//	void showNavBar(){
//		MainActivity.getInstance().getWindow().getDecorView().setSystemUiVisibility(
//				View.SYSTEM_UI_FLAG_VISIBLE);
//	}
// --Commented out by Inspection STOP (2019-10-12 22:46)
}
