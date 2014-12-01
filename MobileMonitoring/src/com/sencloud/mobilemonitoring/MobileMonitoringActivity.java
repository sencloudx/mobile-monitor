package com.sencloud.mobilemonitoring;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.sencloud.cameratest.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("NewApi")
public class MobileMonitoringActivity extends Activity {

	SurfaceView sView;
	SurfaceHolder surfaceHolder;
	int screenWidth, screenHeight;
	Camera camera; // 定义系统所用的照相机
	StreamIt previewCallback;
	SimpleDateFormat formatter;
	Date curDate;
	MyThread videoThread;
	Thread th;
	byte[] data;
	boolean isPreview = false; // 是否在浏览中
	private String ipname, str;

	private TextView showTime, flash;
	private ImageView connect, exit;// 视频传输速度
	private ImageView torchSwitch;// 闪光灯

	private Timer timer = null;
	private TimerTask task = null;
	private long TimerNuit = 60;

	private Handler stepTimeHandler;
	private Message msg = null;
	int flag;// 标志是否开始录像
	long startTime = 0, time = 0;
	private ImageView tapeVideo; // 录像

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置全屏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main);

		MyApplication.getInstance().addActivity(this);

		// 当没有开始录像时，该处显示当前系统时间
		formatter = new SimpleDateFormat("HH:mm");
		curDate = new Date(System.currentTimeMillis());
		str = formatter.format(curDate);
		showTime = (TextView) findViewById(R.id.time);
		showTime.setText(str);

		// 闪光灯
		flash = (TextView) findViewById(R.id.flash);

		// 退出应用
		exit = (ImageView) findViewById(R.id.exit);
		exit.setOnClickListener(exitClick);

		// 闪光灯设置为关闭
		torchSwitch = (ImageView) findViewById(R.id.torchSwitch);
		torchSwitch.setBackgroundResource(R.drawable.off);
		torchSwitch.setOnClickListener(torchSwitchClick);

		// 录像事件
		tapeVideo = (ImageView) findViewById(R.id.tapeVideo);
		tapeVideo.setBackgroundResource(R.drawable.video);
		tapeVideo.setOnClickListener(tapeVideoClick);

		connect = (ImageView) findViewById(R.id.connect);
		connect.setImageResource(R.drawable.disconnect);

		// 获取IP地址
		Intent intent = getIntent();
		Bundle data = intent.getExtras();
		ipname = data.getString("ipname");
		videoThread = new MyThread(ipname);

		screenWidth = 1280;
		screenHeight = 800;
		sView = (SurfaceView) findViewById(R.id.sView); // 获取界面中SurfaceView组件
		surfaceHolder = sView.getHolder(); // 获得SurfaceView的SurfaceHolder

		// 为surfaceHolder添加一个回调监听器
		surfaceHolder.addCallback(new Callback() {
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				initCamera(); // 打开摄像头
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				// 如果camera不为null ,释放摄像头
				if (camera != null) {
					if (isPreview)
						camera.stopPreview();
					camera.release();
					camera = null;
				}
				System.exit(0);
			}
		});
		// 设置该SurfaceView自己不维护缓冲
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		// 记录录像时间
		stepTimeHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					time = System.currentTimeMillis() - startTime;
					if (time >= 360000000) {
						showTime.setText("00:00:00");
					}
					long hourc = time / 3600000;
					String hour = "0" + hourc;
					hour = hour.substring(hour.length() - 2, hour.length());

					long minuec = (time - hourc * 3600000) / (60000);
					String minue = "0" + minuec;
					minue = minue.substring(minue.length() - 2, minue.length());

					long secc = (time - hourc * 3600000 - minuec * 60000) / 1000;
					String sec = "0" + secc;
					sec = sec.substring(sec.length() - 2, sec.length());

					showTime.setText(hour + ":" + minue + ":" + sec);
					break;
				case 2:
					showTime.setText(str);
					break;
				}

				super.handleMessage(msg);

			}
		};
	}

	/**
	 * Sets the camera up to take preview images which are used for both preview
	 * and decoding. We detect the preview format here so that
	 * buildLuminanceSource() can build an appropriate LuminanceSource subclass.
	 * In the future we may want to force YUV420SP as it's the smallest, and the
	 * planar Y can be used for barcode scanning without a copy in some cases.
	 */

	public void setDesiredCameraParameters(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();

		if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
			// 如果是竖屏
			parameters.set("orientation", "portrait");
			// 在2.2以上可以使用
			camera.setDisplayOrientation(90);
		} else {
			parameters.set("orientation", "landscape");
			// 在2.2以上可以使用
			camera.setDisplayOrientation(0);
		}
		parameters.setPreviewSize(screenWidth, screenHeight);
		parameters.setPreviewFpsRange(20, 30); // 每秒显示20~30帧
		parameters.setPictureFormat(ImageFormat.NV21); // 设置图片格式
		parameters.setPictureSize(screenWidth, screenHeight); // 设置照片的大小
		// camera.setParameters(parameters); // android2.3.3以后不需要此行代码
	}

	private void initCamera() {
		if (!isPreview) {
			camera = Camera.open();
		}
		if (camera != null && !isPreview) {
			try {
				setDesiredCameraParameters(camera);
				camera.setPreviewDisplay(surfaceHolder); // 通过SurfaceView显示取景画面

				previewCallback = new StreamIt(ipname);
				camera.setPreviewCallback(previewCallback); // 设置回调的类
				camera.startPreview(); // 开始预览
				camera.autoFocus(null); // 自动对焦
			} catch (Exception e) {
				e.printStackTrace();
			}
			isPreview = true;
		}
	}

	private OnClickListener exitClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			MyApplication.getInstance().exit();
		}
	};

	private OnClickListener torchSwitchClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (camera != null) {

				Camera.Parameters parameters = camera.getParameters();
				if (parameters == null) {
					return;
				}

				List<String> flashModes = parameters.getSupportedFlashModes();
				// Check if camera flash exists
				if (flashModes == null) {
					// Use the screen as a flashlight (next best thing)
					return;
				}
				String flashMode = parameters.getFlashMode();
				if (!Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
					// Turn on the flash
					if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
						parameters
								.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
						camera.setParameters(parameters);
						flash.setText(R.string.close_flash);
						torchSwitch.setBackgroundResource(R.drawable.on);
					}
				} else {
					// Turn off the flash
					if (flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
						parameters
								.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
						camera.setParameters(parameters);
						flash.setText(R.string.open_flash);
						torchSwitch.setBackgroundResource(R.drawable.off);
					}
				}
			}
		}
	};

	private OnClickListener tapeVideoClick = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (flag == 0) {
				flag = 1;
				// 启用线程将图像数据发送出去
				videoThread.isStop = false;
				th = new Thread(videoThread);
				th.start();

				tapeVideo.setBackgroundResource(R.drawable.video_green);
				connect.setImageResource(R.drawable.connect);

				// 清零 开始计时
				showTime.setText("00:00:00");
				startTime = System.currentTimeMillis();

				if (null == timer) {
					if (null == task) {
						task = new TimerTask() {
							@Override
							public void run() {
								if (null == msg) {
									msg = new Message();
								} else {
									msg = Message.obtain();
								}
								msg.what = 1;
								stepTimeHandler.sendMessage(msg);
							}
						};
					}
					timer = new Timer(true);
					timer.schedule(task, TimerNuit, TimerNuit);
				}
			} else if (flag == 1) {
				flag = 0;
				tapeVideo.setBackgroundResource(R.drawable.video);
				connect.setImageResource(R.drawable.disconnect);

				try {
					if (null != task) {
						task.cancel();
						task = null;
						timer.cancel();
						timer.purge();
						timer = null;
						if (null == msg) {
							msg = new Message();
						} else {
							msg = Message.obtain();
						}
						msg.what = 2;
						stepTimeHandler.sendMessage(msg);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				videoThread.setStop(true);
			}
		}
	};

	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

	}

	class StreamIt implements Camera.PreviewCallback {
		private String ipname;

		public StreamIt(String ipname) {
			this.ipname = ipname;
		}

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {

			Size size = camera.getParameters().getPreviewSize();

			// byte[] des = new byte[data.length];
			// byte[] returnDes = rotateYUV(data, des, size.width, size.height);
			try {
				// 调用image.compressToJpeg（）将YUV格式图像数据data转为jpg格式
				YuvImage image = new YuvImage(data, ImageFormat.NV21,
						size.width, size.height, null);
				if (image != null) {
					ByteArrayOutputStream outstream = new ByteArrayOutputStream();
					image.compressToJpeg(
							new Rect(0, 0, size.width, size.height), 80,
							outstream);
					outstream.flush();
					if (flag == 0) {

						videoThread.addStream(outstream);
					}
				}
			} catch (Exception ex) {
				Log.e("Sys", "Error:" + ex.getMessage());
			}
		}

		protected void onActivityResult(int requestCode, int resultCode,
				Intent data) {

		}

	}
}
