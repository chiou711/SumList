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

package com.cw.sumlist.util.often.show;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;

import com.cw.sumlist.R;
import com.cw.sumlist.db.DB_often;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by cw on 2023/09/10
 */

public class Often_grid {
    GridView mGridView;
    public List<String> mListStrArr; // list view string array
    public int count;
    AppCompatActivity mAct;
    EditText titleEditText;
    EditText categoryEditText;
    int editPosition;
    Often_grid_adapter adapter;

    public Often_grid(AppCompatActivity act, View rootView){
        mAct = act;
        // set grid view
        mGridView = rootView.findViewById(R.id.often_grid_view);
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

        adapter = new Often_grid_adapter(
                mAct,
                R.layout.often_grid_item,
                cursor,
                from,
                to,
                0
        );

        db_often.close();

        mGridView.setAdapter(adapter);

        // set up click listener
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View vw, int position, long id){
                // select often item
                mAct.getSupportFragmentManager().popBackStack();
                String title = db_often.getOftenTitle(position,true);
                String category = db_often.getOftenCategory(position,true);

                Bundle result = new Bundle();
                result.putString("oftenItem", title);
                result.putString("categoryItem", category);
                // The child fragment needs to still set the result on its parent fragment manager.
                mAct.getSupportFragmentManager().setFragmentResult("requestOftenItem", result);
            }
        });

        // set up long click listener
        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // edit often item
                editOftenItem(position);
                return false;
            }
        } );

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
        titleEditText.setText(title);
        categoryEditText = view2.findViewById(R.id.edit_category);
        categoryEditText.setText(category);

        AlertDialog mDialog = builder.create();
        mDialog.show();
    }

    // update listener
    DialogInterface.OnClickListener listener_update = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {
            String newOftenItem_title = titleEditText.getText().toString();
            String newOftenItem_category = categoryEditText.getText().toString();

            // update often item to DB
            DB_often db_often = new DB_often(mAct);
            long id = db_often.getOftenId(editPosition,true);
            db_often.updateOften(id,newOftenItem_title,newOftenItem_category,true);//@@@

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