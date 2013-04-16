package finallab;

import java.awt.image.BufferedImage;

import java.util.*;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.io.*;
import javax.swing.JFrame;

import org.openkinect.freenect.Context;
import org.openkinect.freenect.Device;
import org.openkinect.freenect.Freenect;

import april.util.JImage;

import finallab.CalibrateCam.WindowClose;
import finallab.lcmtypes.ball_t;

public class VideoTest {

	Context ctx;
	Device kinect;
	JFrame windowRGB;
	JFrame windowDepth;
	KinectRGBVideo rgbVideo;
	KinectDepthVideo depthVideo;
	
	JFrame unionFindWindow;
	JImage ballJim;
	BufferedImage ballImage;
	boolean newImage = false;
	Statistics BALL;
	
	public VideoTest() {
		ctx = Freenect.createContext();
		if (ctx.numDevices() > 0) {
			kinect = ctx.openDevice(0);
		} else {
			System.err.println("WARNING: No kinects detected");
		}
		
		
		windowRGB = new JFrame("RGB camera");
		windowRGB.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		rgbVideo = new KinectRGBVideo(kinect,true);
		windowRGB.setSize(640, 480);
		windowRGB.setContentPane(rgbVideo);
		windowRGB.setVisible(true);
		
		windowDepth = new JFrame("Depth camera");
		windowDepth.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		depthVideo = new KinectDepthVideo(kinect, true);
		windowDepth.setSize(640, 480);
		windowDepth.setContentPane(depthVideo);
		windowDepth.setVisible(true);
		
		
		
	}
	public static void main(String[] args) {
		final VideoTest vt = new VideoTest();
		// depthUnionFindTest(vt);
		RGBtoDepthTest(vt);
		

	}
	public static void depthUnionFindTest(VideoTest vt) {
		vt.ballImage = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
		vt.ballJim = new JImage();
		vt.ballJim.setImage(vt.ballImage);
		vt.unionFindWindow = new JFrame("union find");
		vt.windowRGB.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		vt.unionFindWindow.setSize(640, 480);
		vt.unionFindWindow.add(vt.ballJim);
		vt.unionFindWindow.setVisible(true);
		BallTracker tracker = new BallTracker(KinectVideo.WIDTH, KinectVideo.HEIGHT,true);
		while(vt.depthVideo.getBuf() == null);
		while(true) 
		{
//			while(!vt.newImage);
			vt.newImage = false;
			ArrayList<Statistics> blobs = tracker.analyzeDepth(vt.depthVideo.getBuf());
			Statistics BiggestBlob = new Statistics();
			for(Statistics blob : blobs)
			{
				if(blob.N > 10)
				{
					// if(kv.BALL.Cxy() > blob.Cxy());
						// if(kv.BALL.abs() > blob.abs())
							// kv.BALL = blob;
					if(BiggestBlob.N < blob.N)
						BiggestBlob = blob;
					blob.center();
				}
			}
			System.out.println("num blobs: " + blobs.size());
			vt.BALL = BiggestBlob;
			vt.BALL.center();
			for(int y = vt.BALL.center_y-3; y < vt.BALL.center_y+3; y++) {
				for(int x = vt.BALL.center_x-3; x < vt.BALL.center_x+3;x++) {
					try{
						vt.ballImage.setRGB(x,y,0xFFFF0000);
						// int depthx = intoDepthY(y);
						// int depthy = intoDepthX(x);
						// kv.depthImg.setRGB(depthx, depthy, 0xFFFFFFFF);
					}
					catch(Exception e){};
				}
			}
		

			vt.ballJim.setImage(vt.ballImage);
			// try {
			// 	Thread.sleep(100);
			// }
			// catch(Exception e) {
				
		}
	}
	public static void drawCircle(Point p, BufferedImage img) {
		for(int y = p.y-2; y < p.y+2; y++) {
			for(int x = p.x-2; x < p.x+2;x++) {
				// System.out.println("x: " + x + ", y: " + y);
				try{
					img.setRGB(x,y,0xFFFF0000);
					// kv.depthImg.setRGB(x,y,0xFFFFFFFF);
				}
				catch(Exception e){};
			}
		}
	}
	public static void RGBtoDepthTest(final VideoTest vt) {
		MouseListener ml = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				vt.depthVideo.pause();
				vt.rgbVideo.pause();
				Point p = e.getPoint();
				//pixel points of click
				int pX = p.x - KinectVideo.C_X;
				int pY = KinectVideo.C_Y - p.y;
				drawCircle(p, vt.rgbVideo.getFrame());
				vt.rgbVideo.repaint();

				Point clickCoord = new Point(pX, pY);
				System.out.println("*******CLICKED COORD*******:  (" + pX + ", " + pY + ")");

				for (double z = .3; z < 5.0; z+=0.02) {
					Point3D world = vt.rgbVideo.getWorldCoords(clickCoord, z);
					// System.out.println("rgb world " + world.x + ", " + world.y + ", " + world.z);
					world.x += KinectVideo.RGB_DEPTH_DIST;
					Point depthCoord = vt.depthVideo.getPixFromWorld(world);
					Point depthPix = new Point();
					depthPix.x = depthCoord.x + KinectVideo.C_X;
					depthPix.y = KinectVideo.C_Y - depthCoord.y;
					drawCircle(depthPix, vt.depthVideo.getFrame());
					vt.depthVideo.repaint();

					// System.out.println(depthCoord.x + ", " + depthCoord.y);
					try {
						world = vt.depthVideo.getWorldCoords(depthCoord);
					}
					catch(Exception ex) {
						System.out.println("depthCoord out of bounds");
						// ex.printStackTrace();
					}
					world.x -= KinectVideo.RGB_DEPTH_DIST;
					Point rgbCoord = vt.rgbVideo.getPixFromWorld(world);
					Point rgbPix = new Point();
					rgbPix.x = rgbCoord.x + KinectVideo.C_X;
					rgbPix.y = KinectVideo.C_Y - rgbCoord.y;
					System.out.println("@" + z + ": (" + rgbCoord.x + ", " + rgbCoord.y + ")");
					drawCircle(rgbPix, vt.rgbVideo.getFrame());
					vt.rgbVideo.repaint();
				}
				//real world points of click
				// double rZ = .85;
				
				
			}
		};
		vt.rgbVideo.addMouseListener(ml);

	}

}
