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

package com.cw.sumlist.note_add;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cw.sumlist.R;
import com.cw.sumlist.db.DB_often;
import com.cw.sumlist.main.MainAct;
import com.mobeta.android.dslv.SimpleDragSortCursorAdapter;

/**
 * Created by cw on 2023/08/29
 */

public class OftenItem_adapter extends SimpleDragSortCursorAdapter
{
    OftenItem_adapter(Context context, int layout, Cursor c,
                      String[] from, int[] to, int flags)
    {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public int getCount() {
        DB_often db_often = new DB_often(MainAct.mAct);
        return db_often.getOftenCount(true);
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
            convertView = MainAct.mAct.getLayoutInflater().inflate(R.layout.often_item_row, parent, false);

            // set up ViewHolder for this ListView item
            viewHolder = new ViewHolder();
            viewHolder.oftenItemTitle = (TextView) convertView.findViewById(R.id.often_item_title);
            viewHolder.dragIcon = (ImageView) convertView.findViewById(R.id.often_item_drag);
            convertView.setTag(viewHolder); // store as View's tag
        }
        else // get the ViewHolder from the convertView's tag
            viewHolder = (ViewHolder) convertView.getTag();

        DB_often db_often= new DB_often(MainAct.mAct);
        viewHolder.oftenItemTitle.setText(db_often.getOftenTitle(position,true));

        viewHolder.dragIcon.setVisibility(View.VISIBLE);

        return convertView;
    }


    private static class ViewHolder
    {
        TextView oftenItemTitle; // refers to ListView item's ImageView
        ImageView dragIcon;
    }
}
