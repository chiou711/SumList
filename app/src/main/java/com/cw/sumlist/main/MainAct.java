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

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.cw.sumlist.R;
import com.cw.sumlist.Utils;
import com.cw.sumlist.config.About;
import com.cw.sumlist.config.Config;
import com.cw.sumlist.config.MonthSummary;
import com.cw.sumlist.db.DB_folder;
import com.cw.sumlist.db.DB_page;
import com.cw.sumlist.drawer.Drawer;
import com.cw.sumlist.folder.Folder;
import com.cw.sumlist.folder.FolderUi;
import com.cw.sumlist.note_add.Note_addText;
import com.cw.sumlist.operation.delete.DeleteFolders;
import com.cw.sumlist.operation.delete.DeletePages;
import com.cw.sumlist.operation.sum_pages.SumPagesFragment;
import com.cw.sumlist.operation.sum_folders.SumFoldersFragment;
import com.cw.sumlist.page.Checked_notes_option;
import com.cw.sumlist.page.PageUi;
import com.cw.sumlist.tabs.TabsHost;
import com.cw.sumlist.db.DB_drawer;
import com.cw.sumlist.util.image.UtilImage;
import com.cw.sumlist.define.Define;
import com.cw.sumlist.util.OnBackPressedListener;
import com.cw.sumlist.util.Util;
import com.cw.sumlist.util.preferences.Pref;
import com.mobeta.android.dslv.DragSortListView;

import android.os.Build;
import android.os.StrictMode;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import androidx.core.view.GravityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import static com.cw.sumlist.define.Define.PREFERENCE_ENABLE_EXPAND_CARD_VIEW;

public class MainAct extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener
{
    public static CharSequence mFolderTitle;
    public static CharSequence mAppTitle;
    public Context mContext;
    public Config mConfigFragment;
    public SumPagesFragment mSumPagesFragment;
    public About mAboutFragment;
    public MonthSummary mMonthSummary;
    public static Menu mMenu;
    public static List<String> mFolderTitles;
    public static AppCompatActivity mAct;//TODO static issue
    public FragmentManager mFragmentManager;
    public static FragmentManager.OnBackStackChangedListener mOnBackStackChangedListener;
    public static int mLastOkTabId = 1;
    public static SharedPreferences mPref_show_note_attribute;
    OnBackPressedListener onBackPressedListener;
    public Drawer drawer;
    public static Folder mFolder;
    public static Toolbar mToolbar;

    public static boolean isEdited_link;
    public static int edit_position;
    public static long folder_sum;
    public static ActivityResultLauncher<Intent> dataActivityResultLauncher;

	// Main Act onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.MyTheme);

        /**
         * Set APP build mode
         * Note:
         *  1. for AdMob: it works after Google Play store release
         *  2. for assets mode: need to enable build.gradle assets.srcDirs = ['preferred/assets/']
         */
        Define.setAppBuildMode();

        // Release mode: no debug message
        if (Define.CODE_MODE == Define.RELEASE_MODE) {
            OutputStream nullDev = new OutputStream() {
                public void close() {}
                public void flush() {}
                public void write(byte[] b) {}
                public void write(byte[] b, int off, int len) {}
                public void write(int b) {}
            };
            System.setOut(new PrintStream(nullDev));
        }

        System.out.println("================start application ==================");
        System.out.println("MainAct / _onCreate");

        mAct = this;
        mAppTitle = getTitle();

        // File provider implementation is needed after Android version 24
        // if not, will encounter android.os.FileUriExposedException
        // cf. https://stackoverflow.com/questions/38200282/android-os-fileuriexposedexception-file-storage-emulated-0-test-txt-exposed

        // add the following to disable this requirement
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                // method 1
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);

                // method 2
//                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//                StrictMode.setVmPolicy(builder.build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Show Api version
        if (Define.CODE_MODE == Define.DEBUG_MODE)
            Toast.makeText(this, mAppTitle + " " + "API_" + Build.VERSION.SDK_INT, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, mAppTitle, Toast.LENGTH_SHORT).show();

        UtilImage.getDefaultScaleInPercent(MainAct.this);

        doCreate();

        isEdited_link = false;
        edit_position = 0;

        // register ForActivityResult
        dataActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    System.out.println("MainAct / _onActivityResult / ADD_NEW_ACTIVITY");
                    updatePageSumArr();

                    // update new folder sum
                    int folder_table_id = Pref.getPref_focusView_folder_tableId(this);
                    Pref.setPref_folder_sum(this,folder_table_id,folder_sum);
                });
    }

    // Do major create operation
    void doCreate() {
        System.out.println("MainAct / _doCreate");

        mFolderTitles = new ArrayList<>();

        // check DB
        final boolean ENABLE_DB_CHECK = false;
        if (ENABLE_DB_CHECK) {
            // list all folder tables
            FolderUi.listAllFolderTables(mAct);

            // recover focus
            DB_folder.setFocusFolder_tableId(Pref.getPref_focusView_folder_tableId(this));
            DB_page.setFocusPage_tableId(Pref.getPref_focusView_page_tableId(this));
        }

        mContext = getBaseContext();

        // add on back stack changed listener
        mFragmentManager = getSupportFragmentManager();
        mOnBackStackChangedListener = this;
        mFragmentManager.addOnBackStackChangedListener(mOnBackStackChangedListener);

        mAct = this;
    }

    // key event: 1 from bluetooth device 2 when notification bar dose not shown
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        System.out.println("MainAct / _onKeyDown / keyCode = " + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS: //88
                return true;

            case KeyEvent.KEYCODE_MEDIA_NEXT: //87
                return true;

            case KeyEvent.KEYCODE_MEDIA_PLAY: //126
                return true;

            case KeyEvent.KEYCODE_MEDIA_PAUSE: //127
                return true;

            case KeyEvent.KEYCODE_BACK:
                onBackPressed();
                return true;

            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                return true;

            case KeyEvent.KEYCODE_MEDIA_REWIND:
                return true;

            case KeyEvent.KEYCODE_MEDIA_STOP:
                return true;
        }
        return false;
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
    }

    /**
     * initialize action bar
     */
    void initActionBar()
    {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            mToolbar.setNavigationIcon(R.drawable.ic_drawer);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Drawer.drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }
    }

    // set action bar for fragment
    void initActionBar_home()
    {
        drawer.drawerToggle.setDrawerIndicatorEnabled(false);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
            getSupportActionBar().setDisplayShowHomeEnabled(false);//false: no launcher icon
        }

        mToolbar.setNavigationIcon(R.drawable.ic_menu_back);
        mToolbar.getChildAt(1).setContentDescription(getResources().getString(R.string.btn_back));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("MainAct / _initActionBar_home / click to popBackStack");

                // check if DB is empty
                DB_drawer db_drawer = new DB_drawer(mAct);
                int focusFolder_tableId = Pref.getPref_focusView_folder_tableId(mAct);
                DB_folder db_folder = new DB_folder(mAct,focusFolder_tableId);
                if((db_drawer.getFoldersCount(true) == 0) ||
                   (db_folder.getPagesCount(true) == 0)      )
                {
                    finish();
                    Intent intent  = new Intent(mAct,MainAct.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                else
                    getSupportFragmentManager().popBackStack();
            }
        });

    }


    /*********************************************************************************
     *
     *                                      Life cycle
     *
     *********************************************************************************/

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("MainAct / _onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
    	System.out.println("MainAct / _onResume");
    }


    @Override
    protected void onResumeFragments() {
        System.out.println("MainAct / _onResumeFragments ");
        super.onResumeFragments();
        if(mFragmentManager != null)
            mFragmentManager.popBackStack();

        configLayoutView(); //createAssetsFile inside
    }

    // open folder
    static int current_folder_position;
    public static List<Long> pageSumArr;
    public static void openFolder()
    {
        System.out.println("MainAct / _openFolder");
        DB_drawer dB_drawer = new DB_drawer(mAct);
        int folders_count = dB_drawer.getFoldersCount(true);

        if (folders_count > 0) {
            int pref_focus_table_id = Pref.getPref_focusView_folder_tableId(MainAct.mAct);
            for(int folder_pos=0; folder_pos<folders_count; folder_pos++)
            {
                if(dB_drawer.getFolderTableId(folder_pos,true) == pref_focus_table_id) {
                    // select folder
                    FolderUi.selectFolder(mAct, folder_pos);
                    current_folder_position = folder_pos;
                    // set focus folder position
                    FolderUi.setFocus_folderPos(folder_pos);
                }
            }
            // set focus table Id
            DB_folder.setFocusFolder_tableId(pref_focus_table_id);

            if (mAct.getSupportActionBar() != null)
                mAct.getSupportActionBar().setTitle(mFolderTitle);
        }

        int folder_table_id = Pref.getPref_focusView_folder_tableId(mAct);

        // set folder_sum
        if(Pref.getPref_folder_sum(mAct,folder_table_id) >= 0)
            folder_sum = Pref.getPref_folder_sum(mAct,folder_table_id);
        else {
            updatePageSumArr();// first time
            Pref.setPref_folder_sum(mAct,folder_table_id,folder_sum);
        }
    }

    // update page sum array
    public static void updatePageSumArr(){
//        Toast.makeText(mAct,R.string.update_view,Toast.LENGTH_SHORT).show();
        folder_sum = 0;
        pageSumArr = new ArrayList<>();
        DB_folder dB_folder = new DB_folder(mAct, Pref.getPref_focusView_folder_tableId(mAct));
        int pageCount = dB_folder.getPagesCount(true);
        dB_folder.open();
        for (int i = 0; i < pageCount; i++) {
            long pageSum = Utils.getPageSum(mAct, dB_folder.getPageTableId(i, false));
            pageSumArr.add(pageSum);
            folder_sum += pageSum;
        }
        dB_folder.close();
    }


    @Override
    protected void onDestroy()
    {
        System.out.println("MainAct / _onDestroy");
        super.onDestroy();
    }

    /**
     *  on Back button pressed
     */
    @Override
    public void onBackPressed()
    {
        System.out.println("MainAct / _onBackPressed");
        doBackKeyEvent();
    }

    void doBackKeyEvent()
    {
        if (onBackPressedListener != null)
        {
            DB_drawer dbDrawer = new DB_drawer(this);
            int foldersCnt = dbDrawer.getFoldersCount(true);

            if(foldersCnt == 0)
            {
                finish();
                Intent intent  = new Intent(this,MainAct.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            else {
                onBackPressedListener.doBack();
            }
        }
        else
        {
            if((drawer != null) && drawer.isDrawerOpen())
                drawer.closeDrawer();
            else
                super.onBackPressed();
        }

    }


    @Override
    public void onBackStackChanged() {
        int backStackEntryCount = mFragmentManager.getBackStackEntryCount();
        System.out.println("MainAct / _onBackStackChanged / backStackEntryCount = " + backStackEntryCount);

        if(backStackEntryCount == 1) // fragment
        {
            System.out.println("MainAct / _onBackStackChanged / fragment");
            initActionBar_home();
        }
        else if(backStackEntryCount == 0) // init
        {
            System.out.println("MainAct / _onBackStackChanged / init");
            onBackPressedListener = null;

            if(mFolder.adapter!=null)
                mFolder.adapter.notifyDataSetChanged();

            configLayoutView();
        }
    }

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }

    /***********************************************************************************
     *
     *                                          Menu
     *
     ***********************************************************************************/

    /****************************************************
     *  On Prepare Option menu :
     *  Called whenever we call invalidateOptionsMenu()
     ****************************************************/
    @Override
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        System.out.println("MainAct / _onPrepareOptionsMenu");

        if((drawer == null) || (drawer.drawerLayout == null))
            return false;

        DB_drawer db_drawer = new DB_drawer(this);
        int foldersCnt = db_drawer.getFoldersCount(true);

        /**
         * Folder group
         */
        // If the navigation drawer is open, hide action items related to the content view
        if(drawer.isDrawerOpen())
        {
            // for landscape: the layout file contains folder menu
            if(Util.isLandscapeOrientation(mAct)) {
                mMenu.setGroupVisible(R.id.group_folders, true);
                // set icon for folder draggable: landscape
                if(MainAct.mPref_show_note_attribute != null)
                {
                    if (MainAct.mPref_show_note_attribute.getString("KEY_ENABLE_FOLDER_DRAGGABLE", "no")
                            .equalsIgnoreCase("yes"))
                        mMenu.findItem(R.id.ENABLE_FOLDER_DRAG_AND_DROP).setIcon(R.drawable.btn_check_on_holo_light);
                    else
                        mMenu.findItem(R.id.ENABLE_FOLDER_DRAG_AND_DROP).setIcon(R.drawable.btn_check_off_holo_light);
                }
            }

            mMenu.setGroupVisible(R.id.group_pages_and_more, false);
            mMenu.setGroupVisible(R.id.group_notes, false);
        }
        else if(!drawer.isDrawerOpen())
        {
            if(Util.isLandscapeOrientation(mAct))
                mMenu.setGroupVisible(R.id.group_folders, false);

            /**
             * Page group and more
             */
//            mMenu.setGroupVisible(R.id.group_pages_and_more, foldersCnt >0);//todo temp need overall check

            if(foldersCnt>0)
            {
                getSupportActionBar().setTitle(mFolderTitle);

                // pages count
                int pgsCnt = FolderUi.getFolder_pagesCount(this,FolderUi.getFocus_folderPos());

                // notes count
                int notesCnt = 0;
                int pageTableId = Pref.getPref_focusView_page_tableId(this);

                if(pageTableId > 0) {
                    DB_page dB_page = new DB_page(this, pageTableId);
                    if (dB_page != null) {
                        try {
                            notesCnt = dB_page.getNotesCount(true);
                        } catch (Exception e) {
                            System.out.println("MainAct / _onPrepareOptionsMenu / dB_page.getNotesCount() error");
                            notesCnt = 0;
                        }
                    }
                }

                // expand card view
                SharedPreferences expand_card_view = MainAct.mAct.getSharedPreferences("show_note_attribute", 0);
                boolean bExpand = expand_card_view.getBoolean("KEY_EXPAND_CARD_VIEW", PREFERENCE_ENABLE_EXPAND_CARD_VIEW);

                if(bExpand)
                    mMenu.findItem(R.id.EXPAND_CARD_VIEW).setIcon(R.drawable.expander_close_holo_dark);
                else
                    mMenu.findItem(R.id.EXPAND_CARD_VIEW).setIcon(R.drawable.expander_open_holo_dark);

                // change page color
                mMenu.findItem(R.id.CHANGE_PAGE_COLOR).setVisible(pgsCnt >0);

                // pages order
                mMenu.findItem(R.id.SHIFT_PAGE).setVisible(pgsCnt >1);

                // delete pages
                mMenu.findItem(R.id.DELETE_PAGES).setVisible(pgsCnt >0);

                /**
                 *  Note group
                 */
                // group of notes
                mMenu.setGroupVisible(R.id.group_notes, pgsCnt > 0);

                // HANDLE CHECKED NOTES
                mMenu.findItem(R.id.HANDLE_CHECKED_NOTES).setVisible( (pgsCnt >0) && (notesCnt>0) );
            }
            else if(foldersCnt==0)
            {
                /**
                 *  Note group
                 */
                mMenu.setGroupVisible(R.id.group_notes, false);

                // disable page operation sub menu entry
                mMenu.findItem(R.id.page_operation).setVisible(false);

            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /*************************
     * onCreate Options Menu
     *
     *************************/
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu)
    {
//		System.out.println("MainAct / _onCreateOptionsMenu");
        mMenu = menu;

        // inflate menu
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /******************************
     * on options item selected
     *
     ******************************/
    public static FragmentTransaction mFragmentTransaction;

    static int mMenuUiState;

    public static void setMenuUiState(int mMenuState) {
        mMenuUiState = mMenuState;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        //System.out.println("MainAct / _onOptionsItemSelected");
        setMenuUiState(item.getItemId());
        DB_drawer dB_drawer = new DB_drawer(this);
        DB_folder dB_folder = new DB_folder(this, Pref.getPref_focusView_folder_tableId(this));

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = getSupportFragmentManager().beginTransaction();

        // Go back: check if Configure fragment now
        if( (item.getItemId() == android.R.id.home ))
        {

            System.out.println("MainAct / _onOptionsItemSelected / Home key of Config is pressed / mFragmentManager.getBackStackEntryCount() =" +
            mFragmentManager.getBackStackEntryCount());

            if(mFragmentManager.getBackStackEntryCount() > 0 )
            {
                int foldersCnt = dB_drawer.getFoldersCount(true);
                System.out.println("MainAct / _onOptionsItemSelected / Home key of Config is pressed / foldersCnt = " + foldersCnt);

                if(foldersCnt == 0)
                {
                    finish();
                    Intent intent  = new Intent(this,MainAct.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                else
                {
                    mFragmentManager.popBackStack();

                    initActionBar();

                    mFolderTitle = dB_drawer.getFolderTitle(FolderUi.getFocus_folderPos(),true);
                    setTitle(mFolderTitle);
                    drawer.closeDrawer();
                }
                return true;
            }
        }


        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (drawer.drawerToggle.onOptionsItemSelected(item))
        {
            System.out.println("MainAct / _onOptionsItemSelected / drawerToggle.onOptionsItemSelected(item) == true ");
            return true;
        }

        switch (item.getItemId())
        {
            // for landscape layout
            case MenuId.ADD_NEW_FOLDER:
                FolderUi.renewFirstAndLast_folderId();
                FolderUi.addNewFolder(this, FolderUi.mLastExist_folderTableId +1, mFolder.getAdapter());
                return true;

            // for landscape layout
            case MenuId.ENABLE_FOLDER_DRAG_AND_DROP:
                if(MainAct.mPref_show_note_attribute.getString("KEY_ENABLE_FOLDER_DRAGGABLE", "no")
                        .equalsIgnoreCase("yes"))
                {
                    mPref_show_note_attribute.edit().putString("KEY_ENABLE_FOLDER_DRAGGABLE","no")
                            .apply();
                    DragSortListView listView = (DragSortListView) this.findViewById(R.id.drawer_listview);
                    listView.setDragEnabled(false);
                    Toast.makeText(this,getResources().getString(R.string.drag_folder)+
                                    ": " +
                                    getResources().getString(R.string.set_disable),
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mPref_show_note_attribute.edit().putString("KEY_ENABLE_FOLDER_DRAGGABLE","yes")
                            .apply();
                    DragSortListView listView = (DragSortListView) this.findViewById(R.id.drawer_listview);
                    listView.setDragEnabled(true);
                    Toast.makeText(this,getResources().getString(R.string.drag_folder) +
                                    ": " +
                                    getResources().getString(R.string.set_enable),
                            Toast.LENGTH_SHORT).show();
                }
                mFolder.getAdapter().notifyDataSetChanged();
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                return true;

            // for landscape layout
            case MenuId.DELETE_FOLDERS:
                mMenu.setGroupVisible(R.id.group_folders, false);

                if(dB_drawer.getFoldersCount(true)>0)
                {
                    drawer.closeDrawer();
                    mMenu.setGroupVisible(R.id.group_notes, false); //hide the menu
                    DeleteFolders delFoldersFragment = new DeleteFolders();
                    mFragmentTransaction.setCustomAnimations(R.anim.fragment_slide_in_left, R.anim.fragment_slide_out_left, R.anim.fragment_slide_in_right, R.anim.fragment_slide_out_right);
                    mFragmentTransaction.replace(R.id.content_frame, delFoldersFragment).addToBackStack("delete_folders").commit();
                }
                return true;

            // for landscape layout
            case MenuId.SUM_FOLDERS:
                if(dB_drawer.getFoldersCount(true)>0){
                    drawer.closeDrawer();
                    mMenu.setGroupVisible(R.id.group_notes, false); //hide the menu
                    SumFoldersFragment sumFoldersFragment = new SumFoldersFragment();
                    mFragmentTransaction = mAct.getSupportFragmentManager().beginTransaction();
                    mFragmentTransaction.setCustomAnimations(R.anim.fragment_slide_in_left, R.anim.fragment_slide_out_left, R.anim.fragment_slide_in_right, R.anim.fragment_slide_out_right);
                    mFragmentTransaction.replace(R.id.content_frame, sumFoldersFragment).addToBackStack("delete_folders").commit();
                }
                return true;

            case MenuId.ADD_NEW_NOTE:
                SharedPreferences mPref_add_new_note_location = getSharedPreferences("add_new_note_option", 0);
                boolean bTop = mPref_add_new_note_location.getString("KEY_ADD_NEW_NOTE_TO","bottom").equalsIgnoreCase("top");

                Intent intent = new Intent(this, Note_addText.class);
                if(bTop)
                    intent.putExtra("extra_ADD_NEW_TO_TOP", "true");
                else
                    intent.putExtra("extra_ADD_NEW_TO_TOP", "false");

                dataActivityResultLauncher.launch(intent);
                return true;

            case MenuId.CHECKED_OPERATION:
                Checked_notes_option op = new Checked_notes_option(this);
                op.open_option_grid(this);
                return true;

            case MenuId.EXPAND_CARD_VIEW:
                SharedPreferences expand_card_view = getSharedPreferences("show_note_attribute", 0);
                boolean bExpand = expand_card_view.getBoolean("KEY_EXPAND_CARD_VIEW", PREFERENCE_ENABLE_EXPAND_CARD_VIEW);
                expand_card_view.edit().putBoolean("KEY_EXPAND_CARD_VIEW",!bExpand).apply();

                TabsHost.reloadCurrentPage();
                invalidateOptionsMenu();

                return true;

            case MenuId.ADD_NEW_PAGE:
                PageUi.addNewPage(this, getCurrentMaxPageTableId() + 1);
                return true;

            case MenuId.ADD_NEW_PAGE_MULTI:
                PageUi.addNewPage_multi(this, getCurrentMaxPageTableId() + 1);
                return true;

            case MenuId.CHANGE_PAGE_COLOR:
                PageUi.changePageColor(this);
                return true;

            case MenuId.SHIFT_PAGE:
                PageUi.shiftPage(this);
            return true;

            case MenuId.DELETE_PAGES:
                //hide the menu
                mMenu.setGroupVisible(R.id.group_notes, false);
                mMenu.setGroupVisible(R.id.group_pages_and_more, false);

                if(dB_folder.getPagesCount(true)>0)
                {

                    DeletePages delPgsFragment = new DeletePages();
                    mFragmentTransaction.setCustomAnimations(R.anim.fragment_slide_in_left, R.anim.fragment_slide_out_left, R.anim.fragment_slide_in_right, R.anim.fragment_slide_out_right);
                    mFragmentTransaction.replace(R.id.content_frame, delPgsFragment).addToBackStack("delete_pages").commit();
                }
                else
                {
                    Toast.makeText(this, R.string.no_page_yet, Toast.LENGTH_SHORT).show();
                }
            return true;

            case MenuId.FOLDER_SUM:
                mMenu.setGroupVisible(R.id.group_notes, false); //hide the menu
                mMenu.setGroupVisible(R.id.group_pages_and_more, false);
                setTitle(R.string.folder_sum);


                mSumPagesFragment = new SumPagesFragment();
                mFragmentTransaction.setCustomAnimations(R.anim.fragment_slide_in_left, R.anim.fragment_slide_out_left, R.anim.fragment_slide_in_right, R.anim.fragment_slide_out_right);
                mFragmentTransaction.replace(R.id.content_frame, mSumPagesFragment).addToBackStack("folder sum").commit();
                return true;

            case MenuId.MONTH_SUMMARY:
                mMenu.setGroupVisible(R.id.group_notes, false); //hide the menu
                mMenu.setGroupVisible(R.id.group_pages_and_more, false);
                setTitle(R.string.month_summary);

                mMonthSummary = new MonthSummary();
                mFragmentTransaction.setCustomAnimations(R.anim.fragment_slide_in_left, R.anim.fragment_slide_out_left, R.anim.fragment_slide_in_right, R.anim.fragment_slide_out_right);
                mFragmentTransaction.replace(R.id.content_frame, mMonthSummary).addToBackStack("month summary").commit();
                return true;

            case MenuId.CONFIG:
                mMenu.setGroupVisible(R.id.group_notes, false); //hide the menu
                mMenu.setGroupVisible(R.id.group_pages_and_more, false);
                setTitle(R.string.settings);

                mConfigFragment = new Config();
                mFragmentTransaction.setCustomAnimations(R.anim.fragment_slide_in_left, R.anim.fragment_slide_out_left, R.anim.fragment_slide_in_right, R.anim.fragment_slide_out_right);
                mFragmentTransaction.replace(R.id.content_frame, mConfigFragment).addToBackStack("config").commit();
                return true;

            case MenuId.ABOUT:
                mMenu.setGroupVisible(R.id.group_notes, false); //hide the menu
                mMenu.setGroupVisible(R.id.group_pages_and_more, false);
                setTitle(R.string.about_title);

                mAboutFragment = new About();
                mFragmentTransaction.setCustomAnimations(R.anim.fragment_slide_in_left, R.anim.fragment_slide_out_left, R.anim.fragment_slide_in_right, R.anim.fragment_slide_out_right);
                mFragmentTransaction.replace(R.id.content_frame, mAboutFragment).addToBackStack("about").commit();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // configure layout view
    void configLayoutView(){
        System.out.println("MainAct / _configLayoutView");

        setContentView(R.layout.drawer);
        initActionBar();

        // new drawer
        drawer = new Drawer(this);
        drawer.initDrawer();
        drawer.drawerToggle.syncState(); // make sure toggle icon state is correct

        // new folder
        mFolder = new Folder(this);

        openFolder();
    }

    // get current max page table Id
    int getCurrentMaxPageTableId(){
        int currentMaxPageTableId = 0;
        int pgCnt = FolderUi.getFolder_pagesCount(this,FolderUi.getFocus_folderPos());
        DB_folder db_folder = new DB_folder(this,DB_folder.getFocusFolder_tableId());

        for(int i=0;i< pgCnt;i++)
        {
            int id = db_folder.getPageTableId(i,true);
            if(id >currentMaxPageTableId)
                currentMaxPageTableId = id;
        }
        return currentMaxPageTableId;
    }
}
