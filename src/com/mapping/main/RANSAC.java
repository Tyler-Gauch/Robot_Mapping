package com.mapping.main;

import java.util.ArrayList;
import java.util.Random;

/*
 * This runs the RANSAC Algorithm on the Lidar Points
 */
public class RANSAC extends Thread{
	
	public ArrayList<Model> bestModels;		//A list of the Best models that have been found
	public double maxEvaluations = 1000;	//How man iterations to run
	public double maxSamplings = 50;		//How many samples to retrieve in each run
	public double probability = .95;		//probability of inliers
	public static int minConsensus = 30;	//The minimum number of points for it to be considered a landmark
	public static int minSameLineDistance = 50;	//The minimum distance 2 landmarks need to be from each other to be considered the same land mark
	public static int sameSampleThreshold = 10; //the minimum average distance between points to add the point to the sample
	public static int s = 5;					//the minimum number of points in a sample
	public static double t = 5;					//the max distance a point can be from a line to be considered an inlier
	public boolean debug = false;
	public static ArrayList<DataPoint> map;		//a list to hold all current points
	private static int delay = 10;				//delay of the thread
	public Model m;								//the type of model we are looking for
	private ArrayList<ArrayList<DataPoint>> usedSets; //a list to hold all the sets we have used
	
	
	public RANSAC(Model m)
	{
		this.m = m;
		bestModels = new ArrayList<Model>();
		usedSets = new ArrayList<ArrayList<DataPoint>>();
	}
	
	
	/*
	 * The main loop for the ransac algorithm
	 */
	public void run()
	{
		//Waits while we don't have enough points to get a model
		while(Map.currentPoints < minConsensus)
		{
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//creates the list
		map = new ArrayList<DataPoint>();
		//Gets the current points in from the lidar
		DataPoint[] points = this.getPoints();
		int lastPointSize = 0;
		Random r = new Random();
		r.setSeed(System.currentTimeMillis());
		
		//The main loop
		while(true)
		{
			//Clears all current best models from previous runs
			bestModels.clear();
			//Clears all current used sets from previous runs
			usedSets.clear();
			int maxInliers = 0;
			this.print("Init");
			int count = 0;
			double N = maxEvaluations;
			Model bestModel = null;
			map.clear();
			//finds all non null points
			for(int i = 0; i < points.length; i++)
			{
				if(points[i] != null && !points[i].in_ransac)
				{
					map.add(points[i]);
				}
			}
			//if we don't have enough points to make a model get a new set of points
			if(map.size() <= minConsensus || map.size() == lastPointSize)
			{
				print("Gathering new points: " + map.size() + " " + lastPointSize);
				lastPointSize = 0;
				points = this.getPoints();
				continue;
			}
			else
			{
				lastPointSize = map.size();
			}
			//loop and find the models
			while(count < N && count < maxEvaluations)
			{
				this.print("LOOP: " + count + " N: " + N + " MAX: " + maxEvaluations + " POINT SIZE: "+map.size());
				ArrayList<DataPoint> sample = new ArrayList<DataPoint>();
				Model model;
				int samplings = 0;
				if(this.m.getClass().getName().equals(LineModel.class.getName()))
				{
					model = new LineModel();
				}
				else
				{
					System.err.println("Model "+m.getClass().getName()+" is not implemented");
					break;
				}
				//get a sample set
				while(samplings < maxSamplings)
				{
					double averageDistance = 0;
					this.print("Processing Sample");
					for(int i = 0; i < s; i++)
					{
						int index = r.nextInt(map.size()-1)+1;
						/*if(sample.size() > 0)
						{
							double distance = this.getDistanceBetweenPoints(sample.get(sample.size()-1).x, sample.get(sample.size()-1).y,map.get(index).x, map.get(index).y);
							if((averageDistance == 0 &&  distance >= sameSampleThreshold) || distance <= averageDistance+sameSampleThreshold)
							{
								averageDistance += distance;
								averageDistance /= sample.size();
								sample.add(map.get(index));
							}
						}else
						{*/
							sample.add(map.get(index));
						//}
					}
					
					//make sure this set wasn't used before
					if(!degenerate(sample))
					{
						//add the set to used sets
						this.usedSets.add(sample);
						//fit a line to the sample
						model.fit(sample);
						break;
					}
					
					samplings++;
				}
				//find the inliers for the model we fit
				this.print("Finding Inliers");
				ArrayList<DataPoint> inliers = new ArrayList<DataPoint>();
				for(int i = 0; i < map.size(); i++)
				{
					//if the distance from the line is less than t then we have an inlier
					if(model.getDistance(map.get(i).x,map.get(i).y) <= t)
					{
						inliers.add(map.get(i));
					}
				}
				print("INLIERS: "+inliers.size());
				
				//if the amount of inliers is more than our last model update our bestModel
				if(inliers.size() > maxInliers && inliers.size() >= minConsensus)
				{
					this.print("More Inliers: " + inliers.size());
					maxInliers = inliers.size();
					//bestModels.clear();
					model.setInliers(inliers);
					bestModel = model;
					bestModel.fit(bestModel.getInliers());
					//mark the landmark as found
					Landmark landmark = Landmark.findOrCreateLandmark((LineModel)bestModel);
					landmark.found();
					
					//recalulate the probability of the number of loops we need to do
					double pInlier = (double)inliers.size()/ (double)map.size();
					double pNoOutliers = 1.0 - Math.pow(pInlier, s);
					
					N = Math.log(1-probability) / Math.log(pNoOutliers);
				}
				else
				{
					this.print("No new inliers");
				}
				
				count++;
				
			}
			
			if(bestModel != null)
			{
				//remove the points that are inliers to that line
				for(int i = 0; i < bestModel.getInliers().size(); i++)
				{
					DataPoint d = bestModel.getInliers().get(i);
					this.print("Removing Point: "+d.toString());
					points[d.angle] = null;
				}
				//check distance between this and other models we already have and see if it is the same model
				//if so update the model with the proper set of inliers and refit the model
				boolean found = false;
				for(Landmark landmark0 : Landmark.allLandmarks)
				{
					LineModel l = landmark0.getModel();
					LineModel l2 = (LineModel)bestModel;
					int x1 = (l.maxX + l.minX)/2;
					int x2 = (l2.maxX + l2.minX)/2;
					double distance1 = getDistanceBetweenPoints(x1, l.getPointOnLine(x1), x1, l2.getPointOnLine(x1));
					double distance2 = getDistanceBetweenPoints(x2, l.getPointOnLine(x2), x2, l2.getPointOnLine(x2));
					double distanceAverage = (distance1+distance2)/2;
					if(distanceAverage <= minSameLineDistance + 10)
						System.out.println(distanceAverage+"");
					if(distanceAverage <= RANSAC.minSameLineDistance)
					{
						Map.linesBeingCheckd[0] = l;
						Map.linesBeingCheckd[1] = l2;
						//these are the same line
						//we found this line more than once
						//remove last created landmark
						System.out.println("===========THESE ARE THE SAME LINES=======");
						Landmark landmark;
						Landmark landmark2;
						landmark = Landmark.findOrCreateLandmark(l);
						landmark2 = Landmark.findOrCreateLandmark(l2);
						if(landmark.equals(landmark2))
						{
							break;
						}
						if(landmark.getModel().getInliers().size() > landmark2.getModel().getInliers().size())
						{
							landmark.found();
							//we are now going to add the landmark2 points to the new model and refit the model
							landmark.getModel().getInliers().addAll(landmark2.getModel().getInliers());
							landmark.getModel().fit(landmark.getModel().getInliers());
							Landmark.removeLandMark(landmark2);	
						}							
						else
						{
							landmark2.found();
							//we are now going to add the landmark2 points to the new model and refit the model
							landmark2.getModel().getInliers().addAll(landmark.getModel().getInliers());
							landmark2.getModel().fit(landmark2.getModel().getInliers());
							Landmark.removeLandMark(landmark2);	
						}
						found = true;
						break;
					}
				}
				if(!found)
				{
					this.bestModels.add(bestModel);
				}
			}
			try
			{
				Thread.sleep(RANSAC.delay );
			}catch(Exception e){}
			this.print("Ransac done");
		}
	}
	
	/*
	 * Checks if the sample set has already been used
	 */
	public boolean degenerate(ArrayList<DataPoint> sample)
	{
		this.print("Checking Degenerate");
		if(this.usedSets.size() == 0)
		{
			this.print("FALSE");
			return false;
		}
		for(int i = 0; i < this.usedSets.size(); i++)
		{
			ArrayList<DataPoint> currentSet = this.usedSets.get(i);
			for(int j = 0; j < sample.size(); j++)
			{
				boolean found = false;
				for(int k = 0; k < currentSet.size(); k++)
				{
					if(currentSet.get(k).equals(sample.get(j)))
					{
						found = true;
						break;
					}
				}
				if(!found)
				{
					this.print("FALSE");
					return false;
				} 
			}
		}
		this.print("TRUE");
		return true;
	}
	
	public void print(String message)
	{
		if(this.debug)
		{
			System.out.println(message);
		}
	}
	/*
	 * Gets the current set of points from the lidar data
	 */
	public DataPoint[] getPoints()
	{
		print("GET POINTS CALLED");
		DataPoint[] d = new DataPoint[181];
		for(int i = 0; i < Map.points.length; i++)
		{
			if(Map.points[i] != null)
			{
				DataPoint newD = new DataPoint(Map.points[i].x,Map.points[i].y,Map.points[i].angle);
				d[i] = newD;
			}
			else
			{
				d[i] = null;
			}
		}
		return d;
	}
	
	public double getDistanceBetweenPoints(int x1,int y1,int x2,int y2)
	{
		return Math.sqrt(Math.pow(x1-x2, 2)+Math.pow(y1-y2, 2));
	}
}