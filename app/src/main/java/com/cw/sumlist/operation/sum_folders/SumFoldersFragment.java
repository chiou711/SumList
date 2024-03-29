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

package com.cw.sumlist.operation.sum_folders;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.cw.sumlist.R;
import com.cw.sumlist.db.DB_drawer;
import com.cw.sumlist.db.DB_folder;
import com.cw.sumlist.main.MainAct;
import com.cw.sumlist.util.BaseBackPressedListener;
import com.cw.sumlist.util.preferences.Pref;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


public class SumFoldersFragment extends Fragment{
	Context mContext;
	TextView title;
	TextView sumText;
    ListView mListView;
    SumFolders sumFolders;
	View rootView;
    AppCompatActivity act;

	public SumFoldersFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mContext = act;
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.sum_folders_list, container, false);
        act = MainAct.mAct;

		// title
		title = (TextView) rootView.findViewById(R.id.select_list_title);
        title.setText(R.string.config_select_sum_folders_title);

        // list view: selecting which pages to send
        mListView = (ListView)rootView.findViewById(R.id.listView1);

        //show list for selection
        sumFolders = new SumFolders(act,rootView , mListView);

		// sum folder textview
		sumText = rootView.findViewById(R.id.textSumFolders);
		sumText.setText(String.valueOf(sumFolders.sumFolders));

        // cancel button
        Button btnSelPageCancel = (Button) rootView.findViewById(R.id.btnSelPageCancel);
		btnSelPageCancel.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);

        btnSelPageCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("DeleteFolders / _btnSelPageCancel");
                DB_drawer db_drawer = new DB_drawer(act);

                int focusFolder_tableId = Pref.getPref_focusView_folder_tableId(act);
                DB_folder db_folder = new DB_folder(act,focusFolder_tableId);
                int pagesCount = db_folder.getPagesCount(true);
                if((db_drawer.getFoldersCount(true) == 0) || (pagesCount == 0)){
                    System.out.println("DeleteFolders / _btnSelPageCancel / will call MainAct");
                    getActivity().finish();
                    Intent intent  = new Intent(act,MainAct.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getActivity().startActivity(intent);
                } else {
                    System.out.println("DeleteFolders / _btnSelPageCancel / will do _popBackStack");
                    act.getSupportFragmentManager().popBackStack();
                }
                // for pages count = 0 case
                // java.lang.IllegalArgumentException: No view found for id 0x1020011 (android:id/tabcontent) for fragment Page{8ac28af #0 id=0x1020011 tab1}
            }
        });

		((MainAct)act).setOnBackPressedListener(new BaseBackPressedListener(act));

		return rootView;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

}