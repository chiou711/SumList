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
import android.view.View;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;

import com.cw.sumlist.R;
import com.cw.sumlist.Utils;
import com.cw.sumlist.db.DB_folder;
import com.cw.sumlist.db.DB_page;
import com.cw.sumlist.util.preferences.Pref;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cw on 2022/9/5
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
        dB_folder = new DB_folder(this.act, Pref.getPref_focusView_folder_tableId(this.act));

        // checked Text View: select all
        checkTvSelAll = rootView.findViewById(R.id.check_box_select_all_pages_folder_sum);

        // grid view: selecting which pages to send
        this.gridView = gridView;
        showPagesOfFolder(_rootView);

        folderSum = 0;
    }

    // select all pages
    public void selectAllPages(boolean enAll) {
        System.out.println("FolderSum_list_grid / _selectAllPages /  enAll = " + enAll);

        mChkNum = 0;
        folderSum = 0;

        pageCount = dB_folder.getPagesCount(true);
        dB_folder.open();
        for (int i = 0; i < pageCount; i++) {
            checkedTabs.add(i, enAll);

            String gridItemStr = dB_folder.getPageTitle(i, false) +
                    " : " + pageSumArr.get(i);

            gridStrArr.add(i, gridItemStr);

            if (enAll) {
                // get sum of each page
                int pageTableId = dB_folder.getPageTableId(i, false);
                folderSum += Utils.getPageSum(act, pageTableId);
            }
        }
        dB_folder.close();

        // show folder sum
        showFolderSum(rootView);

        mChkNum = (enAll) ? pageCount : 0;

        listAdapter.notifyDataSetChanged();
    }

    // show pages in folder for Selection
    void showPagesOfFolder(View root) {
        System.out.println("FolderSum_list_grid / _showPagesOfFolder");
        mChkNum = 0;

        // set grid view
        gridView = (GridView) root.findViewById(R.id.folder_sum_grid_view);

        // set grid string array
        checkedTabs = new ArrayList<Boolean>();
        gridStrArr = new ArrayList<String>();

        // DB
        int pageTableId = Pref.getPref_focusView_page_tableId(act);
        DB_page.setFocusPage_tableId(pageTableId);

        // set list adapter
        listAdapter = new FolderSum_gridAdapter(act, gridStrArr,rootView,checkedTabs);

        // grid view: set adapter
        gridView.setAdapter(listAdapter);
    }

    // show folder sum
    void showFolderSum(View rootView) {
        TextView textFolderSum = (TextView) rootView.findViewById(R.id.textFolderSum);
        String sum = String.valueOf(folderSum);
        textFolderSum.setText(sum);
    }

}