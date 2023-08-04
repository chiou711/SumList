/*
 * Copyright (C) 2023 CW Chiu
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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.cw.sumlist.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

/**
 * Created by cw on 2023/07/27
 */
public class SetOftenItems extends Fragment{
    TextView title;
    ListView mListView;
    SetOftenItems_list list_selOftenIem;
	public static View rootView;
    AppCompatActivity act;
	Button btn_add_often_items;
	AlertDialog mDialog;
	EditText titleEditText;

	public SetOftenItems(){}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.set_often_items, container, false);
		act = (AppCompatActivity) getActivity();

        // title
        title = (TextView) rootView.findViewById(R.id.select_list_title);
        title.setText(R.string.config_often_items);

        // list view: selecting which pages to send
        mListView = (ListView)rootView.findViewById(R.id.listView1);

        //show list for selection
        list_selOftenIem = new SetOftenItems_list(act,rootView , mListView);

		// add button
		btn_add_often_items = rootView.findViewById(R.id.btn_add_often_items);

		btn_add_often_items.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				System.out.println("------- btn_add_often_items on click");
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				LayoutInflater mInflater= (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				View view = mInflater.inflate(R.layout.add_new_often_item, null);
				builder.setTitle(R.string.config_set_often_items)
					   .setPositiveButton(R.string.btn_OK, listener_ok)
					   .setNegativeButton(R.string.btn_Cancel, null);
				builder.setView(view);
				titleEditText =  view.findViewById(R.id.edit_title);
				mDialog = builder.create();
				mDialog.show();
			}
		});

		return rootView;
	}

	DialogInterface.OnClickListener listener_ok = new DialogInterface.OnClickListener()
	{
		@Override
		public void onClick(DialogInterface dialog, int which) {
			String newOftenItem = titleEditText.getText().toString();
			System.out.println("---- new often item = " + newOftenItem);
			dialog.dismiss();
		}
	};

	@Override
	public void onPause() {
		super.onPause();
	}
}