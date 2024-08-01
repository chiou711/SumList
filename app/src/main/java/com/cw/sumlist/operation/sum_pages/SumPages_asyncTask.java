package com.cw.sumlist.operation.sum_pages;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cw.sumlist.R;
import com.cw.sumlist.main.MainAct;
import com.cw.sumlist.util.Util;

/**
 *  Async: show progress bar and do Add all
 */
class SumPages_asyncTask extends AsyncTask<Void, Integer, Void> {

	private ProgressBar progressBar;
	Activity act;
	View rootView;
	private TextView messageText;

	SumPages_asyncTask(Activity _act, View _rootView) {
		act = _act;
		rootView = _rootView;

		Util.lockOrientation(act);

		messageText = (TextView) rootView.findViewById(R.id.sum_pages_message);
		messageText.setText(R.string.reading_data);

		progressBar = (ProgressBar) rootView.findViewById(R.id.sum_pages_progress);
		progressBar.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		if (this.progressBar != null) {
			progressBar.setProgress(values[0]);
		}
	}

	@Override
	protected Void doInBackground(Void... params) {
		// main function for adding
		MainAct.updatePageSumArr();
		SumPages.pageSumArr = MainAct.pageSumArr;

		SumPages.fillArray(act,true);
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		// lock orientation
		Util.unlockOrientation(act);

		// hide progress bar
		rootView.findViewById(R.id.show_sum_pages_progress).setVisibility(View.GONE);

		// init: set All checked
		CheckBox checkTvSelAll = rootView.findViewById(R.id.check_box_select_all_pages_sum_pages);
		checkTvSelAll.callOnClick();
	} // onPostExecute
}