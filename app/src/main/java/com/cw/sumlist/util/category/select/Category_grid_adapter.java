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

package com.cw.sumlist.util.category.select;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cw.sumlist.R;
import com.cw.sumlist.db.DB_category;
import com.cw.sumlist.main.MainAct;

import androidx.cursoradapter.widget.SimpleCursorAdapter;

/**
 * Created by cw on 2023/10/15
 */

public class Category_grid_adapter extends SimpleCursorAdapter
{
	int layout;
	public Category_grid_adapter(Context context, int _layout, Cursor c,
	                             String[] from, int[] to, int flags)   {
		super(context, _layout, c, from, to, flags);
		layout = _layout;
	}

	@Override
	public int getCount() {
		DB_category db_category = new DB_category(MainAct.mAct);
		return db_category.getCategoryCount(true);
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder viewHolder; // holds references to current item's GUI

		// if convertView is null, inflate GUI and create ViewHolder;
		// otherwise, get existing ViewHolder
		if (convertView == null)
		{
			convertView = MainAct.mAct.getLayoutInflater().inflate(layout, parent, false);

			// set up ViewHolder for this ListView item
			viewHolder = new ViewHolder();
			viewHolder.categoryItemTitle = (TextView) convertView.findViewById(R.id.category_item_title);
			convertView.setTag(viewHolder); // store as View's tag
		}
		else // get the ViewHolder from the convertView's tag
			viewHolder = (ViewHolder) convertView.getTag();

		DB_category db_category= new DB_category(MainAct.mAct);
		viewHolder.categoryItemTitle.setText(db_category.getCategoryTitle(position,true));

		return convertView;
	}

	private static class ViewHolder
	{
		TextView categoryItemTitle; // refers to ListView item's ImageView
	}
}
