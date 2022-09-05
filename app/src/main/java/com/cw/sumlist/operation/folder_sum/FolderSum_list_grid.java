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
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.GridView;
import android.widget.TextView;

import com.cw.sumlist.R;
import com.cw.sumlist.Utils;
import com.cw.sumlist.db.DB_folder;
import com.cw.sumlist.db.DB_page;
import com.cw.sumlist.util.ColorSet;
import com.cw.sumlist.util.preferences.Pref;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by cw on 2022/9/5
 */
public class FolderSum_list_grid {
    Activity act;
    View rootView;
    CheckedTextView checkTvSelAll;
    GridView gridView;

    DB_folder dB_folder;
    public List<String> gridStrArr; // grid view string array
    public List<Boolean> checkedTabs; // checked grid items array
    public boolean isCheckAll;
    public int pageCount;
    long folderSum;
    GridSumlistAdapter listAdapter;
    static int style;

    public FolderSum_list_grid(Activity act, View _rootView, GridView gridView) {
        this.act = act;
        dB_folder = new DB_folder(this.act, Pref.getPref_focusView_folder_tableId(this.act));

        // checked Text View: select all
        checkTvSelAll = (CheckedTextView) _rootView.findViewById(R.id.chkSelectAllPages);
        checkTvSelAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View checkSelAll) {
                boolean currentCheck = ((CheckedTextView) checkSelAll).isChecked();
                ((CheckedTextView) checkSelAll).setChecked(!currentCheck);

                if (((CheckedTextView) checkSelAll).isChecked()) {
                    isCheckAll = true;
                    selectAllPages(true);
                } else {
                    isCheckAll = false;
                    selectAllPages(false);
                }
            }
        });

        // grid view: selecting which pages to send
        rootView = _rootView;
        this.gridView = gridView;
        showPagesOfFolder(_rootView);

        folderSum = 0;
    }

    // select all pages
    public void selectAllPages(boolean enAll) {
        System.out.println("FolderSum_list_grid / _selectAllPages /  enAll = " + enAll);

        mChkNum = 0;
        folderSum = 0;

        dB_folder.open();
        pageCount = dB_folder.getPagesCount(false);
        for (int i = 0; i < pageCount; i++) {
            checkedTabs.set(i, enAll);

            long pageSum = Utils.getPageSum(act, dB_folder.getPageTableId(i,false));
            String gridItemStr = dB_folder.getPageTitle(i, false) +
                    " : " + pageSum;

            gridStrArr.set(i, gridItemStr);

            if (enAll) {
                // get sum of each page
                int pageTableId = dB_folder.getPageTableId(i, false);
                folderSum += Utils.getPageSum(act, pageTableId);
            }
        }
        dB_folder.close();

        // show folder sum
        showFolderSum(rootView);

        mChkNum = (enAll == true) ? pageCount : 0;

        listAdapter.notifyDataSetChanged();
    }

    // show pages in folder for Selection
    public static int mChkNum;

    void showPagesOfFolder(View root) {
        System.out.println("FolderSum_list_grid / _showPagesOfFolder");
        mChkNum = 0;

        // set grid view
        gridView = (GridView) root.findViewById(R.id.grid_view_sumlist);

        // set grid string array
        checkedTabs = new ArrayList<Boolean>();
        gridStrArr = new ArrayList<String>();

        // DB
        int pageTableId = Pref.getPref_focusView_page_tableId(act);
        DB_page.setFocusPage_tableId(pageTableId);

        dB_folder.open();
        pageCount = dB_folder.getPagesCount(false);
        for (int i = 0; i < pageCount; i++) {
            // list string array: init
            gridStrArr.add(dB_folder.getPageTitle(i, false));

            // checked mark array: init
            checkedTabs.add(true); // set ture for the first time grid view
        }
        dB_folder.close();

        // set list adapter
        listAdapter = new GridSumlistAdapter(act, gridStrArr,rootView);

        // grid view: set adapter
        gridView.setAdapter(listAdapter);
    }

    // show folder sum
    void showFolderSum(View rootView) {
        TextView textFolderSum = (TextView) rootView.findViewById(R.id.textFolderSum);
        String sum = String.valueOf(folderSum);
        textFolderSum.setText(sum);
    }

    public class GridSumlistAdapter extends ArrayAdapter<String> {
        Activity act;
        View rootView;
        private LayoutInflater inflater;
        DB_folder dB_folder;
        View gridItemView;
        private List<String> gridStrList;

        public GridSumlistAdapter(@NonNull Activity context, List<String> arrayList,View root_view) {
            super(context, 0, arrayList);

            act = context;
            inflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            dB_folder = new DB_folder(act, Pref.getPref_focusView_folder_tableId(act));
            gridStrList = arrayList;
            rootView = root_view;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            gridItemView = inflater.inflate(R.layout.grid_item, null);
            // set checked text view
            CheckedTextView chkTV = (CheckedTextView) gridItemView.findViewById(R.id.checkTV);
            chkTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    System.out.println("FolderSum_grid_list / _getView / position = " + position);

                    chkTV.setChecked(!chkTV.isChecked());
                    checkedTabs.set(position, chkTV.isChecked());
                    if (checkedTabs.get(position) == true)
                        mChkNum++;
                    else
                        mChkNum--;

                    if (!chkTV.isChecked()) {
                        checkTvSelAll.setChecked(false);
                    }

                    int pageTableId = dB_folder.getPageTableId(position, true);

                    // set for contrast
                    if (chkTV.isChecked()) {
                        folderSum += Utils.getPageSum(act, pageTableId);
                    } else {
                        isCheckAll = false;
                        folderSum -= Utils.getPageSum(act, pageTableId);
                    }

                    showFolderSum(rootView);

                    showCheckedSymbol((CheckedTextView) v,style);
                }
            });

            // show style
            style = dB_folder.getPageStyle(position, true);
            chkTV.setBackgroundColor(ColorSet.mBG_ColorArray[style]);
            chkTV.setTextColor(ColorSet.mText_ColorArray[style]);

            // Show current page
            // workaround: set single line to true and add one space in front of the text
            if (dB_folder.getPageTableId(position, true) == Integer.valueOf(DB_page.getFocusPage_tableId())) {
                chkTV.setTypeface(chkTV.getTypeface(), Typeface.BOLD_ITALIC);
                chkTV.setText(" " + gridStrList.get(position) + "*");
            } else
                chkTV.setText(" " + gridStrList.get(position));

            chkTV.setChecked(checkedTabs.get(position));

            // set for contrast
            if (chkTV.isChecked())
                // note: have to remove the following in XML file
                // android:drawableLeft="?android:attr/listChoiceIndicatorMultiple"
                // otherwise, setCompoundDrawablesWithIntrinsicBounds will not work on ICS
                chkTV.setCompoundDrawablesWithIntrinsicBounds(style % 2 == 1 ?
                        R.drawable.btn_check_on_holo_light :
                        R.drawable.btn_check_on_holo_dark, 0, 0, 0);
            else
                chkTV.setCompoundDrawablesWithIntrinsicBounds(style % 2 == 1 ?
                        R.drawable.btn_check_off_holo_light :
                        R.drawable.btn_check_off_holo_dark, 0, 0, 0);

            return gridItemView;
        }

    }

    // show checked symbol
    static void showCheckedSymbol(CheckedTextView chkTV, int style){
        // set for contrast
        if (chkTV.isChecked())
            // note: have to remove the following in XML file
            // android:drawableLeft="?android:attr/listChoiceIndicatorMultiple"
            // otherwise, setCompoundDrawablesWithIntrinsicBounds will not work on ICS
            chkTV.setCompoundDrawablesWithIntrinsicBounds(style % 2 == 1 ?
                    R.drawable.btn_check_on_holo_light :
                    R.drawable.btn_check_on_holo_dark, 0, 0, 0);
        else
            chkTV.setCompoundDrawablesWithIntrinsicBounds(style % 2 == 1 ?
                    R.drawable.btn_check_off_holo_light :
                    R.drawable.btn_check_off_holo_dark, 0, 0, 0);
    }

}