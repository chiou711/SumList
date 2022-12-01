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

package com.cw.sumlist.config;


import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.cw.sumlist.R;
import com.cw.sumlist.main.MainAct;
import com.cw.sumlist.util.BaseBackPressedListener;

import androidx.fragment.app.Fragment;
import de.psdev.licensesdialog.LicensesDialogFragment;

public class MonthSummary extends Fragment
{

	public MonthSummary(){}
	static View mRootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		System.out.println("================ About / onCreateView ==================");

		mRootView = inflater.inflate(R.layout.month_summary, container, false);

		aboutDialog();

		// set Back pressed listener
		((MainAct)getActivity()).setOnBackPressedListener(new BaseBackPressedListener(MainAct.mAct));

		return mRootView;
	}

    // About dialog
	void aboutDialog()
	{
		   AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		   PackageInfo pInfo = null;
		   String version_name = "NA";
		   int version_code = 0;
           try 
           {
        	   Context context = getActivity();
        	   pInfo = context.getPackageManager()
        			   		  .getPackageInfo(context.getPackageName(),PackageManager.GET_META_DATA);
           } catch (NameNotFoundException e) {
        	   e.printStackTrace();
           }

           if(pInfo != null)
           {
        	   version_name = pInfo.versionName;
        	   version_code = pInfo.versionCode;
           }
           String msgStr = getActivity().getResources().getString(R.string.about_version_name) +
        		   			" : " + version_name + "\n" + 
        		   		   getActivity().getResources().getString(R.string.about_version_code) +
           					" : " + version_code + "\n\n" + 
   		   				   getActivity().getResources().getString(R.string.EULA_string);
           
		   builder.setTitle(R.string.about_version)
		   		  .setMessage(msgStr)
				  .setNegativeButton(R.string.notices_close, (dialog1, which1) ->
						  getActivity().getSupportFragmentManager().popBackStack())
				  .show();
	}
}