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

public class DB_often
{

    private Context mContext;
    private static DatabaseHelper mDbHelper ;
    private SQLiteDatabase mSqlDb;

    // Table name format: Often
    public static String DB_OFTEN_TABLE_NAME = "Often";

	// Cursor
	public Cursor mCursor_often;

    /** Constructor */
	public DB_often(Context context)
    {
        mContext = context;

	    // initial titles
	    if(getOftenCount(true) == 0){
		    String[] oftenItems = {
				    "麵包","飲料",
				    "早餐","午餐","晚餐",
				    "Costco","全聯","大潤發","億客來"
		    };

		    String[] categoryItems = {
				    "食","食",
				    "食","食","食",
				    "綜合","綜合","綜合","綜合"
		    };

		    for(int i=0;i<oftenItems.length;i++)
			    insertOften(this,oftenItems[i] ,categoryItems[i] ,true);
	    }
    }

    /**
     * DB functions
     *
     */
	public DB_often open() throws SQLException	{
		mDbHelper = new DatabaseHelper(mContext);

		// Will call DatabaseHelper.onCreate()first time when WritableDatabase is not created yet
		mSqlDb = mDbHelper.getWritableDatabase();

		String tableCreated;
		String DB_CREATE;

		// Create Often table
		tableCreated = DB_OFTEN_TABLE_NAME;
		DB_CREATE = "CREATE TABLE IF NOT EXISTS " + tableCreated + "(" +
				KEY_OFTEN_ID + " INTEGER PRIMARY KEY," +
				KEY_OFTEN_TITLE + " TEXT," +
				KEY_OFTEN_CATEGORY + " TEXT);";
		mSqlDb.execSQL(DB_CREATE);

        mCursor_often = this.getOftenCursor();
		return DB_often.this;
	}

	public void close()	{
        if((mCursor_often != null) && (!mCursor_often.isClosed()))
            mCursor_often.close();
		mDbHelper.close();
	}

	// Often rows
	public static final String KEY_OFTEN_ID = "often_id"; //can rename _id for using BaseAdapter
	public static final String KEY_OFTEN_TITLE = "often_title";
	public static final String KEY_OFTEN_CATEGORY = "often_category";

	/*
	 * Often table columns
	 */
	private String[] strOftenColumns = new String[] {
			KEY_OFTEN_ID + " AS " + BaseColumns._ID,
			KEY_OFTEN_TITLE,
			KEY_OFTEN_CATEGORY,
	};

	public Cursor getOftenCursor() {
		return mSqlDb.query(DB_OFTEN_TABLE_NAME,
				strOftenColumns,
				null,
				null,
				null,
				null,
				null
		);
	}

	public int getOftenCount(boolean enDbOpenClose){
		if(enDbOpenClose)
			this.open();

		int count = 0;
		if((mCursor_often != null) && !mCursor_often.isClosed())
			count = mCursor_often.getCount();

		if(enDbOpenClose)
			this.close();

		return count;
	}

	public long insertOften(DB_often db,String title,String category, boolean enDbOpenClose){
		if(enDbOpenClose)
			db.open();

		ContentValues args = new ContentValues();
		args.put(KEY_OFTEN_TITLE, title);
		args.put(KEY_OFTEN_CATEGORY, category);
		long rowId = mSqlDb.insert(DB_often.DB_OFTEN_TABLE_NAME, null, args);

		if(enDbOpenClose)
			db.close();

		return rowId;
	}

	public long deleteOften(DB_often db,long id, boolean enDbOpenClose){
		if(enDbOpenClose)
			db.open();
		long rowsNumber = mSqlDb.delete(DB_often.DB_OFTEN_TABLE_NAME, KEY_OFTEN_ID + "='" + id +"'", null);

		if(enDbOpenClose)
			db.close();

		return rowsNumber;
	}


	public boolean updateOften(long rowId, String title, String category, boolean enDbOpenClose){
		if(enDbOpenClose)
			this.open();

		ContentValues args = new ContentValues();
		args.put(KEY_OFTEN_TITLE, title);
		args.put(KEY_OFTEN_CATEGORY, category);

		int cUpdateItems = mSqlDb.update(DB_OFTEN_TABLE_NAME, args, KEY_OFTEN_ID + "=" + rowId, null);

		if(enDbOpenClose)
			this.close();

		return cUpdateItems > 0;
	}

	@SuppressLint("Range")
	public Long getOftenId(int position,boolean enDbOpenClose){
		if(enDbOpenClose)
			this.open();

		mCursor_often.moveToPosition(position);
		// note: KEY_OFTEN_ID + " AS " + BaseColumns._ID
		long column = -1;
		try {
			column = mCursor_often.getLong(mCursor_often.getColumnIndex(BaseColumns._ID));
		}
		catch (Exception e) {
			System.out.println("DB_often / _getOftenId / exception ");
		}

		if(enDbOpenClose)
			this.close();

		return column;
	}

	@SuppressLint("Range")
	public String getOftenTitle(int position, boolean enDbOpenClose){
		if(enDbOpenClose)
			this.open();

		mCursor_often.moveToPosition(position);
		String title = mCursor_often.getString(mCursor_often.getColumnIndex(KEY_OFTEN_TITLE));

		if(enDbOpenClose)
			this.close();

		return title;
	}

	@SuppressLint("Range")
	public String getOftenCategory(int position, boolean enDbOpenClose){
		if(enDbOpenClose)
			this.open();

		mCursor_often.moveToPosition(position);
		String title = mCursor_often.getString(mCursor_often.getColumnIndex(KEY_OFTEN_CATEGORY));

		if(enDbOpenClose)
			this.close();

		return title;
	}

}