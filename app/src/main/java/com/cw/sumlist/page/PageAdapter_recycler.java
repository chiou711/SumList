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

import android.graphics.Color;
import android.graphics.Paint;
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
        implements ItemTouchHelperAdapter {
	private AppCompatActivity mAct;
	private String strTitle;
	private String strBody;
	private Integer quantity;
	private Integer marking;
	private int style;
	DB_folder dbFolder;
	private int page_pos;
	private final OnStartDragListener mDragStartListener;
	DB_page mDb_page;
	int page_table_id;
	List<Db_cache> listCache;
	int QTY_ZERO_COLOR;

	PageAdapter_recycler(int pagePos, int pageTableId, OnStartDragListener dragStartListener) {
		mAct = MainAct.mAct;
		mDragStartListener = dragStartListener;

		dbFolder = new DB_folder(mAct, Pref.getPref_focusView_folder_tableId(mAct));
		page_pos = pagePos;
		page_table_id = pageTableId;

		QTY_ZERO_COLOR = Color.rgb(77, 77, 77);

		updateDbCache();
	}

	/**
	 * Provide a reference to the type of views that you are using (custom ViewHolder)
	 */
	public class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
		View controls;
		ImageViewCustom btnDrag;
		ImageView btnMarking;
		ImageView btnViewNote;
		ImageView btnEditNote;
		TextView rowId;
		TextView textTitle;
		TextView textBody;
		TextView textQuantity;

		public ViewHolder(View v) {
			super(v);

			// Define click listener for the ViewHolder's View.
			v.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
				}
			});

			controls = v.findViewById(R.id.row_controls);
			btnMarking = v.findViewById(R.id.btn_marking);
			btnViewNote = v.findViewById(R.id.btn_view_note);
			btnEditNote = v.findViewById(R.id.btn_edit_note);
			btnDrag = v.findViewById(R.id.btn_drag);

			rowId = v.findViewById(R.id.row_id);
			textTitle = v.findViewById(R.id.row_title);
			textBody = v.findViewById(R.id.row_body);
			textQuantity = v.findViewById(R.id.row_quantity);
		}

		public TextView getItemIdView() {
			return rowId;
		}

		public TextView getTitleView() {
			return textTitle;
		}

		public TextView getBodyView() {
			return textBody;
		}

		public TextView getQuantityView() {
			return textQuantity;
		}

		int selectColor;
		@Override
		public void onItemSelected() {
//            itemView.setBackgroundColor(Color.LTGRAY);
			selectColor = ((CardView) itemView).getCardBackgroundColor().getDefaultColor();
			((CardView) itemView).setCardBackgroundColor(MainAct.mAct.getResources().getColor(R.color.highlight_color));
		}

		@Override
		public void onItemClear() {
			((CardView) itemView).setCardBackgroundColor(selectColor);
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

        System.out.println("PageAdapter_recycler / _onBindViewHolder / position = " + _position);

		int position = holder.getAdapterPosition();

		SharedPreferences pref_show_note_attribute = MainAct.mAct.getSharedPreferences("show_note_attribute", 0);

		// get DB data
		// add check to avoid exception during Copy/Move checked
//        System.out.println("PageAdapter / _onBindViewHolder / listCache.size() = " + listCache.size());
		if ((listCache != null) &&
			(listCache.size() > 0) &&
			(position != listCache.size())) {
			strTitle = listCache.get(position).title;
			strBody = listCache.get(position).body;
			quantity = listCache.get(position).quantity;
			marking = listCache.get(position).marking;
		} else {
			strTitle = "";
			strBody = "";
			quantity = 0;
			marking = 0;
		}

		// style
		style = dbFolder.getPageStyle(page_pos, true);

		// show checked only or not
		if(pref_show_note_attribute.getString("KEY_SHOW_CHECKED_ONLY", "no").equalsIgnoreCase("yes")) {
			if (marking == 0) {
				holder.itemView.setVisibility(View.GONE);
				// hide unchecked item of recycler view
				holder.itemView.setLayoutParams(new RecyclerView
						.LayoutParams(0, 0));
			}
			else {
				holder.itemView.setVisibility(View.VISIBLE);
				// cf https://stackoverflow.com/questions/41223413/how-to-hide-an-item-from-recycler-view-on-a-particular-condition
				holder.itemView.setLayoutParams(new RecyclerView
						.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
									  ViewGroup.LayoutParams.WRAP_CONTENT));
			}
		}

		if (quantity > 0)
			((CardView) holder.itemView).setCardBackgroundColor(ColorSet.mBG_ColorArray[style]);
		else
			((CardView) holder.itemView).setCardBackgroundColor(QTY_ZERO_COLOR);

		// expand card view or not
		SharedPreferences expand_card_view = MainAct.mAct.getSharedPreferences("show_note_attribute", 0);
		boolean bExpand = expand_card_view.getBoolean("KEY_EXPAND_CARD_VIEW", true);
		if (bExpand)
			holder.controls.setVisibility(View.VISIBLE);
		else
			holder.controls.setVisibility(View.GONE);

		/**
		 *  control block
		 */
		// show row Id
		holder.rowId.setText(String.valueOf(position + 1));

		// show marking check box
		if (marking == 1) {
			holder.btnMarking.setBackgroundResource(style % 2 == 1 ?
					R.drawable.btn_check_on_holo_light :
					R.drawable.btn_check_on_holo_dark);
		} else {
			holder.btnMarking.setBackgroundResource(style % 2 == 1 ?
					R.drawable.btn_check_off_holo_light :
					R.drawable.btn_check_off_holo_dark);
		}

		// set strike through
		setTextStrikeThrough(holder);

		// set Text Color;
		setTextColor(holder);

		// show drag button
		if (pref_show_note_attribute.getString("KEY_ENABLE_DRAGGABLE", "yes").equalsIgnoreCase("yes"))
			holder.btnDrag.setVisibility(View.VISIBLE);
		else
			holder.btnDrag.setVisibility(View.GONE);

		// show text title
		if (Util.isEmptyString(strTitle)) {
			// make sure empty title is empty after scrolling
			holder.textTitle.setVisibility(View.VISIBLE);
			holder.textTitle.setText("");
		} else {
			holder.textTitle.setVisibility(View.VISIBLE);
			holder.textTitle.setTextSize((float) 18.00);
			holder.textTitle.setText(strTitle);
		}

		// text body
		if (Util.isEmptyString(strBody)) {
			// make sure empty title is empty after scrolling
			holder.textBody.setVisibility(View.VISIBLE);
			holder.textBody.setText("");
		} else {
			holder.textBody.setVisibility(View.VISIBLE);
			holder.textBody.setText(strBody);
			holder.textBody.setTextSize((float) 18.00);
		}

		// text quantity
		if (Util.isEmptyString(String.valueOf(quantity))) {
			// make sure empty title is empty after scrolling
			holder.textQuantity.setVisibility(View.VISIBLE);
			holder.textQuantity.setText("");
		} else {
			holder.textQuantity.setVisibility(View.VISIBLE);
			holder.textQuantity.setText("x" + String.valueOf(quantity));
			holder.textQuantity.setTextSize((float) 11.00);
		}

		setBindViewHolder_listeners(holder, position);
	}


	/**
	 * Set bind view holder listeners
	 *
	 * @param viewHolder
	 * @param position
	 */
	void setBindViewHolder_listeners(ViewHolder viewHolder, final int position) {
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
				toggleNoteMarking(mAct, position);

				updateDbCache();

				//Toggle marking will resume page, so do Store v scroll
				RecyclerView listView = TabsHost.mTabsPagerAdapter.fragmentList.get(TabsHost.getFocus_tabPos()).recyclerView;
				TabsHost.store_listView_vScroll(listView);

				notifyDataSetChanged();

				TabsHost.showFooter(MainAct.mAct);
			}
		});

		// item click
		viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				viewNote(position);
			}
		});

		// item long click
		viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				editNote(position);
				return false;
			}
		});

		// on view note
		viewHolder.btnViewNote.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				viewNote(position);
			}
		});

		// on edit note
		viewHolder.btnEditNote.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				editNote(position);
			}
		});

		// Start a drag whenever the handle view it touched
		viewHolder.btnDrag.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				System.out.println("PageAdapter_recycler / onTouch / event.getActionMasked() = " + event.getActionMasked());

				switch (event.getActionMasked()) {

					case MotionEvent.ACTION_DOWN:
						System.out.println("PageAdapter_recycler / onTouch / ACTION_DOWN");
						mDragStartListener.onStartDrag(viewHolder);
						return true;
					case MotionEvent.ACTION_UP:
//						v.performClick();
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
		return mDb_page.getNotesCount(true);
	}

	// toggle mark of note
	public static int toggleNoteMarking(AppCompatActivity mAct, int position) {
		int marking = 0;
		DB_page db_page = new DB_page(mAct, TabsHost.getCurrentPageTableId());
		db_page.open();
		int count = db_page.getNotesCount(false);
		if (position >= count) //end of list
		{
			db_page.close();
			return marking;
		}

		String strNote = db_page.getNoteTitle(position, false);
		int strBody = db_page.getNoteBody(position, false);
		Integer quantity = db_page.getNoteQuantity(position, false);
		Long idNote = db_page.getNoteId(position, false);

		// toggle the marking
		if (db_page.getNoteMarking(position, false) == 0) {
			db_page.updateNote(idNote, strNote, strBody, quantity, 1, false);
			marking = 1;
		} else {
			db_page.updateNote(idNote, strNote, strBody, quantity, 0, false);
			marking = 0;
		}
		db_page.close();

		System.out.println("PageAdapter_recycler / _toggleNoteMarking / position = " + position + ", marking = " + db_page.getNoteMarking(position, true));
		return marking;
	}

	@Override
	public void onItemDismiss(int position) {
		System.out.println("PageAdapter_recycler / _onItemDismiss");
		notifyItemRemoved(position);

//		mDb_page.deleteNote(mDb_page.getNoteId(position,true),true);
	}

	@Override
	public boolean onItemMove(int fromPos, int toPos) {
//        System.out.println("PageAdapter_recycler / _onItemMove / fromPos = " +
//                        fromPos + ", toPos = " + toPos);

		notifyItemMoved(fromPos, toPos);

		int oriStartPos = fromPos;
		int oriEndPos = toPos;

		mDb_page = new DB_page(mAct, TabsHost.getCurrentPageTableId());
		if (fromPos >= mDb_page.getNotesCount(true)) // avoid footer error
			return false;

		//reorder data base storage
		int loop = Math.abs(fromPos - toPos);
		for (int i = 0; i < loop; i++) {
			swapRows(mDb_page, fromPos, toPos);
			if ((fromPos - toPos) > 0)
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
		((TextView) sourceViewHolder.itemView.findViewById(R.id.row_id)).setText(String.valueOf(toPos + 1));
		((TextView) targetViewHolder.itemView.findViewById(R.id.row_id)).setText(String.valueOf(fromPos + 1));

		setBindViewHolder_listeners((ViewHolder) sourceViewHolder, toPos);
		setBindViewHolder_listeners((ViewHolder) targetViewHolder, fromPos);

		updateDbCache();

		notifyDataSetChanged();
	}

	// update list cache from DB
	public void updateDbCache() {
//        System.out.println("PageAdapter_recycler / _updateDbCache " );
		listCache = new ArrayList<>();

		int notesCount = getItemCount();
		mDb_page = new DB_page(mAct, page_table_id);
		mDb_page.open();
		for (int i = 0; i < notesCount; i++) {
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

	// view note
	void viewNote(int position) {
		TabsHost.getCurrentPage().mCurrPlayPosition = position;
		DB_page db_page = new DB_page(mAct, TabsHost.getCurrentPageTableId());
		int count = db_page.getNotesCount(true);
		if (position < count) {
			// apply Note class
			Intent intent;
			intent = new Intent(mAct, Note.class);
			intent.putExtra("POSITION", position);
			mAct.startActivity(intent);
		}
	}

	// edit note
	void editNote(int position) {
		DB_page db_page = new DB_page(mAct, TabsHost.getCurrentPageTableId());
		Long rowId = db_page.getNoteId(position, true);

		Intent i = new Intent(mAct, Note_edit.class);
		i.putExtra("list_view_position", position);
		i.putExtra(DB_page.KEY_NOTE_ID, rowId);
		i.putExtra(DB_page.KEY_NOTE_TITLE, db_page.getNoteTitle_byId(rowId));
		i.putExtra(DB_page.KEY_NOTE_BODY, db_page.getNoteBody_byId(rowId));
		i.putExtra(DB_page.KEY_NOTE_QUANTITY, db_page.getNoteQuantity_byId(rowId));
		mAct.startActivity(i);
	}

	// set strike through
	void setTextStrikeThrough(ViewHolder holder) {
		setTextView_strikeThrough(holder.getItemIdView());
		setTextView_strikeThrough(holder.getTitleView());
		setTextView_strikeThrough(holder.getBodyView());
		setTextView_strikeThrough(holder.getQuantityView());
	}

	// set text view strike through
	void setTextView_strikeThrough(TextView textView) {
		if (marking == 0)
			textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		else
			textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
	}

	// set text color
	void setTextColor(ViewHolder holder) {
		setTextView_color(holder.getItemIdView());
		setTextView_color(holder.getTitleView());
		setTextView_color(holder.getBodyView());
		setTextView_color(holder.getQuantityView());
	}

	// set text view color
	void setTextView_color(TextView textView) {
		textView.setTextColor(ColorSet.mText_ColorArray[style]);
	}
}