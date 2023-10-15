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

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;

import com.cw.sumlist.R;
import com.cw.sumlist.db.DB_category;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by cw on 2023/10/15
 */

public class Category_grid {
	GridView mGridView;
	public List<String> mListStrArr; // list view string array
	public int count;
	AppCompatActivity mAct;
	EditText categoryEditText;
	int editPosition;
	Category_grid_adapter adapter;

	public Category_grid(AppCompatActivity act, View rootView){
		mAct = act;
		// set grid view
		mGridView = rootView.findViewById(R.id.category_grid_view);
		initCategoryItem();
	}

	// init Category item
	private void initCategoryItem() {
		// set category item title
		DB_category db_category = new DB_category(mAct);

		mListStrArr = new ArrayList<String>();

		int categoryCount =db_category.getCategoryCount(true);

		for(int i=0;i<categoryCount;i++)
			mListStrArr.add(db_category.getCategoryTitle(i,true));

		// set adapter
		db_category.open();
		Cursor cursor = db_category.mCursor_category;

		String[] from = new String[] { DB_category.KEY_CATEGORY_TITLE};
		int[] to = new int[] { R.id.category_item_title};

		adapter = new Category_grid_adapter(
				mAct,
				R.layout.category_grid_item,
				cursor,
				from,
				to,
				0
		);

		db_category.close();

		mGridView.setAdapter(adapter);

		// set up click listener
		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View vw, int position, long id){
				// select category item
				mAct.getSupportFragmentManager().popBackStack();
				String title = db_category.getCategoryTitle(position,true);

				Bundle result = new Bundle();
				result.putString("categoryItem", title);
				// The child fragment needs to still set the result on its parent fragment manager.
				mAct.getSupportFragmentManager().setFragmentResult("requestCategoryItem", result);
			}
		});

		// set up long click listener
		mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				// edit category item
				editCategoryItem(position);
				return false;
			}
		} );

	}

	// edit category item
	void editCategoryItem(int position){
		editPosition = position;
		DB_category db_category = new DB_category(mAct);
		String title = db_category.getCategoryTitle(position, true);

		AlertDialog.Builder builder = new AlertDialog.Builder(mAct);
		LayoutInflater mInflater = (LayoutInflater) mAct.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view2 = mInflater.inflate(R.layout.add_new_category_item, null);
		builder.setTitle(R.string.config_set_category_item)
				.setPositiveButton(R.string.btn_OK, listener_update)
				.setNegativeButton(R.string.edit_note_button_delete, listener_delete)
				.setNeutralButton(R.string.btn_Cancel, null);
		builder.setView(view2);
		categoryEditText = view2.findViewById(R.id.edit_category);
		categoryEditText.setText(title);
		AlertDialog mDialog = builder.create();
		mDialog.show();
	}

	// update listener
	DialogInterface.OnClickListener listener_update = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {
			String newCategoryItem = categoryEditText.getText().toString();

			// update category item to DB
			DB_category db_category = new DB_category(mAct);
			long id = db_category.getCategoryId(editPosition,true);
			db_category.updateCategory(id,newCategoryItem,true);

			// refresh list view
			initCategoryItem();

			dialog.dismiss();
		}
	};

	// delete listener
	DialogInterface.OnClickListener listener_delete = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {

			// delete category item
			DB_category db_category = new DB_category(mAct);
			long id = db_category.getCategoryId(editPosition,true);
			db_category.deleteCategory(db_category,id ,true);

			// refresh list view
			initCategoryItem();

			dialog.dismiss();
		}
	};

}