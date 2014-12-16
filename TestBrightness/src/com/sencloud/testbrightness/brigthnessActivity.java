package com.sencloud.testbrightness;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class brigthnessActivity extends Activity{
	
	public  SystemManager systemManager ;
	private Button os_btn;
	private Button current_btn;
	
	private int brightness;
	private int mOldBrightness;
	private int mCurrentBrightness = 0;
	private int mOldAutomatic;
	private boolean mAutomaticAvailable;
	/**
	 * Brightness value for dim backlight
	 */
	private static final int BRIGHTNESS_DIM = 20;
	/**
	 * Brightness value for fully on
	 */
	private static final int BRIGHTNESS_ON = 255;

	// Backlight range is from 0 - 255. Need to make sure that user
	// doesn't set the backlight to 0 and get stuck
	private static final int MINIMUM_BACKLIGHT = BRIGHTNESS_DIM + 10;
	private static final int MAXIMUM_BACKLIGHT = BRIGHTNESS_ON;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		SystemManager.init(this);
		//showBrightnessSettingDialog();
		
		systemManager = SystemManager.getInstance();
		
		os_btn = (Button)findViewById(R.id.os_button);
		os_btn.setOnClickListener(osClick);
		current_btn = (Button)findViewById(R.id.current_button);
		current_btn.setOnClickListener(currentClick);
	}
	
	private OnClickListener currentClick = new OnClickListener(){
		
		@Override
		public void onClick(View v){
			 if(brightness != BRIGHTNESS_ON){
				 brightness = systemManager.getScreenBrightness();
				 systemManager.setBrightness(brigthnessActivity.this, MAXIMUM_BACKLIGHT);
			 }
		}
	};
	private  OnClickListener osClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			showBrightnessSettingDialog();
		}
	};
	
	private void showBrightnessSettingDialog() {
		 brightness = systemManager.getScreenBrightness();
		final Builder builder = new AlertDialog.Builder(this);
		final View view = getLayoutInflater().inflate(R.layout.brightness,
				null);

		// set brightness seekbar
		final SeekBar brightnessBar = (SeekBar) view
				.findViewById(R.id.brightness_bar);
		brightnessBar.setMax(MAXIMUM_BACKLIGHT - MINIMUM_BACKLIGHT);
		
		int process = brightness - MINIMUM_BACKLIGHT;
		if (process < 0) {
			process = 0;
			mOldBrightness = MINIMUM_BACKLIGHT;
			mCurrentBrightness = MINIMUM_BACKLIGHT;
		} else {
			mOldBrightness = brightness;
			mCurrentBrightness = brightness;
		}
		brightnessBar.setProgress(process);

		// set automatic available checkbox
		final CheckBox autoBrightness = (CheckBox) view
				.findViewById(R.id.auto_brightness);
		mOldAutomatic = systemManager.getBrightnessMode();
		mAutomaticAvailable = systemManager.isAutoBrightness();
		autoBrightness.setChecked(mAutomaticAvailable);
		if (mAutomaticAvailable) {
			brightnessBar.setVisibility(View.GONE);
		} else {
			brightnessBar.setVisibility(View.VISIBLE);
		}
		autoBrightness
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(
							final CompoundButton buttonView,
							final boolean isChecked) {
						mAutomaticAvailable = isChecked;
						if (isChecked) {
							brightnessBar.setVisibility(View.GONE);
							systemManager.startAutoBrightness();
							// systemManager.setBrightness(MainActivity.this,
							// systemManager.getScreenBrightness());
							int process = systemManager.getScreenBrightness()
									- MINIMUM_BACKLIGHT;
							if (process < 0) {
								process = 0;
							}// end if
							brightnessBar.setProgress(process);
						} else {
							brightnessBar.setVisibility(View.VISIBLE);
							systemManager.stopAutoBrightness();
							// systemManager.setBrightness(MainActivity.this,
							// systemManager.getScreenBrightness());
							int process = systemManager.getScreenBrightness()
									- MINIMUM_BACKLIGHT;
							if (process < 0) {
								process = 0;
							}// end if
							brightnessBar.setProgress(process);
						}
					}

				});

		brightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(final SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(final SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(final SeekBar seekBar,
					final int progress, final boolean fromUser) {
				mCurrentBrightness = progress + MINIMUM_BACKLIGHT;
				systemManager.setBrightness(brigthnessActivity.this,
						mCurrentBrightness);
			}
		});

		builder.setTitle(R.string.os_brightness);
		builder.setView(view);
		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						// set brightness
						if (mAutomaticAvailable) {
							systemManager.saveBrightness(systemManager
									.getScreenBrightness());
						} else {
							systemManager.saveBrightness(mCurrentBrightness);
						}
					}

				});
		builder.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						// recover brightness
						systemManager.setBrightness(brigthnessActivity.this,
								mOldBrightness);
						systemManager.saveBrightness(mOldBrightness);
						// recover automatic brightness mode
						systemManager.setBrightnessMode(mOldAutomatic);
					}
				});

		builder.show();
	}
	}
	

