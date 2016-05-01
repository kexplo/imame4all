/*
 * This file is part of MAME4droid.
 *
 * Copyright (C) 2015 David Valdeita (Seleuco)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Linking MAME4droid statically or dynamically with other modules is
 * making a combined work based on MAME4droid. Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 *
 * In addition, as a special exception, the copyright holders of MAME4droid
 * give you permission to combine MAME4droid with free software programs
 * or libraries that are released under the GNU LGPL and with code included
 * in the standard release of MAME under the MAME License (or modified
 * versions of such code, with unchanged license). You may copy and
 * distribute such a system following the terms of the GNU GPL for MAME4droid
 * and the licenses of the other code concerned, provided that you include
 * the source code of that other code when and as the GNU GPL requires
 * distribution of source code.
 *
 * Note that people who make modified versions of MAME4idroid are not
 * obligated to grant this special exception for their modified versions; it
 * is their choice whether to do so. The GNU General Public License
 * gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version
 * which carries forward this exception.
 *
 * MAME4droid is dual-licensed: Alternatively, you can license MAME4droid
 * under a MAME license, as set out in http://mamedev.org/
 */

package com.seleuco.mame4droid.helpers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Toast;

import com.seleuco.mame4droid.Emulator;
import com.seleuco.mame4droid.MAME4droid;
import com.kexplo.mame4droid.R;
import com.seleuco.mame4droid.WebHelpActivity;
import com.seleuco.mame4droid.input.ControlCustomizer;
import com.seleuco.mame4droid.input.InputHandler;
import com.seleuco.mame4droid.prefs.GameFilterPrefs;
import com.seleuco.mame4droid.prefs.UserPreferences;
import com.seleuco.mame4droid.views.IEmuView;
import com.seleuco.mame4droid.views.InputView;

public class MainHelper {
	
	final static public  int SUBACTIVITY_USER_PREFS = 1;
	final static public  int SUBACTIVITY_HELP = 2;
	final static public  int BUFFER_SIZE = 1024*48;
	
	//final static public  String MAGIC_FILE = "dont-delete-00005.bin";
	
	final public static int DEVICE_GENEREIC = 1;
	final public static int DEVICE_OUYA = 2;
	final public static int DEVICE_SHIELD = 3;
	final public static int DEVICE_JXDS7800 = 4;
	final public static int DEVICE_AGAMEPAD2 = 5;
	
	protected int deviceDetected = DEVICE_GENEREIC;
	
	protected int oldInMAME = 0;
	
	public int getDeviceDetected() {
		return deviceDetected;
	}

	protected MAME4droid mm = null;
	
	public MainHelper(MAME4droid value){
		mm = value;
	}
	
	public String getLibDir(){	
		String cache_dir, lib_dir;
		try {
			//cache_dir = mm.getCacheDir().getCanonicalPath();				
			//lib_dir = cache_dir.replace("cache", "lib");
			lib_dir = mm.getApplicationInfo().nativeLibraryDir;
		} catch (Exception e) {
			e.printStackTrace();
			lib_dir = "/data/data/com.seleuco.mame4droid/lib";
		}
		return lib_dir;
	}
	

	public String getInstallationDIR()
	{
		String res_dir = null;
		
		if(mm.getPrefsHelper().getInstallationDIR()!=null)
			return mm.getPrefsHelper().getInstallationDIR();
				
		//android.os.Debug.waitForDebugger();
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	    	res_dir = Environment.getExternalStorageDirectory().getAbsolutePath()+"/MAME4droid/";  	    	
	    }
	    else
	    	res_dir = mm.getFilesDir().getAbsolutePath()+"/MAME4droid/";
	    	    
	    //res_dir = mm.getExternalFilesDir(null).getAbsolutePath()+"/MAME4droid/";
		//File[] f = mm.getExternalFilesDirs(null);
		//res_dir =  f[f.length-1].getAbsolutePath();
	    
	    mm.getPrefsHelper().setInstallationDIR(res_dir);
			    		 			
		return res_dir;
	}

	
	public boolean ensureInstallationDIR(String dir){
				
		if(!dir.endsWith("/"))
			dir+="/";
		
		File res_dir = new File(dir);
		
		boolean created = false;
		
		if(res_dir.exists() == false)
		{
			if(!res_dir.mkdirs())
			{
				mm.getDialogHelper().setErrorMsg("Can't find/create: '"+dir+"' Is it writeable?.\nReverting...");
				mm.showDialog(DialogHelper.DIALOG_ERROR_WRITING);
				return false;				
			}
			else
			{
               created= true;
			}
		}
		
		String str_sav_dir = dir+"saves/";
		File sav_dir = new File(str_sav_dir);
		if(sav_dir.exists() == false)
		{			
			if(!sav_dir.mkdirs())
			{
				mm.getDialogHelper().setErrorMsg("Can't find/create: '"+str_sav_dir+"' Is it writeable?.\nReverting...");
				mm.showDialog(DialogHelper.DIALOG_ERROR_WRITING);
				return false;				
			}
		}
		
		if(created )
		{		
            String rompath = mm.getPrefsHelper().getROMsDIR() != null ? mm.getPrefsHelper().getROMsDIR() : dir +"roms"; 
			mm.getDialogHelper().setInfoMsg("Created: '"+dir+"' to store save states, cfg files and MAME assets.\n\nBeware, copy or move your zipped ROMs under '"+ rompath +"' directory!\n\nMAME4droid 0.139 uses only 0.139 MAME romset.\n\nYou may have to completely turn off your device to see new folders. You might need to unplug also.");
			mm.showDialog(DialogHelper.DIALOG_INFO);
		}
		
		mm.getPrefsHelper().setOldInstallationDIR(dir);
						
		return true;		
	}
	
    protected boolean deleteRecursive(File path) throws FileNotFoundException{
        if (!path.exists()) throw new FileNotFoundException(path.getAbsolutePath());
        boolean ret = true;
        if (path.isDirectory()){
            for (File f : path.listFiles()){
                ret = ret && deleteRecursive(f);
            }
        }
        return ret && path.delete();
    }
    
	
	public void removeFiles(){
		try 
		{			
			if(mm.getPrefsHelper().isDefaultData())
			{
				String dir = mm.getMainHelper().getInstallationDIR();
				
				File f1 = new File(dir + File.separator + "cfg/");
				File f2 = new File(dir + File.separator + "nvram/");
				
				deleteRecursive(f1);
				deleteRecursive(f2);
				
				Toast.makeText(mm, "Deleted MAME cfg and NVRAM files...", Toast.LENGTH_LONG).show();
			}
			
		} catch (Exception e) {
			Toast.makeText(mm, "Failed deleting:"+e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}
		
	public void copyFiles(){
		
		try {
			
			String roms_dir = mm.getMainHelper().getInstallationDIR();
			 
			File fm = new File(roms_dir + File.separator + "saves/" + "dont-delete-"+getVersion()+".bin");
			if(fm.exists())
				return;
						
			fm.mkdirs();			
			fm.createNewFile();
			
			// Create a ZipInputStream to read the zip file
			BufferedOutputStream dest = null;
			InputStream fis = mm.getResources().openRawResource(R.raw.files);
			ZipInputStream zis = new ZipInputStream(

			new BufferedInputStream(fis));
			// Loop over all of the entries in the zip file
			int count;
			byte data[] = new byte[BUFFER_SIZE];
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (!entry.isDirectory()) {

					String destination = roms_dir;
					String destFN = destination + File.separator + entry.getName();
					// Write the file to the file system
					FileOutputStream fos = new FileOutputStream(destFN);
					dest = new BufferedOutputStream(fos, BUFFER_SIZE);
					while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
						dest.write(data, 0, count);
					}
					dest.flush();
					dest.close();
				}
				else
				{
					File f = new File(roms_dir+ File.separator + entry.getName());
					f.mkdirs();
				}
				
			}
			zis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int getscrOrientation() {
		Display getOrient = mm.getWindowManager().getDefaultDisplay();
		//int orientation = getOrient.getOrientation();
		
		int orientation  = mm.getResources().getConfiguration().orientation;


		// Sometimes you may get undefined orientation Value is 0
		// simple logic solves the problem compare the screen
		// X,Y Co-ordinates and determine the Orientation in such cases
		if (orientation == Configuration.ORIENTATION_UNDEFINED) {

			Configuration config = mm.getResources().getConfiguration();
			orientation = config.orientation;

			if (orientation == Configuration.ORIENTATION_UNDEFINED) {
				// if emu_height and widht of screen are equal then
				// it is square orientation
				if (getOrient.getWidth() == getOrient.getHeight()) {
					orientation = Configuration.ORIENTATION_SQUARE;
				} else { // if widht is less than emu_height than it is portrait
					if (getOrient.getWidth() < getOrient.getHeight()) {
						orientation = Configuration.ORIENTATION_PORTRAIT;
					} else { // if it is not any of the above it will defineitly
								// be landscape
						orientation = Configuration.ORIENTATION_LANDSCAPE;
					}
				}
			}
		}
		return orientation; // return values 1 is portrait and 2 is Landscape
							// Mode
	}
	
	public void reload() {
		
		if(true)
		 return;
	    System.out.println("RELOAD!!!!!");	   
		
		Intent intent = mm.getIntent();
		System.out.println("RELOAD intent:"+intent.getAction());

	    mm.overridePendingTransition(0 , 0);
	    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
	    mm.finish();

	    mm.overridePendingTransition(0, 0);
	    mm.startActivity(intent);
	    mm.overridePendingTransition(0, 0);
	}
	
	public void updateOverlayFilter(){
		
        String value = PrefsHelper.PREF_OVERLAY_NONE;        
        
        if(getscrOrientation() == Configuration.ORIENTATION_PORTRAIT)
        	value = mm.getPrefsHelper().getPortraitOverlayFilterValue();
        else
        	value = mm.getPrefsHelper().getLandscapeOverlayFilterValue();
					
		if(Emulator.getOverlayFilterValue() != value)
		{
			Emulator.setOverlayFilterValue(value);
			Emulator.setFilterBitmap(null);
            if(!value.equals(PrefsHelper.PREF_OVERLAY_NONE))
            {	            
	            String fileName = mm.getMainHelper().getInstallationDIR()+File.separator+"overlays"+File.separator+value;
	            
	            Bitmap bmp = BitmapFactory.decodeFile(fileName);
	            Emulator.setFilterBitmap(bmp);	           
            }
		}
		else
		{
			Emulator.setOverlayFilterValue(value);
		}    					
	}

	public void updateVideoRender (){
		
		if(Emulator.getVideoRenderMode() != mm.getPrefsHelper().getVideoRenderMode())
		{						
			Emulator.setVideoRenderMode(mm.getPrefsHelper().getVideoRenderMode());		
		}
		else
		{
		    Emulator.setVideoRenderMode(mm.getPrefsHelper().getVideoRenderMode());
		}    					
    }
	
	public void setBorder(){
		
		if(true)
		  return;
		
		int size = mm.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK; 
		
		if((size  == Configuration.SCREENLAYOUT_SIZE_LARGE || size  == Configuration.SCREENLAYOUT_SIZE_XLARGE) 
			&& mm.getMainHelper().getscrOrientation() == Configuration.ORIENTATION_PORTRAIT)
			{
		        LayoutParams lp  = (LayoutParams) mm.getEmuView().getLayoutParams();
				View v =  mm.findViewById(R.id.EmulatorFrame);			    
				if(mm.getPrefsHelper().isPortraitTouchController())
			    {					
			       v.setBackgroundDrawable(mm.getResources().getDrawable(R.drawable.border_view));
			       lp.setMargins(15, 15, 15, 15);
			    }   
			    else
			    {
			    	v.setBackgroundDrawable(null);
			    	v.setBackgroundColor(mm.getResources().getColor(R.color.emu_back_color));
			    	lp.setMargins(0, 0, 0, 0);
			    }			    
			}		   	
	}
	
	public void updateEmuValues(){
		
		PrefsHelper prefsHelper = mm.getPrefsHelper();
		
		Emulator.setValue(Emulator.FPS_SHOWED_KEY, prefsHelper.isFPSShowed() ? 1 : 0);
		Emulator.setValue(Emulator.INFOWARN_KEY, prefsHelper.isShowInfoWarnings() ? 1 : 0);
		
		Emulator.setValue(Emulator.IDLE_WAIT,prefsHelper.isIdleWait() ? 1 : 0);	
		Emulator.setValue(Emulator.THROTTLE,prefsHelper.isThrottle() ? 1 : 0);
		Emulator.setValue(Emulator.AUTOSAVE,prefsHelper.isAutosave() ? 1 : 0);
		Emulator.setValue(Emulator.CHEAT,prefsHelper.isCheat() ? 1 : 0);
		Emulator.setValue(Emulator.SOUND_VALUE,prefsHelper.getSoundValue());
		Emulator.setValue(Emulator.FRAME_SKIP_VALUE,prefsHelper.getFrameSkipValue());

		Emulator.setValue(Emulator.EMU_AUTO_RESOLUTION,prefsHelper.isAutoSwitchRes() ? 1 : 0);
		Emulator.setValue(Emulator.EMU_RESOLUTION,prefsHelper.getEmulatedResolution());
		Emulator.setValue(Emulator.FORCE_PXASPECT,prefsHelper.getForcedPixelAspect());
		
		Emulator.setValue(Emulator.DOUBLE_BUFFER,mm.getPrefsHelper().isDoubleBuffer() ? 1 : 0);
		Emulator.setValue(Emulator.PXASP1,mm.getPrefsHelper().isPlayerXasPlayer1() ? 1 : 0);
		Emulator.setValue(Emulator.SAVELOAD_COMBO,mm.getPrefsHelper().isSaveLoadCombo() ? 1 : 0);
				
		Emulator.setValue(Emulator.AUTOFIRE,mm.getPrefsHelper().getAutofireValue());
		
		Emulator.setValue(Emulator.HISCORE,mm.getPrefsHelper().isHiscore() ? 1 : 0);
		
		Emulator.setValue(Emulator.VBEAN2X,mm.getPrefsHelper().isVectorBeam2x() ? 1 : 0);
		Emulator.setValue(Emulator.VANTIALIAS,mm.getPrefsHelper().isVectorAntialias() ? 1 : 0);
		Emulator.setValue(Emulator.VFLICKER,mm.getPrefsHelper().isVectorFlicker() ? 1 : 0);
		
		Emulator.setValue(Emulator.NETPLAY_DELAY,mm.getPrefsHelper().getNetplayDelay());
		
		Emulator.setValue(Emulator.RENDER_RGB,
				mm.getPrefsHelper().isRenderRGB() && mm.getPrefsHelper().getVideoRenderMode()!= PrefsHelper.PREF_RENDER_SW ? 1 : 0);
		
		Emulator.setValue(Emulator.IMAGE_EFFECT , mm.getPrefsHelper().getImageEffectValue());
		Emulator.setValue(Emulator.MOUSE , mm.getPrefsHelper().isMouseEnabled() ? 1 : 0);	
			
		Emulator.setValue(Emulator.REFRESH , mm.getPrefsHelper().getRefresh());	
		
		GameFilterPrefs gfp = mm.getPrefsHelper().getGameFilterPrefs();
		boolean dirty = gfp.readValues();
		gfp.sendValues();
		if(dirty)
		{
			if(!Emulator.isInMAME())
				Emulator.setValue(Emulator.RESET_FILTER, 1);
			Emulator.setValue(Emulator.LAST_GAME_SELECTED, 0);
		}
					
		Emulator.setValue(Emulator.EMU_SPEED,mm.getPrefsHelper().getEmulatedSpeed());
		Emulator.setValue(Emulator.VSYNC,mm.getPrefsHelper().getVSync());	
				
		Emulator.setValue(Emulator.SOUND_ENGINE,mm.getPrefsHelper().getSoundEngine() > 2 ? 2 : 1);
		
		AudioManager am = (AudioManager) mm.getSystemService(Context.AUDIO_SERVICE);
		int sfr = 2048;		
		try{
			sfr = Integer.valueOf(am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)).intValue();
			System.out.println("PROPERTY_OUTPUT_FRAMES_PER_BUFFER:"+sfr);
		}catch(Error e){}
		
		if (mm.getPrefsHelper().getSoundEngine()==PrefsHelper.PREF_SNDENG_OPENSL)
			sfr *= 2;
		Emulator.setValue(Emulator.SOUND_DEVICE_FRAMES,sfr);
		
		int sr = 44100;
		try{
			sr = Integer.valueOf(am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)).intValue();
			System.out.println("PROPERTY_OUTPUT_SAMPLE_RATE:"+sr);
		}catch(Error e){}		
		Emulator.setValue(Emulator.SOUND_DEVICE_SR,sr);
		
		Emulator.setValueStr(Emulator.BIOS, mm.getPrefsHelper().getCustomBIOS());	
	}
	

	public void updateMAME4droid(){
		
		if(Emulator.isRestartNeeded())
		{
			mm.showDialog(DialogHelper.DIALOG_EMU_RESTART);
			return;
		}
		
		//updateVideoRender();
		Emulator.setVideoRenderMode(mm.getPrefsHelper().getVideoRenderMode());	

		updateOverlayFilter();
		
		if(Emulator.isPortraitFull() != mm.getPrefsHelper().isPortraitFullscreen())								
            mm.inflateViews();
		
		View emuView =  mm.getEmuView();

		InputView inputView =  mm.getInputView();
		InputHandler inputHandler = mm.getInputHandler();
		PrefsHelper prefsHelper = mm.getPrefsHelper();
		
		String definedKeys = prefsHelper.getDefinedKeys();
		final String[] keys = definedKeys.split(":");
		for(int i=0;i<keys.length;i++)
			InputHandler.keyMapping[i]=Integer.valueOf(keys[i]).intValue();
		
		Emulator.setDebug(prefsHelper.isDebugEnabled());
		Emulator.setThreadedSound(!prefsHelper.isSoundSync());
		
		updateEmuValues();
		
		setBorder();
		
	    if(prefsHelper.isTiltSensor())
	    	inputHandler.getTiltSensor().enable();
	    else
	    	inputHandler.getTiltSensor().disable();
	           	    
		inputHandler.setTrackballSensitivity( prefsHelper.getTrackballSensitivity());
		inputHandler.setTrackballEnabled(!prefsHelper.isTrackballNoMove());
				
		int state = mm.getInputHandler().getInputHandlerState();
		
		if(this.getscrOrientation() == Configuration.ORIENTATION_PORTRAIT)
		{
				        
			((IEmuView)emuView).setScaleType(prefsHelper.getPortraitScaleMode());

			Emulator.setFrameFiltering(prefsHelper.isPortraitBitmapFiltering());
			
			if(state == InputHandler.STATE_SHOWING_CONTROLLER && !prefsHelper.isPortraitTouchController())
			   //{reload();return;}
			   inputHandler.changeState();
				
			if(state == InputHandler.STATE_SHOWING_NONE && prefsHelper.isPortraitTouchController())
			   //{reload();return;}
			   inputHandler.changeState();	
			
			state = mm.getInputHandler().getInputHandlerState();
			
			if(state == InputHandler.STATE_SHOWING_NONE)
			{	
				inputView.setVisibility(View.GONE);
			}	
			else
			{	
			    inputView.setVisibility(View.VISIBLE);
			}   

			if(state == InputHandler.STATE_SHOWING_CONTROLLER)
			{			    				   	
				if(Emulator.isPortraitFull())
				{
				   inputView.bringToFront();
				   inputHandler.readControllerValues(R.raw.controller_portrait_full);
				}
				else 
				{
				   inputView.setImageDrawable(mm.getResources().getDrawable(R.drawable.back_portrait));				 
			   	   inputHandler.readControllerValues(R.raw.controller_portrait);
				}
			}
			
			if(ControlCustomizer.isEnabled() && !Emulator.isPortraitFull())
			{
				ControlCustomizer.setEnabled(false);
				mm.getDialogHelper().setInfoMsg("Control layout customization is only allowed in fullscreen mode");
				mm.showDialog(DialogHelper.DIALOG_INFO);
			}			
		}
		else
		{
			((IEmuView)emuView).setScaleType(mm.getPrefsHelper().getLandscapeScaleMode());
			
			Emulator.setFrameFiltering(mm.getPrefsHelper().isLandscapeBitmapFiltering());
			
			if(state == InputHandler.STATE_SHOWING_CONTROLLER && !prefsHelper.isLandscapeTouchController())
			   //{reload();return;}
			   inputHandler.changeState();
			
			if(state == InputHandler.STATE_SHOWING_NONE && prefsHelper.isLandscapeTouchController())
			   //{reload();return;}
			   inputHandler.changeState();	
			
			state = mm.getInputHandler().getInputHandlerState();
			
		    inputView.bringToFront();
			
			if(state == InputHandler.STATE_SHOWING_NONE)
			{	
				inputView.setVisibility(View.GONE);
			}	
			else
			{	
			    inputView.setVisibility(View.VISIBLE);
			}   

			if(state == InputHandler.STATE_SHOWING_CONTROLLER)
			{			    				    		
				inputView.setImageDrawable(null);
				
				Display dp = mm.getWindowManager().getDefaultDisplay();
				
				float w = dp.getWidth();
				float h = dp.getHeight();
						
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
				{
					Point pt = new Point();
	                dp.getRealSize(pt);
	                w = pt.x;
	                h = pt.y;
				}
				else
				{
					try {
						Method mGetRawW;
						mGetRawW = Display.class.getMethod("getRawWidth");
						Method mGetRawH = Display.class.getMethod("getRawHeight");
						w = (Integer)mGetRawW.invoke(dp);
						h = (Integer)mGetRawH.invoke(dp);					
					} catch (Exception e) {
					}
				}
				
				if(h==0)h=1;
				
				//System.out.println("--->>> "+w+" "+h+ " "+w/h+ " "+ (float)(16.0/9.0));
				
			   	if(w/h != (float)(16.0/9.0) /*&& false*/)
			   	{					   
				   inputHandler.readControllerValues(R.raw.controller_landscape);
			   	}
			   	else
			   	{
			   	   inputHandler.readControllerValues(R.raw.controller_landscape_16_9);
			   	}
			}		
		}
				
    	if(Emulator.getValue(Emulator.IN_MAME)==1 && (Emulator.getValue(Emulator.IN_MENU)==0 || oldInMAME==0) &&	
    	    	   ((mm.getPrefsHelper().isLightgun() &&  mm.getInputHandler().getInputHandlerState() != InputHandler.STATE_SHOWING_NONE) || mm.getPrefsHelper().isTiltSensor()))
    	{
    			    CharSequence text = mm.getPrefsHelper().isTiltSensor() ? "Tilt sensor is enabled!" : "Touch lightgun is enabled!";
    			    int duration = Toast.LENGTH_SHORT;
    			    Toast toast = Toast.makeText(mm, text, duration);
    			    toast.show();
    	}
    	
    	oldInMAME = Emulator.getValue(Emulator.IN_MAME);
		
		if(state != InputHandler.STATE_SHOWING_CONTROLLER && ControlCustomizer.isEnabled())
		{
   		    ControlCustomizer.setEnabled(false);
		    mm.getDialogHelper().setInfoMsg("Control layout customization is only allowed when touch controller is visible");
			mm.showDialog(DialogHelper.DIALOG_INFO);			
		}
		
		if(ControlCustomizer.isEnabled())
		{
			//mm.getEmuView().setVisibility(View.INVISIBLE);
		    //mm.getInputView().requestFocus();
		}   
		
		int op = inputHandler.getOpacity();
		if (op != -1 && (state == InputHandler.STATE_SHOWING_CONTROLLER) )
			inputView.setAlpha(op);

		inputView.requestLayout();
				
		emuView.requestLayout();
		
		inputView.invalidate();
		emuView.invalidate();
	}
	
	public void showWeb(){		
		Intent browserIntent = new Intent("android.intent.action.VIEW",
				Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=seleuco%2enicator%40gmail%2ecom&lc=US&item_name=Seleuco%20Nicator&item_number=ixxxx4all&no_note=0&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHostedGuest")
                //Uri.parse("http://code.google.com/p/xpectrum/")				
				);  
		mm.startActivity(browserIntent);
	}
	
	public void showSettings(){
		Intent i = new Intent(mm, UserPreferences.class);
		mm.startActivityForResult(i, MainHelper.SUBACTIVITY_USER_PREFS);
	}
	
	public void showHelp(){
		//Intent i2 = new Intent(mm, HelpActivity.class);
		//mm.startActivityForResult(i2, MainHelper.SUBACTIVITY_HELP);			
		Intent i = new Intent(mm, WebHelpActivity.class);
		i.putExtra("INSTALLATION_PATH", mm.getMainHelper().getInstallationDIR());
		mm.startActivityForResult(i, MainHelper.SUBACTIVITY_HELP);
	}
	
	public void activityResult(int requestCode, int resultCode, Intent data) {
		
		if(requestCode == SUBACTIVITY_USER_PREFS)
		{	
            updateMAME4droid();
		}   
	}
	
	public ArrayList<Integer> measureWindow(int widthMeasureSpec, int heightMeasureSpec, int scaleType) {
		   
		int widthSize = 1;
		int heightSize = 1;
		
		if(!Emulator.isInMAME() && !(scaleType==PrefsHelper.PREF_STRETCH))
			scaleType = PrefsHelper.PREF_SCALE;
						
		if (scaleType == PrefsHelper.PREF_STRETCH)// FILL ALL
		{
			widthSize = MeasureSpec.getSize(widthMeasureSpec);
			heightSize = MeasureSpec.getSize(heightMeasureSpec);
		} 
		else 
		{
			int emu_w = Emulator.getEmulatedVisWidth();
		    int emu_h = Emulator.getEmulatedVisHeight();		    
		    
		    if(scaleType == PrefsHelper.PREF_SCALE_INTEGER)
		    {		    	
		    	int e = mm.getPrefsHelper().getImageEffectValue()+1;
		    	emu_w = emu_w/e;
		    	emu_h = emu_h/e;
		    	
		    	int ax = (MeasureSpec.getSize(widthMeasureSpec) / emu_w);
		    	int ay = (MeasureSpec.getSize(heightMeasureSpec) / emu_h);

		    	int xx = Math.min(ax,ay);
		    	
		    	if(xx==0)
		    		xx=1;
		    	
		    	emu_w = emu_w * xx;
		    	emu_h = emu_h * xx;		    	
		    } else
			
		    if(scaleType == PrefsHelper.PREF_SCALE_INTEGER_BEYOND)
			{	
		    	int e = mm.getPrefsHelper().getImageEffectValue()+1;
		    	emu_w = emu_w/e;
		    	emu_h = emu_h/e;
		    	
			  	int ax = (MeasureSpec.getSize(widthMeasureSpec) / emu_w);
			   	int ay = (MeasureSpec.getSize(heightMeasureSpec) / emu_h);

			   	ax++;ay++;		    	
			   	int xx = Math.min(ax,ay);
			    	
			   	if(xx==0)
     		       xx=1;
			    	
			   	emu_w = emu_w * xx;
			   	emu_h = emu_h * xx;		    	
			} else
				
		    if(scaleType == PrefsHelper.PREF_15X)
			{
		    	emu_w = (int)(emu_w * 1.5f);
		    	emu_h = (int)(emu_h * 1.5f);		    	
			} else
			    	
		    if(scaleType == PrefsHelper.PREF_20X)
		    {
		    	emu_w = emu_w * 2;
		    	emu_h = emu_h * 2;
		    } else
		    
		    if(scaleType == PrefsHelper.PREF_25X)
		    {
		    	emu_w = (int)(emu_w * 2.5f);
		    	emu_h = (int)(emu_h * 2.5f);
		    } else
		    
		    if(scaleType == PrefsHelper.PREF_3X)
		    {
		    	emu_w = (int)(emu_w * 3.0f);
		    	emu_h = (int)(emu_h * 3.0f);
		    } else
		    
		    if(scaleType == PrefsHelper.PREF_35X)
		    {
		    	emu_w = (int)(emu_w * 3.5f);
		    	emu_h = (int)(emu_h * 3.5f);
		    } else
		    
		    if(scaleType == PrefsHelper.PREF_4X)
		    {
		    	emu_w = (int)(emu_w * 4.0f);
		    	emu_h = (int)(emu_h * 4.0f);
		    } else

		    if(scaleType == PrefsHelper.PREF_45X)
		    {
		    	emu_w = (int)(emu_w * 4.5f);
		    	emu_h = (int)(emu_h * 4.5f);
		    } else	    

		    if(scaleType == PrefsHelper.PREF_5X)
		    {
		    	emu_w = (int)(emu_w * 5.0f);
		    	emu_h = (int)(emu_h * 5.0f);
		    } 

		    if(scaleType == PrefsHelper.PREF_55X)
		    {
		    	emu_w = (int)(emu_w * 5.5f);
		    	emu_h = (int)(emu_h * 5.5f);
		    }
		    
		    if(scaleType == PrefsHelper.PREF_6X)
		    {
		    	emu_w = (int)(emu_w * 6.0f);
		    	emu_h = (int)(emu_h * 6.0f);
		    }
		    		    
			int w = emu_w;
			int h = emu_h;

			if(scaleType == PrefsHelper.PREF_SCALE || scaleType == PrefsHelper.PREF_STRETCH || !Emulator.isInMAME() || !mm.getPrefsHelper().isScaleBeyondBoundaries())
			{
			    widthSize = MeasureSpec.getSize(widthMeasureSpec);
			    heightSize = MeasureSpec.getSize(heightMeasureSpec);
			    
			    if(mm.getPrefsHelper().isOverscan())
			    {
			       widthSize *= 0.93;
			       heightSize *= 0.93;
			    }
			    		    
			}
			else
			{
				widthSize = emu_w;
				heightSize = emu_h;
			}
									
			if(heightSize==0)heightSize=1;
			if(widthSize==0)widthSize=1;

			float scale = 1.0f;

			if (scaleType == PrefsHelper.PREF_SCALE)
				scale = Math.min((float) widthSize / (float) w,
						(float) heightSize / (float) h);

			w = (int) (w * scale);
			h = (int) (h * scale);

			float desiredAspect = (float) emu_w / (float) emu_h;

			widthSize = Math.min(w, widthSize);
			heightSize = Math.min(h, heightSize);
			
			if(heightSize==0)heightSize=1;
			if(widthSize==0)widthSize=1;
			
			float actualAspect = (float) (widthSize / heightSize);

			if (Math.abs(actualAspect - desiredAspect) > 0.0000001) {

				boolean done = false;

				// Try adjusting emu_width to be proportional to emu_height
				int newWidth = (int) (desiredAspect * heightSize);

				if (newWidth <= widthSize) {
					widthSize = newWidth;
					done = true;
				}

				// Try adjusting emu_height to be proportional to emu_width
				if (!done) {
					int newHeight = (int) (widthSize / desiredAspect);
					if (newHeight <= heightSize) {
						heightSize = newHeight;
					}
				}
			}
		}
		
		ArrayList<Integer> l = new ArrayList<Integer>();
		l.add(Integer.valueOf(widthSize));
		l.add(Integer.valueOf(heightSize));
		return l;		
	}		
	
	public void detectDevice() {

		boolean ouya = android.os.Build.MODEL.equals("OUYA Console");
		boolean shield = android.os.Build.MODEL.equals("SHIELD");
		boolean S7800 = android.os.Build.MODEL.equals("S7800");
		boolean GP2 = android.os.Build.MODEL.equals("ARCHOS GAMEPAD2");

		if (ouya) {
			Context context = mm.getApplicationContext();
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			if (!prefs.getBoolean("ouya_2", false)) {
				SharedPreferences.Editor edit = prefs.edit();
				edit.putBoolean("ouya_2", true);
				edit.putBoolean(PrefsHelper.PREF_LANDSCAPE_TOUCH_CONTROLLER,
						false);
				edit.putBoolean(PrefsHelper.PREF_LANDSCAPE_BITMAP_FILTERING,
						true);
				edit.putString(PrefsHelper.PREF_GLOBAL_NAVBAR_MODE, PrefsHelper.PREF_NAVBAR_VISIBLE+"");
				edit.putString(PrefsHelper.PREF_GLOBAL_RESOLUTION,
						"11");	
				edit.putString(PrefsHelper.PREF_AUTOMAP_OPTIONS,PrefsHelper.PREF_AUTOMAP_THUMBS_AS_COINSTART_L2R2_DISABLED+"");
										
				// edit.putString("", "");
				edit.commit();
			}
			deviceDetected = DEVICE_OUYA;
		} else

		if (shield) {
			Context context = mm.getApplicationContext();
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			if (!prefs.getBoolean("shield_3", false)) {
				SharedPreferences.Editor edit = prefs.edit();
				edit.putBoolean("shield_3", true);
				edit.putBoolean(PrefsHelper.PREF_LANDSCAPE_TOUCH_CONTROLLER,
						false);
				edit.putString(PrefsHelper.PREF_GLOBAL_NAVBAR_MODE, PrefsHelper.PREF_NAVBAR_VISIBLE+"");
				edit.putBoolean(PrefsHelper.PREF_LANDSCAPE_BITMAP_FILTERING,
						true);
				edit.putString(PrefsHelper.PREF_GLOBAL_RESOLUTION,
						"14");					
				edit.commit();
			}
			deviceDetected = DEVICE_SHIELD;
		} else
		
		if (S7800) {
			Context context = mm.getApplicationContext();
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			if (!prefs.getBoolean("S7800", false)) {
				SharedPreferences.Editor edit = prefs.edit();
				edit.putBoolean("S7800", true);
				edit.putBoolean(PrefsHelper.PREF_LANDSCAPE_TOUCH_CONTROLLER,
						false);
				edit.putString(PrefsHelper.PREF_GLOBAL_NAVBAR_MODE, PrefsHelper.PREF_NAVBAR_VISIBLE+"");
				edit.commit();
			}
			deviceDetected = DEVICE_JXDS7800;
		}
		
		if (GP2) {
			Context context = mm.getApplicationContext();
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			if (!prefs.getBoolean("GAMEPAD2", false)) {
				SharedPreferences.Editor edit = prefs.edit();
				edit.putBoolean("GAMEPAD2", true);
				edit.putBoolean(PrefsHelper.PREF_LANDSCAPE_TOUCH_CONTROLLER,
						false);
				//edit.putString(PrefsHelper.PREF_AUTOMAP_OPTIONS,PrefsHelper.PREF_AUTOMAP_L1R1_AS_EXITMENU_L2R2_AS_L1R1+"");	
				edit.commit();
			}
			deviceDetected = DEVICE_AGAMEPAD2;
		}
	}
	
	public void restartApp(){
		Intent oldintent = mm.getIntent();
		//System.out.println("OLD INTENT:"+oldintent.getAction());
	    PendingIntent intent = PendingIntent.getActivity(mm.getBaseContext(), 0, new Intent(oldintent), oldintent.getFlags());
		AlarmManager manager = (AlarmManager) mm.getSystemService(Context.ALARM_SERVICE);
		manager.set(AlarmManager.RTC, System.currentTimeMillis() + 250, intent);
        android.os.Process.killProcess(android.os.Process.myPid());  	
	}
	
	public void checkNewViewIntent(Intent intent){
	    if(intent.getAction() == Intent.ACTION_VIEW && Emulator.isEmulating()) 
	    {
	       Uri uri = intent.getData();
	       java.io.File f = new java.io.File(uri.getPath());
	       String name = f.getName();
	       String romName = Emulator.getValueStr(Emulator.ROM_NAME);
	       //System.out.print("Intent view: "+name + " "+ romName);
	       if(romName!=null && name.equals(romName))
	    	   return;	       
	       mm.setIntent(intent);
			new Thread(new Runnable() {
                public void run() {
         	       try {
         				Thread.sleep(500);
         			   } catch (InterruptedException e) {			
         				e.printStackTrace();
         			   }
         		       restartApp();
                }
            }).start(); 
	    }	
	}
	
	public String getVersion(){
		String version = "???";
		try
	    {
		   version = mm.getPackageManager().getPackageInfo(mm.getPackageName(), 0).versionName;	
	    } catch (NameNotFoundException e) {
		   e.printStackTrace();
	    } 
		return version;
	}
	
}
