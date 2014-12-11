package com.comp4905.jasonfleischer.midimusic.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.comp4905.jasonfleischer.midimusic.MainActivity;
import com.comp4905.jasonfleischer.midimusic.R;

public class LoadingDialogFragment extends DialogFragment {

	final private static LoadingDialogFragment dialogInstance= new LoadingDialogFragment();
	private static String text = MainActivity.getInstance().getResources().getString(R.string.loading);
	private static TextView messageTV;
	public LoadingDialogFragment(){
		super();
	}

	public synchronized static LoadingDialogFragment getInstance(){
		return dialogInstance;
	}

	public void show(String message) {
		if(LoadingDialogFragment.getInstance().isAdded())
			return;
		text = message;
		dialogInstance.show(MainActivity.getInstance().getFragmentManager(), "loadingDialog");
	}

	public void updateProgress(int percent){
		messageTV.setText(text+": "+percent+"%");
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = this.getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_loading, null);
		messageTV = (TextView) view.findViewById(R.id.loading_text);
		Dialog dialog = new Dialog(getActivity());
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		dialog.setContentView(view);
		dialog.setCanceledOnTouchOutside(false);
		dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.transparent_bg));
		dialog.show();
		return dialog;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public void dismiss(){
		if (getFragmentManager() != null) super.dismiss();
	}
}
