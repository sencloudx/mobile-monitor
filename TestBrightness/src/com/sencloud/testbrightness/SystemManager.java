package com.sencloud.testbrightness;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.format.Formatter;
import android.view.WindowManager;

public class SystemManager {
	
	private Context mContext;

	private static SystemManager sInstance;

	private SystemManager(final Context context) {
		mContext = context;
	}

	/**
	 * Advice to invoke init in application.
	 * 
	 * @param context
	 * @return
	 */
	public static SystemManager init(final Context context) {
		if (null == sInstance) {
			sInstance = new SystemManager(context);
		}
		return sInstance;
	}

	/**
	 * 
	 * @return
	 */
	public static SystemManager getInstance() {
		return sInstance;
	}

	/**
	 * 判断是否开启了自动亮度调节
	 * 
	 * @param aContext
	 * @return
	 */
	public boolean isAutoBrightness() {
		boolean automicBrightness = false;
		try {
			final ContentResolver resolver = mContext.getContentResolver();
			automicBrightness = Settings.System.getInt(resolver,
					Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
		} catch (final SettingNotFoundException e) {
			e.printStackTrace();
		}
		return automicBrightness;
	}

	/**
	 * 设置屏幕亮度，这会反映到真实屏幕上
	 * 
	 * @param activity
	 * @param brightness
	 */
	public void setBrightness(final Activity activity, final int brightness) {
		final WindowManager.LayoutParams lp = activity.getWindow()
				.getAttributes();
		lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
		activity.getWindow().setAttributes(lp);
	}

	/**
	 * 保存亮度设置状态
	 * 
	 * @param resolver
	 * @param brightness
	 */
	public void saveBrightness(final int brightness) {
		final ContentResolver resolver = mContext.getContentResolver();
		final Uri uri = android.provider.Settings.System
				.getUriFor("screen_brightness");
		android.provider.Settings.System.putInt(resolver, "screen_brightness",
				brightness);
		// resolver.registerContentObserver(uri, true, myContentObserver);
		resolver.notifyChange(uri, null);
	}

	/**
	 * 开启亮度自动调节
	 * 
	 * @param activity
	 */
	public void startAutoBrightness() {
		final ContentResolver resolver = mContext.getContentResolver();
		Settings.System.putInt(resolver,
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
		final Uri uri = android.provider.Settings.System
				.getUriFor("screen_brightness");
		resolver.notifyChange(uri, null);
	}

	/**
	 * 停止自动亮度调节
	 * 
	 * @param activity
	 */
	public void stopAutoBrightness() {
		final ContentResolver resolver = mContext.getContentResolver();
		Settings.System.putInt(resolver,
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
		final Uri uri = android.provider.Settings.System
				.getUriFor("screen_brightness");
		resolver.notifyChange(uri, null);
	}

	/**
	 * 获取屏幕的亮度
	 * 
	 * @param activity
	 * @return
	 */
	public int getScreenBrightness() {
		int nowBrightnessValue = 0;
		try {
			final ContentResolver resolver = mContext.getContentResolver();
			nowBrightnessValue = android.provider.Settings.System.getInt(
					resolver, Settings.System.SCREEN_BRIGHTNESS);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return nowBrightnessValue;
	}

	/**
	 * 保存亮度的显示模式
	 * 
	 * @return
	 */
	public void setBrightnessMode(int mode) {
		Settings.System.putInt(mContext.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
	}

	/**
	 * 获取亮度的显示模式
	 * 
	 * @return
	 */
	public int getBrightnessMode() {
		try {
			return Settings.System.getInt(mContext.getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS_MODE);
		} catch (SettingNotFoundException e) {
			return Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
		}
	}

	public String formatMemorySize(final long memory) {
		return Formatter.formatFileSize(mContext, memory);
	}

}
