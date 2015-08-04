package com.mapping.main;

public class DataPoint {
	public int x;
	public int y;
	public int angle;
	public boolean in_ransac = false;
	
	
	public DataPoint(int x, int y, int angle)
	{
		this.x = x;
		this.y = y;
		this.angle = angle;
		this.in_ransac = false;
	}
	
	public String toString()
	{
		return "(" + x + ", " + y + ")-"+ angle;
	}
	
	public boolean equals(DataPoint p)
	{
		if(this.x == p.x && this.y == p.y)
		{
			return true;
		}
		return false;
	}
}
