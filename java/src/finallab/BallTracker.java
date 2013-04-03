package finallab;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;

import april.util.*;
import april.jmat.*;
import april.jmat.geom.*;

import java.awt.*;
import java.awt.image.*;

import finallab.lcmtypes.*;

public class BallTracker
{
	int width;
	int height;
	int size;
	UnionFind finder;

	BufferedImage output;
	JFrame outputFrame;
	JImage outputImage;

	ParameterGUI pg;

	public BallTracker(int _width, int _height)
	{
		width = _width;
		height = _height;
		size = width*height;

		pg = new ParameterGUI();
		pg.addIntSlider("AbsThres","AbsThres",0,200,0);

		output = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
		outputFrame = new JFrame("KinectView");
		outputImage = new JImage();
		outputFrame.setLayout(new GridLayout(1,2));
		outputFrame.add(outputImage, 0, 0);
		outputFrame.add(pg,0,1);
		outputFrame.setSize(800,480);
		outputFrame.setVisible(true);
		outputFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public ArrayList<Statistics> analyze2(boolean[] thresholdMap)
	{
		finder = new UnionFind(size);
		HashMap <Integer, Statistics> map = new HashMap<Integer, Statistics>();
		int y = 0;
		for(int x = 0; x < width; x++)
		{
			int access = x;
			int plusX = x+1;
			int plusY = width+x;
			if(thresholdMap[access])
			{
				output.setRGB(x,y,0xFFFF0000);
				if((x != width-1) && thresholdMap[plusX])
					finder.join(access,plusX);
				if(thresholdMap[plusY])
					finder.join(access,plusY);
			}
			else
			{
				output.setRGB(x,y,0xFFFFFFFF);
			}
		}
		for(y = 1; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				int delayedPointer = (y-1)*width+x;
				int access = y*width+x;
				int plusX = y*width+x+1;
				int plusY = (y+1)*width+x;
				if(thresholdMap[access])
				{
					output.setRGB(x,y,0xFFFF0000);
					if((x != width-1) && thresholdMap[plusX])
						finder.join(access,plusX);
					if((y != height-1) && thresholdMap[plusY])
						finder.join(access,plusY);
				}
				else
				{
					output.setRGB(x,y,0xFFFFFFFF);
				}
				if(!thresholdMap[delayedPointer])
					continue;
				if(finder.find(delayedPointer) == delayedPointer)
				{
					Statistics input = new Statistics();
					input.update(x,y);
					map.put(delayedPointer, input);
				}
				else if(map.containsKey(finder.find(delayedPointer)))
				{
					Statistics output = map.get(finder.find(delayedPointer));
					output.update(x,y);
					map.put(finder.find(delayedPointer),output);
				}
			}
		}
		y = height-1;
		for(int x = 0; x < width; x++)
		{
			int delayedPointer = y*width+x;
			if(!thresholdMap[delayedPointer])
				continue;
			if(finder.find(delayedPointer) == delayedPointer)
			{
				Statistics input = new Statistics();
				input.update(x,y);
				map.put(delayedPointer, input);
			}
			else if(map.containsKey(finder.find(delayedPointer)))
			{
				Statistics output = map.get(finder.find(delayedPointer));
				output.update(x,y);
				map.put(finder.find(delayedPointer),output);
			}
		}
		Iterator obIter = map.keySet().iterator();
		ArrayList<Statistics> blobs = new ArrayList<Statistics> ();
		while(obIter.hasNext())
		{
			Integer key = (Integer) obIter.next();
			Statistics value = (Statistics) map.get(key);
			blobs.add(value);
		}
		outputImage.setImage(output);
		return blobs;
	}

	public ArrayList<Statistics> analyze(boolean[] thresholdMap)
	{
		finder = new UnionFind(size);
		HashMap <Integer, Statistics> map = new HashMap<Integer, Statistics>();
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				int access = y*width+x;
				int plusX = y*width+x+1;
				int plusY = (y+1)*width+x;
				if(thresholdMap[access])
				{
					output.setRGB(x,y,0xFFFF0000);
					if((x != width-1) && thresholdMap[plusX])
						finder.join(access,plusX);
					if((y != height-1) && thresholdMap[plusY])
						finder.join(access,plusY);
				}
				else
				{
					output.setRGB(x,y,0xFFFFFFFF);
				}
			}
		}
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				int access = y*width+x;
				if(!thresholdMap[access])
					continue;
				if(finder.find(access) == access)
				{
					Statistics input = new Statistics();
					input.update(x,y);
					map.put(access, input);
				}
				else if(map.containsKey(finder.find(access)))
				{
					Statistics output = map.get(finder.find(access));
					output.update(x,y);
					map.put(finder.find(access),output);
				}
			}
		}
		Iterator obIter = map.keySet().iterator();
		ArrayList<Statistics> blobs = new ArrayList<Statistics> ();
		while(obIter.hasNext())
		{
			Integer key = (Integer) obIter.next();
			Statistics value = (Statistics) map.get(key);
			blobs.add(value);
		}
		outputImage.setImage(output);
		return blobs;
	}

	public ArrayList<Statistics> analyzeDepth(ByteBuffer buf) {
		finder = new UnionFind(size);
		HashMap <Integer, Statistics> map = new HashMap<Integer, Statistics>();
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				int access = y*width+x;
				int plusX = y*width+x+1;
				int plusY = (y+1)*width+x;

				if((x != width-1)) {
					if (Math.abs(getDepth(buf, access) - getDepth(buf, plusX)) < 6) {
						finder.join(access,plusX);
						output.setRGB(x + 1,y,0xFFFF0000);
					}
					else {
						output.setRGB(x + 1,y,0x00000000);
					}
				}
				if((y != height-1)) {
					if (Math.abs(getDepth(buf, access) - getDepth(buf, plusY)) < 6) {
						finder.join(access,plusY);
						output.setRGB(x,y + 1,0xFFFF0000);
					}
					else {
						output.setRGB(x,y+1,0x00000000);
					}
					
				}
			}

		}
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				int access = y*width+x;
				if(finder.find(access) == access)
				{
					Statistics input = new Statistics();
					input.update(x,y);
					map.put(access, input);
				}
				else if(map.containsKey(finder.find(access)))
				{
					Statistics output = map.get(finder.find(access));
					output.update(x,y);
					map.put(finder.find(access),output);
				}
			}
		}
		Iterator obIter = map.keySet().iterator();
		ArrayList<Statistics> blobs = new ArrayList<Statistics> ();
		while(obIter.hasNext())
		{
			Integer key = (Integer) obIter.next();
			Statistics value = (Statistics) map.get(key);
			int absThres = pg.gi("AbsThres");
			if(value.abs() < absThres)
				blobs.add(value);
		}
		outputImage.setImage(output);
		return blobs;
	
	}

	public ArrayList<Statistics> analyzeDepthPartition(ByteBuffer buf, Point poi, int bound) {
		finder = new UnionFind(bound*bound);
		int startX = poi.x-bound/2;
		int startY = poi.y-bound/2;
		int endX = poi.x+bound/2;
		int endY = poi.y+bound/2;
		HashMap <Integer, Statistics> map = new HashMap<Integer, Statistics>();

		for(int y = startY; y < endY; y++)
		{
			for(int x = startX; x < endX; x++)
			{
				int access = (y-startY)*width+(x-startX);
				int plusX = (y-startY)*width+(x+1-startX);
				int plusY = (y-startY+1)*width+(x-startX);

				if((x != width-1)) {
					if (Math.abs(getDepth(buf, (y*width+x)) - getDepth(buf, y*width+(x+1))) < 6) {
						finder.join(y*width+x,(y*width+x+1));
						output.setRGB(x + 1,y,0xFFFF0000);
					}
					else {
						output.setRGB(x + 1,y,0x00000000);
					}
				}
				if((y != height-1)) {
					if (Math.abs(getDepth(buf, (y*width+x)) - getDepth(buf, ((y+1)*width+x))) < 6) {
						finder.join(access,plusY);
						output.setRGB(x,y + 1,0xFFFF0000);
					}
					else {
						output.setRGB(x,y+1,0x00000000);
					}
					
				}
			}

		}
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				int access = y*width+x;
				if(finder.find(access) == access)
				{
					Statistics input = new Statistics();
					input.update(x,y);
					map.put(access, input);
				}
				else if(map.containsKey(finder.find(access)))
				{
					Statistics output = map.get(finder.find(access));
					output.update(x,y);
					map.put(finder.find(access),output);
				}
			}
		}
		Iterator obIter = map.keySet().iterator();
		ArrayList<Statistics> blobs = new ArrayList<Statistics> ();
		while(obIter.hasNext())
		{
			Integer key = (Integer) obIter.next();
			Statistics value = (Statistics) map.get(key);
			int absThres = pg.gi("AbsThres");
			if(value.abs() < absThres)
				blobs.add(value);
		}
		outputImage.setImage(output);
		return blobs;
	
	}


	public ArrayList<Statistics> analyzePartition(boolean[] thresholdMap, Point poi, int xlength, int ylength, String pointOfInterest)
	{
		int startX;
		int startY;
		int endX;
		int endY;

		if(pointOfInterest.equals("topLeft"))
		{
			startX = poi.x;
			startY = poi.y;
			endX = poi.x+xlength;
			endY = poi.y+ylength;
		}
		else if(pointOfInterest.equals("topRight"))
		{
			startX = poi.x-xlength;
			startY = poi.y;
			endX = poi.x;
			endY = poi.y+ylength;
		}
		else if(pointOfInterest.equals("center"))
		{
			startX = poi.x-xlength/2;
			startY = poi.y-ylength/2;
			endX = poi.x+xlength/2;
			endY = poi.y+ylength/2;
		}
		else if(pointOfInterest.equals("bottomLeft"))
		{
			startX = poi.x;
			startY = poi.y-ylength;
			endX = poi.x+xlength;
			endY = poi.y;

		}
		else if(pointOfInterest.equals("bottomRight"))
		{
			startX = poi.x-xlength;
			startY = poi.y-ylength;
			endX = poi.x;
			endY = poi.y;
		}
		else
		{
			System.out.println("Invalid Option for poi");
			return null;
		}
		
		finder = new UnionFind(xlength*ylength);
		HashMap <Integer, Statistics> map = new HashMap<Integer, Statistics>();
		for(int y = startY; y < endY; y++)
		{
			for(int x = startX; x < endX; x++)
			{
				int access = (y-startY)*xlength+(x-startX);
				int plusX = (y-startY)*xlength+(x+1-startX);
				int plusY = (y-startY+1)*xlength+(x-startX);
				// try{
				if((x < 0) || (x >= width) || (y < 0) || (y >= height))
					continue;

				if(thresholdMap[y*width+x])
				{
					output.setRGB(x,y,0xFFFF0000);
					if((plusX < xlength*ylength) && (x != width-1) && thresholdMap[y*width+x+1])
						finder.join(access,plusX);
					if((plusY < xlength*ylength) && (y != height-1) && thresholdMap[(y+1)*width+x])
						finder.join(access,plusY);
				}
				else
				{
					output.setRGB(x,y,0xFFFFFFFF);
				}
			}
				// catch(Exception e)
				// {
				// 	e.printStackTrace();
				// 	System.out.println("x: "+x+" y: "+ y + " plusY: " + plusY);
				// }
		}
		for(int y = startY; y < endY; y++)
		{
			for(int x = startX; x < endX; x++)
			{
				if((x < 0) || (x >= width) || (y < 0) || (y >= height))
					continue;
				int access = (y-startY)*xlength+(x-startX);
				if(!thresholdMap[y*width+x])
					continue;
				if(finder.find(access) == access)
				{
					Statistics input = new Statistics();
					input.update(x,y);
					map.put(access, input);
				}
				else if(map.containsKey(finder.find(access)))
				{
					Statistics output = map.get(finder.find(access));
					output.update(x,y);
					map.put(finder.find(access),output);
				}
			}
		}
		Iterator obIter = map.keySet().iterator();
		ArrayList<Statistics> blobs = new ArrayList<Statistics> ();
		while(obIter.hasNext())
		{
			Integer key = (Integer) obIter.next();
			Statistics value = (Statistics) map.get(key);
			blobs.add(value);
		}
		outputImage.setImage(output);
		return blobs;

	}


	public int getDepth(ByteBuffer bb, int index) {
		int depth = 0;
		byte byte1 = bb.get(index * 2);
		byte byte2 = bb.get(index * 2 + 1);
		depth = byte2 & 0x3;
		depth = depth << 8;
		depth = depth | (byte1 & 0xFF);
		return depth & 0x3FF;
	}
}
