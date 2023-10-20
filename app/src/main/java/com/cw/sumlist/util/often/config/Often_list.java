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

package com.cw.sumlist.util.often.config;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;

import com.cw.sumlist.R;
import com.cw.sumlist.db.DB_often;
import com.cw.sumlist.main.MainAct;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by cw on 2023/09/10
 */

public class Often_list {
    DragSortListView mListView;
    public List<String> mListStrArr; // list view string array
    public int count;
    AppCompatActivity mAct;
    EditText titleEditText;
    EditText categoryEditText;
    private DragSortController controller;
    int editPosition;
    Often_list_adapter adapter;

    public Often_list(AppCompatActivity act, View rootView){
        mAct = act;
        // set list view
        mListView = rootView.findViewById(R.id.listView1);
        initOftenItem();
    }

    // init often item
    private void initOftenItem() {
        // set often item title
        DB_often db_often = new DB_often(mAct);

        mListStrArr = new ArrayList<String>();

        int oftenCount =db_often.getOftenCount(true);

        for(int i=0;i<oftenCount;i++)
            mListStrArr.add(db_often.getOftenTitle(i,true));

        // set adapter
        db_often.open();
        Cursor cursor = db_often.mCursor_often;

        String[] from = new String[] { DB_often.KEY_OFTEN_TITLE};
        int[] to = new int[] { R.id.often_item_title};

        adapter = new Often_list_adapter(
                mAct,
                R.layout.often_list_row,
                cursor,
                from,
                to,
                0
        );

        db_often.close();

        mListView.setAdapter(adapter);

        // set up click listener
//        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View vw, int position, long id){
//                // edit often item
//                editOftenItem(position);
//            }
//        });

        // set up long click listener
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // edit often item
                editOftenItem(position);
                return false;
            }
        } );

        controller = buildController(mListView);
        mListView.setFloatViewManager(controller);
        mListView.setOnTouchListener(controller);

        // init dragger
        mListView.setDragEnabled(true);

        mListView.setDragListener(onDrag);
        mListView.setDropListener(onDrop);
    }

    // edit often item
    void editOftenItem(int position){
        editPosition = position;
        DB_often db_often = new DB_often(mAct);
        String title = db_often.getOftenTitle(position, true);
        String category = db_often.getOftenCategory(position, true);

        AlertDialog.Builder builder = new AlertDialog.Builder(mAct);
        LayoutInflater mInflater = (LayoutInflater) mAct.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view2 = mInflater.inflate(R.layout.add_new_often_item, null);
        builder.setTitle(R.string.config_set_often_item)
                .setPositiveButton(R.string.btn_OK, listener_update)
                .setNegativeButton(R.string.edit_note_button_delete, listener_delete)
                .setNeutralButton(R.string.btn_Cancel, null);
        builder.setView(view2);
        titleEditText = view2.findViewById(R.id.edit_title);
        categoryEditText = view2.findViewById(R.id.edit_category);
        titleEditText.setText(title);
        categoryEditText.setText(category);
        AlertDialog mDialog = builder.create();
        mDialog.show();
    }

    private static DragSortController buildController(DragSortListView dslv) {
        // defaults are
        DragSortController controller = new DragSortController(dslv);
        controller.setSortEnabled(true);
        controller.setDragInitMode(DragSortController.ON_DOWN); // click
        controller.setDragHandleId(R.id.often_item_drag);// handler
        controller.setBackgroundColor(Color.argb(128,128,64,0));// background color when dragging

        return controller;
    }

    // list view listener: on drag
    private DragSortListView.DragListener onDrag = new DragSortListView.DragListener(){
        @Override
        public void drag(int startPosition, int endPosition) {
        }
    };

    // list view listener: on drop
    private DragSortListView.DropListener onDrop = new DragSortListView.DropListener(){
        @Override
        public void drop(int startPosition, int endPosition) {
            //reorder data base storage
            int loop = Math.abs(startPosition-endPosition);
            for(int i=0;i< loop;i++){
                swapOftenItemRows(startPosition,endPosition);
                if((startPosition-endPosition) >0)
                    endPosition++;
                else
                    endPosition--;
            }

            adapter.notifyDataSetChanged();
        }
    };

    // swap rows
    private static Long mOftenId1 = (long) 1;
    private static Long mOftenId2 = (long) 1;
    private static String mOftenTitle1;
    private static String mOftenTitle2;
    private static String mOftenCategory1;
    private static String mOftenCategory2;
    static void swapOftenItemRows(int startPosition, int endPosition)
    {
        Activity act = MainAct.mAct;
        DB_often db_often = new DB_often(act);

        db_often.open();
        mOftenId1 = db_often.getOftenId(startPosition,false);
        mOftenTitle1 = db_often.getOftenTitle(startPosition,false);
        mOftenCategory1 = db_often.getOftenCategory(startPosition,false);

        mOftenId2 = db_often.getOftenId(endPosition,false);
        mOftenTitle2 = db_often.getOftenTitle(endPosition,false);
        mOftenCategory2 = db_often.getOftenCategory(endPosition,false);

        db_often.updateOften(mOftenId1,
                mOftenTitle2,
                mOftenCategory2,
                false);

        db_often.updateOften(mOftenId2,
                mOftenTitle1,
                mOftenCategory1,
                false);
        db_often.close();
    }

    // update listener
    DialogInterface.OnClickListener listener_update = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {
            String newOftenItem = titleEditText.getText().toString();
            String newOften_categoryItem = categoryEditText.getText().toString();

            // update often item to DB
            DB_often db_often = new DB_often(mAct);
            long id = db_often.getOftenId(editPosition,true);
            db_often.updateOften(id,newOftenItem,newOften_categoryItem,true);

            // refresh list view
            initOftenItem();

            dialog.dismiss();
        }
    };

    // delete listener
    DialogInterface.OnClickListener listener_delete = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

            // delete often item
            DB_often db_often = new DB_often(mAct);
            long id = db_often.getOftenId(editPosition,true);
            db_often.deleteOften(db_often,id ,true);

            // refresh list view
            initOftenItem();

            dialog.dismiss();
        }
    };

}