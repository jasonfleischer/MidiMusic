package com.comp4905.jasonfleischer.midimusic.fragments;

import com.comp4905.jasonfleischer.midimusic.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

public class InitFragment extends Fragment{

	private TextView progress;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_init, container, false);
		FragMentManager.getInstance().hideNavBar();
		progress = (TextView) rootView.findViewById(R.id.progress);
		WebView wv = (WebView) rootView.findViewById(R.id.webview);
		wv.loadUrl("file:///android_asset/index.html");
		return rootView;
	}

	void updateProgress(int percent) {
		progress.setText(getResources().getString(R.string.loading)+": "+percent+"%");
	}
}
