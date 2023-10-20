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

package com.cw.sumlist.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class DB_category
{

    private Context mContext;
    private static DatabaseHelper mDbHelper ;
    private SQLiteDatabase mSqlDb;

    // Table name format: Category
    public static String DB_CATEGORY_TABLE_NAME = "Category";

	// Cursor
	public Cursor mCursor_category;

    /** Constructor */
	public DB_category(Context context)
    {
        mContext = context;

	    // initial titles
	    if(getCategoryCount(true) == 0){
		    String[] categoryItems = {
				    "食","衣","住","行","育","樂","綜合"
		    };

		    for(int i=0;i<categoryItems.length;i++)
			    insertCategory(this,categoryItems[i] ,true);
	    }
    }

    /**
     * DB functions
     *
     */
	public DB_category open() throws SQLException	{
		mDbHelper = new DatabaseHelper(mContext);

		// Will call DatabaseHelper.onCreate()first time when WritableDatabase is not created yet
		mSqlDb = mDbHelper.getWritableDatabase();

		String tableCreated;
		String DB_CREATE;

		// Create Category table
		tableCreated = DB_CATEGORY_TABLE_NAME;
		DB_CREATE = "CREATE TABLE IF NOT EXISTS " + tableCreated + "(" +
				KEY_CATEGORY_ID + " INTEGER PRIMARY KEY," +
				KEY_CATEGORY_TITLE + " TEXT);";
		mSqlDb.execSQL(DB_CREATE);

        mCursor_category = this.getCategoryCursor();
		return DB_category.this;
	}

	public void close()	{
        if((mCursor_category != null) && (!mCursor_category.isClosed()))
            mCursor_category.close();
		mDbHelper.close();
	}

	// Category rows
	public static final String KEY_CATEGORY_ID = "category_id"; //can rename _id for using BaseAdapter
	public static final String KEY_CATEGORY_TITLE = "category_title";

	/*
	 * Category table columns
	 */
	private String[] strCategoryColumns = new String[] {
			KEY_CATEGORY_ID + " AS " + BaseColumns._ID,
			KEY_CATEGORY_TITLE,
	};

	public Cursor getCategoryCursor() {
		return mSqlDb.query(DB_CATEGORY_TABLE_NAME,
				strCategoryColumns,
				null,
				null,
				null,
				null,
				null
		);
	}

	public int getCategoryCount(boolean enDbOpenClose){
		if(enDbOpenClose)
			this.open();

		int count = 0;
		if((mCursor_category != null) && !mCursor_category.isClosed())
			count = mCursor_category.getCount();

		if(enDbOpenClose)
			this.close();

		return count;
	}

	public long insertCategory(DB_category db, String title, boolean enDbOpenClose){
		if(enDbOpenClose)
			db.open();

		ContentValues args = new ContentValues();
		args.put(KEY_CATEGORY_TITLE, title);
		long rowId = mSqlDb.insert(DB_category.DB_CATEGORY_TABLE_NAME, null, args);

		if(enDbOpenClose)
			db.close();

		return rowId;
	}

	public long deleteCategory(DB_category db, long id, boolean enDbOpenClose){
		if(enDbOpenClose)
			db.open();
		long rowsNumber = mSqlDb.delete(DB_category.DB_CATEGORY_TABLE_NAME, KEY_CATEGORY_ID + "='" + id +"'", null);

		if(enDbOpenClose)
			db.close();

		return rowsNumber;
	}


	public boolean updateCategory(long rowId, String title, boolean enDbOpenClose){
		if(enDbOpenClose)
			this.open();

		ContentValues args = new ContentValues();
		args.put(KEY_CATEGORY_TITLE, title);

		int cUpdateItems = mSqlDb.update(DB_CATEGORY_TABLE_NAME, args, KEY_CATEGORY_ID + "=" + rowId, null);

		if(enDbOpenClose)
			this.close();

		return cUpdateItems > 0;
	}

	@SuppressLint("Range")
	public Long getCategoryId(int position,boolean enDbOpenClose){
		if(enDbOpenClose)
			this.open();

		mCursor_category.moveToPosition(position);
		// note: KEY_CATEGORY_ID + " AS " + BaseColumns._ID
		long column = -1;
		try {
			column = mCursor_category.getLong(mCursor_category.getColumnIndex(BaseColumns._ID));
		}
		catch (Exception e) {
			System.out.println("DB_category / _getCategoryId / exception ");
		}

		if(enDbOpenClose)
			this.close();

		return column;
	}

	@SuppressLint("Range")
	public String getCategoryTitle(int position, boolean enDbOpenClose){
		if(enDbOpenClose)
			this.open();

		mCursor_category.moveToPosition(position);
		String title = mCursor_category.getString(mCursor_category.getColumnIndex(KEY_CATEGORY_TITLE));

		if(enDbOpenClose)
			this.close();

		return title;
	}

}