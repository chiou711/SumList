/*
 * Copyright (C) 2022 CW Chiu
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

package com.cw.sumlist.operation.sum_pages;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.cw.sumlist.R;
import com.cw.sumlist.db.DB_folder;
import com.cw.sumlist.db.DB_page;
import com.cw.sumlist.main.MainAct;
import com.cw.sumlist.util.BaseBackPressedListener;
import com.cw.sumlist.util.Util;
import com.cw.sumlist.util.preferences.Pref;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

public class SumPagesFragment extends Fragment{
	AppCompatActivity act;
	public View rootView;
    TextView title;
	SumPages sum_pages;
    TextView textFolderSum;
	Button mailBtn;

	SharedPreferences mPref_email;
	EditText editEMailAddrText;

	public SumPagesFragment(){
	}

	GridView gridview_sumlist;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.folder_sum, container, false);
        act = MainAct.mAct;

		gridview_sumlist = rootView.findViewById(R.id.folder_sum_grid_view);

        // title
        title = (TextView) rootView.findViewById(R.id.select_list_title);
        title.setText(R.string.folder_sum_title);

        // folder sum
        textFolderSum = (TextView) rootView.findViewById(R.id.textFolderSum);

		((MainAct)act).setOnBackPressedListener(new BaseBackPressedListener(act));

		// mail button
		mailBtn = rootView.findViewById(R.id.mail_pages);
		mailBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// mail
				inputEMailDialog();
			}
		});
		return rootView;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

    @Override
    public void onResume() {
        super.onResume();

        //show list for selection
	    if(sum_pages == null)
            sum_pages = new SumPages(act,rootView , gridview_sumlist, MainAct.pageSumArr);
    }

	void inputEMailDialog()	{
		AlertDialog.Builder builder1;

		mPref_email = getActivity().getSharedPreferences("email_addr", 0);
		editEMailAddrText = (EditText)getActivity().getLayoutInflater()
				.inflate(R.layout.edit_text_dlg, null);
		builder1 = new AlertDialog.Builder(getActivity());

		// get default email address
		String mDefaultEmailAddr = mPref_email.getString("KEY_DEFAULT_EMAIL_ADDR","");
		editEMailAddrText.setText(mDefaultEmailAddr);

		builder1.setTitle(R.string.mail_notes_dlg_title)
				.setMessage(R.string.mail_notes_dlg_message)
				.setView(editEMailAddrText)
				.setNegativeButton(R.string.edit_note_button_back,
						new DialogInterface.OnClickListener()
						{   @Override
						public void onClick(DialogInterface dialog, int which)
						{/*cancel*/
							dialog.dismiss();
						}

						})
				.setPositiveButton(R.string.mail, null); //call override

		AlertDialog mDialog = builder1.create();
		mDialog.show();

		// override positive button
		Button enterButton = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
		enterButton.setOnClickListener(new CustomListener(mDialog));

		// back
		mDialog.setOnKeyListener(new Dialog.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface arg0, int keyCode,
			                     KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					mDialog.dismiss();
					return true;
				}
				return false;
			}
		});
	}

	Intent mEMailIntent;
	//for keeping dialog when eMail address is empty
	class CustomListener implements View.OnClickListener
	{
		private final Dialog dialog;
		public CustomListener(Dialog dialog){
			this.dialog = dialog;
		}

		@Override
		public void onClick(View v){
			String[] attachmentFileName={""};
			String strEMailAddr = editEMailAddrText.getText().toString();

			if(strEMailAddr.length() > 0)
			{
				Bundle extras = getActivity().getIntent().getExtras();

				// default file name: with tab title
				String defaultFileName = "SumList_summary";
				attachmentFileName[0] = defaultFileName + "_" +
						Util.getCurrentTimeString() + // time
						".txt"; // extension name

				System.out.println("--- attachment file name = " + attachmentFileName[0]);
				Util util = new Util(getActivity());
				String summary = getSummary_content();

				if(extras == null){
					// TXT file
					util.exportToSdCardFile(attachmentFileName[0], // attachment name
							summary); // sent string
				}

				mPref_email.edit().putString("KEY_DEFAULT_EMAIL_ADDR", strEMailAddr).apply();

				// call next dialog
				sendEMail(strEMailAddr,  // eMail address
						attachmentFileName, // attachment file name
						summary
				);
				dialog.dismiss();
			}
			else
			{
				Toast.makeText(getActivity(),
						"No email address",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	// Send e-Mail : send file by e-Mail
	public static String[] mAttachmentFileName;
	void sendEMail(String strEMailAddr,  // eMail address
	               String[] attachmentFileName,
	               String summary)
	{
		mAttachmentFileName = attachmentFileName;
		// new ACTION_SEND intent
		mEMailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE); // for multiple attachments

		// set type
		mEMailIntent.setType("text/plain");//can select which APP will be used to send mail

		// attachment: message
		List<String> filePaths = new ArrayList<String>();
		for(int i=0;i<attachmentFileName.length;i++){
			String messagePath = act.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() +
					"/" +
					attachmentFileName[i];// message file name
			filePaths.add(messagePath);
		}

		ArrayList<Uri> uris = new ArrayList<Uri>();
		Uri uri = null;
		for (String file : filePaths) {
			if (mEMailIntent.resolveActivity(act.getPackageManager()) != null) {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
					uri = Uri.fromFile(new File(file));
				} else {
					uri = FileProvider.getUriForFile(act, getContext().getPackageName() + ".MonthSummary" , new File(file));
				}
			}

			uris.add(uri);
		}

		mEMailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{strEMailAddr}) // eMail address
				.putExtra(Intent.EXTRA_SUBJECT,"Mail SumList summary" )// eMail subject
				.putExtra(Intent.EXTRA_TEXT,summary) // eMail body (open issue)
				.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris ); // multiple eMail attachment

		getActivity().startActivity(Intent.createChooser(mEMailIntent,
				"mail_chooser_title") );
	}

	// get summary content string
	String getSummary_content(){
		String summary = "";
		int length = SumPages.checkedTabs.size();
		DB_folder dB_folder = new DB_folder(act, Pref.getPref_focusView_folder_tableId(act));
		dB_folder.open();
		int folder_sum = 0;
		for (int i = 0; i < length; i++) {
			if(SumPages.checkedTabs.get(i)) {
				summary = summary.concat(dB_folder.getPageTitle(i, false));

				DB_page db_page = new DB_page(act, dB_folder.getPageTableId(i,false));

				summary = summary.concat("\n");

				// note title, note body
				db_page.open();
				int page_sum = 0;
				for(int j=0;j<db_page.getNotesCount(false);j++) {
					String title = db_page.getNoteTitle(j, false);
					summary = summary.concat(title);
					summary = summary.concat(" ");
					int price = db_page.getNoteBody(j,false);
					int quantity = db_page.getNoteQuantity(j,false);
					summary = summary.concat(String.valueOf(price));
					summary = summary.concat("*");
					summary = summary.concat(String.valueOf(quantity));
					summary = summary.concat("\n");

					page_sum += price*quantity;
				}
				db_page.close();

				summary = summary.concat("page sum = ");
				summary = summary.concat(String.valueOf(page_sum));
				summary = summary.concat("\n");

				folder_sum += page_sum;
			}
			summary = summary.concat("\n");
		}
		dB_folder.close();

		summary = summary.concat("Sum of selected items = ");
		summary = summary.concat(String.valueOf(folder_sum));
		return summary;
	}
}