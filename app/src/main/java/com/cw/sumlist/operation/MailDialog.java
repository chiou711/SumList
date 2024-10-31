package com.cw.sumlist.operation;

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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cw.sumlist.R;
import com.cw.sumlist.db.DB_category;
import com.cw.sumlist.db.DB_folder;
import com.cw.sumlist.db.DB_page;
import com.cw.sumlist.main.MainAct;
import com.cw.sumlist.operation.sum_pages.SumPages;
import com.cw.sumlist.util.Util;
import com.cw.sumlist.util.preferences.Pref;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.core.content.FileProvider;

public class MailDialog {
	EditText editEMailAddrText;
	SharedPreferences mPref_email;
	Intent mEMailIntent;
	Activity act;
	public MailDialog(Activity act) {
		this.act = act;
	}

	public void inputMailAddress(String mailTitle,String mailContent) {

		AlertDialog.Builder builder1;

		mPref_email = act.getSharedPreferences("email_addr", 0);
		editEMailAddrText = (EditText) act.getLayoutInflater()
				.inflate(R.layout.edit_text_dlg, null);
		builder1 = new AlertDialog.Builder(act);

		// get default email address
		String mDefaultEmailAddr = mPref_email.getString("KEY_DEFAULT_EMAIL_ADDR", "");
		editEMailAddrText.setText(mDefaultEmailAddr);

		builder1.setTitle(R.string.mail_notes_dlg_title)
				.setMessage(R.string.mail_notes_dlg_message)
				.setView(editEMailAddrText)
				.setNegativeButton(R.string.edit_note_button_back,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {/*cancel*/
								dialog.dismiss();
							}

						})
				.setPositiveButton(R.string.mail, null); //call override

		AlertDialog mDialog = builder1.create();
		mDialog.show();

		// override positive button
		Button enterButton = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
		enterButton.setOnClickListener(new Click2mail(act, mDialog,mailTitle,mailContent));

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

	// get summary title
	public String getSummayTitleString(){
		String summary_title;
		String folder_title = (String) MainAct.mFolderTitle;
		String title = folder_title.concat(" ( ").concat(String.valueOf(MainAct.folder_sum)).concat(" ) ");

		// summary title
		summary_title = act.getResources().getString(R.string.month_summary) +
				" : " + title + "\n";
		return summary_title;
	}

	// get summary string
	public String getSummaryString(){
		DB_folder dB_folder = new DB_folder(act , Pref.getPref_focusView_folder_tableId(act));
		int pages_count = dB_folder.getPagesCount(true);
		String summaryStr = "";

		DB_category db_category = new DB_category(act);
		int categoryCount =db_category.getCategoryCount(true);

		// category title array
		List<String> category_title_array = new ArrayList<>();
		for(int i=0;i<categoryCount;i++)
			category_title_array.add(db_category.getCategoryTitle(i,true));

		// category sum array
		List<Integer> category_sum = new ArrayList<>();
		for(int i=0;i<categoryCount;i++)
			category_sum.add(0);

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

					// note price * quantity
					int price = db_page.getNoteBody(j,false);
					int quantity = db_page.getNoteQuantity(j,false);
					summaryStr = summaryStr.concat(" = ");
					summaryStr = summaryStr.concat(String.valueOf(price));
					summaryStr = summaryStr.concat(" * ");
					summaryStr = summaryStr.concat(String.valueOf(quantity));

					// category title
					String cat_string = String.valueOf(db_page.getNoteCategory(j,false));
					summaryStr = summaryStr.concat(" (");
					summaryStr = summaryStr.concat(cat_string);
					summaryStr = summaryStr.concat(")");

					// get category sum
					for(int cat=0;cat<categoryCount;cat++){
						if(cat_string.contains(category_title_array.get(cat)))
							category_sum.set(cat, category_sum.get(cat) + price*quantity);
					}

					summaryStr = summaryStr.concat("\n");
				}
				db_page.close();

				summaryStr = summaryStr.concat("\n");
			}
		}

		// set header: category item sum
		String header =act.getResources().getString(R.string.category_ratio);
		for(int cat=0;cat<categoryCount;cat++){
			int categorySum = category_sum.get(cat);
			double ratio = Math.round( categorySum*100.0/MainAct.folder_sum);
			String ratioStr = String.valueOf((int)ratio);

			header = header.concat("\n")
					.concat(" - ")
					.concat(category_title_array.get(cat))
					.concat(" : ")
					.concat(ratioStr)
					.concat("%")
					.concat(" (")
					.concat(String.valueOf(categorySum))
					.concat(")");
		}

		// final summary
		summaryStr = header.concat("\n").concat("\n")
				.concat(summaryStr);

		return summaryStr;
	}

	// get sum pages title
	public String getSumPagesTitleString(){
		String sumPages_title;

//		String folder_title = (String) MainAct.mFolderTitle;
//		String title = folder_title.concat(" ( ").concat(String.valueOf(MainAct.folder_sum)).concat(" ) ");

		String title = (String) MainAct.mFolderTitle;

		// summary title
		sumPages_title = act.getResources().getString(R.string.sum_pages) +
				" : " + title + "\n";
		return sumPages_title;
	}

	// get Sum pages string
	public String getSumPagesContentString(){
		String sumPagesStr = "";
		int length = SumPages.checkedTabs.size();

		DB_folder dB_folder = new DB_folder(act, Pref.getPref_focusView_folder_tableId(act));
		dB_folder.open();
		int sum_pages = 0;
		for (int i = 0; i < length; i++) {
			if(SumPages.checkedTabs.get(i)) {
				sumPagesStr = sumPagesStr.concat(dB_folder.getPageTitle(i, false));

				DB_page db_page = new DB_page(act, dB_folder.getPageTableId(i,false));

				sumPagesStr = sumPagesStr.concat("\n");

				// title, price, selected, quantity
				db_page.open();
				int page_sum = 0;
				for(int j=0;j<db_page.getNotesCount(false);j++) {
					int selected = db_page.getNoteMarking(j,false);

					String title = db_page.getNoteTitle(j, false);

					// show selected or not
					if(selected != 1)
						title = "(x) ".concat(title);

					sumPagesStr = sumPagesStr.concat(title);
					sumPagesStr = sumPagesStr.concat(" ");

					int price = db_page.getNoteBody(j, false);

					int quantity = db_page.getNoteQuantity(j, false);
					sumPagesStr = sumPagesStr.concat(String.valueOf(price));
					sumPagesStr = sumPagesStr.concat("*");
					sumPagesStr = sumPagesStr.concat(String.valueOf(quantity));
					sumPagesStr = sumPagesStr.concat("\n");

					// count selected items
					if(selected==1)
						page_sum += price * quantity;
				}
				db_page.close();

				sumPagesStr = sumPagesStr.concat("page sum = ");
				sumPagesStr = sumPagesStr.concat(String.valueOf(page_sum));
				sumPagesStr = sumPagesStr.concat("\n\n");

				sum_pages += page_sum;
			}
		}
		dB_folder.close();

		String finalTag = "--- Sum of selected items ---\n => ";
		sumPagesStr = sumPagesStr.concat(finalTag);
		sumPagesStr = sumPagesStr.concat(String.valueOf(sum_pages));
		return sumPagesStr;
	}

	class Click2mail implements View.OnClickListener {
		private final Dialog dialog;
		Activity act;
		String mailTitle,mailContent;

		public Click2mail(Activity _act, Dialog dialog,String _mailTitle,String _mailContent) {
			this.dialog = dialog;
			this.act = _act;
			mailTitle = _mailTitle;
			mailContent = _mailContent;
		}

		@Override
		public void onClick(View v) {
			String[] attachmentFileName = {""};
			String strEMailAddr = editEMailAddrText.getText().toString();
			if (strEMailAddr.length() > 0) {
				Bundle extras = act.getIntent().getExtras();

				// default file name: with tab title
				String defaultFileName = "SumList_summary";
				attachmentFileName[0] = defaultFileName + "_" +
						Util.getCurrentTimeString() + // time
						".txt"; // extension name

				System.out.println("--- attachment file name = " + attachmentFileName[0]);
				Util util = new Util(act);

				String textFileBody = mailTitle.concat("-------------\n").concat(mailContent);
				if (extras == null) {
					// TXT file
					util.exportToSdCardFile(attachmentFileName[0], // attachment name
							textFileBody); // sent string
				}

				mPref_email.edit().putString("KEY_DEFAULT_EMAIL_ADDR", strEMailAddr).apply();

				// call next dialog
				sendMail(act,
						strEMailAddr,  // eMail address
						attachmentFileName, // attachment file name
						mailTitle,
						mailContent
				);
				dialog.dismiss();
			} else {
				Toast.makeText(act,
						"No email address",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	// Send e-Mail : send file by e-Mail
	public static String[] mAttachmentFileName;
	void sendMail(Activity act,
	              String strEMailAddr,  // eMail address
	              String[] attachmentFileName,
	              String mailTitle,
	              String mailContent)
	{
		mAttachmentFileName = attachmentFileName;
		// new ACTION_SEND intent
		mEMailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE); // for multiple attachments

		// set type
		mEMailIntent.setType("text/plain");//can select which APP will be used to send mail

		// open issue: cause warning for Key android.intent.extra.TEXT expected ArrayList
		String text_body = mailTitle.concat("-------------\n").concat(mailContent);

		// attachment: message
		List<String> filePaths = new ArrayList<String>();
		for (String s : attachmentFileName) {
			String messagePath = Objects.requireNonNull(act.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)).toString() +
					"/" +
					s;// message file name
			filePaths.add(messagePath);
		}

		ArrayList<Uri> uris = new ArrayList<Uri>();
		Uri uri = null;
		for (String file : filePaths) {
			if (mEMailIntent.resolveActivity(act.getPackageManager()) != null) {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
					uri = Uri.fromFile(new File(file));
				} else {
					uri = FileProvider.getUriForFile(act, act.getPackageName()
//							+ ".operation"
							, new File(file));
				}
			}

			uris.add(uri);
		}

		mEMailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{strEMailAddr}) // eMail address
				.putExtra(Intent.EXTRA_SUBJECT,mailTitle)// eMail subject
				.putExtra(Intent.EXTRA_TEXT,text_body) // eMail body (open issue)
				.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris ); // multiple eMail attachment

		act.startActivity(Intent.createChooser(mEMailIntent,
				"mail_chooser_title") );
	}

}
