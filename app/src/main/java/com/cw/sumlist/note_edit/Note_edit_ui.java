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

import com.cw.sumlist.db.DB_folder;
import com.cw.sumlist.main.MainAct;
import com.cw.sumlist.R;
import com.cw.sumlist.db.DB_page;
import com.cw.sumlist.tabs.TabsHost;
import com.cw.sumlist.util.MyEditText;
import com.cw.sumlist.util.ColorSet;
import com.cw.sumlist.util.preferences.Pref;
import com.cw.sumlist.util.Util;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

public class Note_edit_ui {

	private MyEditText titleEditText;
	private MyEditText bodyEditText;
	private MyEditText quantityEditText;
	private String oriTitle;
	private Integer oriBody;
	private Integer oriQuantity;
	private Integer oriMarking;

	boolean bRollBackData;

    private DB_page dB_page;
	private Activity act;
	private int style;

	Note_edit_ui(Activity act, DB_page _db, Long noteId, String strTitle, Integer body ,Integer quantity)
    {
    	this.act = act;
	    dB_page = _db;//Page.mDb_page;

    	oriTitle = strTitle;
	    oriBody = body;
	    oriQuantity = quantity;
	    oriMarking = dB_page.getNoteMarking_byId(noteId);
		
	    bRollBackData = false;
    }

	void UI_init()
    {

		UI_init_text();

	    bodyEditText = (MyEditText) act.findViewById(R.id.edit_body);
	    quantityEditText = (MyEditText) act.findViewById(R.id.edit_quantity);

		DB_folder dbFolder = new DB_folder(act, Pref.getPref_focusView_folder_tableId(act));
		style = dbFolder.getPageStyle(TabsHost.getFocus_tabPos(), true);

		//set body color
		if(bodyEditText != null){
			bodyEditText.setTextColor(ColorSet.mText_ColorArray[style]);
			bodyEditText.setBackgroundColor(ColorSet.mBG_ColorArray[style]);
		}

	    //set quantity color
	    if(quantityEditText != null){
		    quantityEditText.setTextColor(ColorSet.mText_ColorArray[style]);
		    quantityEditText.setBackgroundColor(ColorSet.mBG_ColorArray[style]);
	    }

	    final InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

	private void UI_init_text()
	{
        int focusFolder_tableId = Pref.getPref_focusView_folder_tableId(act);
        DB_folder db = new DB_folder(MainAct.mAct, focusFolder_tableId);
		style = db.getPageStyle(TabsHost.getFocus_tabPos(), true);

		LinearLayout block = (LinearLayout) act.findViewById(R.id.edit_title_block);
		if(block != null)
			block.setBackgroundColor(ColorSet.mBG_ColorArray[style]);

		titleEditText =  (MyEditText) act.findViewById(R.id.edit_title);
		bodyEditText =  (MyEditText) act.findViewById(R.id.edit_body);

		//set title color
		titleEditText.setTextColor(ColorSet.mText_ColorArray[style]);
		titleEditText.setBackgroundColor(ColorSet.mBG_ColorArray[style]);

		//set body color
		bodyEditText.setTextColor(ColorSet.mText_ColorArray[style]);
		bodyEditText.setBackgroundColor(ColorSet.mBG_ColorArray[style]);
	}

	void deleteNote(Long rowId)
    {
    	System.out.println("Note_edit_ui / _deleteNote");
        // for Add new note (noteId is null first), but decide to cancel
        if(rowId != null)
        	dB_page.deleteNote(rowId,true);
    }
    
    // populate text fields
	void populateFields_text(Long rowId)
	{
		if (rowId != null) {
			// title
			String strTitleEdit = dB_page.getNoteTitle_byId(rowId);
			titleEditText.setText(strTitleEdit);
			titleEditText.setSelection(strTitleEdit.length());

			// body
			int strBodyEdit = dB_page.getNoteBody_byId(rowId);
			bodyEditText.setText(String.valueOf(strBodyEdit));
			bodyEditText.setSelection(String.valueOf(strBodyEdit).length());

			// request cursor
			titleEditText.requestFocus();
		}
        else
        {
            // renew title
            String strBlank = "";
            titleEditText.setText(strBlank);
            titleEditText.setSelection(strBlank.length());
            titleEditText.requestFocus();

			// renew body
			bodyEditText.setText(strBlank);
			bodyEditText.setSelection(strBlank.length());
        }
	}

    // populate all fields
	void populateFields_all(Long rowId){
    	if (rowId != null){
			populateFields_text(rowId);

		    // title
		    String strTitleEdit = dB_page.getNoteTitle_byId(rowId);
		    titleEditText.setText(strTitleEdit);
		    titleEditText.setSelection(strTitleEdit.length());

		    // body
		    int strBodyEdit = dB_page.getNoteBody_byId(rowId);
		    bodyEditText.setText(String.valueOf(strBodyEdit));
		    bodyEditText.setSelection(String.valueOf(strBodyEdit).length());

		    // quantity
		    int strQuantityEdit = dB_page.getNoteQuantity_byId(rowId);
		    quantityEditText.setText(String.valueOf(strQuantityEdit));
		    quantityEditText.setSelection(String.valueOf(strQuantityEdit).length());

        } else {
            // renew
			String strEmpty = "";
			if(titleEditText != null) {
	            titleEditText.setText(strEmpty);
	            titleEditText.setSelection(strEmpty.length());
	            titleEditText.requestFocus();
			}

		    if(bodyEditText != null) {
			    bodyEditText.setText(strEmpty);
			    bodyEditText.setSelection(strEmpty.length());
			    bodyEditText.requestFocus();
		    }

		    if(quantityEditText != null) {
			    quantityEditText.setText(strEmpty);
			    quantityEditText.setSelection(strEmpty.length());
			    quantityEditText.requestFocus();
		    }
    	}
    }

	private boolean isBodyModified()
    {
    	int value = Integer.valueOf(bodyEditText.getText().toString());
	    System.out.println("--- value = " + value);
	    System.out.println("--- oriBody = " + oriBody);


    	return (oriBody != value);
    }

	private boolean isTitleModified()
    {
    	return !oriTitle.equals(titleEditText.getText().toString());
    }

	boolean isNoteModified()
    {
    	boolean bModified = false;
//		System.out.println("Note_edit_ui / _isNoteModified / isTitleModified() = " + isTitleModified());
    	if( isTitleModified() ||
    		isBodyModified()  )
    	{
    		bModified = true;
    	}
    	
    	return bModified;
    }

	Long saveStateInDB(Long rowId,boolean enSaveDb)
	{
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
	        		(!Util.isEmptyString(body)) ||
		            (!Util.isEmptyString(quantity)) )
	        	{
	        		// insert
	        		System.out.println("Note_edit_ui / _saveStateInDB / insert");
	        		rowId = dB_page.insertNote(title, Integer.valueOf(body),  Integer.valueOf(quantity), 1);// add new note, get return row Id
	        	}
	        }
	        else // for Edit
	        {
	        	if( !Util.isEmptyString(title) ||
			        !Util.isEmptyString(body) ||
	        		!Util.isEmptyString(quantity)       )
	        	{
	        		// update
	        		if(bRollBackData) //roll back
	        		{
			        	System.out.println("Note_edit_ui / _saveStateInDB / update: roll back");
	        			dB_page.updateNote(rowId, oriTitle, oriBody,  oriQuantity,oriMarking,true);
	        		}
	        		else // update new
	        		{
	        			System.out.println("Note_edit_ui / _saveStateInDB / update new");
						System.out.println("--- rowId = " + rowId);
						System.out.println("--- oriMarking = " + oriMarking);

                        Integer marking;
                        if(null == oriMarking)
                            marking = 0;
                        else
                            marking = oriMarking;

	        			dB_page.updateNote(rowId, title, Integer.valueOf(body),  Integer.valueOf(quantity),
												marking, true); // update note
	        		}
	        	}
	        	else if( Util.isEmptyString(title) &&
			        	 Util.isEmptyString(body) &&
				         Util.isEmptyString(quantity) )
	        	{
	        		// delete
	        		System.out.println("Note_edit_ui / _saveStateInDB / delete");
	        		deleteNote(rowId);
			        rowId = null;
	        	}
	        }
        }

		return rowId;
	}

	public int getCount()
	{
		int noteCount = dB_page.getNotesCount(true);
		return noteCount;
	}
	
}
