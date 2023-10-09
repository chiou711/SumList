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

package com.cw.sumlist.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


/**
 *  Data Base Class for Page
 *
 */
public class DB_page
{

    private Context mContext;
    private static DatabaseHelper mDbHelper ;
    private SQLiteDatabase mSqlDb;

	// Table name format: Page1_2
	public static String DB_PAGE_TABLE_PREFIX = "Page";
    public static String DB_PAGE_TABLE_NAME; // Note: name = prefix + id

	// Note rows
    public static final String KEY_NOTE_ID = "_id"; //do not rename _id for using CursorAdapter (BaseColumns._ID)
	public static final String KEY_NOTE_TITLE = "note_title";
	public static final String KEY_NOTE_BODY = "note_body";
	public static final String KEY_NOTE_CATEGORY = "note_category";
	public static final String KEY_NOTE_QUANTITY = "note_quantity";
    public static final String KEY_NOTE_MARKING = "note_marking";

	// Cursor
	public Cursor mCursor_note;

	// Table Id
    private static int mTableId_page;

    /** Constructor */
	public DB_page(Context context, int pageTableId)
	{
		mContext = context;
		//System.out.println("DB_page / constructor / pageTableId = " + pageTableId);
		setFocusPage_tableId(pageTableId);
	}

    /**
     * DB functions
     * 
     */
	public DB_page open() throws SQLException
	{
		mDbHelper = new DatabaseHelper(mContext);

		// Will call DatabaseHelper.onCreate()first time when WritableDatabase is not created yet
		mSqlDb = mDbHelper.getWritableDatabase();

		//try to get note cursor
		try
		{
			//System.out.println("DB_page / _open / open page table Try / getFocusPage_tableId() = " + getFocusPage_tableId());
			mCursor_note = this.getNoteCursor_byPageTableId(getFocusPage_tableId());
		}
		catch(Exception e)
		{
			System.out.println("DB_page / _open / open page table NG! / table name = " + DB_PAGE_TABLE_NAME);
		}//catch

		return DB_page.this;
	}

	public void close()
	{
		if((mCursor_note != null)&& (!mCursor_note.isClosed()))
			mCursor_note.close();

		mDbHelper.close();
	}

    /**
     *  Page table columns for note row
     * 
     */
    private String[] strNoteColumns = new String[] {
          KEY_NOTE_ID,
		  KEY_NOTE_TITLE,
		  KEY_NOTE_BODY,
		  KEY_NOTE_CATEGORY,//@@@ how to add this to ready DB?
          KEY_NOTE_QUANTITY,
          KEY_NOTE_MARKING,
      };

    // select all notes
    private Cursor getNoteCursor_byPageTableId(int pageTableId) {

        // table number initialization: name = prefix + id
        DB_PAGE_TABLE_NAME = DB_PAGE_TABLE_PREFIX.concat(
                                                    DB_folder.getFocusFolder_tableId()+
                                                    "_"+
                                                    pageTableId);

        return mSqlDb.query(DB_PAGE_TABLE_NAME,
             strNoteColumns,
             null, 
             null, 
             null, 
             null, 
             null  
             );    
    }   
    
    //set page table id
    public static void setFocusPage_tableId(int id)
    {
    	mTableId_page = id;
    }
    
    //get page table id
    public static int getFocusPage_tableId()
    {
    	return mTableId_page;
    }
    
    // Insert note
    // createTime: 0 will update time
    public long insertNote(String title, int body,  String category, int quantity,int marking )
    {
    	this.open();

        ContentValues args = new ContentValues();
	    args.put(KEY_NOTE_TITLE, title);
	    args.put(KEY_NOTE_BODY, body);
	    args.put(KEY_NOTE_CATEGORY, category);
        args.put(KEY_NOTE_QUANTITY, quantity);
        args.put(KEY_NOTE_MARKING,marking);
        long rowId = mSqlDb.insert(DB_PAGE_TABLE_NAME, null, args);

        this.close();

        return rowId;  
    }  
    
    public boolean deleteNote(long rowId,boolean enDbOpenClose) 
    {
    	if(enDbOpenClose)
    		this.open();

    	int rowsEffected = mSqlDb.delete(DB_PAGE_TABLE_NAME, KEY_NOTE_ID + "=" + rowId, null);

        if(enDbOpenClose)
        	this.close();

        return (rowsEffected > 0);
    }    
    
    //query note
    public Cursor queryNote(long rowId) throws SQLException 
    {  
        Cursor mCursor = mSqlDb.query(true,
									DB_PAGE_TABLE_NAME,
					                new String[] {KEY_NOTE_ID,
									              KEY_NOTE_TITLE,
							                      KEY_NOTE_BODY,
							                      KEY_NOTE_CATEGORY,
				  								  KEY_NOTE_QUANTITY,
        										  KEY_NOTE_MARKING},
					                KEY_NOTE_ID + "=" + rowId,
					                null, null, null, null, null);

        if (mCursor != null) { 
            mCursor.moveToFirst();
        }

        return mCursor;
    }

    // update note
    // 		createTime:  0 for Don't update time
    public boolean updateNote(long rowId, String title, Integer body, String category, Integer quantity, Integer marking, boolean enDbOpenClose)
    {
    	if(enDbOpenClose)
    		this.open();

        ContentValues args = new ContentValues();
	    args.put(KEY_NOTE_TITLE, title);
	    args.put(KEY_NOTE_BODY, body);
	    args.put(KEY_NOTE_CATEGORY, category);
        args.put(KEY_NOTE_QUANTITY, quantity);
        args.put(KEY_NOTE_MARKING, marking);
        
        int cUpdateItems = mSqlDb.update(DB_PAGE_TABLE_NAME, args, KEY_NOTE_ID + "=" + rowId, null);

		if(enDbOpenClose)
        	this.close();

		return cUpdateItems > 0;
    }    
    
    
	public int getNotesCount(boolean enDbOpenClose)
	{
		if(enDbOpenClose)
			this.open();

		int count = 0;
		if(mCursor_note != null)
			count = mCursor_note.getCount();

		if(enDbOpenClose)
			this.close();

		return count;
	}	
	
	public int getCheckedNotesCount()
	{
		this.open();

		int countCheck =0;
		int notesCount = getNotesCount(false);
		for(int i=0;i< notesCount ;i++)
		{
			if(getNoteMarking(i,false) == 1)
				countCheck++;
		}

		this.close();

		return countCheck;
	}
	
	
	// get note quantity
	public Integer getNoteQuantity_byId(Long mRowId)
	{
		this.open();

		int quantity = queryNote(mRowId).getInt(queryNote(mRowId)
									   .getColumnIndexOrThrow(DB_page.KEY_NOTE_QUANTITY));
		this.close();

		return quantity;
	}	
	
	public String getNoteTitle_byId(Long mRowId)
	{
		this.open();

		String title = queryNote(mRowId).getString(queryNote(mRowId)
											.getColumnIndexOrThrow(DB_page.KEY_NOTE_TITLE));

		this.close();

		return title;
	}

	public Integer getNoteBody_byId(Long mRowId)
	{
		this.open();

		Integer body = queryNote(mRowId).getInt(queryNote(mRowId)
				.getColumnIndexOrThrow(DB_page.KEY_NOTE_BODY));

		this.close();

		return body;
	}

	public String getNoteCategory_byId(Long mRowId)
	{
		this.open();

		String category = queryNote(mRowId).getString(queryNote(mRowId)
				.getColumnIndexOrThrow(DB_page.KEY_NOTE_CATEGORY));

		this.close();

		return category;
	}
	
	public Integer getNoteMarking_byId(Long mRowId)
	{
		this.open();
		Integer marking = queryNote(mRowId).getInt(queryNote(mRowId)
											.getColumnIndexOrThrow(DB_page.KEY_NOTE_MARKING));
		this.close();

		return marking;

	}

	// get note by position
	@SuppressLint("Range")
	public Long getNoteId(int position,boolean enDbOpenClose)
	{
		if(enDbOpenClose)
			this.open();

		mCursor_note.moveToPosition(position);
	    Long id = mCursor_note.getLong(mCursor_note.getColumnIndex(KEY_NOTE_ID));

		if(enDbOpenClose)
	    	this.close();

		return id;
	}	
	
	@SuppressLint("Range")
	public String getNoteTitle(int position, boolean enDbOpenClose)
	{
		String title = null;

		if(enDbOpenClose)
			this.open();

		if(mCursor_note.moveToPosition(position))
			title = mCursor_note.getString(mCursor_note.getColumnIndex(KEY_NOTE_TITLE));

		if(enDbOpenClose)
        	this.close();

		return title;
	}

	@SuppressLint("Range")
	public Integer getNoteBody(int position,boolean enDbOpenClose)
	{
		int body = 0;

		if(enDbOpenClose)
			this.open();

		if(mCursor_note.moveToPosition(position))
			body = mCursor_note.getInt(mCursor_note.getColumnIndex(KEY_NOTE_BODY));

		if(enDbOpenClose)
			this.close();

		return body;
	}

	@SuppressLint("Range")
	public String getNoteCategory(int position,boolean enDbOpenClose)
	{
		String category = "";

		if(enDbOpenClose)
			this.open();

		if(mCursor_note.moveToPosition(position))
			category = mCursor_note.getString(mCursor_note.getColumnIndex(KEY_NOTE_CATEGORY));

		if(enDbOpenClose)
			this.close();

		return category;
	}

	@SuppressLint("Range")
	public Integer getNoteQuantity(int position,boolean enDbOpenClose)
	{
		if(enDbOpenClose) 
			this.open();

		mCursor_note.moveToPosition(position);
        Integer quantity = mCursor_note.getInt(mCursor_note.getColumnIndex(KEY_NOTE_QUANTITY));

		if(enDbOpenClose)
        	this.close();

		return quantity;
	}

	@SuppressLint("Range")
	public int getNoteMarking(int position,boolean enDbOpenClose)
	{
		if(enDbOpenClose)
			this.open();

		mCursor_note.moveToPosition(position);

		int marking = mCursor_note.getInt(mCursor_note.getColumnIndex(KEY_NOTE_MARKING));

		if(enDbOpenClose)
			this.close();

		return marking;
	}

}