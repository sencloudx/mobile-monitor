package com.sencloud.mobilemonitoring;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class MyThread implements Runnable{

	private static final String TAG = MyThread.class.getSimpleName();
	
	private byte byteBuffer[] = new byte[2048];
	private OutputStream outsocket;
	// private ByteArrayOutputStream myoutputstream;
	private String ipname;
	private ArrayList<ByteArrayOutputStream> uploadQueue;
	public boolean isStop;

	public void setStop(boolean isStop) {
		this.isStop = isStop;
	}

	public MyThread(String ipname) {
		this.ipname = ipname;
		uploadQueue = new ArrayList<ByteArrayOutputStream>();
	}

	public void addStream(ByteArrayOutputStream stream) {
		uploadQueue.add(stream);
	}

	public void run() {

//		Log.i(TAG, "MyThread.run start" + isStop);
		while (!isStop) {
			if (uploadQueue.size() > 0) {
				try { // 将图像数据通过Socket发送出去

					ByteArrayOutputStream stream = uploadQueue.get(0);

					Socket tempSocket = new Socket(ipname, 6000);
					outsocket = tempSocket.getOutputStream();
					ByteArrayInputStream inputstream = new ByteArrayInputStream(
							stream.toByteArray());
					int amount;
					while ((amount = inputstream.read(byteBuffer)) != -1) {
						outsocket.write(byteBuffer, 0, amount);
					}
					stream.flush();
					stream.close();
					tempSocket.close();
					uploadQueue.remove(0);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				try {
					Thread.sleep(6000);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
