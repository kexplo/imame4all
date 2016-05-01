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

package com.seleuco.mame4droid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.seleuco.mame4droid.helpers.DialogHelper;
import com.seleuco.mame4droid.helpers.MainHelper;
import com.seleuco.mame4droid.helpers.MenuHelper;
import com.seleuco.mame4droid.helpers.PrefsHelper;
import com.seleuco.mame4droid.input.ControlCustomizer;
import com.seleuco.mame4droid.input.InputHandler;
import com.seleuco.mame4droid.input.InputHandlerExt;
import com.seleuco.mame4droid.input.InputHandlerFactory;
import com.seleuco.mame4droid.views.IEmuView;
import com.seleuco.mame4droid.views.InputView;
import com.kexplo.mame4droid.R;

public class MAME4droid extends Activity {

	protected View emuView = null;

	protected InputView inputView = null;

	protected MainHelper mainHelper = null;
	protected MenuHelper menuHelper = null;
	protected PrefsHelper prefsHelper = null;
	protected DialogHelper dialogHelper = null;

	protected InputHandler inputHandler = null;

	protected FileExplorer fileExplore = null;

	protected NetPlay netPlay = null;

	public NetPlay getNetPlay() {
		return netPlay;
	}

	public FileExplorer getFileExplore() {
		return fileExplore;
	}

	public MenuHelper getMenuHelper() {
		return menuHelper;
	}

    public PrefsHelper getPrefsHelper() {
		return prefsHelper;
	}

    public MainHelper getMainHelper() {
		return mainHelper;
	}

    public DialogHelper getDialogHelper() {
		return dialogHelper;
	}

	public View getEmuView() {
		return emuView;
	}

	public InputView getInputView() {
		return inputView;
	}

    public InputHandler getInputHandler() {
		return inputHandler;
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		Log.d("EMULATOR", "onCreate "+this);
		System.out.println("onCreate intent:"+getIntent().getAction());

		overridePendingTransition(0, 0);
		getWindow().setWindowAnimations(0);

		prefsHelper = new PrefsHelper(this);

        dialogHelper  = new DialogHelper(this);

        mainHelper = new MainHelper(this);

        fileExplore = new FileExplorer(this);

        netPlay = new NetPlay(this);

        menuHelper = new MenuHelper(this);

        inputHandler = InputHandlerFactory.createInputHandler(this);

        mainHelper.detectDevice();

        inflateViews();

        Emulator.setMAME4droid(this);

        mainHelper.updateMAME4droid();

        //mainHelper.checkNewViewIntent(this.getIntent());
		if(Build.VERSION.SDK_INT >= 0x17) {
			if(checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
				if(shouldShowRequestPermissionRationale("android.permission.WRITE_EXTERNAL_STORAGE")) {
					//dialogHelper.showMessage("You need to allow read on external storage so MAME4droid can read ROM files!", getIntent().getAction());
					dialogHelper.showMessage("You need to allow read on external storage so MAME4droid can read ROM files!", new DialogInterface.OnClickListener() {
						@TargetApi(Build.VERSION_CODES.M)
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							requestPermissions(new String[] {"android.permission.WRITE_EXTERNAL_STORAGE"}, 0x1);
						}
					});
					return;
				}
				requestPermissions(new String[] {"android.permission.WRITE_EXTERNAL_STORAGE"}, 0x1);
				return;
			}
			initMame4droid();
			return;
		}
		initMame4droid();
    }

	protected void initMame4droid() {
        if(!Emulator.isEmulating())
        {
			if(prefsHelper.getROMsDIR()==null)
			{
				if(DialogHelper.savedDialog==DialogHelper.DIALOG_NONE)
				{
				   showDialog(DialogHelper.DIALOG_ROMs_DIR);
				}
			}
			else
			{
				boolean res = getMainHelper().ensureInstallationDIR(mainHelper.getInstallationDIR());
				if(res==false)
				{
					this.getPrefsHelper().setInstallationDIR(this.getPrefsHelper().getOldInstallationDIR());
				}
				else
				{
				    runMAME4droid();
				}
			}
        }
	}

    public void inflateViews(){
    	inputHandler.unsetInputListeners();

        Emulator.setPortraitFull(getPrefsHelper().isPortraitFullscreen());

        boolean full = false;
		if(prefsHelper.isPortraitFullscreen() && mainHelper.getscrOrientation() == Configuration.ORIENTATION_PORTRAIT)
		{
			setContentView(R.layout.main_fullscreen);
			full = true;
		}
		else
		{
            setContentView(R.layout.main);
		}

        FrameLayout fl = (FrameLayout)this.findViewById(R.id.EmulatorFrame);

        Emulator.setVideoRenderMode(getPrefsHelper().getVideoRenderMode());

        if(prefsHelper.getVideoRenderMode()==PrefsHelper.PREF_RENDER_SW)
        {
        	/*
        	if(emuView != null && (emuView instanceof EmulatorViewSW))
        	{
        		EmulatorViewSW s = (EmulatorViewSW)emuView;
        		s.getHolder().removeCallback(s);
        	}*/

        	this.getLayoutInflater().inflate(R.layout.emuview_sw, fl);
        	emuView = this.findViewById(R.id.EmulatorViewSW);
        }
        else
        {
        	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN  && prefsHelper.getNavBarMode()!=PrefsHelper.PREF_NAVBAR_VISIBLE)
        	    this.getLayoutInflater().inflate(R.layout.emuview_gl_ext, fl);
        	else
        		this.getLayoutInflater().inflate(R.layout.emuview_gl, fl);

        	emuView = this.findViewById(R.id.EmulatorViewGL);
        }

        if(full && prefsHelper.isPortraitTouchController())
        {
        	FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams )emuView.getLayoutParams();
        	lp.gravity =  Gravity.TOP | Gravity.CENTER;
        }

        inputView = (InputView) this.findViewById(R.id.InputView);

        ((IEmuView)emuView).setMAME4droid(this);

        inputView.setMAME4droid(this);

        View frame = this.findViewById(R.id.EmulatorFrame);
	    frame.setOnTouchListener(inputHandler);


        inputHandler.setInputListeners();
    }

    public void runMAME4droid(){

	    getMainHelper().copyFiles();
	    getMainHelper().removeFiles();

    	Emulator.emulate(mainHelper.getLibDir(),mainHelper.getInstallationDIR());
    }

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		overridePendingTransition(0, 0);

		inflateViews();

		getMainHelper().updateMAME4droid();

		overridePendingTransition(0, 0);
	}

	//MENU STUFF
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		if(menuHelper!=null)
		{
		   if(menuHelper.createOptionsMenu(menu))return true;
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(menuHelper!=null)
		{
		   if(menuHelper.prepareOptionsMenu(menu)) return true;
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(menuHelper!=null)
		{
		   if(menuHelper.optionsItemSelected(item))
			   return true;
		}
		return super.onOptionsItemSelected(item);
	}

	//ACTIVITY
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(mainHelper!=null)
		   mainHelper.activityResult(requestCode, resultCode, data);
	}

	//LIVE CYCLE
	@Override
	protected void onResume() {
		Log.d("EMULATOR", "onResume "+this);
		super.onResume();

		if(prefsHelper!=null)
		   prefsHelper.resume();

		if(DialogHelper.savedDialog!=-1)
			showDialog(DialogHelper.savedDialog);
		else if(!ControlCustomizer.isEnabled())
		  Emulator.resume();

		if(inputHandler!= null)
		{
			if(inputHandler.getTiltSensor()!=null)
			   inputHandler.getTiltSensor().enable();
			inputHandler.resume();
		}

		NotificationHelper.removeNotification();
		//System.out.println("OnResume");		 
	}

	@Override
	protected void onPause() {
		Log.d("EMULATOR", "onPause "+this);
		super.onPause();
		if(prefsHelper!=null)
		   prefsHelper.pause();
		if(!ControlCustomizer.isEnabled())
		   Emulator.pause();
		if(inputHandler!= null)
		{
			if(inputHandler.getTiltSensor()!=null)
			   inputHandler.getTiltSensor().disable();
		}

		if(dialogHelper!=null)
		{
			dialogHelper.removeDialogs();
		}

		if(prefsHelper.isNotifyWhenSuspend())
		  NotificationHelper.addNotification(getApplicationContext(), "MAME4droid was suspended!", "MAME4droid was suspended", "Press to return to MAME4droid");

		//System.out.println("OnPause");
	}

	@Override
	protected void onStart() {
		Log.d("EMULATOR", "onStart "+this);
		super.onStart();
		try{InputHandlerExt.resetAutodetected();}catch(Error e){};
		//System.out.println("OnStart");
	}

	@Override
	protected void onStop() {
		Log.d("EMULATOR", "onStop "+this);
		super.onStop();
		//System.out.println("OnStop");
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.d("EMULATOR", "onNewIntent "+this);
		System.out.println("onNewIntent action:"+intent.getAction() );
		mainHelper.checkNewViewIntent(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("EMULATOR", "onDestroy "+this);

        View frame = this.findViewById(R.id.EmulatorFrame);
	    if(frame!=null)
           frame.setOnTouchListener(null);

		if(inputHandler!= null)
		{
		   inputHandler.unsetInputListeners();

			if(inputHandler.getTiltSensor()!=null)
				   inputHandler.getTiltSensor().disable();
		}

        if(emuView!=null)
		   ((IEmuView)emuView).setMAME4droid(null);

        /*
        if(inputView!=null)
           inputView.setMAME4droid(null);
        
        if(filterView!=null)
           filterView.setMAME4droid(null);
                       
        prefsHelper = null;
        
        dialogHelper = null;
        
        mainHelper = null;
        
        fileExplore = null;
        
        menuHelper = null;
        
        inputHandler = null;
        
        inputView = null;
        
        emuView = null;
        
        filterView = null; */
	}


	//Dialog Stuff
	@Override
	protected Dialog onCreateDialog(int id) {

		if(dialogHelper!=null)
		{
			Dialog d = dialogHelper.createDialog(id);
			if(d!=null)return d;
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		if(dialogHelper!=null)
		   dialogHelper.prepareDialog(id, dialog);
	}

	@Override
	public boolean dispatchGenericMotionEvent(MotionEvent event)
	{
		if(inputHandler!=null)
		   return inputHandler.genericMotion(event);
		return false;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch(requestCode) {
			case 1:
			{
				if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					initMame4droid();
					return;
				}
				showDialog(DialogHelper.DIALOG_NO_PERMISSIONS);
				return;
			}
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
}
