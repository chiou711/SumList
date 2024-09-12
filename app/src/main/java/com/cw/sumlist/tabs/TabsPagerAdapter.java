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

package com.cw.sumlist.tabs;

import android.icu.util.Calendar;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.cw.sumlist.db.DB_folder;
import com.cw.sumlist.page.Page;
import com.cw.sumlist.util.preferences.Pref;

import java.util.ArrayList;

/**
 * Created by cw on 2018/3/20.
 *
 *  View Pager Adapter Class
 *
 */
public class TabsPagerAdapter extends FragmentPagerAdapter {
    public ArrayList<Page> fragmentList = new ArrayList<>();
    DB_folder dbFolder;

    TabsPagerAdapter(AppCompatActivity act, FragmentManager fm){
        super(fm);
        int folderTableId = Pref.getPref_focusView_folder_tableId(act);
        dbFolder = new DB_folder(act, folderTableId);
    }

    @Override
    public Page getItem(int position) {
        return fragmentList.get(position);
    }

    // add fragment
    public void addFragment(Page fragment) {
        fragmentList.add(fragment);
    }

    @Override
    public int getCount(){
        return fragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position){
//        System.out.println("TabsPagerAdapter / _getPageTitle / position = " + position);
        String title = dbFolder.getPageTitle(position,true);

        // add current date symbol to tab title
        //  eg. (#12)
        int date = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        // position starts from 0 and date starts from 1
        date--;
        if(date == position)
            title = "(".concat(title).concat(")");

        return title;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
//        System.out.println("TabsPagerAdapter / _setPrimaryItem / position = " + position);
    }
}

