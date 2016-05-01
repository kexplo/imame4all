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

package com.seleuco.mame4droid.prefs;

import com.kexplo.mame4droid.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class SensitivityPrefScreen extends DialogPreference implements
		SeekBar.OnSeekBarChangeListener {
	private SeekBar seekBar;
	private TextView valueView;
	private int minValue = 1, maxValue = 10;
	private int oldValue, newValue;

	
	public SensitivityPrefScreen(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		//minValue = attrs.getAttributeIntValue("", "minValue", 1);
		//maxValue = attrs.getAttributeIntValue("", "maxValue", 10);
		
		setDialogLayoutResource(R.layout.bar);
		setPositiveButtonText("Save");
		setNegativeButtonText("Cancel");
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		if (newValue < minValue)
			newValue = minValue;
		if (newValue > maxValue)
			newValue = maxValue;
		seekBar = (SeekBar) view.findViewById(R.id.seekbar);
		seekBar.setMax(maxValue - minValue);
		seekBar.setProgress(newValue - minValue);
		seekBar.setSecondaryProgress(newValue - minValue);
		seekBar.setOnSeekBarChangeListener(this);
		valueView = (TextView) view.findViewById(R.id.value);
		valueView.setText(Integer.toString(newValue));
	}

	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		newValue = progress + minValue;
		valueView.setText(Integer.toString(newValue));
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (!positiveResult)
			newValue = oldValue;
		else {
			
			//InputHandler.trackballSensitivity = newValue;
			
			oldValue = newValue;
			persistInt(newValue);
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInteger(index, 0);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		oldValue = (restoreValue ? getPersistedInt(0)
				: ((Integer) defaultValue).intValue());
		newValue = oldValue;
	}
}