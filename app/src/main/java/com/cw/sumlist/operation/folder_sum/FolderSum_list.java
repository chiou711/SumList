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

package com.cw.sumlist.operation.folder_sum;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

import com.cw.sumlist.R;
import com.cw.sumlist.Utils;
import com.cw.sumlist.db.DB_folder;
import com.cw.sumlist.db.DB_page;
import com.cw.sumlist.util.ColorSet;
import com.cw.sumlist.util.preferences.Pref;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by cw on 2022/6/7
 */

public class FolderSum_list
{
    View mView;
    CheckedTextView mCheckTvSelAll;
    ListView mListView;
    public List<String> mListStrArr; // list view string array
    public List<Boolean> mCheckedTabs; // checked list view items array
    DB_folder mDb_folder;
    public int pageCount;
    Activity mAct;
    public boolean isCheckAll;
    long folderSum;
    View rootView;

    public FolderSum_list(Activity act, View _rootView, ListView listView)
    {
        mAct = act;
        mDb_folder = new DB_folder(mAct, Pref.getPref_focusView_folder_tableId(mAct));

        // checked Text View: select all
        mCheckTvSelAll = (CheckedTextView) _rootView.findViewById(R.id.chkSelectAllPages);
        mCheckTvSelAll.setOnClickListener(new View.OnClickListener()
        {	@Override
            public void onClick(View checkSelAll)
            {
                boolean currentCheck = ((CheckedTextView)checkSelAll).isChecked();
                ((CheckedTextView)checkSelAll).setChecked(!currentCheck);

                if( ((CheckedTextView)checkSelAll).isChecked() )
                {
                    isCheckAll = true;
                    selectAllPages(true);
                }
                else
                {
                    isCheckAll = false;
                    selectAllPages(false);
                }
            }
        });

        // list view: selecting which pages to send
        mListView = listView;
        rootView = _rootView;
        showPagesOfFolder(_rootView);

        folderSum = 0;
    }

    // select all pages
    public void selectAllPages(boolean enAll)
    {
        mChkNum = 0;
        folderSum = 0;

        mDb_folder.open();
        pageCount = mDb_folder.getPagesCount(false);
        for(int i = 0; i< pageCount; i++){
            CheckedTextView chkTextView = (CheckedTextView) mListView.findViewById(R.id.checkTV);
            mCheckedTabs.set(i, enAll);
            mListStrArr.set(i, mDb_folder.getPageTitle(i,false));

            if( enAll) {
                // get sum of each page
                int pageTableId = mDb_folder.getPageTableId(i,false);
                folderSum += Utils.getPageSum(mAct,pageTableId);
            }
        }
        mDb_folder.close();

        // show folder sum
        showFolderSum();

        mChkNum = (enAll == true)? pageCount : 0;

        // set list adapter
        ListAdapter listAdapter = new ListAdapter(mAct, mListStrArr);

        // list view: set adapter
        mListView.setAdapter(listAdapter);
    }

    // show pages in folder for Selection
    public int mChkNum;
    void showPagesOfFolder(View root)
    {
        mChkNum = 0;
        // set list view
        mListView = (ListView) root.findViewById(R.id.listView1);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View vw, int position, long id)
            {
                System.out.println("FolderSum_list / _showListViewInFolder / _onItemClick / position = " + position);
                CheckedTextView chkTV = (CheckedTextView) vw.findViewById(R.id.checkTV);
                chkTV.setChecked(!chkTV.isChecked());
                mCheckedTabs.set(position, chkTV.isChecked());
                if(mCheckedTabs.get(position) == true)
                    mChkNum++;
                else
                    mChkNum--;

                if(!chkTV.isChecked())
                {
                    mCheckTvSelAll.setChecked(false);
                }

                int pageTableId = mDb_folder.getPageTableId(position,true);

                // set for contrast
                int mStyle = mDb_folder.getPageStyle(position, true);
                if( chkTV.isChecked()) {
                    chkTV.setCompoundDrawablesWithIntrinsicBounds(mStyle % 2 == 1 ?
                            R.drawable.btn_check_on_holo_light :
                            R.drawable.btn_check_on_holo_dark, 0, 0, 0);
                    folderSum += Utils.getPageSum(mAct,pageTableId);
                    showFolderSum();
                }
                else {
                    chkTV.setCompoundDrawablesWithIntrinsicBounds(mStyle % 2 == 1 ?
                            R.drawable.btn_check_off_holo_light :
                            R.drawable.btn_check_off_holo_dark, 0, 0, 0);
                    isCheckAll = false;
                    folderSum -= Utils.getPageSum(mAct,pageTableId);
                    showFolderSum();
                }

            }
        });

        // set list string array
        mCheckedTabs = new ArrayList<Boolean>();
        mListStrArr = new ArrayList<String>();

        // DB
        int pageTableId = Pref.getPref_focusView_page_tableId(mAct);
        DB_page.setFocusPage_tableId(pageTableId);

        mDb_folder.open();
        pageCount = mDb_folder.getPagesCount(false);
        for(int i = 0; i< pageCount; i++)
        {
            // list string array: init
            mListStrArr.add(mDb_folder.getPageTitle(i,false));
            // checked mark array: init
            mCheckedTabs.add(false);
        }
        mDb_folder.close();

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
            return mCheckedTabs.get(position);
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

            int pageTableId = mDb_folder.getPageTableId(position,true);

            // get sum of each page
            long pageSum = Utils.getPageSum(activity,pageTableId);

            // Show current page
            // workaround: set single line to true and add one space in front of the text
            if(pageTableId == Integer.valueOf(Pref.getPref_focusView_page_tableId(activity)))
                chkTV.setTypeface(chkTV.getTypeface(), Typeface.BOLD_ITALIC);

            chkTV.setText( " " + mList.get(position) + " : " + pageSum);

            chkTV.setChecked(mCheckedTabs.get(position));

            // set for contrast
            if( chkTV.isChecked())
            // note: have to remove the following in XML file
            // android:drawableLeft="?android:attr/listChoiceIndicatorMultiple"
            // otherwise, setCompoundDrawablesWithIntrinsicBounds will not work on ICS
                chkTV.setCompoundDrawablesWithIntrinsicBounds(style%2 == 1 ?
                R.drawable.btn_check_on_holo_light:
                R.drawable.btn_check_on_holo_dark,0,0,0);
            else
                chkTV.setCompoundDrawablesWithIntrinsicBounds(style%2 == 1 ?
                R.drawable.btn_check_off_holo_light:
                R.drawable.btn_check_off_holo_dark,0,0,0);

            return mView;
        }
    }

    // show folder sum
    void showFolderSum(){
        TextView textFolderSum = (TextView) rootView.findViewById(R.id.textFolderSum);
        String sum = String.valueOf(folderSum);
        textFolderSum.setText(sum);
    }
}
