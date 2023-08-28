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
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.cw.sumlist.R;
import com.cw.sumlist.db.DB_often;
import com.cw.sumlist.main.MainAct;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by cw on 2023/06/29
 */

public class List_selectOftenItem
{
    DragSortListView mListView;
    public List<String> mListStrArr; // list view string array
    public int count;
    AppCompatActivity mAct;
    public boolean isCheckAll;
    private DragSortController controller;

    public List_selectOftenItem(AppCompatActivity act, View rootView, DragSortListView listView)
    {
        mAct = act;

        // list view: selecting which pages to send
        mListView = listView;
        showOftenItemsList(rootView);

        isCheckAll = false;
    }

    // show list for Select
    public int mChkNum;
    void showOftenItemsList(View root)
    {
        mChkNum = 0;
        // set list view
        mListView = root.findViewById(R.id.listView1);

        initOftenItem();
    }

    OftenItem_adapter adapter;
    private void initOftenItem()
    {
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

        adapter = new OftenItem_adapter(
                mAct,
                R.layout.often_item_row,
                cursor,
                from,
                to,
                0
        );

        db_often.close();

        mListView.setAdapter(adapter);

        // set up click listener
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View vw, int position, long id)
            {
                System.out.println("List_selectOftenItem / _showOftenItemsList / _onItemClick / position = " + position);
                mAct.getSupportFragmentManager().popBackStack();
                String title = db_often.getOftenTitle(position,true);

                Bundle result = new Bundle();
                result.putString("oftenItem", title);
                // The child fragment needs to still set the result on its parent fragment manager.
                mAct.getSupportFragmentManager().setFragmentResult("requestOftenItem", result);

            }
        });

        // set up long click listener
//        mListView.setOnItemLongClickListener(new Folder.FolderListener_longClick(mAct,adapter));

        controller = buildController(mListView);
        mListView.setFloatViewManager(controller);
        mListView.setOnTouchListener(controller);

        // init dragger
        mListView.setDragEnabled(true);

        mListView.setDragListener(onDrag);
        mListView.setDropListener(onDrop);
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
    private DragSortListView.DragListener onDrag = new DragSortListView.DragListener()
    {
        @Override
        public void drag(int startPosition, int endPosition) {
        }
    };

    // list view listener: on drop
    private DragSortListView.DropListener onDrop = new DragSortListView.DropListener()
    {
        @Override
        public void drop(int startPosition, int endPosition) {
            //reorder data base storage
            int loop = Math.abs(startPosition-endPosition);
            for(int i=0;i< loop;i++)
            {
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
    static void swapOftenItemRows(int startPosition, int endPosition)
    {
        Activity act = MainAct.mAct;
        DB_often db_often = new DB_often(act);

        db_often.open();
        mOftenId1 = db_often.getOftenId(startPosition,false);
        mOftenTitle1 = db_often.getOftenTitle(startPosition,false);

        mOftenId2 = db_often.getOftenId(endPosition,false);
        mOftenTitle2 = db_often.getOftenTitle(endPosition,false);

        db_often.updateOften(mOftenId1,
                mOftenTitle2
                ,false);

        db_often.updateOften(mOftenId2,
                mOftenTitle1,false);
        db_often.close();
    }
}
