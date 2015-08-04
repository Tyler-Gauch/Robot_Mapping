package com.mapping.main;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class Landmark {

	private LineModel model;
	private Point maxEndPoints;
	private Point minEndPoints;
	private int timesFound;
	public static ArrayList<Landmark> allLandmarks = new ArrayList<Landmark>();
	public static ArrayList<Landmark> bestLandmarks = new ArrayList<Landmark>();
	public static final int BEST_LANDMARK_THRESHOLD = 15;
	public UUID uuid;
	private boolean isBestLandmark;
	private static boolean debug = false;
	
	public Landmark(LineModel m)
	{
		allLandmarks.add(this);
		isBestLandmark = false;
		uuid = new UUID((long)new Random().nextInt(100000), (long)new Random().nextInt(100000));
		this.model = m;
	}
	
	public LineModel getModel()
	{
		return this.model;
	}
	
	public void setModel(LineModel m)
	{
		this.model = m;
	}
	
	public Point getMaxEndPoints()
	{
		return this.maxEndPoints;
	}
	public void setMaxEndPoints(Point p)
	{
		maxEndPoints = p;
	}
	public Point getMinEndPoints()
	{
		return this.minEndPoints;
	}
	public void setMinEndPoints(Point p)
	{
		minEndPoints = p;
	}
	
	public int getTimesFound()
	{
		return timesFound;
	}
	public void found()
	{
		this.timesFound++;
		if(!this.isBestLandmark && this.timesFound >= BEST_LANDMARK_THRESHOLD)
		{
			bestLandmarks.add(this);
			this.isBestLandmark = true;
		}
	}
	public UUID getUuid()
	{
		return this.uuid;
	}
	
	public static Landmark findOrCreateLandmark(LineModel line)
	{
		for(Landmark landmark : allLandmarks)
		{
			if(landmark.getModel().equals(line))
			{
				if(landmark.getModel().getInliers().size() < line.getInliers().size())
				{
					landmark.setModel(line);
				}
				print("LANDMARK: "+landmark.getUuid().toString()+" found -- Line: "+landmark.getModel().toString()+"--- total: "+Landmark.bestLandmarks.size());
				return landmark;
			}
		}
		Landmark newLandmark = new Landmark(line);
		print("LANDMARK: "+newLandmark.getUuid().toString()+" created -- Line: "+newLandmark.getModel().toString()+"--- total: "+Landmark.bestLandmarks.size());
		return newLandmark;
	}
	
	private static void print(String message)
	{
		if(debug)
		{
			System.out.println(message);
		}
	}
	
	public static void removeLandMark(Landmark landmark)
	{
		for(int i = 0; i < Landmark.bestLandmarks.size(); i++)
		{
			if(Landmark.bestLandmarks.get(i).getUuid().equals(landmark.getUuid()))
			{
				Landmark.bestLandmarks.remove(i);
				break;
			}
		}
		for(int i = 0; i < Landmark.allLandmarks.size(); i++)
		{
			if(Landmark.allLandmarks.get(i).getUuid().equals(landmark.getUuid()))
			{
				Landmark.allLandmarks.remove(i);
				break;
			}
		}
	}
}
