package com.cw.sumlist.operation.sum_pages;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.cw.sumlist.R;
import com.cw.sumlist.Utils;
import com.cw.sumlist.db.DB_folder;
import com.cw.sumlist.db.DB_page;
import com.cw.sumlist.util.ColorSet;
import com.cw.sumlist.util.preferences.Pref;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class SumPagesAdapter extends ArrayAdapter<String> {
	Activity act;
	View rootView;
	DB_folder dB_folder;
	private final List<String> gridStrList;
	List<Boolean> checkedTabs;

	public SumPagesAdapter(@NonNull Activity context, List<String> arrayList, View root_view, List<Boolean> _checkedTabs) {
		super(context, 0, arrayList);
		act = context;
		dB_folder = new DB_folder(act, Pref.getPref_focusView_folder_tableId(act));
		gridStrList = arrayList;
		rootView = root_view;
		checkedTabs = _checkedTabs;
	}

	@Override
	public int getCount() {
		return dB_folder.getPagesCount(true);
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		if(convertView == null) {
			LayoutInflater inflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.sum_pages_grid_item, parent,false);
		}

		// check box
		CheckBox chkBox = convertView.findViewById(R.id.checkBox);
		chkBox.setChecked(checkedTabs.get(position));

		chkBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// System.out.println("FolderSum_grid_list / _getView / position = " + position);

				checkedTabs.set(position, chkBox.isChecked());
				if (checkedTabs.get(position))
					SumPages.mChkNum++;
				else
					SumPages.mChkNum--;

				if (!chkBox.isChecked()) {
					CheckBox checkTvSelAll = rootView.findViewById(R.id.check_box_select_all_pages_sum_pages);
					checkTvSelAll.setChecked(false);
				}

				int pageTableId = dB_folder.getPageTableId(position, true);

				// set for contrast
				if (chkBox.isChecked())
					SumPages.folderSum += Utils.getPageSum(act, pageTableId);
				else
					SumPages.folderSum -= Utils.getPageSum(act, pageTableId);

				updateFolderSum(rootView);
			}
		});

		// set text view long click listener
		TextView chkTV = convertView.findViewById(R.id.itemText);
		chkTV.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				//Show page detail
				String message = getMessageByPagePosition(position);

				AlertDialog.Builder builder = new AlertDialog.Builder(act);
				builder.setTitle(R.string.dlg_day_list)
				.setMessage(message)
				.setNeutralButton(R.string.btn_OK, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which)
						{   // do nothing
						}})
				.show();

				return false;
			}
		});

		// show style
		int style = dB_folder.getPageStyle(position, true);
		chkTV.setBackgroundColor(ColorSet.mBG_ColorArray[style]);
		chkTV.setTextColor(ColorSet.mText_ColorArray[style]);

		// Show current page
		// workaround: set single line to true and add one space in front of the text
		if (dB_folder.getPageTableId(position, true) == Pref.getPref_focusView_page_tableId(act)) {
			chkTV.setTypeface(chkTV.getTypeface(), Typeface.BOLD_ITALIC);
			chkTV.setText(" " + gridStrList.get(position) + "*");
		} else
			chkTV.setText(" " + gridStrList.get(position));

		return convertView;
	}

	// update folder sum
	void updateFolderSum(View rootView){
		TextView textFolderSum = (TextView) rootView.findViewById(R.id.textFolderSum);
		String sum = String.valueOf(SumPages.folderSum);
		textFolderSum.setText(sum);
	}

	// show message of a given page position
	String getMessageByPagePosition(int position) {
//		System.out.println("FolderSum_grid / _showMessageByPagePosition / position = " + position);
		String message="- - - - - - - - - -\n";
		DB_folder mDb_folder = new DB_folder(act, Pref.getPref_focusView_folder_tableId(act));
		mDb_folder.open();
		int pageTableId = mDb_folder.getPageTableId(position,true);
		DB_page db_page = new DB_page(act,pageTableId);
		String title;
		int price,total = 0,marking;

		db_page.open();
		int count = db_page.getNotesCount(false);
		for(int i=0;i<count;i++) {
			title = db_page.getNoteTitle(i,false);
			price = db_page.getNoteBody(i,false);
			marking = db_page.getNoteMarking(i,false);

			if(marking==1) {
				// checked
				total += price;
				message = message.concat("[v] ")
						.concat(title).concat(" : ")
						.concat(String.valueOf(price));
			} else {
				// unchecked
				message = message.concat("[ ] ")
						.concat(title).concat(" : ")
						.concat(String.valueOf(price));
			}

			if(i==count-1) {
				message = message.concat("\n- - - - - - - - - -\n");
				message = message.concat(act.getString(R.string.footer_total)).concat(" : ");
				message = message.concat(String.valueOf(total));
			}
			else
				message = message.concat("\n");
		}
		db_page.close();

		return message;
	}

}