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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
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
public class FolderSum_grid {
    Activity act;
    View rootView;
    CheckBox checkTvSelAll;
    GridView gridView;

    DB_folder dB_folder;
    public List<String> gridStrArr; // grid view string array
    public List<Boolean> checkedTabs; // checked grid items array
    public boolean isCheckAll;
    public int pageCount;
    long folderSum;
    GridSumlistAdapter listAdapter;
    static int style;

    public FolderSum_grid(Activity act, View _rootView, GridView gridView) {
        this.act = act;
        rootView = _rootView;
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

            // todo Move to?
            long pageSum = Utils.getPageSum(act, dB_folder.getPageTableId(i,false));
            String gridItemStr = dB_folder.getPageTitle(i, false) +
                    " : " + pageSum;

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

        mChkNum = (enAll == true) ? pageCount : 0;

        listAdapter.notifyDataSetChanged();
    }

    // show pages in folder for Selection
    public static int mChkNum;

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
            gridItemView = inflater.inflate(R.layout.folder_sum_grid_item, null);

            // check box
            CheckBox chkBox = gridItemView.findViewById(R.id.checkBox);
            chkBox.setChecked(checkedTabs.get(position));

            chkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    System.out.println("FolderSum_grid_list / _getView / position = " + position);

                    checkedTabs.set(position, chkBox.isChecked());
                    if (checkedTabs.get(position) == true)
                        mChkNum++;
                    else
                        mChkNum--;

                    if (!chkBox.isChecked()) {
                        checkTvSelAll.setChecked(false);
                    }

                    int pageTableId = dB_folder.getPageTableId(position, true);

                    // set for contrast
                    if (chkBox.isChecked()) {
                        folderSum += Utils.getPageSum(act, pageTableId);
                    } else {
                        isCheckAll = false;
                        folderSum -= Utils.getPageSum(act, pageTableId);
                    }

                    showFolderSum(rootView);
                }
            });

            // set text view long click listener
            TextView chkTV = gridItemView.findViewById(R.id.itemText);
            chkTV.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //Show page detail
                    String message = getMessageByPagePosition(position);

                    AlertDialog.Builder builder = new AlertDialog.Builder(act);
                    builder.setTitle(R.string.dlg_day_list)
                            .setMessage(message)
                            .setNeutralButton(R.string.btn_OK, new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {   // do nothing
                                }})
                            .show();

                    return false;
                }
            });

            // show style
            style = dB_folder.getPageStyle(position, true);
            chkTV.setBackgroundColor(ColorSet.mBG_ColorArray[style]);
            chkTV.setTextColor(ColorSet.mText_ColorArray[style]);

            // Show current page
            // workaround: set single line to true and add one space in front of the text
            if (dB_folder.getPageTableId(position, true) == Pref.getPref_focusView_page_tableId(act)) {
                chkTV.setTypeface(chkTV.getTypeface(), Typeface.BOLD_ITALIC);
                chkTV.setText(" " + gridStrList.get(position) + "*");
            } else
                chkTV.setText(" " + gridStrList.get(position));

            return gridItemView;
        }

    }

    // show message of a given page position
    String getMessageByPagePosition(int position) {
        System.out.println("FolderSum_grid / _showMessageByPagePosition / position = " + position);

        String message="- - - - - - - - - -\n";
        DB_folder mDb_folder = new DB_folder(act, Pref.getPref_focusView_folder_tableId(act));
        mDb_folder.open();
        int pageTableId = mDb_folder.getPageTableId(position,true);
        DB_page db_page = new DB_page(act,pageTableId);
        int count = db_page.getNotesCount(true);
        String title;
        int price,total = 0;
        db_page.open();
        for(int i=0;i<count;i++) {
            title = db_page.getNoteTitle(i,false);
            price = db_page.getNoteBody(i,false);
            total += price;
            message = message.concat(title).concat(" : ")
                             .concat(String.valueOf(price));

            if(i==count-1) {
                message = message.concat("\n- - - - - - - - - -\n");
                message = message.concat(act.getString(R.string.footer_total)).concat(" : ");
                message = message.concat(String.valueOf(total));
            }
            else
                message = message.concat("\n");
        }
        db_page.close();
        return message;
    }

}