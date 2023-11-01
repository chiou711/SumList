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

package com.cw.sumlist.note_edit;

import com.cw.sumlist.R;
import com.cw.sumlist.db.DB_page;
import com.cw.sumlist.main.MainAct;
import com.cw.sumlist.tabs.TabsHost;
import com.cw.sumlist.util.Util;
import com.cw.sumlist.util.category.select.Category_select_grid;
import com.cw.sumlist.util.often.select.Often_select_grid;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;

public class Note_edit_act extends AppCompatActivity
{

    private Long noteId;
    Integer body,quantity;
    private String title,category;
    Note_edit_ui note_edit_ui;
    private boolean enSaveDb = true;
    DB_page dB;
    int position;
    final int EDIT_LINK = 1;
	static final int CHANGE_LINK = R.id.ADD_LINK;
	EditText titleEditText;
	EditText categoryEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        // check note count first
	    dB = new DB_page(this, TabsHost.getCurrentPageTableId());

        if(dB.getNotesCount(true) ==  0)
        {
        	finish(); // add for last note being deleted
        	return;
        }
        
        setContentView(R.layout.note_edit);
        setTitle(R.string.edit_note_title);// set title
    	
        System.out.println("Note_edit / onCreate");
        
	    Toolbar toolbar = (Toolbar) findViewById(R.id.edit_toolbar);
	    toolbar.setPopupTheme(R.style.ThemeOverlay_AppCompat_Light);
	    if (toolbar != null)
		    setSupportActionBar(toolbar);

	    ActionBar actionBar = getSupportActionBar();
	    if (actionBar != null) {
		    actionBar.setTitle(R.string.edit_note_title);
		    actionBar.setDisplayHomeAsUpEnabled(true);
		    actionBar.setDisplayShowHomeEnabled(true);
	    }

    	Bundle extras = getIntent().getExtras();
    	position = extras.getInt("list_view_position");
    	noteId = extras.getLong(DB_page.KEY_NOTE_ID);
    	title = extras.getString(DB_page.KEY_NOTE_TITLE);
	    body = extras.getInt(DB_page.KEY_NOTE_BODY);
	    category = extras.getString(DB_page.KEY_NOTE_CATEGORY);
    	quantity = extras.getInt(DB_page.KEY_NOTE_QUANTITY);

		//initialization
        note_edit_ui = new Note_edit_ui(this, dB, noteId, title, body,category,quantity);
        note_edit_ui.UI_init();

    	// show view
		note_edit_ui.populateFields_all(noteId);
		
		// OK button: edit OK, save
        Button okButton = (Button) findViewById(R.id.note_edit_ok);
//        okButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_save, 0, 0, 0);
		// OK
        okButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                enSaveDb = true;
                finish();
            }

        });
        
        // delete button: delete note
        Button delButton = (Button) findViewById(R.id.note_edit_delete);
//        delButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delete, 0, 0, 0);
        // delete
        delButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view)
			{
				Util util = new Util(Note_edit_act.this);
				util.vibrate();

				Builder builder1 = new Builder(Note_edit_act.this );
				builder1.setTitle(R.string.confirm_dialog_title)
					.setMessage(R.string.confirm_dialog_message)
					.setNegativeButton(R.string.confirm_dialog_button_no, new OnClickListener()
						{   @Override
							public void onClick(DialogInterface dialog1, int which1)
							{/*nothing to do*/}
						})
					.setPositiveButton(R.string.confirm_dialog_button_yes, new OnClickListener()
						{   @Override
							public void onClick(DialogInterface dialog1, int which1)
							{
								note_edit_ui.deleteNote(noteId);

								finish();
							}
						})
					.show();//warning:end
            }
        });
        
        // cancel button: leave, do not save current modification
        Button cancelButton = (Button) findViewById(R.id.note_edit_cancel);
//        cancelButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
        cancelButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                
                // check if note content is modified
               	if(note_edit_ui.isNoteModified())
            	{
               		// show confirmation dialog
            		confirmToUpdateDlg();
            	}
            	else
            	{
            		enSaveDb = false;
                    finish();
            	}
            }
        });

	    // often item button
	    Button selOftenItemBtn = findViewById(R.id.btn_select_often_item);
	    selOftenItemBtn.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View view) {
			    // hide IME
			    hideIME(findViewById(R.id.edit_title));

			    selectOftenItem();
		    }
	    });

	    // category item button
	    Button selCategoryItemBtn = findViewById(R.id.btn_select_category_item);
	    selCategoryItemBtn.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View view) {
			    // hide IME
			    hideIME(findViewById(R.id.edit_title));

			    selectCategoryItem();
		    }
	    });

	    titleEditText = (EditText) findViewById(R.id.edit_title);
	    categoryEditText = (EditText) findViewById(R.id.edit_category);

	    getSupportFragmentManager().setFragmentResultListener("requestOftenItem", this, new FragmentResultListener() {
		    @Override
		    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
			    // We use a String here, but any type that can be put in a Bundle is supported.
			    String title = bundle.getString("oftenItem");
			    titleEditText.setText(title);

			    // also add category
			    String category = bundle.getString("categoryItem");
			    categoryEditText.setText(category);

			    oftenItem = null;
		    }
	    });

	    getSupportFragmentManager().setFragmentResultListener("requestCategoryItem", this, new FragmentResultListener() {
		    @Override
		    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
			    // We use a String here, but any type that can be put in a Bundle is supported.
			    String result = bundle.getString("categoryItem");
			    categoryEditText.setText(result);

			    categoryItem = null;
		    }
	    });

    }
    
    // confirm to update change or not
    void confirmToUpdateDlg()
    {
		AlertDialog.Builder builder = new AlertDialog.Builder(Note_edit_act.this);
		builder.setTitle(R.string.confirm_dialog_title)
	           .setMessage(R.string.edit_note_confirm_update)
	           // Yes, to update
			   .setPositiveButton(R.string.confirm_dialog_button_yes, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
					    enSaveDb = true;
					    finish();
					}})
			   // cancel
			   .setNeutralButton(R.string.btn_Cancel, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{   // do nothing
					}})
			   // no, roll back to original status		
			   .setNegativeButton(R.string.confirm_dialog_button_no, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						note_edit_ui.bRollBackData = true;
						enSaveDb = true;
	                    finish();
					}})
			   .show();
    }
    

    // for finish(), for Rotate screen
    @Override
    protected void onPause() {
        super.onPause();
        
        System.out.println("Note_edit / onPause / enSaveDb = " + enSaveDb);
        noteId = note_edit_ui.saveStateInDB(noteId, enSaveDb);
    }

	// for Rotate screen
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        System.out.println("Note_edit / onSaveInstanceState / enSaveDb = " + enSaveDb);
//        System.out.println("Note_edit / onSaveInstanceState / bUseCameraImage = " + bUseCameraImage);

        noteId = note_edit_ui.saveStateInDB(noteId, enSaveDb);
        outState.putSerializable(DB_page.KEY_NOTE_ID, noteId);
        
    }
    
    // for After Rotate
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
    	super.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    public void onBackPressed() {
        if(note_edit_ui.isNoteModified())
        {
            confirmToUpdateDlg();
        }
        else
        {
            enSaveDb = false;
            finish();
        }
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// inflate menu
		getMenuInflater().inflate(R.menu.edit_note_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}
    
    @Override 
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	switch (item.getItemId()) 
        {
		    case android.R.id.home:
		    	if(note_edit_ui.isNoteModified())
		    	{
		    		confirmToUpdateDlg();
		    	}
		    	else
		    	{
		            enSaveDb = false;
		            finish();
		    	}
		        return true;

            case CHANGE_LINK:
			    return true;
			    
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent)
	{
		super.onActivityResult(requestCode,resultCode,returnedIntent);

		//todo Why this is not called when Share link
		System.out.println("Note_edit / _onActivityResult");
        // choose link
		if( (requestCode == EDIT_LINK) && (resultCode == RESULT_CANCELED))
		{
			System.out.println("Note_edit / _onActivityResult / canceled");
			Toast.makeText(Note_edit_act.this, R.string.note_cancel_add_new, Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED, getIntent());
            enSaveDb = true;
			MainAct.isEdited_link = false;
            return; // must add this
		}

	}

	Often_select_grid oftenItem;
	void selectOftenItem(){

		// disable category grid view
		if(categoryItem != null) {
			categoryItem.hideGridView();
			categoryItem = null;
		}

		if(oftenItem == null) {
			oftenItem = new Often_select_grid();
			FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
			mFragmentTransaction.setCustomAnimations(R.anim.fragment_slide_in_left, R.anim.fragment_slide_out_left, R.anim.fragment_slide_in_right, R.anim.fragment_slide_out_right);
			mFragmentTransaction.add(R.id.container, oftenItem, "select often item").addToBackStack("select often item").commit();
		} else {
			oftenItem.hideGridView();
			oftenItem = null;
		}

	}

	Category_select_grid categoryItem;
	void selectCategoryItem(){

		// disable often grid view
		if(oftenItem != null) {
			oftenItem.hideGridView();
			oftenItem = null;
		}

		if(categoryItem == null) {
			categoryItem = new Category_select_grid();
			FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
			mFragmentTransaction.setCustomAnimations(R.anim.fragment_slide_in_left, R.anim.fragment_slide_out_left, R.anim.fragment_slide_in_right, R.anim.fragment_slide_out_right);
			mFragmentTransaction.add(R.id.container, categoryItem, "select category item").addToBackStack("select category item").commit();
		} else {
			categoryItem.hideGridView();
			categoryItem = null;
		}
	}

	// hide IME
	void hideIME(EditText titleEditText){
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(titleEditText.getWindowToken(), 0);
	}
}
