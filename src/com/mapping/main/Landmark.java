package com.mapping.main;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

/*
 * This class holds the information for the landmarks extracted by RANSAC
 */

public class Landmark {

	private LineModel model;		//The current Model that the landmark is of
	private Point maxEndPoints;		//The Max end points x and y of the landmark in the global system
	private Point minEndPoints;		//the min endpoints x and y of the landmark in the global system
	private int timesFound;			//how many times we have seen this landmark
	public UUID uuid;				//the unique identifier for this landmark
	private boolean isBestLandmark;	//if this is a best landmark or not
	public static ArrayList<Landmark> allLandmarks = new ArrayList<Landmark>(); //list of all found landmarks
	public static ArrayList<Landmark> bestLandmarks = new ArrayList<Landmark>();//list of all landmarks that were seen enough to be considered best landmarks
	public static final int BEST_LANDMARK_THRESHOLD = 15;	//how many times we need to see a landmark for it to be considered a best landmark
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
	
	/*
	 * given a line model it will find a landmark in the list of all landmarks that has that line model
	 * or create a new landmark
	 */
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
	
	/*
	 * Removes a landmark from both lists of landmarks
	 */
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
