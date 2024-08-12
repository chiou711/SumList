/*
 * Copyright (C) 2023 CW Chiu
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

package com.cw.sumlist.operation.month_summary;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cw.sumlist.R;
import com.cw.sumlist.main.MainAct;
import com.cw.sumlist.util.BaseBackPressedListener;

import androidx.fragment.app.Fragment;

public class MonthSummary extends Fragment {

	static View mRootView;
	Activity act;
	TextView summary_text_view;
	Button backBtn,mailBtn;
	String summary_content;
	String summary_title;
	String text;

	public MonthSummary(){}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		System.out.println("================ MonthSummary / onCreateView ==================");
		act = getActivity();

		mRootView = inflater.inflate(R.layout.month_summary, container, false);

		MailDialog_monthSummary mailDialog = new MailDialog_monthSummary(requireActivity());

		// summary content
		summary_content = mailDialog.summary_content;
		summary_title = mailDialog.summary_title;

		// summary text view
		text = summary_title.concat("\n").concat(summary_content);
		summary_text_view = mRootView.findViewById(R.id.month_summary_text);
		summary_text_view.setText(text);

		// back button
		backBtn = mRootView.findViewById(R.id.month_summary_back);
		backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getActivity().getSupportFragmentManager().popBackStack();
			}
		});

		// mail button
		mailBtn = mRootView.findViewById(R.id.month_summary_mail);
		mailBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// mail
				mailDialog.inputMailAddress(requireActivity());
			}
		});

		// set Back pressed listener
		((MainAct)act).setOnBackPressedListener(new BaseBackPressedListener(MainAct.mAct));

		return mRootView;
	}

}