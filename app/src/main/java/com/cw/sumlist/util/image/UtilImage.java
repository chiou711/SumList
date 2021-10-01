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

package com.cw.sumlist.util.image;


import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class UtilImage
{
    public UtilImage(){};

    public static int getScreenWidth(Context context)
	{
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		
		if(Build.VERSION.SDK_INT >= 13)
		{
		    Point outSize = new Point();
	        display.getSize(outSize);
	        return outSize.x;
		}
		else
		{
			return display.getWidth();
		}
	}
	
    public static int getScreenHeight(Context context)
	{
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	    Display display = wm.getDefaultDisplay();
		if(Build.VERSION.SDK_INT >= 13)
		{
		    Point outSize = new Point();
	        display.getSize(outSize);
	        return outSize.y;
		}
		else
		{
			return display.getHeight();
		}
	}    
    
	public static int getScreenWidth(Activity activity)
	{
	    Display display = activity.getWindowManager().getDefaultDisplay();
		if(Build.VERSION.SDK_INT >= 13)
		{
		    Point outSize = new Point();
	        display.getSize(outSize);
	        return outSize.x;
		}
		else
		{
			return display.getWidth();
		}
	}
	
	public static int getScreenHeight(Activity activity)
	{
	    Display display = activity.getWindowManager().getDefaultDisplay();
		if(Build.VERSION.SDK_INT >= 13)
		{
		    Point outSize = new Point();
	        display.getSize(outSize);
	        return outSize.y;
		}
		else
		{
			return display.getHeight();
		}
	}
	
    // Get default scale in percent
    public static int getDefaultScaleInPercent(Activity act)
    {
        // px = dp * (dpi / 160),
        // px:pixel, scale in percent here 
        // dp:density-independent pixels
        // dpi:dots per inch
        int dpi = (int)act.getResources().getDisplayMetrics().densityDpi;
        switch (dpi) 
        {
	        case DisplayMetrics.DENSITY_LOW:
	            System.out.println("DENSITY_LOW");
	            break;
	        case DisplayMetrics.DENSITY_MEDIUM:
	            System.out.println("DENSITY_MEDIUM");
	            break;
	        case DisplayMetrics.DENSITY_HIGH:
	            System.out.println("DENSITY_HIGH");
	            break;
	        case DisplayMetrics.DENSITY_XHIGH:
	            System.out.println("DENSITY_XHIGH");
	            break;
	        case DisplayMetrics.DENSITY_XXHIGH:
	            System.out.println("DENSITY_XXHIGH");
	            break;
	        case DisplayMetrics.DENSITY_XXXHIGH:
	            System.out.println("DENSITY_XXXHIGH");
	            break;
        } 
        
        System.out.println("densityDpi = " + dpi);
        int dp = 100;
        int px = (int)(dp*(dpi/160.0f));
        System.out.println("Default Sacle In Percent = " + px);
        return px;
    }
    
    // Get default scale
    public static float getDefaultSacle(Activity act)
    {
        // scale = (dpi / 160),
        // dpi:dots per inch
        int dpi = (int)act.getResources().getDisplayMetrics().densityDpi;
//        System.out.println("= densityDpi = " + dpi);
        float scale = dpi/160.0f;
//        System.out.println("= default scale = " + scale);
        return scale;
    }
    
}