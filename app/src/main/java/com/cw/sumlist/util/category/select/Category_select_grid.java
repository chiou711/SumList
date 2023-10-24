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

package com.cw.sumlist.util.category.select;

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
import com.cw.sumlist.db.DB_category;
import com.mobeta.android.dslv.DragSortListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

/**
 * Created by cw on 2023/10/15
 */
public class Category_select_grid extends Fragment{
	TextView title;
	DragSortListView mListView;
	Category_grid setCategoryIem_grid;
	public static View rootView;
	AppCompatActivity act;
	Button btn_add_category_item;
	AlertDialog mDialog;
	EditText categoryEditText;

	public Category_select_grid(){}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.category_select_grid, container, false);

		act = (AppCompatActivity) getActivity();

		// title
		title = (TextView) rootView.findViewById(R.id.select_list_title);
		title.setText(R.string.category_item_title);

		// list view: selecting which pages to send
		mListView = rootView.findViewById(R.id.listView1);

		//show grid view for selection
		showGridView();

		// add Category item button
		btn_add_category_item = rootView.findViewById(R.id.btn_add_category_item);
		btn_add_category_item.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				// add category item dialog
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				LayoutInflater mInflater= (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				View view = mInflater.inflate(R.layout.add_new_category_item, null);
				builder.setTitle(R.string.config_set_category_item)
						.setPositiveButton(R.string.btn_OK, listener_ok)
						.setNegativeButton(R.string.btn_Cancel, null);
				builder.setView(view);

				categoryEditText =  view.findViewById(R.id.edit_category);

				mDialog = builder.create();
				mDialog.show();
			}
		});

		return rootView;
	}

	// confirm Add new Category item
	DialogInterface.OnClickListener listener_ok = new DialogInterface.OnClickListener()	{
		@Override
		public void onClick(DialogInterface dialog, int which) {
			String newCategoryItem = categoryEditText.getText().toString();

			// add Category item to DB
			DB_category db_category = new DB_category(act);
			db_category.insertCategory(db_category,newCategoryItem ,true);

			// refresh listview
			showGridView();

			dialog.dismiss();
		}
	};

	@Override
	public void onPause() {
		super.onPause();
	}

	// show grid view
	void showGridView(){
		setCategoryIem_grid = new Category_grid(act, rootView);
	}

	public void hideGridView(){
		getActivity().getSupportFragmentManager().popBackStack();
	}
}