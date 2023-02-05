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

package com.cw.sumlist.config;


import android.app.Activity;
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

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

public class MonthSummary extends Fragment
{

	static View mRootView;
	Activity act;
	public MonthSummary(){}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		System.out.println("================ About / onCreateView ==================");
		act = getActivity();

		mRootView = inflater.inflate(R.layout.month_summary, container, false);
		showSummaryDialog();

		// set Back pressed listener
		((MainAct)act).setOnBackPressedListener(new BaseBackPressedListener(MainAct.mAct));

		return mRootView;
	}

	String summaryString;
	String msgStr;
	// Show summary dialog
	void showSummaryDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		String title = (String) MainAct.mFolderTitle;
		title = title.concat(" ( ").concat(String.valueOf(MainAct.folder_sum)).concat(" ) ");
        msgStr = act.getResources().getString(R.string.month_summary) +
                        " : " + title + "\n" ;
		summaryString = getSummaryString();

	    builder.setTitle(msgStr)
	          .setMessage(summaryString)
			  .setNegativeButton(R.string.notices_close, (dialog1, which1) ->
					  getActivity().getSupportFragmentManager().popBackStack())
			  .setPositiveButton(R.string.mail, new DialogInterface.OnClickListener() {
					  @Override
					  public void onClick(DialogInterface dialog2, int which2) {
						  // mail
						  inputEMailDialog();
					  }
			  })
			  .show();
	}

	// get summary string
	public String getSummaryString(){
		DB_folder dB_folder = new DB_folder(act , Pref.getPref_focusView_folder_tableId(act));
		int pages_count = dB_folder.getPagesCount(true);
		String summaryStr = "";

		for (int i = 0; i < pages_count; i++) {
			// page title
			String pageTitle = dB_folder.getPageTitle(i,true);

			DB_page db_page = new DB_page(act,dB_folder.getPageTableId(i,true));
			int notes_count = db_page.getNotesCount(true);

			// notes
			if(notes_count > 0) {
				// page title
				summaryStr = summaryStr.concat(pageTitle);
				summaryStr = summaryStr.concat("\n");

				db_page.open();
				for(int j=0; j< notes_count;j++) {
					// note mark
					if(db_page.getNoteMarking(j,false) == 0)
						summaryStr = summaryStr.concat("\t").concat("? ");
					else
						summaryStr = summaryStr.concat("\t");

					// note title
					summaryStr = summaryStr.concat(db_page.getNoteTitle(j,false));

					// note quantity
					summaryStr = summaryStr.concat(" x ");
					summaryStr = summaryStr.concat(String
							.valueOf(db_page.getNoteQuantity(j,false)));

					// note price
					summaryStr = summaryStr.concat(" = ");
					summaryStr = summaryStr.concat(String.valueOf(db_page.getNoteBody(j,false)));
					summaryStr = summaryStr.concat("\n");
				}
				db_page.close();

				summaryStr = summaryStr.concat("\n");
			}
		}
		return summaryStr;
	}


	void inputEMailDialog()
	{
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

	EditText editEMailAddrText;
	SharedPreferences mPref_email;
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

				if(extras == null){
					// TXT file
					util.exportToSdCardFile(attachmentFileName[0], // attachment name
							summaryString); // sent string
				}

				mPref_email.edit().putString("KEY_DEFAULT_EMAIL_ADDR", strEMailAddr).apply();

				// call next dialog
				sendEMail(strEMailAddr,  // eMail address
						attachmentFileName // attachment file name
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
	               String[] attachmentFileName)
	{
		mAttachmentFileName = attachmentFileName;
		// new ACTION_SEND intent
		mEMailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE); // for multiple attachments

		// set type
		mEMailIntent.setType("text/plain");//can select which APP will be used to send mail

		// open issue: cause warning for Key android.intent.extra.TEXT expected ArrayList
		String text_body = msgStr.concat("-------------\n").concat(summaryString);

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
				.putExtra(Intent.EXTRA_TEXT,text_body) // eMail body (open issue)
				.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris ); // multiple eMail attachment

		getActivity().startActivity(Intent.createChooser(mEMailIntent,
						"mail_chooser_title") );
	}

}