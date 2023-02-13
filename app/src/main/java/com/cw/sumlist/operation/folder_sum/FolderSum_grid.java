/*
 * Copyright (C) 2022 CW Chiu
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
import android.os.AsyncTask;
import android.view.View;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cw.sumlist.R;
import com.cw.sumlist.Utils;
import com.cw.sumlist.db.DB_folder;
import com.cw.sumlist.db.DB_page;
import com.cw.sumlist.util.Util;
import com.cw.sumlist.util.preferences.Pref;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cw on 2022/9/5
 * Modified on 2023/02/13
 */
public class FolderSum_grid {
    Activity act;
    View rootView;
    CheckBox checkTvSelAll;
    GridView gridView;

    DB_folder dB_folder;
    public List<String> gridStrArr; // grid view string array
    List<Boolean> checkedTabs; // checked grid items array
    public int pageCount;
    public static long folderSum;
    FolderSum_gridAdapter listAdapter;
    List<Long> pageSumArr;
    public static int mChkNum;

    public FolderSum_grid(Activity act, View _rootView, GridView gridView,List<Long> _pageSumArr) {
        this.act = act;
        rootView = _rootView;
        pageSumArr = _pageSumArr;

        // checked Text View: select all
        checkTvSelAll = rootView.findViewById(R.id.check_box_select_all_pages_folder_sum);

        // init
        checkTvSelAll.setChecked(true);

        // checked Text View: select all
        checkTvSelAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View checkSelAll){
                if(((CheckBox)checkSelAll).isChecked())
                    selectAllPages(true);
                else
                    selectAllPages(false);
            }
        });

        // grid view: selecting which pages to send
        this.gridView = gridView;
        folderSum = 0;

        // show progress bar
        Folder_sum_asyncTask task = new Folder_sum_asyncTask(act,rootView);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // select all pages
    public void selectAllPages(boolean enAll) {
        System.out.println("FolderSum_list_grid / _selectAllPages /  enAll = " + enAll);
        mChkNum = 0;
        fillArray(enAll);
        showPagesOfFolder(rootView);
    }

    // fill array
    void fillArray(boolean enAll){
        mChkNum = 0;
        folderSum = 0;

        checkedTabs = new ArrayList<Boolean>();
        gridStrArr = new ArrayList<String>();

        dB_folder = new DB_folder(this.act, Pref.getPref_focusView_folder_tableId(this.act));

        pageCount = dB_folder.getPagesCount(true);
        dB_folder.open();
        for (int i = 0; i < pageCount; i++) {
            checkedTabs.add(i, enAll);

            String gridItemStr = dB_folder.getPageTitle(i, false) +
                    " : " + pageSumArr.get(i);

            gridStrArr.add(i, gridItemStr);

            if (enAll) {
                // get sum of each page
//                int pageTableId = dB_folder.getPageTableId(i, false);
//                folderSum += Utils.getPageSum(act, pageTableId);
                folderSum += pageSumArr.get(i);
            }
        }
        dB_folder.close();
    }

    // show pages in folder for Selection
    void showPagesOfFolder(View root) {
        System.out.println("FolderSum_list_grid / _showPagesOfFolder");
        mChkNum = 0;

        // set grid view
        gridView = (GridView) root.findViewById(R.id.folder_sum_grid_view);

        // DB
        int pageTableId = Pref.getPref_focusView_page_tableId(act);
        DB_page.setFocusPage_tableId(pageTableId);

        // set list adapter
        listAdapter = new FolderSum_gridAdapter(act, gridStrArr,rootView,checkedTabs);

        // grid view: set adapter
        gridView.setAdapter(listAdapter);

        showFolderSum(rootView);
    }

    // show folder sum
    void showFolderSum(View rootView) {
        TextView textFolderSum = (TextView) rootView.findViewById(R.id.textFolderSum);
        String sum = String.valueOf(folderSum);
        textFolderSum.setText(sum);
    }

    /**
     *  Async: show progress bar and do Add all
     */
    class Folder_sum_asyncTask extends AsyncTask<Void, Integer, Void> {

        private ProgressBar progressBar;
        Activity act;
        View rootView;
        private TextView messageText;

        Folder_sum_asyncTask(Activity _act, View _rootView) {
            act = _act;
            rootView = _rootView;

            Util.lockOrientation(act);

            messageText = (TextView) rootView.findViewById(R.id.folder_sum_message);
            messageText.setText("Sum Pages ...");

            progressBar = (ProgressBar) rootView.findViewById(R.id.folder_sum_progress);
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
            fillArray(true);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // lock orientation
            Util.unlockOrientation(act);

            rootView.findViewById(R.id.show_folder_sum_progress).setVisibility(View.GONE);

            // init: set All checked
            checkTvSelAll.callOnClick();
        } // onPostExecute
    }

}