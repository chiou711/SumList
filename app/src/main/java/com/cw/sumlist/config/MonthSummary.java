/*
 * Copyright (C) 2019 CW Chiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cw.sumlist.config;


import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cw.sumlist.R;
import com.cw.sumlist.db.DB_folder;
import com.cw.sumlist.db.DB_page;
import com.cw.sumlist.main.MainAct;
import com.cw.sumlist.util.BaseBackPressedListener;
import com.cw.sumlist.util.preferences.Pref;

import androidx.fragment.app.Fragment;

public class MonthSummary extends Fragment
{

	static View mRootView;
	Activity act;
	public MonthSummary(){}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		System.out.println("================ About / onCreateView ==================");
		act = getActivity();

		mRootView = inflater.inflate(R.layout.month_summary, container, false);
		showSummaryDialog();

		// set Back pressed listener
		((MainAct)act).setOnBackPressedListener(new BaseBackPressedListener(MainAct.mAct));

		return mRootView;
	}

	// Show summary dialog
	void showSummaryDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		String title = (String) MainAct.mFolderTitle;
		title = title.concat(" ( ").concat(String.valueOf(MainAct.folder_sum)).concat(" ) ");
        String msgStr = act.getResources().getString(R.string.month_summary) +
                        " : " + title + "\n" ;

	    builder.setTitle(msgStr)
	          .setMessage(getSummaryString())
			  .setNegativeButton(R.string.notices_close, (dialog1, which1) ->
					  getActivity().getSupportFragmentManager().popBackStack())
			  .show();
	}

	// get summary string
	public String getSummaryString(){
		DB_folder dB_folder = new DB_folder(act , Pref.getPref_focusView_folder_tableId(act));
		int pages_count = dB_folder.getPagesCount(true);
		String summaryStr = "";

		for (int i = 0; i < pages_count; i++) {
			// page title
			String pageTitle = dB_folder.getPageTitle(i,true);

			DB_page db_page = new DB_page(act,dB_folder.getPageTableId(i,true));
			int notes_count = db_page.getNotesCount(true);

			// notes
			if(notes_count > 0) {
				// page title
				summaryStr = summaryStr.concat(pageTitle);
				summaryStr = summaryStr.concat("\n");

				db_page.open();
				for(int j=0; j< notes_count;j++) {
					// note mark
					if(db_page.getNoteMarking(j,false) == 0)
						summaryStr = summaryStr.concat("\t").concat("? ");
					else
						summaryStr = summaryStr.concat("\t");

					// note title
					summaryStr = summaryStr.concat(db_page.getNoteTitle(j,false));

					// note quantity
					summaryStr = summaryStr.concat(" x ");
					summaryStr = summaryStr.concat(String
							.valueOf(db_page.getNoteQuantity(j,false)));

					// note price
					summaryStr = summaryStr.concat(" = ");
					summaryStr = summaryStr.concat(String.valueOf(db_page.getNoteBody(j,false)));
					summaryStr = summaryStr.concat("\n");
				}
				db_page.close();

				summaryStr = summaryStr.concat("\n");
			}
		}
		return summaryStr;
	}

}