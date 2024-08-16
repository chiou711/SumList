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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.cw.sumlist.R;
import com.cw.sumlist.main.MainAct;
import com.cw.sumlist.operation.MailDialog;
import com.cw.sumlist.util.BaseBackPressedListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class SumPagesFragment extends Fragment{
	AppCompatActivity act;
	public View rootView;
    TextView title;
	SumPages sum_pages;
    TextView textFolderSum;
	Button mailBtn;

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
		rootView = inflater.inflate(R.layout.sum_pages, container, false);
        act = MainAct.mAct;

		gridview_sumlist = rootView.findViewById(R.id.sum_pages_grid_view);

        // title
        title = (TextView) rootView.findViewById(R.id.select_list_title);
        title.setText(R.string.sum_pages_title);

        // folder sum
        textFolderSum = (TextView) rootView.findViewById(R.id.textFolderSum);

		((MainAct)act).setOnBackPressedListener(new BaseBackPressedListener(act));

		// mail button
		mailBtn = rootView.findViewById(R.id.mail_pages);
		mailBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// mail dialog
				MailDialog mailDialog = new MailDialog(requireActivity());
				mailDialog.inputMailAddress(mailDialog.getSumPagesTitleString(),
											mailDialog.getSumPagesContentString());
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

}