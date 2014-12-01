package com.sencloud.mobilemonitoring;

import com.sencloud.cameratest.R;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import android.view.animation.Animation.AnimationListener;  

public class LoadingActivity extends Activity {
	private ImageView  loadingImg;
//	private Animation mAnimation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        /*
		boolean isConnected = Utils.isNetworkConnected(this);
		if (!isConnected) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setIcon(android.R.drawable.ic_dialog_info);
			dialog.setTitle("警告");
			dialog.setMessage("当前网络不可用，请连通网络再启动");
			dialog.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
							System.exit(0);
						}
					});
			dialog.show();
		} else {
         */
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.loading);
			loadingImg = (ImageView) findViewById(R.id.loadingImg);
//			mAnimation = AnimationUtils.loadAnimation(this,
//					R.anim.rotate_progress);
//			mAnimation.setInterpolator(new LinearInterpolator());
//			mAnimation.setDuration(3000);
//			mRetote.startAnimation(mAnimation);
//			mAnimation.start();
			
			AlphaAnimation anima = new AlphaAnimation(0.3f, 1.0f);  
	        anima.setDuration(2000);// 设置动画显示时间  
	        loadingImg.startAnimation(anima);  
	        anima.setAnimationListener(new AnimationImpl());  
			
//			Intent intent = new Intent();
//			intent.setClass(LoadingActivity.this, LoginActivity.class);
//			startActivity(intent);
//			LoadingActivity.this.finish();
		}
	
	private class AnimationImpl implements AnimationListener {  
		  
        @Override  
        public void onAnimationStart(Animation animation) {  
        	loadingImg.setBackgroundResource(R.drawable.loading);  
        }  
  
        @Override  
        public void onAnimationEnd(Animation animation) {  
        	
        	Intent intent = new Intent();
    		intent.setClass(LoadingActivity.this, LoginActivity.class);
    		startActivity(intent);
    		LoadingActivity.this.finish(); 
        }  
  
        @Override  
        public void onAnimationRepeat(Animation animation) {  
  
        }  
    }  

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// 这里什么都不要做
		super.onConfigurationChanged(newConfig);
	}

}
