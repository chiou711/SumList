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

package com.cw.sumlist.note_add;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.cw.sumlist.R;
import com.cw.sumlist.db.DB_folder;
import com.cw.sumlist.util.ColorSet;
import com.cw.sumlist.util.preferences.Pref;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by cw on 2023/06/29
 */

public class List_selectOftenItem
{
    View mView;
    ListView mListView;
    public List<String> mListStrArr; // list view string array
    public List<Boolean> mCheckedTabs; // checked list view items array
    DB_folder mDb_folder;
    public int count;
    AppCompatActivity mAct;
    public boolean isCheckAll;

    public List_selectOftenItem(AppCompatActivity act, View rootView, ListView listView)
    {
        mAct = act;
        mDb_folder = new DB_folder(mAct, Pref.getPref_focusView_folder_tableId(mAct));

        // list view: selecting which pages to send
        mListView = listView;
        showOftenItemsList(rootView);

        isCheckAll = false;
    }

    // show list for Select
    public int mChkNum;
    String[] oftenItems = {"麵包","全聯","大潤發","午餐","晚餐"};
    void showOftenItemsList(View root)
    {
        mChkNum = 0;
        // set list view
        mListView = (ListView) root.findViewById(R.id.listView1);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View vw, int position, long id)
            {
                System.out.println("List_selectOftenItem / _showOftenItemsList / _onItemClick / position = " + position);
                mAct.getSupportFragmentManager().popBackStack();

                Bundle result = new Bundle();
                result.putString("oftenItem", mListStrArr.get(position));
                // The child fragment needs to still set the result on its parent fragment manager.
                mAct.getSupportFragmentManager().setFragmentResult("requestOftenItem", result);

            }
        });

        // set list string array
        mCheckedTabs = new ArrayList<Boolean>();
        mListStrArr = new ArrayList<String>();

        for(int i=0;i<oftenItems.length;i++)
            mListStrArr.add(oftenItems[i]);

        // set list adapter
        ListAdapter listAdapter = new ListAdapter(mAct, mListStrArr);

        // list view: set adapter
        mListView.setAdapter(listAdapter);
    }

    // list adapter
    public class ListAdapter extends BaseAdapter
    {
        private Activity activity;
        private List<String> mList;
        private LayoutInflater inflater = null;

        public ListAdapter(Activity a, List<String> list)
        {
            activity = a;
            mList = list;
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount()
        {
            return mList.size();
        }

        public Object getItem(int position)
        {
            return mList.get(position);
        }

        public long getItemId(int position)
        {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            mView = inflater.inflate(R.layout.select_page_list_row, null);

            // set checked text view
            CheckedTextView chkTV = (CheckedTextView) mView.findViewById(R.id.checkTV);
            // show style
            int style = mDb_folder.getPageStyle(position, true);
            chkTV.setBackgroundColor(ColorSet.mBG_ColorArray[style]);
            chkTV.setTextColor(ColorSet.mText_ColorArray[style]);

            chkTV.setText( " " + mList.get(position));

            return mView;
        }
    }
}
