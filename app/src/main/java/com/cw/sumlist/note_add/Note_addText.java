/*
 * Copyright (C) 2021 CW Chiu
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

package com.cw.sumlist.note_add;

import com.cw.sumlist.db.DB_folder;
import com.cw.sumlist.main.MainAct;
import com.cw.sumlist.R;
import com.cw.sumlist.db.DB_page;
import com.cw.sumlist.tabs.TabsHost;
import com.cw.sumlist.util.ColorSet;
import com.cw.sumlist.util.Util;
import com.cw.sumlist.util.preferences.Pref;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


public class Note_addText extends AppCompatActivity {

	DB_page dB_page;
    static Long rowId;
    boolean enSaveDb = true;
	static final int ADD_TEXT_NOTE = R.id.ADD_TEXT_NOTE;
	EditText titleEditText;
	EditText bodyEditText;
	EditText quantityEditText;
	TextView newSumText;
	Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	    System.out.println("Note_addText / _onCreate");

        // get row Id from saved instance
        rowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(DB_page.KEY_NOTE_ID);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
	    System.out.println("Note_addText / _onResume");

	    setContentView(R.layout.note_add_new_text);

	    Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
	    setSupportActionBar(mToolbar);
	    if (getSupportActionBar() != null) {
		    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	    }

	    setTitle(R.string.add_new_note_title);// set title

	    dB_page = new DB_page(this, TabsHost.getCurrentPageTableId());

	    init_text_color();

	    populate_text_view(rowId);

		// add new button
		Button addBtn = findViewById(R.id.btn_add);
		addBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				addNewNote();
			}
		});
    }

	// for Add new note
	// for Rotate screen
	@Override
	protected void onPause() {
		System.out.println("Note_addText / _onPause");
		super.onPause();
		rowId = saveStateInDB(rowId, enSaveDb);
		System.out.println("Note_addText / _onPause / rowId = " + rowId);

		// hide IME
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		EditText titleEditText =  findViewById(R.id.edit_title);
		imm.hideSoftInputFromWindow(titleEditText.getWindowToken(), 0);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.out.println("Note_addText / _onDestroy");
	}

	// for Rotate screen
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		System.out.println("Note_addText / _onSaveInstanceState");
		outState.putSerializable(DB_page.KEY_NOTE_ID, rowId);
	}

	@Override
	public void onBackPressed()
	{
		stopEdit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.add_note_menu, menu);
		mMenu = menu;
		mMenu.findItem(R.id.ADD_TEXT_NOTE).setIcon(R.drawable.ic_input_add);

		titleEditText.addTextChangedListener(setTextWatcher());
		bodyEditText.addTextChangedListener(setTextWatcher());
		quantityEditText.addTextChangedListener(setTextWatcher());

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// called after onCreateOptionsMenu
		if(!isTextAdded())
			mMenu.findItem(R.id.ADD_TEXT_NOTE).setVisible(false);
		else
			mMenu.findItem(R.id.ADD_TEXT_NOTE).setVisible(true);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				stopEdit();
				return true;

			//add new note
			case ADD_TEXT_NOTE:
				addNewNote();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// add new note
	void addNewNote(){
		//add new note
		if(isTextAdded())
		{
			enSaveDb = true;
			rowId = saveStateInDB(rowId, enSaveDb);

			int notes_count = TabsHost.getCurrentPage().getNotesCountInPage(this);

			if( getIntent().getExtras().getString("extra_ADD_NEW_TO_TOP", "false").equalsIgnoreCase("true") &&
					(notes_count > 0) )
				TabsHost.getCurrentPage().swapTopBottom();

			//Toast.makeText(Note_addText.this, getString(R.string.toast_saved) +" + 1", Toast.LENGTH_SHORT).show();

			init_text_color();

			// new input
			rowId = null;
			populate_text_view(rowId);
		}
	}

	TextWatcher setTextWatcher(){
		return new TextWatcher(){
			public void afterTextChanged(Editable s)
			{
				if(!isTextAdded())
					mMenu.findItem(R.id.ADD_TEXT_NOTE).setVisible(false);
				else
					mMenu.findItem(R.id.ADD_TEXT_NOTE).setVisible(true);

					int price = 0;
					String priceStr = String.valueOf(bodyEditText.getText());
					if(!priceStr.equalsIgnoreCase(""))
						price = Integer.parseInt(priceStr);

					int qty = 0;
					String qtyStr = String.valueOf(quantityEditText.getText());
					if(!qtyStr.equalsIgnoreCase(""))
						qty = Integer.valueOf(qtyStr);

					long newSum = TabsHost.getListSum(Note_addText.this) + (int) (price * qty);
					newSumText.setText(" : " + newSum);

			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			public void onTextChanged(CharSequence s, int start, int before, int count){}
		};
	}

	// confirmation to update change or not
	void confirmUpdateChangeDlg(){
		getIntent().putExtra("NOTE_ADDED","edited");

		AlertDialog.Builder builder = new AlertDialog.Builder(Note_addText.this);
		builder.setTitle(R.string.confirm_dialog_title)
				.setMessage(R.string.add_new_note_confirm_save)
				.setPositiveButton(R.string.confirm_dialog_button_yes, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						enSaveDb = true;
						setResult(RESULT_OK, getIntent());
						finish();
					}})
				.setNeutralButton(R.string.btn_Cancel, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which)
					{   // do nothing
					}})
				.setNegativeButton(R.string.confirm_dialog_button_no, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						deleteNote(rowId);
						enSaveDb = false;
						setResult(RESULT_CANCELED, getIntent());
						finish();
					}})
				.show();
	}


	boolean isTextAdded(){
		boolean bEdit = false;
		String curTitle = titleEditText.getText().toString();
		String curBody = bodyEditText.getText().toString();
		String curQuantiry = quantityEditText.getText().toString();

		if(!Util.isEmptyString(curTitle)||
		   !Util.isEmptyString(curBody)   )
		{
			bEdit = true;
		}

		return bEdit;
	}

	void init_text_color(){
		titleEditText = (EditText) findViewById(R.id.edit_title);
		bodyEditText = (EditText) findViewById(R.id.edit_body);
		quantityEditText = (EditText) findViewById(R.id.edit_quantity);
		newSumText = findViewById(R.id.new_sum);

		int focusFolder_tableId = Pref.getPref_focusView_folder_tableId(this);
		DB_folder db = new DB_folder(MainAct.mAct, focusFolder_tableId);
		int style = db.getPageStyle(TabsHost.getFocus_tabPos(), true);

		LinearLayout block = (LinearLayout) findViewById(R.id.edit_title_block);
		if(block != null)
			block.setBackgroundColor(ColorSet.mBG_ColorArray[style]);

		//set title color
		titleEditText.setTextColor(ColorSet.mText_ColorArray[style]);
		titleEditText.setBackgroundColor(ColorSet.mBG_ColorArray[style]);

		// show IME for focus view
		if(titleEditText.requestFocus()) {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
			//	imm.showSoftInput(titleEditText, InputMethodManager.SHOW_IMPLICIT); //todo ? not work
		}

		//set body color
		bodyEditText.setTextColor(ColorSet.mText_ColorArray[style]);
		bodyEditText.setBackgroundColor(ColorSet.mBG_ColorArray[style]);

		//set quantity color
		quantityEditText.setTextColor(ColorSet.mText_ColorArray[style]);
		quantityEditText.setBackgroundColor(ColorSet.mBG_ColorArray[style]);

		newSumText.setText(" : " + TabsHost.getListSum(this));
	}

	// populate text view
	void populate_text_view(Long rowId){
		if (rowId != null) {
			// title
			String strTitleEdit = dB_page.getNoteTitle_byId(rowId);
			titleEditText.setText(strTitleEdit);
			titleEditText.setSelection(strTitleEdit.length());

			// body
			int strBodyEdit = dB_page.getNoteBody_byId(rowId);
			bodyEditText.setText(strBodyEdit);
			bodyEditText.setSelection(String.valueOf(strBodyEdit).length());

			// quantity
			int strQuantityEdit = dB_page.getNoteQuantity_byId(rowId);
			quantityEditText.setText(strQuantityEdit);
			quantityEditText.setSelection(String.valueOf(strQuantityEdit).length());

		} else {
			// renew title
			String strBlank = "";
			titleEditText.setText(strBlank);
			titleEditText.setSelection(strBlank.length());
			titleEditText.requestFocus();

			// renew body
			bodyEditText.setText(strBlank);
			bodyEditText.setSelection(strBlank.length());

			// quantity
			quantityEditText.setText("1"); // default
			quantityEditText.setSelection(1);
		}
	}

    void stopEdit(){
	    if(isTextAdded())
		    confirmUpdateChangeDlg();
	    else
	    {
		    deleteNote(rowId);
		    enSaveDb = false;
		    NavUtils.navigateUpFromSameTask(this);
	    }
    }

	void deleteNote(Long rowId){
		System.out.println("Note_addText / _deleteNote");
		// for Add new note (noteId is null first), but decide to cancel
		if(rowId != null)
			dB_page.deleteNote(rowId,true);
	}


	Long saveStateInDB(Long rowId,boolean enSaveDb){
		String title = titleEditText.getText().toString();

		String body = "";
		if(bodyEditText != null)
			body = bodyEditText.getText().toString();

		String quantity = "";
		if(quantityEditText != null)
			quantity = quantityEditText.getText().toString();

		if(enSaveDb)
		{
			if (rowId == null) // for Add new
			{
				if( (!Util.isEmptyString(title)) ||
					(!Util.isEmptyString(body))    )
				{
					// insert
					System.out.println("Note_addText / _saveStateInDB / insert");

					// value
					int value = 0;
					if(!Util.isEmptyString(body))
						value = Integer.valueOf(body);

					// quantity
					int qty = 1; // default 1
					if(!Util.isEmptyString(quantity))
						qty = Integer.valueOf(quantity);

					rowId = dB_page.insertNote(title, value,  qty,  1);// add new note, get return row Id
				}
			}
			else if ( Util.isEmptyString(title) &&
					Util.isEmptyString(body)         )
			{
				// delete
				System.out.println("Note_edit_ui / _saveStateInDB / delete");
				deleteNote(rowId);
				rowId = null;
			}
		}
		return rowId;
	}

}
