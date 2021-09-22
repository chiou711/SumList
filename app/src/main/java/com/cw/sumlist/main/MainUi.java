/*
 * Copyright (C) 2020 CW Chiu
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

package com.cw.sumlist.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import com.cw.sumlist.R;
import com.cw.sumlist.db.DB_drawer;
import com.cw.sumlist.db.DB_folder;
import com.cw.sumlist.db.DB_page;
import com.cw.sumlist.page.Page_recycler;
import com.cw.sumlist.tabs.TabsHost;
import com.cw.sumlist.util.CustomWebView;
import com.cw.sumlist.util.Util;
import com.cw.sumlist.util.preferences.Pref;

import java.util.Date;

/**
 * Created by cw on 2017/10/7.
 */

public class MainUi {

    MainUi(){}

    /**
     * Add note with Intent link
     */
    String titleReceived,title;
    String addNote_IntentLink(Intent intent,final AppCompatActivity act,boolean isAdded_onNewIntent)
    {
        System.out.println("MainUi / _addNote_IntentLink /  ");

        Bundle extras = intent.getExtras();
        String pathOri = null;
        String path;
        if(extras != null)
            pathOri = extras.getString(Intent.EXTRA_TEXT);
        else
            System.out.println("MainUi / _addNote_IntentLink / extras == null");

        path = pathOri;

        if(!Util.isEmptyString(pathOri))
        {
            System.out.println("MainUi / _addNote_IntentLink / pathOri = " + pathOri);
            // for SoundCloud case, path could contain other strings before URI path
            if(pathOri.contains("http"))
            {
                String[] str = pathOri.split("http");

                for(int i=0;i< str.length;i++)
                {
                    if(str[i].contains("://"))
                        path = "http".concat(str[i]);
                }
            }

            // case example: Google news, title is within URL
            if(Util.isWebLink(path)){
                String[] str = pathOri.split("http");
                titleReceived = str[0];
            }

            DB_drawer db_drawer = new DB_drawer(act);
            int folders_count = db_drawer.getFoldersCount(true);

            DB_folder db_folder = new DB_folder(act, Pref.getPref_focusView_folder_tableId(act));
            int pages_count = db_folder.getPagesCount(true);

            if((folders_count == 0) || (pages_count == 0))
            {
                Toast.makeText(act,"No folder or no page yet, please add a new one in advance.",Toast.LENGTH_LONG).show();
                return null;
            }

            System.out.println("MainUi / _addNote_IntentLink / titleReceived = " + titleReceived);

            // insert link
            DB_page dB_page = new DB_page(act,Pref.getPref_focusView_page_tableId(act));
            dB_page.insertNote("", "",  path,  0, (long) 0);// add new note, get return row Id

            // save to top or to bottom
            final String link =path;
            int count = dB_page.getNotesCount(true);
            SharedPreferences pref_show_note_attribute = act.getSharedPreferences("add_new_note_option", 0);

            // swap if new position is top
            boolean isAddedToTop = pref_show_note_attribute.getString("KEY_ADD_NEW_NOTE_TO","bottom").equalsIgnoreCase("top");
            if( isAddedToTop && (count > 1) )
                TabsHost.getCurrentPage().swapTopBottom();

            {
                title = pathOri;
                if (pref_show_note_attribute.getString("KEY_ADD_NEW_NOTE_TO", "bottom").equalsIgnoreCase("top") &&
                        (count > 1)) {
                    TabsHost.getCurrentPage().swapTopBottom();
                }

                Toast.makeText(act,
                        act.getResources().getText(R.string.add_new_note_option_title) + title,
                        Toast.LENGTH_SHORT)
                        .show();
            }

            return title;
        }
        else
            return null;
    }

    /**
     * Edit note with Intent link
     */
    String editNote_IntentLink(Intent intent,final AppCompatActivity act,boolean isEdited_onNewIntent,int position)
    {
        System.out.println("MainUi / _editNote_IntentLink /  ");

        Bundle extras = intent.getExtras();
        String pathOri = null;
        String path;
        if(extras != null)
            pathOri = extras.getString(Intent.EXTRA_TEXT);
        else
            System.out.println("MainUi / _editNote_IntentLink / extras == null");

        path = pathOri;

        if(!Util.isEmptyString(pathOri))
        {
            System.out.println("MainUi / _editNote_IntentLink / pathOri = " + pathOri);
            // for SoundCloud case, path could contain other strings before URI path
            if(pathOri.contains("http"))
            {
                String[] str = pathOri.split("http");

                for(int i=0;i< str.length;i++)
                {
                    if(str[i].contains("://"))
                        path = "http".concat(str[i]);
                }
            }

            DB_drawer db_drawer = new DB_drawer(act);
            int folders_count = db_drawer.getFoldersCount(true);

            DB_folder db_folder = new DB_folder(act, Pref.getPref_focusView_folder_tableId(act));
            int pages_count = db_folder.getPagesCount(true);

            if((folders_count == 0) || (pages_count == 0))
            {
                Toast.makeText(act,"No folder or no page yet, please add a new one in advance.",Toast.LENGTH_LONG).show();
                return null;
            }

            System.out.println("MainUi / _editNote_IntentLink / path = " + path);

            // save to top or to bottom
            final String link =path;

            {
                title = pathOri;
                Toast.makeText(act,
                        act.getResources().getText(R.string.add_new_note_option_title) + title,
                        Toast.LENGTH_SHORT)
                        .show();
            }

            return title;
        }
        else
            return null;
    }


    /****************************
     *          YouTube
     *
     ****************************/
    /**
     *  get YouTube link
     */
    String getYouTubeLink(AppCompatActivity act,int pos)
    {
        DB_page dB_page = new DB_page(act, TabsHost.getCurrentPageTableId());
        int count = dB_page.getNotesCount(true);
        if(pos >= count)
        {
            pos = 0;
            Page_recycler.mCurrPlayPosition = 0;
        }

        String linkStr="";
        if(pos < count)
            linkStr =dB_page.getNoteLinkUri(pos,true);

        return linkStr;
    }

    /**
     *  launch next YouTube intent
     */
    void launchNextYouTubeIntent(AppCompatActivity act, Handler handler, Runnable runCountDown)
    {
        String link = getYouTubeLink(act,TabsHost.getCurrentPage().mCurrPlayPosition);
        if( Util.isYouTubeLink(link) )
        {
            Util.openLink_YouTube(act, link);
            cancelYouTubeHandler(handler,runCountDown);
        }
    }

    /**
     *  cancel YouTube Handler
     */
    void cancelYouTubeHandler(Handler handler,Runnable runCountDown)
    {
        if(handler != null) {
            handler.removeCallbacks(runCountDown);
//            handler = null;
        }
    }

}
