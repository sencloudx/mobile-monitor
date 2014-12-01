package com.sencloud.server;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ImageServer {
	public static ServerSocket ss = null;

	public static void main(String args[]) throws IOException {
		ss = new ServerSocket(6000);

		final ImageFrame frame = new ImageFrame(ss);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		while (true) {
			frame.panel.getimage();
			frame.repaint();
		}
	}

}

/**
 * A frame with an image panel
 */
@SuppressWarnings("serial")
class ImageFrame extends JFrame {
	public ImagePanel panel;

	 public JButton jb;

	public ImageFrame(ServerSocket ss) {
		// get screen dimensions
		Toolkit kit = Toolkit.getDefaultToolkit();
		/**
		 * Dimension:Java的一个类，封装了一个构件的高度和宽度，这个类与一个构件的许多属性具有相关性，因此在Component类中定义多个与之有关的方法，LayoutManager接口也与一个Dimension对象有关联。Dimension类的高度和宽度值是一个整数，表明有多少个像素点。
		 * 与Dimension类相关方法：getSize()和setSize(Dimension size)。分别用来获得和设置方格的大小。
		 */
		Dimension screenSize = kit.getScreenSize();
		int screenHeight = screenSize.height;
		int screenWidth = screenSize.width;

		// center frame in screen
		setTitle("ImageTest");
		setLocation((screenWidth - DEFAULT_WIDTH) / 2,
				(screenHeight - DEFAULT_HEIGHT) / 2);
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

		// add panel to frame
		this.getContentPane().setLayout(null);
		panel = new ImagePanel(ss);
		panel.setSize(640, 480);
		panel.setLocation(0, 0);
		add(panel);
		 jb = new JButton("拍照");
		 jb.setBounds(0,480,640,50);
		 add(jb);
		 saveimage saveaction = new saveimage(ss);
		 jb.addActionListener(saveaction);
	}

	public static final int DEFAULT_WIDTH = 640;
	public static final int DEFAULT_HEIGHT = 560;
}

/**
 * A panel that displays a tiled image
 */
@SuppressWarnings("serial")
class ImagePanel extends JPanel {
	private ServerSocket ss;
	private Image image;
	private InputStream ins;

	public ImagePanel(ServerSocket ss) {
		this.ss = ss;
	}

	public void getimage() throws IOException {
		Socket s = this.ss.accept();
		System.out.println("连接成功!");
		this.ins = s.getInputStream();
		this.image = ImageIO.read(ins);
		this.ins.close();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image == null)
			return;
		g.drawImage(image, 0, 0, null);
	}

}

class saveimage implements ActionListener {
	RandomAccessFile inFile = null;
	byte byteBuffer[] = new byte[2048];
	InputStream ins;
	private ServerSocket ss;

	public saveimage(ServerSocket ss) {
		this.ss = ss;
	}

	public void actionPerformed(ActionEvent event) {
		try {
			Socket s = ss.accept();
			ins = s.getInputStream();

			// 文件选择器以当前的目录打开
			JFileChooser jfc = new JFileChooser(".");
			jfc.showSaveDialog(new javax.swing.JFrame());
			// 获取当前的选择文件引用
			File savedFile = jfc.getSelectedFile();

			// 已经选择了文件
			if (savedFile != null) {
				// 读取文件的数据，可以每次以快的方式读取数据
				try {
					inFile = new RandomAccessFile(savedFile, "rw");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}

			int amount;
			while ((amount = ins.read(byteBuffer)) != -1) {
				inFile.write(byteBuffer, 0, amount);
			}
			inFile.close();
			ins.close();
			s.close();
			javax.swing.JOptionPane.showMessageDialog(new javax.swing.JFrame(),
					"已接保存成功", "提示!", javax.swing.JOptionPane.PLAIN_MESSAGE);
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
}