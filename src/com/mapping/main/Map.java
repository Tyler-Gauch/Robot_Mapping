package com.mapping.main;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Map extends JPanel implements ActionListener
{

	public static DataPoint[] points;
	private JButton clear;
	private int[] lastPoint;
	private Main m;
	private static final int pointSize = 5;
	private static final boolean showPoints = false;
	public static int currentPoints = 0;
	public static int scaleSize = 100;
	public static LineModel[] linesBeingCheckd = new LineModel[2];
	public Map(Main m)
	{
		this.m = m;
		this.init(500,500);
	}

	public Map(int x, int y, Main m)
	{
		this.m= m;
		this.init(x, y);
	}

	private void init(int x, int y)
	{
		this.setPreferredSize(new Dimension(x,y));
		this.add(new JLabel("Mapping"));
		points = new DataPoint[181];
		for(int i = 0; i < 181; i++)
		{
			points[i] = null;
		}
		lastPoint = new int[2];
		clear = new JButton("Clear");
		clear.addActionListener(this);
		this.add(clear);
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;

		g2d.setColor(Color.BLACK);
		g2d.fillRect(this.convertToGlobalPoint(0, true), this.convertToGlobalPoint(0, false), 10, 10);
		
        g2d.setPaint(Color.blue);

        //draw Scale
        g2d.drawLine(10, 10, scaleSize, 10);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 8));
        for(int i = 0; i < scaleSize/10; i++)
        {
        	g2d.drawLine(i*10+10, 8, i*10+10, 12);
        	g2d.drawString(""+(i*10), i*10+10, 20);
        }
	    for(int i = 0; i < points.length; i++)
		{	
	    	int angle = i;
	    	if(points[angle] == null)
	    	{
	    		continue;
	    	}
	    	int x = this.convertToGlobalPoint(points[angle].x,true);
	    	int y = this.convertToGlobalPoint(points[angle].y, false);
	    	
	    	//g2d.drawLine(this.getW()/2, this.getH()/2, x, y);
	    	g2d.fillRect(x, y, pointSize, pointSize);
	    	if(showPoints)
	    	{
	    		g2d.drawString("("+points[angle].x+", "+points[angle].y+")", x, y);
	    	}
		}	
	    g2d.setPaint(Color.RED);
		//g2d.drawLine(this.getW()/2, this.getH()/2, lastPoint[0]+this.getW()/2, (lastPoint[1] * -1)+this.getH()/2);
		g2d.fillRect(lastPoint[0]+this.getW()/2, (lastPoint[1] * -1)+this.getH()/2, pointSize, pointSize);
		
		LineModel l = new LineModel();
		ArrayList<DataPoint> p = new ArrayList<DataPoint>();
		for(int i = 0; i < points.length; i++)
		{
			if(points[i] != null)	
			{
				p.add(points[i]);
			}
		}
		l.fit(p);
		l.setInliers(points);
		
		g2d.setColor(Color.ORANGE);
		l.draw(g2d, this);
		g2d.setColor(Color.GREEN);
		ArrayList<Model> models = m.getRansac().bestModels;
		for(int j = 0; j < models.size(); j++)
		{
			models.get(j).draw(g2d, this);
		}
		g2d.setColor(Color.MAGENTA);
		ArrayList<Landmark> landmarks = Landmark.bestLandmarks;
		for(int j = 0; j < landmarks.size(); j++)
		{
			landmarks.get(j).getModel().draw(g2d, this);
		}
		/*g2d.setColor(Color.RED);
		if(linesBeingCheckd[0] != null)
		{
			linesBeingCheckd[0].draw(g2d, this);
			linesBeingCheckd[1].draw(g2d, this);
		}*/
		
	}
	
	public int convertToGlobalPoint(double number, boolean isX)
	{
		if(isX)
		{
			return (int)number+this.getW()/2;
		}
		else
		{
			return (int)(number*-1)+this.getH()/2;
		}
	}
	
	public void addPoint(Point p, int angle)
	{
		//System.out.println("Adding point: " + p.x + ","+p.y+" at angle " + angle);
		DataPoint d = new DataPoint(p.x,p.y,angle);
		if(points[angle] != null)
		{
			d.in_ransac = points[angle].in_ransac;
		}
		points[angle] = d;
		lastPoint[0] = p.x;
		lastPoint[1] = p.y;
		currentPoints++;
	}

	public int getH()
	{
		return this.getHeight();
	}

	public int getW()
	{
		return this.getWidth();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == clear)
		{
			this.clear();
			m.getRansac().bestModels.clear();
		}
		
	}
	public void clear()
	{
		for(int i = 0; i < points.length; i++)
		{
			points[i] = null;
		}
	}
}