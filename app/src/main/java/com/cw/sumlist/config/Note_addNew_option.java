/*
 * Copyright (C) 2021 CW Chiu
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
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cw.sumlist.R;

import java.util.Objects;

public class Note_addNew_option
{
	private RadioGroup mRadioGroup0;
    AlertDialog mDialog;
    private SharedPreferences mPref_add_new_note_location;
    private boolean bAddToTop;

	Note_addNew_option(final Activity activity, TextView textViewAddNewOption)
	{
		mPref_add_new_note_location = activity.getSharedPreferences("add_new_note_option", 0);
  		// inflate select style layout
  		LayoutInflater inflater;
  		inflater= (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  		View view = Objects.requireNonNull(inflater).inflate(R.layout.note_add_new_option, null);

		mRadioGroup0 = (RadioGroup)view.findViewById(R.id.radioGroup_new_at);

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
	
		builder.setTitle(R.string.add_new_note_position);

		// add to top: init
		if(mPref_add_new_note_location.getString("KEY_ADD_NEW_NOTE_TO","bottom").equalsIgnoreCase("top"))
		{
			mRadioGroup0.check(mRadioGroup0.getChildAt(0).getId());
			bAddToTop = true;
		}
		else if (mPref_add_new_note_location.getString("KEY_ADD_NEW_NOTE_TO","bottom").equalsIgnoreCase("bottom"))
		{
			mRadioGroup0.check(mRadioGroup0.getChildAt(1).getId());
			bAddToTop = false;
		}

        // add to top: listener
        mRadioGroup0.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup RG, int id) {
                bAddToTop = mRadioGroup0.indexOfChild(mRadioGroup0.findViewById(id))==0;

	            respondToSelection();

	            if(mPref_add_new_note_location.getString("KEY_ADD_NEW_NOTE_TO","bottom").equalsIgnoreCase("top"))
	            {
		            textViewAddNewOption.setText(activity.getResources().getText(R.string.add_new_note_top).toString());
	            }
	            else if (mPref_add_new_note_location.getString("KEY_ADD_NEW_NOTE_TO","bottom").equalsIgnoreCase("bottom"))
	            {
		            textViewAddNewOption.setText(activity.getResources().getText(R.string.add_new_note_bottom).toString());
	            }

	            mDialog.dismiss();
            }
        });


		builder.setView(view);
  		mDialog = builder.create();
  		mDialog.show();
	}
	
	// respond to selection
	void respondToSelection()
	{
		if(bAddToTop)
			mPref_add_new_note_location.edit().putString("KEY_ADD_NEW_NOTE_TO", "top").apply();
		else
			mPref_add_new_note_location.edit().putString("KEY_ADD_NEW_NOTE_TO", "bottom").apply();
	}
}