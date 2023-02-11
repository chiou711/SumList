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

package com.cw.sumlist.operation.sum_folders;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cw.sumlist.R;
import com.cw.sumlist.Utils;
import com.cw.sumlist.db.DB_drawer;
import com.cw.sumlist.folder.FolderUi;
import com.cw.sumlist.util.ColorSet;
import com.cw.sumlist.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cw on 2023/02/11
 */

public class List_sumFolders {
    View mView;
    CheckedTextView mCheckTvSelAll;
    ListView mListView;
    List<String> mListStrArr; // list view string array
    List<Long> mListLongArr;
    public List<Boolean> mCheckedArr; // checked list view items array
    DB_drawer dB_drawer;
    public int count;
    Activity mAct;

    public List_sumFolders(Activity act, View rootView, View view){
        mAct = act;

        dB_drawer = new DB_drawer(mAct);

        // checked Text View: select all
        mCheckTvSelAll = (CheckedTextView) rootView.findViewById(R.id.chkSelectAllPages);
        mCheckTvSelAll.setOnClickListener(new View.OnClickListener()
        {	@Override
            public void onClick(View checkSelAll)
            {
                boolean currentCheck = ((CheckedTextView)checkSelAll).isChecked();
                ((CheckedTextView)checkSelAll).setChecked(!currentCheck);

                if(((CheckedTextView)checkSelAll).isChecked())
                    selectAllPages(true);
                else
                    selectAllPages(false);
            }
        });

        // list view: selecting which pages to send
        mListView = (ListView)view;

        showFolderList(rootView);
    }

    // select all pages
    public void selectAllPages(boolean enAll){
        mChkNum = 0;

        dB_drawer.open();
        count = dB_drawer.getFoldersCount(false);
        for(int i = 0; i< count; i++){
            mCheckedArr.set(i, enAll);
            mListStrArr.set(i, dB_drawer.getFolderTitle(i,false)
                                        .concat(" : ")
                                        .concat(String.valueOf(mListLongArr.get(i)))
                            );
        }
        dB_drawer.close();

        mChkNum = (enAll == true)? count : 0;

        // set list adapter
        ListAdapter listAdapter = new ListAdapter(mAct, mListStrArr);

        // list view: set adapter
        mListView.setAdapter(listAdapter);
    }

    // show list for Select
    public int mChkNum;
    private void showFolderList(View root){
        mChkNum = 0;
        // set list view
        mListView = (ListView) root.findViewById(R.id.listView1);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View vw, int position, long id)
            {
                System.out.println("List_selectFolder / _showFolderList / _onItemClick / position = " + position);
                CheckedTextView chkTV = (CheckedTextView) vw.findViewById(R.id.checkTV);
                chkTV.setChecked(!chkTV.isChecked());
                mCheckedArr.set(position, chkTV.isChecked());
                if(mCheckedArr.get(position))
                    mChkNum++;
                else
                    mChkNum--;

                if(!chkTV.isChecked()){
                    mCheckTvSelAll.setChecked(false);
                }

                // set for contrast
                int mStyle = 1;
                if( chkTV.isChecked())
                    chkTV.setCompoundDrawablesWithIntrinsicBounds(mStyle%2 == 1 ?
                    R.drawable.btn_check_on_holo_light:
                    R.drawable.btn_check_on_holo_dark,0,0,0);
                else
                    chkTV.setCompoundDrawablesWithIntrinsicBounds(mStyle%2 == 1 ?
                    R.drawable.btn_check_off_holo_light:
                    R.drawable.btn_check_off_holo_dark,0,0,0);

            }
        });

        // show progress bar
        Sum_folders_asyncTask task = new Sum_folders_asyncTask(mAct,root);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void fillArray(){
        // set list string array
        mListStrArr = new ArrayList<>();
        mListLongArr = new ArrayList<>();
        mCheckedArr = new ArrayList<>();

        dB_drawer.open();
        count = dB_drawer.getFoldersCount(false);

        for(int i = 0; i< count; i++){
            int folder_table_id = dB_drawer.getFolderTableId(i,false);

            //todo Wait too ling?
            long folderSum = Utils.getFolderSum(mAct,folder_table_id);

            // init mListLongArr
            mListLongArr.add(i,folderSum);

            // list string array: init
            mListStrArr.add(dB_drawer.getFolderTitle(i,false)
                    .concat(" : ")
//                    .concat(String.valueOf(folderSum)));
                    .concat(String.valueOf(mListLongArr.get(i))));
            // checked mark array: init
            mCheckedArr.add(false);
        }
        dB_drawer.close();
    }

    // list adapter
    public class ListAdapter extends BaseAdapter {
        private Activity activity;
        private List<String> mList;
        private LayoutInflater inflater = null;

        ListAdapter(Activity a, List<String> list){
            activity = a;
            mList = list;
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount(){
            return mList.size();
        }

        public Object getItem(int position){
            return mCheckedArr.get(position);
        }

        public long getItemId(int position){
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent){
            mView = inflater.inflate(R.layout.select_page_list_row, null);

            // set checked text view
            CheckedTextView chkTV = (CheckedTextView) mView.findViewById(R.id.checkTV);

            // show style
            int style = 1;
            chkTV.setBackgroundColor(ColorSet.mBG_ColorArray[style]);
            chkTV.setTextColor(ColorSet.mText_ColorArray[style]);

            // Show current page
            //??? how to set left padding of text view of a CheckedTextview
            // workaround: set single line to true and add one space in front of the text
            if(position == FolderUi.getFocus_folderPos()){
                chkTV.setTypeface(chkTV.getTypeface(), Typeface.BOLD_ITALIC);
                chkTV.setText( " * " + mList.get(position) );
            }else
                chkTV.setText( " " + mList.get(position).toString());

            chkTV.setChecked(mCheckedArr.get(position));

            // set for contrast
            if( chkTV.isChecked()) {
                // note: have to remove the following in XML file
                // android:drawableLeft="?android:attr/listChoiceIndicatorMultiple"
                // otherwise, setCompoundDrawablesWithIntrinsicBounds will not work on ICS
                chkTV.setCompoundDrawablesWithIntrinsicBounds(style % 2 == 1 ?
                        R.drawable.btn_check_on_holo_light :
                        R.drawable.btn_check_on_holo_dark, 0, 0, 0);
            }
            else {
                chkTV.setCompoundDrawablesWithIntrinsicBounds(style % 2 == 1 ?
                        R.drawable.btn_check_off_holo_light :
                        R.drawable.btn_check_off_holo_dark, 0, 0, 0);
            }

            return mView;
        }
    }

    // get sum of folders
    long getSumFolders(){
        int size = mListLongArr.size();
        long sum = 0;
        for(int i=0;i<size;i++){
            sum += mListLongArr.get(i);
        }
        return sum;
    }

    long sumFolders;
    /**
     *  Async: show progress bar and do Add all
     */
    class Sum_folders_asyncTask extends AsyncTask<Void, Integer, Void> {

        private ProgressBar progressBar;
        Activity act;
        View rootView;
        private TextView messageText;

        Sum_folders_asyncTask(Activity _act, View _rootView) {
            act = _act;
            rootView = _rootView;

            Util.lockOrientation(act);

            messageText = (TextView) rootView.findViewById(R.id.sum_folders_message);
            messageText.setText("Sum folders ...");

            progressBar = (ProgressBar) rootView.findViewById(R.id.sum_folders_progress);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (this.progressBar != null) {
                progressBar.setProgress(values[0]);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            // main function for adding audio links
            fillArray();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // lock orientation
            Util.unlockOrientation(act);
            sumFolders = getSumFolders();
//            progressBar.setVisibility(View.GONE);
//            messageText.setVisibility(View.GONE);

            // set list adapter
            ListAdapter listAdapter = new ListAdapter(mAct, mListStrArr);

            // list view: set adapter
            mListView.setAdapter(listAdapter);

            rootView.findViewById(R.id.show_progress).setVisibility(View.GONE);
            ((Button) rootView.findViewById(R.id.btnSelPageOK)).setText(String.valueOf(sumFolders));

            // init: set All checked
            mCheckTvSelAll.callOnClick();
        } // onPostExecute
    }
}