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

package com.cw.sumlist.util.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;


/**
 * Created by cw on 2017/10/11.
 */

public class Pref
{

    // set folder table id of focus view
    public static void setPref_focusView_folder_tableId(Activity act, int folderTableId )
    {
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyName = "KEY_FOCUS_VIEW_FOLDER_TABLE_ID";
        pref.edit().putInt(keyName, folderTableId).apply();
    }

    // get folder table id of focus view
    public static int getPref_focusView_folder_tableId(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("focus_view", 0);
        String keyName = "KEY_FOCUS_VIEW_FOLDER_TABLE_ID";
        return pref.getInt(keyName, 1); // folder table Id: default is 1
    }

    // remove key of focus view for folder
    public static void removePref_focusView_folder_tableId_key(Activity act)
    {
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyName = "KEY_FOCUS_VIEW_FOLDER_TABLE_ID";
        pref.edit().remove(keyName).apply();
    }

    // set page table id of focus view
    public static void setPref_focusView_page_tableId(Activity act, int pageTableId )
    {
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyPrefix = "KEY_FOCUS_VIEW_PAGE_TABLE_ID_";
        int folderTableId = getPref_focusView_folder_tableId(act);
        String keyName = keyPrefix.concat(String.valueOf(folderTableId));
        pref.edit().putInt(keyName, pageTableId).apply();
    }

    // get page table id of focus view
    public static int getPref_focusView_page_tableId(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("focus_view", 0);
        String keyPrefix = "KEY_FOCUS_VIEW_PAGE_TABLE_ID_";
        int folderTableId = getPref_focusView_folder_tableId(context);
        String keyName = keyPrefix.concat(String.valueOf(folderTableId));
        // page table Id: default is 1
        return pref.getInt(keyName, 1);
    }

    // remove key of focus view for page table Id
    public static void removePref_focusView_page_tableId_key(Activity act, int folderTableId)
    {
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyPrefix = "KEY_FOCUS_VIEW_PAGE_TABLE_ID_";
        String keyName = keyPrefix.concat(String.valueOf(folderTableId));
        pref.edit().remove(keyName).apply();
    }

    // Set list view first visible Index of focus view
    public static void setPref_focusView_list_view_first_visible_index(Activity act, int index )
    {
//		System.out.println("Pref / _setPref_focusView_list_view_first_visible_index / index = " + index);
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyName = "KEY_LIST_VIEW_FIRST_VISIBLE_INDEX";
        String location = getCurrentListViewLocation(act);
        keyName = keyName.concat(location);
        pref.edit().putInt(keyName, index).apply();
    }

    // Get list view first visible Index of focus view
    public static Integer getPref_focusView_list_view_first_visible_index(Activity act)
    {
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyName = "KEY_LIST_VIEW_FIRST_VISIBLE_INDEX";
        String location = getCurrentListViewLocation(act);
        keyName = keyName.concat(location);
        return pref.getInt(keyName, 0); // default scroll X is 0
    }

    // Set list view first visible index Top of focus view
    public static void setPref_focusView_list_view_first_visible_index_top(Activity act, int top )
    {
//        System.out.println("Pref / _setPref_focusView_list_view_first_visible_index_top / top = " + top);
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyName = "KEY_LIST_VIEW_FIRST_VISIBLE_INDEX_TOP";
        String location = getCurrentListViewLocation(act);
        keyName = keyName.concat(location);
        pref.edit().putInt(keyName, top).apply();
    }

    // Get list view first visible index Top of focus view
    public static Integer getPref_focusView_list_view_first_visible_index_top(Activity act)
    {
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyName = "KEY_LIST_VIEW_FIRST_VISIBLE_INDEX_TOP";
        String location = getCurrentListViewLocation(act);
        keyName = keyName.concat(location);
        return pref.getInt(keyName, 0);
    }

    // location about drawer table Id and page table Id
    static String getCurrentListViewLocation(Activity act)
    {
        String strLocation = "";
        // folder
        int folderTableId = getPref_focusView_folder_tableId(act);
        String strFolderTableId = String.valueOf(folderTableId);
        // page
        int pageTableId = getPref_focusView_page_tableId(act);
        String strPageTableId = String.valueOf(pageTableId);
        strLocation = "_" + strFolderTableId + "_" + strPageTableId;
        return strLocation;
    }

    // Set folder sum by folder table id
    public static void setPref_folder_sum(Activity act, int folder_table_id, long sum ) {
		System.out.println("Pref / _setPref_folder_sum / sum = " + sum);
        SharedPreferences pref = act.getSharedPreferences("sum", 0);
        String keyName = "KEY_FOLDER_SUM".concat(String.valueOf(folder_table_id));
        pref.edit().putLong(keyName, sum).apply();
    }

    // Get folder sum by folder table id
    public static Long getPref_folder_sum(Activity act,int folder_table_id) {
        SharedPreferences pref = act.getSharedPreferences("sum", 0);
        String keyName = "KEY_FOLDER_SUM".concat(String.valueOf(folder_table_id));
        return pref.getLong(keyName, -1); // default sum is -1
    }

    // Set Same as last: title
    public static void setPref_same_as_last_tilte(Activity act, String title ) {
        SharedPreferences pref = act.getSharedPreferences("same_as_last", 0);
        String keyName = "KEY_SAME_AS_LAST_TITLE";
        pref.edit().putString(keyName, title).apply();
    }

    // Get Same as last: title
    public static String getPref_same_as_last_title(Activity act) {
        SharedPreferences pref = act.getSharedPreferences("same_as_last", 0);
        String keyName = "KEY_SAME_AS_LAST_TITLE";
        return pref.getString(keyName, "none_title");
    }

    // Set Same as last: category
    public static void setPref_same_as_last_category(Activity act, String category) {
        SharedPreferences pref = act.getSharedPreferences("same_as_last", 0);
        String keyName = "KEY_SAME_AS_LAST_CATEGORY";
        pref.edit().putString(keyName, category).apply();
    }

    // Get Same as last: category
    public static String getPref_same_as_last_category(Activity act) {
        SharedPreferences pref = act.getSharedPreferences("same_as_last", 0);
        String keyName = "KEY_SAME_AS_LAST_CATEGORY";
        return pref.getString(keyName, "none_category");
    }
}
