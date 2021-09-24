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

package com.cw.sumlist.note;

import com.cw.sumlist.db.DB_page;
import com.cw.sumlist.tabs.TabsHost;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.PopupMenu;

public class NoteUi
{
    private static int mPosition;
    private ViewPager pager;
    private AppCompatActivity act;

    public static void setNotesCnt(int notesCnt) {
        NoteUi.notesCnt = notesCnt;
    }

    public static int notesCnt;



    // getter and setter of focus note position
    public static int mFocus_notePos;

    public static int getFocus_notePos() {
        return mFocus_notePos;
    }

    public static void setFocus_notePos(int Pos) {
        mFocus_notePos = Pos;
    }


    // constructor
    public NoteUi(AppCompatActivity activity, ViewPager viewPager, int position)
    {

        System.out.println("NoteUi / constructor");
        pager = viewPager;
        act = activity;
        mPosition = position;

	    DB_page db_page = new DB_page(act,TabsHost.getCurrentPageTableId());
        setNotesCnt(db_page.getNotesCount(true));

    } //Note_view_UI constructor

    static PopupMenu popup;

}
