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

package com.cw.sumlist.util.category.config;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cw.sumlist.R;
import com.cw.sumlist.db.DB_often;
import com.mobeta.android.dslv.DragSortListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

/**
 * Created by cw on 2023/09/10
 */
public class Often_config_list extends Fragment{
    TextView title;
	DragSortListView mListView;
	Often_list setOftenIem_list;
	public static View rootView;
    AppCompatActivity act;
	Button btn_add_often_item;
	AlertDialog mDialog;
	EditText titleEditText;

	public Often_config_list(){}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.often_config_list, container, false);
		act = (AppCompatActivity) getActivity();

        // title
        title = (TextView) rootView.findViewById(R.id.select_list_title);
        title.setText(R.string.often_item_title);

        // list view: selecting which pages to send
        mListView = rootView.findViewById(R.id.listView1);

        //show list for selection
		showListView();

		// add often item button
		btn_add_often_item = rootView.findViewById(R.id.btn_add_often_item);
		btn_add_often_item.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				// add often item dialog
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				LayoutInflater mInflater= (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				View view = mInflater.inflate(R.layout.add_new_often_item, null);
				builder.setTitle(R.string.config_set_often_item)
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

	// confirm Add new often item
	DialogInterface.OnClickListener listener_ok = new DialogInterface.OnClickListener()	{
		@Override
		public void onClick(DialogInterface dialog, int which) {
			String newOftenItem = titleEditText.getText().toString();

			// add often item to DB
			DB_often db_often = new DB_often(act);
			db_often.insertOften(db_often,newOftenItem ,true);

			// refresh listview
			showListView();

			dialog.dismiss();
		}
	};

	@Override
	public void onPause() {
		super.onPause();
	}

	// show list view
	void showListView(){
		setOftenIem_list = new Often_list(act, rootView);
	}

}