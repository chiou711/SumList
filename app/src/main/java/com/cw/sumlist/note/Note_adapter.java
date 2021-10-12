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

package com.cw.sumlist.note;

import com.cw.sumlist.R;
import com.cw.sumlist.db.DB_page;
import com.cw.sumlist.tabs.TabsHost;
import com.cw.sumlist.util.ColorSet;
import com.cw.sumlist.util.CustomWebView;
import com.cw.sumlist.util.Util;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Layout.Alignment;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class Note_adapter extends FragmentStatePagerAdapter
{
	static int mLastPosition;
	private static LayoutInflater inflater;
	private AppCompatActivity act;
	private static String mWebTitle;
	private ViewPager pager;
	DB_page db_page;

    public Note_adapter(ViewPager viewPager, AppCompatActivity activity)
    {
    	super(activity.getSupportFragmentManager());
		pager = viewPager;
    	act = activity;
        inflater = act.getLayoutInflater();
        mLastPosition = -1;
	    db_page = new DB_page(act, TabsHost.getCurrentPageTableId());
        System.out.println("Note_adapter / constructor / mLastPosition = " + mLastPosition);
    }

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

    @SuppressLint("SetJavaScriptEnabled")
	@Override
	public Object instantiateItem(ViewGroup container, final int position) 
    {
    	System.out.println("Note_adapter / instantiateItem / position = " + position);
    	// Inflate the layout containing 
    	// 1. picture group: image,video, thumb nail, control buttons
    	// 2. text group: title, body, time 
    	View pagerView = inflater.inflate(R.layout.note_view_adapter, container, false);
    	int style = Note.getStyle();
        pagerView.setBackgroundColor(ColorSet.mBG_ColorArray[style]);

    	// text group
        ViewGroup textGroup = (ViewGroup) pagerView.findViewById(R.id.textGroup);

        // Set tag for text web view
    	CustomWebView textWebView = ((CustomWebView) textGroup.findViewById(R.id.textBody));

    	// set accessibility
        textGroup.setContentDescription(act.getResources().getString(R.string.note_text));
		textWebView.getRootView().setContentDescription(act.getResources().getString(R.string.note_text));

		// set text web view
        setWebView(textWebView,null,CustomWebView.TEXT_VIEW);

        String strTitle = db_page.getNoteTitle(position,true);

        // View mode
	    // text only
	  	if(Note.isTextMode())
	  	{
			System.out.println("Note_adapter / _instantiateItem / isTextMode ");

	  		textGroup.setVisibility(View.VISIBLE);

		    if( !Util.isEmptyString(strTitle) )
			    showTextWebView(position,textWebView);
	  	}


		// footer of note view
		TextView footerText = (TextView) pagerView.findViewById(R.id.note_view_footer);
		footerText.setVisibility(View.VISIBLE);
		footerText.setText(String.valueOf(position+1)+"/"+ pager.getAdapter().getCount());
        footerText.setTextColor(ColorSet.mText_ColorArray[Note.mStyle]);
        footerText.setBackgroundColor(ColorSet.mBG_ColorArray[Note.mStyle]);

    	container.addView(pagerView, 0);
    	
		return pagerView;			
    } //instantiateItem
	
    // show text web view
    private void showTextWebView(int position,CustomWebView textWebView)
    {
    	System.out.println("Note_adapter/ _showTextView / position = " + position);

    	int viewPort;
    	// load text view data
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			viewPort = VIEW_PORT_BY_DEVICE_WIDTH;
    	else
    		viewPort = VIEW_PORT_BY_NONE;

    	String strHtml;
		strHtml = getHtmlStringWithViewPort(position,viewPort);
//	    textWebView.loadData(strHtml,"text/html; charset=utf-8", "UTF-8");
	    //refer https://stackoverflow.com/questions/3312643/android-webview-utf-8-not-showing
	    textWebView.loadDataWithBaseURL(null, strHtml, "text/html", "UTF-8", null);
    }
    
	@Override
	public Fragment getItem(int position) {
		return null;
	}

    // Add for calling mPagerAdapter.notifyDataSetChanged()
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
    
	@Override
    public int getCount() 
    {
		if(db_page != null)
			return db_page.getNotesCount(true);
		else
			return 0;
    }

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals(object);
	}
	
	static Intent mIntentView;
	static NoteUi picUI_primary;

	@Override
	public void setPrimaryItem(final ViewGroup container, int position, Object object) 
	{
		// set primary item only
	    if(mLastPosition != position)
		{
			System.out.println("Note_adapter / _setPrimaryItem / mLastPosition = " + mLastPosition);
            System.out.println("Note_adapter / _setPrimaryItem / position = " + position);

			String tag = "current" + mLastPosition + "textWebView";
			CustomWebView textWebView = (CustomWebView) pager.findViewWithTag(tag);
			if (textWebView != null) {
				textWebView.onPause();
				textWebView.onResume();
			}
		}
	    mLastPosition = position;
	    
	} //setPrimaryItem		

	// Set web view
	private void setWebView(final CustomWebView webView,Object object, int whichView)
	{
        final SharedPreferences pref_web_view = act.getSharedPreferences("web_view", 0);
        if( whichView == CustomWebView.TEXT_VIEW )
        {
            int scale = pref_web_view.getInt("KEY_WEB_VIEW_SCALE",0);
            webView.setInitialScale(scale);
        }

        int style = Note.getStyle();
		webView.setBackgroundColor(ColorSet.mBG_ColorArray[style]);

    	webView.getSettings().setBuiltInZoomControls(true);
    	webView.getSettings().setSupportZoom(true);
    	webView.getSettings().setUseWideViewPort(true);
//    	customWebView.getSettings().setLoadWithOverviewMode(true);
    	webView.getSettings().setJavaScriptEnabled(true);//warning: Using setJavaScriptEnabled can introduce XSS vulnerabilities

        if( whichView == CustomWebView.TEXT_VIEW )
   		{
	    	webView.setWebViewClient(new WebViewClient()
	        {
	            @Override
	            public void onScaleChanged(WebView web_view, float oldScale, float newScale)
	            {
	                super.onScaleChanged(web_view, oldScale, newScale);
	//                System.out.println("Note_adapter / onScaleChanged");
	//                System.out.println("    oldScale = " + oldScale);
	//                System.out.println("    newScale = " + newScale);

	                int newDefaultScale = (int) (newScale*100);
	                pref_web_view.edit().putInt("KEY_WEB_VIEW_SCALE",newDefaultScale).apply();

	                //update current position
	                NoteUi.setFocus_notePos(pager.getCurrentItem());
	            }

	            @Override
	            public void onPageFinished(WebView view, String url) {}
	        });

   		}

	}

    final private static int VIEW_PORT_BY_NONE = 0;
    final private static int VIEW_PORT_BY_DEVICE_WIDTH = 1;
    final private static int VIEW_PORT_BY_SCREEN_WIDTH = 2;
    
    // Get HTML string with view port
    private String getHtmlStringWithViewPort(int position, int viewPort)
    {
    	int mStyle = Note.mStyle;
    	
    	System.out.println("Note_adapter / _getHtmlStringWithViewPort");
	    String strTitle = db_page.getNoteTitle(position,true);
	    Integer strBody = db_page.getNoteBody(position,true);
	    Integer quantity = db_page.getNoteQuantity(position,true);

    	// replace note title
		boolean bSetGray = false;
		if( Util.isEmptyString(strTitle)    )
		{
		}

    	String head = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"+
		       	  	  "<html><head>" +
	  		       	  "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />";
    	
    	if(viewPort == VIEW_PORT_BY_NONE)
    	{
	    	head = head + "<head>";
    	}
    	else if(viewPort == VIEW_PORT_BY_DEVICE_WIDTH)
    	{
	    	head = head + 
	    		   "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
	     	  	   "<head>";
    	}
    	else if(viewPort == VIEW_PORT_BY_SCREEN_WIDTH)
    	{
//        	int screen_width = UtilImage.getScreenWidth(act);
        	int screen_width = 640;
	    	head = head +
	    		   "<meta name=\"viewport\" content=\"width=" + String.valueOf(screen_width) + ", initial-scale=1\">"+
   	  			   "<head>";
    	}
    		
       	String separatedLineTitle = (!Util.isEmptyString(strTitle))?"<hr size=2 color=blue width=99% >":"";

       	// title
       	if(!Util.isEmptyString(strTitle))
       	{
       		Spannable spanTitle = new SpannableString(strTitle);
       		Linkify.addLinks(spanTitle, Linkify.ALL);
       		spanTitle.setSpan(new AlignmentSpan.Standard(Alignment.ALIGN_CENTER),
       							0,
       							spanTitle.length(),
       							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

			//ref http://stackoverflow.com/questions/3282940/set-color-of-textview-span-in-android
			if(bSetGray) {
				ForegroundColorSpan foregroundSpan = new ForegroundColorSpan(Color.GRAY);
				spanTitle.setSpan(foregroundSpan, 0, spanTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}

       		strTitle = Html.toHtml(spanTitle);
       	}
       	else
       		strTitle = "";
    	
    	// set web view text color
    	String colorStr = Integer.toHexString(ColorSet.mText_ColorArray[mStyle]);
    	colorStr = colorStr.substring(2);
    	
    	String bgColorStr = Integer.toHexString(ColorSet.mBG_ColorArray[mStyle]);
    	bgColorStr = bgColorStr.substring(2);
    	
    	return   head + "<body color=\"" + bgColorStr + "\">" +
				 "<br>" + //Note: text mode needs this, otherwise title is overlaid
		         "<p align=\"center\"><b>" +
		         "<font color=\"" + colorStr + "\">" + strTitle + "</font>" +
         		 "</b></p>" + separatedLineTitle +
		         "<p align=\"left\">" +
				 "<font color=\"" + colorStr + "\">"  + strBody + "</font>" +
		         "</p>" +
			     "</b></p>" + separatedLineTitle +
			     "<p align=\"right\">" +
			     "<font color=\"" + colorStr + "\">"  + quantity + "</font>" +
			     "</p>" +
			     "</body></html>";
    }

}