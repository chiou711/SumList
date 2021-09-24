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

package com.cw.sumlist.page;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cw.sumlist.R;
import com.cw.sumlist.db.DB_folder;
import com.cw.sumlist.db.DB_page;
import com.cw.sumlist.main.MainAct;
import com.cw.sumlist.note.Note;
import com.cw.sumlist.note_edit.Note_edit;
import com.cw.sumlist.page.item_touch_helper.ItemTouchHelperAdapter;
import com.cw.sumlist.page.item_touch_helper.ItemTouchHelperViewHolder;
import com.cw.sumlist.page.item_touch_helper.OnStartDragListener;
import com.cw.sumlist.tabs.TabsHost;
import com.cw.sumlist.util.ColorSet;
import com.cw.sumlist.util.Util;
import com.cw.sumlist.util.preferences.Pref;

import java.util.ArrayList;
import java.util.List;

import static com.cw.sumlist.db.DB_page.KEY_NOTE_MARKING;
import static com.cw.sumlist.db.DB_page.KEY_NOTE_QUANTITY;
import static com.cw.sumlist.db.DB_page.KEY_NOTE_TITLE;
import static com.cw.sumlist.db.DB_page.KEY_NOTE_BODY;
import static com.cw.sumlist.page.Page_recycler.swapRows;

// Pager adapter
public class PageAdapter_recycler extends RecyclerView.Adapter<PageAdapter_recycler.ViewHolder>
        implements ItemTouchHelperAdapter
{
	private AppCompatActivity mAct;
	private String strTitle;
	private String strBody;
	private Integer quantity;
	private Integer marking;
	private static int style;
    DB_folder dbFolder;
	private int page_pos;
    private final OnStartDragListener mDragStartListener;
	DB_page mDb_page;
	int page_table_id;
	List<Db_cache> listCache;

    PageAdapter_recycler(int pagePos,  int pageTableId, OnStartDragListener dragStartListener) {
	    mAct = MainAct.mAct;
	    mDragStartListener = dragStartListener;

	    dbFolder = new DB_folder(mAct,Pref.getPref_focusView_folder_tableId(mAct));
	    page_pos = pagePos;
	    page_table_id = pageTableId;

	    updateDbCache();
    }

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        ImageView btnMarking;
        ImageView btnViewNote;
        ImageView btnEditNote;
		TextView rowId;
	    TextView textTitle;
	    TextView textBody;
		TextView textQuantity;
        ImageViewCustom btnDrag;

        public ViewHolder(View v) {
            super(v);

            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

	        btnMarking = (ImageView) v.findViewById(R.id.btn_marking);
	        btnViewNote = (ImageView) v.findViewById(R.id.btn_view_note);
	        btnEditNote = (ImageView) v.findViewById(R.id.btn_edit_note);
	        btnDrag = (ImageViewCustom) v.findViewById(R.id.btn_drag);

	        rowId= (TextView) v.findViewById(R.id.row_id);
	        textTitle = (TextView) v.findViewById(R.id.row_title);
	        textBody = (TextView) v.findViewById(R.id.row_body);
	        textQuantity = (TextView) v.findViewById(R.id.row_quantity);
        }

        public TextView getTextView() {
            return textTitle;
        }

        @Override
        public void onItemSelected() {
//            itemView.setBackgroundColor(Color.LTGRAY);
            ((CardView)itemView).setCardBackgroundColor(MainAct.mAct.getResources().getColor(R.color.button_color));
        }

        @Override
        public void onItemClear() {
            ((CardView)itemView).setCardBackgroundColor(ColorSet.mBG_ColorArray[style]);
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.page_view_card, viewGroup, false);

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int _position) {

//        System.out.println("PageAdapter_recycler / _onBindViewHolder / position = " + position);

	    int position = holder.getAdapterPosition();

	    // style
        style = dbFolder.getPageStyle(page_pos, true);

        ((CardView)holder.itemView).setCardBackgroundColor(ColorSet.mBG_ColorArray[style]);

		SharedPreferences pref_show_note_attribute = MainAct.mAct.getSharedPreferences("show_note_attribute", 0);

	    // get DB data
	    // add check to avoid exception during Copy/Move checked
//        System.out.println("PageAdapter / _onBindViewHolder / listCache.size() = " + listCache.size());
	    if( (listCache != null) &&
		    (listCache.size() > 0) &&
		    (position!=listCache.size()) )
	    {
            strTitle =  listCache.get(position).title;
            strBody = listCache.get(position).body;
            quantity = listCache.get(position).quantity;
		    marking = listCache.get(position).marking;

	    } else  {
		    strTitle ="";
		    strBody ="";
		    quantity = 0;
		    marking = 0;
	    }

        /**
         *  control block
         */
        // show row Id
        holder.rowId.setText(String.valueOf(position+1));
        holder.rowId.setTextColor(ColorSet.mText_ColorArray[style]);

        // show marking check box
        if(marking == 1){
            holder.btnMarking.setBackgroundResource(style % 2 == 1 ?
                    R.drawable.btn_check_on_holo_light :
                    R.drawable.btn_check_on_holo_dark);
        } else {
            holder.btnMarking.setBackgroundResource(style % 2 == 1 ?
                    R.drawable.btn_check_off_holo_light :
                    R.drawable.btn_check_off_holo_dark);
        }

        // show drag button
        if(pref_show_note_attribute.getString("KEY_ENABLE_DRAGGABLE", "yes").equalsIgnoreCase("yes"))
            holder.btnDrag.setVisibility(View.VISIBLE);
        else
            holder.btnDrag.setVisibility(View.GONE);

		// show text title
		if( Util.isEmptyString(strTitle)){
			// make sure empty title is empty after scrolling
			holder.textTitle.setVisibility(View.VISIBLE);
			holder.textTitle.setText("");
		} else {
			holder.textTitle.setVisibility(View.VISIBLE);
			holder.textTitle.setTextSize((float) 24.00);
			holder.textTitle.setText(strTitle);
			holder.textTitle.setTextColor(ColorSet.mText_ColorArray[style]);
		}

		// text body
	    if( Util.isEmptyString(strBody)){
		    // make sure empty title is empty after scrolling
		    holder.textBody.setVisibility(View.VISIBLE);
		    holder.textBody.setText("");
	    }
	    else{
		    holder.textBody.setVisibility(View.VISIBLE);
		    holder.textBody.setText(strBody);
		    holder.textBody.setTextSize((float) 34.00);
		    holder.textBody.setTextColor(ColorSet.mText_ColorArray[style]);
	    }

	    // text quantity
	    if( Util.isEmptyString(String.valueOf(quantity))){
		    // make sure empty title is empty after scrolling
		    holder.textQuantity.setVisibility(View.VISIBLE);
		    holder.textQuantity.setText("");
	    }
	    else{
		    holder.textQuantity.setVisibility(View.VISIBLE);
		    holder.textQuantity.setText(String.valueOf(quantity));
		    holder.textQuantity.setTextSize((float) 34.00);
		    holder.textQuantity.setTextColor(ColorSet.mText_ColorArray[style]);
	    }

        setBindViewHolder_listeners(holder,position);
    }


    /**
     * Set bind view holder listeners
     * @param viewHolder
     * @param position
     */
    void setBindViewHolder_listeners(ViewHolder viewHolder, final int position)
    {
//        System.out.println("PageAdapter_recycler / setBindViewHolder_listeners / position = " + position);
        /**
         *  control block
         */
        // on mark note
        viewHolder.btnMarking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("PageAdapter / _getView / btnMarking / _onClick");

	            // toggle marking and get new setting
	            int marking = toggleNoteMarking(mAct,position);

	            updateDbCache();

                //Toggle marking will resume page, so do Store v scroll
                RecyclerView listView = TabsHost.mTabsPagerAdapter.fragmentList.get(TabsHost.getFocus_tabPos()).recyclerView;
                TabsHost.store_listView_vScroll(listView);

	            // set marking icon
	            if(marking == 1)
	            {
		            v.setBackgroundResource(style % 2 == 1 ?
				            R.drawable.btn_check_on_holo_light :
				            R.drawable.btn_check_on_holo_dark);
	            }
	            else
	            {
		            v.setBackgroundResource(style % 2 == 1 ?
				            R.drawable.btn_check_off_holo_light :
				            R.drawable.btn_check_off_holo_dark);
	            }

	            TabsHost.showFooter(MainAct.mAct);
            }
        });

        // on view note
        viewHolder.btnViewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TabsHost.getCurrentPage().mCurrPlayPosition = position;
                DB_page db_page = new DB_page(mAct,TabsHost.getCurrentPageTableId());
                int count = db_page.getNotesCount(true);
                if(position < count)
                {
                    // apply Note class
                    Intent intent;
                    intent = new Intent(mAct, Note.class);
                    intent.putExtra("POSITION", position);
                    mAct.startActivity(intent);
                }
            }
        });

        // on edit note
        viewHolder.btnEditNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DB_page db_page = new DB_page(mAct, TabsHost.getCurrentPageTableId());
                Long rowId = db_page.getNoteId(position,true);

                Intent i = new Intent(mAct, Note_edit.class);
                i.putExtra("list_view_position", position);
                i.putExtra(DB_page.KEY_NOTE_ID, rowId);
	            i.putExtra(DB_page.KEY_NOTE_TITLE, db_page.getNoteTitle_byId(rowId));
	            i.putExtra(DB_page.KEY_NOTE_BODY, db_page.getNoteBody_byId(rowId));
	            i.putExtra(DB_page.KEY_NOTE_QUANTITY, db_page.getNoteQuantity_byId(rowId));
                mAct.startActivity(i);
            }
        });

        // Start a drag whenever the handle view it touched
        viewHolder.btnDrag.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked())
                {
                    case MotionEvent.ACTION_DOWN:
                        mDragStartListener.onStartDrag(viewHolder);
                        System.out.println("PageAdapter_recycler / onTouch / ACTION_DOWN");
                        return true;
                    case MotionEvent.ACTION_UP:
                        v.performClick();
                        return true;
                }
                return false;
            }


        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
	    mDb_page = new DB_page(mAct, page_table_id);
	    return  mDb_page.getNotesCount(true);
    }

    // toggle mark of note
    public static int toggleNoteMarking(AppCompatActivity mAct, int position)
    {
        int marking = 0;
		DB_page db_page = new DB_page(mAct,TabsHost.getCurrentPageTableId());
        db_page.open();
        int count = db_page.getNotesCount(false);
        if(position >= count) //end of list
        {
            db_page.close();
            return marking;
        }

	    String strNote = db_page.getNoteTitle(position,false);
	    String strBody = db_page.getNoteBody(position,false);
        Integer quantity = db_page.getNoteQuantity(position,false);
        Long idNote =  db_page.getNoteId(position,false);

        // toggle the marking
        if(db_page.getNoteMarking(position,false) == 0)
        {
            db_page.updateNote(idNote, strNote, strBody, quantity, 1, false);
            marking = 1;
        }
        else
        {
            db_page.updateNote(idNote, strNote, strBody,  quantity, 0, false);
            marking = 0;
        }
        db_page.close();

        System.out.println("PageAdapter_recycler / _toggleNoteMarking / position = " + position + ", marking = " + db_page.getNoteMarking(position,true));
        return  marking;
    }

    @Override
    public void onItemDismiss(int position) {
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPos, int toPos) {
//        System.out.println("PageAdapter_recycler / _onItemMove / fromPos = " +
//                        fromPos + ", toPos = " + toPos);

        notifyItemMoved(fromPos, toPos);

        int oriStartPos = fromPos;
        int oriEndPos = toPos;

        mDb_page = new DB_page(mAct, TabsHost.getCurrentPageTableId());
        if(fromPos >= mDb_page.getNotesCount(true)) // avoid footer error
            return false;

        //reorder data base storage
        int loop = Math.abs(fromPos-toPos);
        for(int i=0;i< loop;i++)
        {
            swapRows(mDb_page, fromPos,toPos);
            if((fromPos-toPos) >0)
                toPos++;
            else
                toPos--;
        }

        // update footer
        TabsHost.showFooter(mAct);
        return true;
    }

    @Override
    public void onItemMoved(RecyclerView.ViewHolder sourceViewHolder, int fromPos, RecyclerView.ViewHolder targetViewHolder, int toPos) {
        System.out.println("PageAdapter_recycler / _onItemMoved");
        ((TextView)sourceViewHolder.itemView.findViewById(R.id.row_id)).setText(String.valueOf(toPos+1));
        ((TextView)targetViewHolder.itemView.findViewById(R.id.row_id)).setText(String.valueOf(fromPos+1));

        setBindViewHolder_listeners((ViewHolder)sourceViewHolder,toPos);
        setBindViewHolder_listeners((ViewHolder)targetViewHolder,fromPos);

	    updateDbCache();
    }

	// update list cache from DB
	public void updateDbCache() {
//        System.out.println("PageAdapter_recycler / _updateDbCache " );
		listCache = new ArrayList<>();

		int notesCount = getItemCount();
		mDb_page = new DB_page(mAct, page_table_id);
		mDb_page.open();
		for(int i=0;i<notesCount;i++) {
			Cursor cursor = mDb_page.mCursor_note;
			if (cursor.moveToPosition(i)) {
				Db_cache cache = new Db_cache();
				cache.title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_TITLE));
				cache.body = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_BODY));
				cache.quantity = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NOTE_QUANTITY));
				cache.marking = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NOTE_MARKING));

				listCache.add(cache);
			}
		}
		mDb_page.close();
	}

}