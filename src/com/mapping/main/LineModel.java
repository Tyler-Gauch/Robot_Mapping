package com.mapping.main;

import java.awt.Graphics;
import java.util.ArrayList;


/*
 * This object holds the information for RANSAC to extract lines from the lidar points
 */

public class LineModel extends Model{
	public double a;	//line equation
	public double b;	//line equation
	public double c;	//line equation
	public int maxX = 0, minX = 0, maxY = 0, minY = 0; //endpoints
	
	/*
	 * Given an x value its the point on that line
	 */
	public int getPointOnLine(int x)
	{
		double slope = -a/b;
		double tm = 0,tb = 0;
		double y;
		
		if(slope >= -1 && slope <= 1)
		{
			tm = -a/b;
			tb = -c/b;
			y = tm * x + tb;
		}
		else
		{
			tm = -b/a;
			tb = -c/a;
			y = (x - tb)/tm;
		}
		return (int)y;
	}
	
	/*
	 * draws the given line on the Map
	 */
	@Override
	public void draw(Graphics g, Map m)
	{
		//y = mx + b;
		double slope = -a/b;
		double tm = 0,tb = 0;
		double x1 = 0,x2 = 0,y1 = 0,y2 =0;
		
		if(slope >= -1 && slope <= 1)
		{
			tm = -a/b;
			tb = -c/b;
			x1 = minX;
			y1 = tm * x1 + tb;
			x2 = maxX;
			y2 = tm * x2 + tb;
		}
		else
		{
			tm = -b/a;
			tb = -c/a;
			y1 = minY;
			x1 = tm * y1 + tb;
			y2 = maxY;
			x2 = tm * y2 + tb;
		}
		g.drawLine(m.convertToGlobalPoint(x1, true),
				m.convertToGlobalPoint(y1, false),
				m.convertToGlobalPoint(x2, true),
				m.convertToGlobalPoint(y2, false));
	}

	
	/*
	 * Uses the Least Squares Approximation to fit a line to a set points
	 */
	@Override
	public double fit(ArrayList<DataPoint> points) {
		int n = points.size();
		double ss_xx = 0, ss_yy = 0, ss_xy = 0, mean_x = 0, mean_y = 0;
		
		for(DataPoint d : points)
		{			ss_xx += Math.pow(d.x, 2);
			ss_yy += Math.pow(d.y, 2);
			ss_xy += d.x*d.y;
			mean_x += d.x;
			mean_y += d.y;
		}
		
		mean_x /= n;
		mean_y /= n;
		ss_xx -= n* Math.pow(mean_x, 2);
		ss_yy -= n * Math.pow(mean_y, 2);
		ss_xy -= n * mean_x * mean_y;
		
		
		double r2 = Math.pow(ss_xy, 2) / (ss_xx * ss_yy);
		
		double slope = ss_xy / ss_xx;
		if(slope >= -1 && slope <= 1)
		{
			//line is horizontal
			double m = ss_xy / ss_xx;
			double b = mean_y - m * mean_x;
			this.a = m;
			this.b = -1;
			this.c = b;
		}
		else
		{
			//vertical line
			double rm = ss_xx /ss_xy;
			double rb = mean_x - rm * mean_y;
			this.a = -1;
			this.b = rm;
			this.c = rb;
		}
		return r2;
	}
	
	/*
	 * Returns the Distance of a point to the line
	 */
	public int getDistance(int x, int y)
	{
		return (int)((Math.abs(a*x + b*y + c))/(Math.sqrt(a*a + b*b)));
	}
	
	
	@Override
	public void setInliers(ArrayList<DataPoint> inliers)
	{
		super.setInliers(inliers);
		findEndPoints(true);
		findEndPoints(false);
	}
	
	/*
	 * Finds the end points min and max of the line in the global system
	 */
	public void findEndPoints(boolean isX)
	{
		int i = 0;
		for(DataPoint p : this.getInliers())
		{
			if(i == 0)
			{
				this.maxX = p.x;
				this.maxY = p.y;
				this.minX = p.x;
				this.minY = p.y;
			}
			else
			{
				if(p.x < this.minX)
				{
					minX = p.x;
				}
				if(p.x > this.maxX)
				{
					maxX = p.x;
				}
				if(p.y < this.minY)
				{
					minY = p.y;
				}
				if(p.y > this.maxY)
				{
					maxY = p.y;
				}
			}
			i++;
		}
	}
	/*
	 *If the line equation is the same the line is the same
	 */
	@Override
	public boolean equals(Model m)
	{
		LineModel l = (LineModel)m;
		if(this.a == l.a && this.c == l.c && this.b == l.b)
		{
			return true;
		}
		return false;
	}
	
	@Override
	public String toString()
	{
		return this.a + "y + " + this.b + "x = " + this.c;
	}
}
