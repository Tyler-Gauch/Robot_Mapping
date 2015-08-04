package com.mapping.main;

import java.awt.Graphics;
import java.util.ArrayList;


/*
 * Super class for all models passed to the ransac algorithm
 */
abstract public class Model {
	
	private ArrayList<DataPoint> inliers;
	
	public Model()
	{
		inliers = new ArrayList<DataPoint>();
	}
	
	public void setInliers(ArrayList<DataPoint> inliers)
	{
		this.inliers = inliers;
	}
	public void setInliers(DataPoint[] inliers)
	{
		for(int i = 0; i < inliers.length; i++)
		{
			this.inliers.add(inliers[i]) ;
		}
	}
	
	public ArrayList<DataPoint> getInliers()
	{
		return this.inliers;
	}
	
	
	abstract public double fit(ArrayList<DataPoint> points);
	abstract public int getDistance(int x,int y);
	abstract public void draw(Graphics g, Map m);
	abstract public boolean equals(Model m); 
	abstract public String toString();
}
