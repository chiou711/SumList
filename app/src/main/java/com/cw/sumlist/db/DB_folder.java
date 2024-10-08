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

package com.cw.sumlist.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

/**
 *  Data Base Class for Folder
 *
 */
public class DB_folder
{

    private Context mContext = null;
    private static DatabaseHelper mDbHelper ;
    public SQLiteDatabase mSqlDb;

	// Table name format: Folder1
	public static String DB_FOLDER_TABLE_PREFIX = "Folder";

	// Table name format: Page1_2
	public static String DB_PAGE_TABLE_PREFIX = "Page";
    private static String DB_PAGE_TABLE_NAME; // Note: name = prefix + id

	// Page rows
    public static final String KEY_PAGE_ID = "page_id"; //can rename _id for using BaseAdapter
    public static final String KEY_PAGE_TITLE = "page_title";
    public static final String KEY_PAGE_TABLE_ID = "page_table_id";
    public static final String KEY_PAGE_STYLE = "page_style";
    public static final String KEY_PAGE_CREATED = "page_created";

	// DB
    DB_folder mDb_folder;

	// Cursor
	static Cursor mCursor_page;

	// Table Id
	private static int mTableId_folder;

    /** Constructor */
	public DB_folder(Context context, int folderTableId)
	{
		mContext = context;
		setFocusFolder_tableId(folderTableId);
	}

    /**
     * DB functions
     * 
     */
	public DB_folder open() throws SQLException
	{
		mDbHelper = new DatabaseHelper(mContext);

		// Will call DatabaseHelper.onCreate()first time when WritableDatabase is not created yet
		mSqlDb = mDbHelper.getWritableDatabase();

        try
        {
            mCursor_page = this.getPageCursor_byFolderTableId(getFocusFolder_tableId());
        }
        catch (Exception e)
        {
        	e.printStackTrace();
	        System.out.println("DB_folder / open folder table NG! / table id = " + getFocusFolder_tableId());
        }

        return DB_folder.this;
	}

	public void close()
	{
        if((mCursor_page != null) && (!mCursor_page.isClosed()))
            mCursor_page.close();
		mDbHelper.close();
	}

    //insert new page table by
    // 1 SQLiteDatabase
    // 2 assigned drawer Id
    // 3 page table Id
    public void insertPageTable(DB_folder db, int drawerId, int pageId, boolean enDbOpenClose)
    {   
    	if(enDbOpenClose)
    		db.open();

        //format "Page1_2"
    	DB_PAGE_TABLE_NAME = DB_PAGE_TABLE_PREFIX.concat(String.valueOf(drawerId)+
    														"_"+
    														String.valueOf(pageId));
        String dB_insert_table = "CREATE TABLE IF NOT EXISTS " + DB_PAGE_TABLE_NAME + "(" +
        							DB_page.KEY_NOTE_ID + " INTEGER PRIMARY KEY," +
							        DB_page.KEY_NOTE_TITLE + " TEXT," +
							        DB_page.KEY_NOTE_BODY + " INTEGER," +
		                            DB_page.KEY_NOTE_CATEGORY + " TEXT," +
		                            DB_page.KEY_NOTE_QUANTITY + " INTEGER," +
		                            DB_page.KEY_NOTE_MARKING + " INTEGER);";
        mSqlDb.execSQL(dB_insert_table);

        if(enDbOpenClose)
        	db.close();
    }

    //delete page table
    public void dropPageTable(int id,boolean enDbOpenClose)
    {
        if(enDbOpenClose)
            this.open();

        //format "Page1_2"
        DB_PAGE_TABLE_NAME = DB_PAGE_TABLE_PREFIX.concat(String.valueOf(getFocusFolder_tableId())+"_"+String.valueOf(id));
        String dB_drop_table = "DROP TABLE IF EXISTS " + DB_PAGE_TABLE_NAME + ";";
        mSqlDb.execSQL(dB_drop_table);

        if(enDbOpenClose)
            this.close();
    }

    //delete page table by folder table Id
    public void dropPageTable(int folderTableId, int id)
    {   
    	this.open();

        //format "Page1_2"
    	DB_PAGE_TABLE_NAME = DB_PAGE_TABLE_PREFIX.concat(String.valueOf(folderTableId)+"_"+String.valueOf(id));
        String dB_drop_table = "DROP TABLE IF EXISTS " + DB_PAGE_TABLE_NAME + ";";
        mSqlDb.execSQL(dB_drop_table);         

        this.close();
    } 
    
    /*
     * Folder table columns for page row
     * 
     */
    String[] strPageColumns = new String[] {
			KEY_PAGE_ID,
			KEY_PAGE_TITLE,
			KEY_PAGE_TABLE_ID,
			KEY_PAGE_STYLE,
			KEY_PAGE_CREATED
        };   

    // get page cursor
    public Cursor getPageCursor_byFolderTableId(int i) {
        return mSqlDb.query(DB_FOLDER_TABLE_PREFIX + String.valueOf(i),
							strPageColumns,
							null,
							null,
							null,
							null,
							null
							);
    }

    // insert page with SqlDb parameter
    public long insertPage(SQLiteDatabase sqlDb, String intoTable, String title, long ntId, int style)
    {
        Date now = new Date();
        ContentValues args = new ContentValues();
        args.put(KEY_PAGE_TITLE, title);
        args.put(KEY_PAGE_TABLE_ID, ntId);
        args.put(KEY_PAGE_STYLE, style);
        args.put(KEY_PAGE_CREATED, now.getTime());

        return sqlDb.insert(intoTable, null, args);
    }
    
    // insert page
    public long insertPage(String intoTable, String title, long ntId, int style, boolean enDbOpenClose)
    {
        if(enDbOpenClose)
    	    this.open();

        Date now = new Date();
        ContentValues args = new ContentValues();
        args.put(KEY_PAGE_TITLE, title);
        args.put(KEY_PAGE_TABLE_ID, ntId);
        args.put(KEY_PAGE_STYLE, style);
        args.put(KEY_PAGE_CREATED, now.getTime());
        long rowId = mSqlDb.insert(intoTable, null, args);

        if(enDbOpenClose)
            this.close();
        return rowId;
    }
    
    // delete page
    public long deletePage(String table, int pageId, boolean enDbOpenClose)
    {
        System.out.println("DB / deletePage / table = " + table + ", page Id = " + pageId);

        if(enDbOpenClose)
            this.open();
        long rowsNumber = mSqlDb.delete(table, KEY_PAGE_ID + "='" + pageId +"'", null);
        if(enDbOpenClose)
            this.close();

        if(rowsNumber > 0)
            System.out.println("DB / deletePage / rowsNumber =" + rowsNumber);
        else
            System.out.println("DB / deletePage / failed to delete");

        return rowsNumber;
    }

    //update page
    public boolean updatePage(long id, String title, long ntId, int style, boolean enDbOpenClose)
    {
        if(enDbOpenClose)
    	    this.open();

        ContentValues args = new ContentValues();
        Date now = new Date();
        args.put(KEY_PAGE_TITLE, title);
        args.put(KEY_PAGE_TABLE_ID, ntId);
        args.put(KEY_PAGE_STYLE, style);
        args.put(KEY_PAGE_CREATED, now.getTime());
        int rowsNumber = mSqlDb.update(DB_FOLDER_TABLE_PREFIX +String.valueOf(getFocusFolder_tableId()), args, KEY_PAGE_ID + "=" + id, null);

        if(enDbOpenClose)
            this.close();

        return  (rowsNumber>0)?true:false;
    }

    public Cursor getPageCursor()
    {
		return mCursor_page;
    }
    
	public int getPagesCount(boolean enDbOpenClose)
	{
		if(enDbOpenClose)
			this.open();

		int count = 0;
		if((mCursor_page != null) && !mCursor_page.isClosed())
            count = mCursor_page.getCount();

        if(enDbOpenClose)
			this.close();

		return count;
	}

	@SuppressLint("Range")
	public int getPageId(int position, boolean enDbOpenClose)
	{
        if(enDbOpenClose)
            this.open();

        if(mCursor_page.moveToPosition(position))
        {
            int pageId = mCursor_page.getInt(mCursor_page.getColumnIndex(KEY_PAGE_ID));
//			System.out.println("DB_folder / _getPageId / pageId = " + pageId);

            if(enDbOpenClose)
                this.close();

            return pageId;
        }
        else
        {
            if(enDbOpenClose)
                this.close();

            return -1;
        }
	}

    //get current page title
    public String getCurrentPageTitle()
    {
    	String title = null;

    	this.open();
    	int pagesCount = getPagesCount(false);
    	for(int i=0;i< pagesCount; i++ )
    	{
    		if( Integer.valueOf(DB_page.getFocusPage_tableId()) == getPageTableId(i,false))
    		{
    			title = getPageTitle(i,false);
    		}
    	}
    	this.close();

        return title;
    }

	@SuppressLint("Range")
	public int getPageTableId(int position, boolean enDbOpenClose)
	{
		if(enDbOpenClose)
			this.open();

        mCursor_page.moveToPosition(position);
        int id = mCursor_page.getInt(mCursor_page.getColumnIndex(KEY_PAGE_TABLE_ID));

        if(enDbOpenClose)
        	this.close();

        return id;
	}

	@SuppressLint({"Range", "SuspiciousIndentation"})
	public String getPageTitle(int position, boolean enDbOpenClose)
	{
		if(enDbOpenClose)
			this.open();

        mCursor_page.moveToPosition(position);
        String title = mCursor_page.getString(mCursor_page.getColumnIndex(KEY_PAGE_TITLE));

        if(enDbOpenClose)
        	this.close();

        return title;
	}

	@SuppressLint("Range")
	public int getPageStyle(int position, boolean enDbOpenClose)
	{
		int style = 0;

        if(enDbOpenClose)
		    this.open();

        if(mCursor_page.moveToPosition(position))
			style = mCursor_page.getInt(mCursor_page.getColumnIndex(KEY_PAGE_STYLE));

        if(enDbOpenClose)
            this.close();

        return style;
	}

    public static void setFocusFolder_tableId(int i)
    {
//        System.out.println("DB_folder / _setFocusFolder_tableId / i = " + i);
    	mTableId_folder = i;
    }
    
    public static int getFocusFolder_tableId()
    {
//        System.out.println("DB_folder / _getFocusFolder_tableId / mTableId_folder = " + mTableId_folder);
    	return mTableId_folder;
    }
    
	// get current folder table name
	public static String getFocusFolder_tableName()
	{
		return DB_folder.DB_FOLDER_TABLE_PREFIX + DB_folder.getFocusFolder_tableId();
	}
}