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

package com.cw.sumlist.util.category.config;

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
import com.cw.sumlist.db.DB_category;
import com.cw.sumlist.main.MainAct;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by cw on 2023/10/18
 */

public class Category_list {
    DragSortListView mListView;
    public List<String> mListStrArr; // list view string array
    public int count;
    AppCompatActivity mAct;
    EditText categoryEditText;
    private DragSortController controller;
    int editPosition;
    Category_list_adapter adapter;

    public Category_list(AppCompatActivity act, View rootView){
        mAct = act;
        // set list view
        mListView = rootView.findViewById(R.id.listView1);
        initCategoryItem();
    }

    // init category item
    private void initCategoryItem() {
        // set category item title
        DB_category db_category = new DB_category(mAct);

        mListStrArr = new ArrayList<String>();

        int categoryCount =db_category.getCategoryCount(true);

        for(int i=0;i<categoryCount;i++)
            mListStrArr.add(db_category.getCategoryTitle(i,true));

        // set adapter
        db_category.open();
        Cursor cursor = db_category.mCursor_category;

        String[] from = new String[] { DB_category.KEY_CATEGORY_TITLE};
        int[] to = new int[] { R.id.category_item_title};

        adapter = new Category_list_adapter(
                mAct,
                R.layout.category_list_row,
                cursor,
                from,
                to,
                0
        );

        db_category.close();

        mListView.setAdapter(adapter);

        // set up click listener
//        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View vw, int position, long id){
//                // edit category item
//                editCategoryItem(position);
//            }
//        });

        // set up long click listener
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // edit category item
                editCategoryItem(position);
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

    // edit category item
    void editCategoryItem(int position){
        editPosition = position;
        DB_category db_category = new DB_category(mAct);
        String category = db_category.getCategoryTitle(position, true);

        AlertDialog.Builder builder = new AlertDialog.Builder(mAct);
        LayoutInflater mInflater = (LayoutInflater) mAct.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view2 = mInflater.inflate(R.layout.add_new_category_item, null);
        builder.setTitle(R.string.config_set_category_item)
                .setPositiveButton(R.string.btn_OK, listener_update)
                .setNegativeButton(R.string.edit_note_button_delete, listener_delete)
                .setNeutralButton(R.string.btn_Cancel, null);
        builder.setView(view2);
        categoryEditText = view2.findViewById(R.id.edit_category);
        categoryEditText.setText(category);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static DragSortController buildController(DragSortListView dslv) {
        // defaults are
        DragSortController controller = new DragSortController(dslv);
        controller.setSortEnabled(true);
        controller.setDragInitMode(DragSortController.ON_DOWN); // click
        controller.setDragHandleId(R.id.category_item_drag);// handler
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
                swapCategoryItemRows(startPosition,endPosition);
                if((startPosition-endPosition) >0)
                    endPosition++;
                else
                    endPosition--;
            }

            adapter.notifyDataSetChanged();
        }
    };

    // swap rows
    private static Long mCategoryId1 = (long) 1;
    private static Long mCategoryId2 = (long) 1;
    private static String mCategoryTitle1;
    private static String mCategoryTitle2;
    static void swapCategoryItemRows(int startPosition, int endPosition)
    {
        Activity act = MainAct.mAct;
        DB_category db_category = new DB_category(act);

        db_category.open();
        mCategoryId1 = db_category.getCategoryId(startPosition,false);
        mCategoryTitle1 = db_category.getCategoryTitle(startPosition,false);

        mCategoryId2 = db_category.getCategoryId(endPosition,false);
        mCategoryTitle2 = db_category.getCategoryTitle(endPosition,false);

        db_category.updateCategory(mCategoryId1,
                mCategoryTitle2
                ,false);

        db_category.updateCategory(mCategoryId2,
                mCategoryTitle1,false);
        db_category.close();
    }

    // update listener
    DialogInterface.OnClickListener listener_update = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {
            String newCategoryItem = categoryEditText.getText().toString();

            // update category item to DB
            DB_category db_category = new DB_category(mAct);
            long id = db_category.getCategoryId(editPosition,true);
            db_category.updateCategory(id,newCategoryItem,true);

            // refresh list view
            initCategoryItem();

            dialog.dismiss();
        }
    };

    // delete listener
    DialogInterface.OnClickListener listener_delete = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

            // delete category item
            DB_category db_category = new DB_category(mAct);
            long id = db_category.getCategoryId(editPosition,true);
            db_category.deleteCategory(db_category,id ,true);

            // refresh list view
            initCategoryItem();

            dialog.dismiss();
        }
    };

}