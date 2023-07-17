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

    // Table name format: Drawer
    public static String DB_OFTEN_TABLE_NAME = "Often";

	// Cursor
	public Cursor mCursor_often;

    /** Constructor */
	public DB_often(Context context)
    {
        mContext = context;
    }

    /**
     * DB functions
     *
     */
	public DB_often open() throws SQLException
	{
		mDbHelper = new DatabaseHelper(mContext);

		// Will call DatabaseHelper.onCreate()first time when WritableDatabase is not created yet
		mSqlDb = mDbHelper.getWritableDatabase();

		String tableCreated;
		String DB_CREATE;


		// Create Often table
		tableCreated = DB_often.DB_OFTEN_TABLE_NAME;
		DB_CREATE = "CREATE TABLE IF NOT EXISTS " + tableCreated + "(" +
				DB_often.KEY_OFTEN_ID + " INTEGER PRIMARY KEY," +
				DB_often.KEY_OFTEN_TITLE + " TEXT);";
		mSqlDb.execSQL(DB_CREATE);

        mCursor_often = this.getOftenCursor();
		return DB_often.this;
	}

	public void close()
	{
        if((mCursor_often != null) && (!mCursor_often.isClosed()))
            mCursor_often.close();
		mDbHelper.close();
	}

	// Often rows
	public static final  String KEY_OFTEN_ID = "often_id"; //can rename _id for using BaseAdapter
	public static final String KEY_OFTEN_TITLE = "often_title";

	/*
	 * Often table columns
	 */
	private String[] strOftenColumns = new String[] {
			KEY_OFTEN_ID + " AS " + BaseColumns._ID,
			KEY_OFTEN_TITLE,
	};

	public Cursor getOftenCursor() {
		return mSqlDb.query("Often",
				strOftenColumns,
				null,
				null,
				null,
				null,
				null
		);
	}

}