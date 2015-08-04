package com.mapping.main;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Random;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class Main extends JFrame
{
	private Map map;
	private SerialTest serial;
	private RANSAC ransac;
	private static final int DELAY = 5;

	public Main()
	{
		this.init(500,500);
	}	

	public Main(int x, int y)
	{
		this.init(x,y);
	}

	public void init(int x, int y)
	{
		serial = new SerialTest();
		serial.initialize();
		ransac = new RANSAC(new LineModel());
		map = new Map(x, y, this);
		this.add(map);
		this.setLocation(0,0);
		this.setResizable(false);
		this.pack();
	}	

	public void refresh()
	{
		ransac.start();
		String l = "";
		while(true)
		{
			try
			{
	        	String lastLine = serial.lastResponse;
	        	if(l.equals(lastLine))
	        	{
	        		continue;
	        	}
	        	l = lastLine;
        		double angle = Integer.parseInt(lastLine.substring(lastLine.indexOf(":")+1, lastLine.length()));
        		double hy = Integer.parseInt(lastLine.substring(0, lastLine.indexOf(":")));
        		int a = (int)angle;

	        	this.map.addPoint(this.findXY(hy, angle), a);
			}catch(Exception e)
			{
				continue;
			}
			this.repaint();
			try
			{
				Thread.sleep(Main.DELAY);
			}
			catch(Exception e)
			{
			}
		}
	}
	
	public void testLines()
	{
		this.serial.close();
		int angle = 0;
		ransac.start();
		int points = 181/8;
		Random r = new Random();
		r.setSeed(System.currentTimeMillis());
		for(int i = 0; i < 4; i++)
		{
			int m = r.nextInt(9)+1;
			int b = r.nextInt(10);
			for(int j = -points; j < points; j++, angle++)
			{
				int y = m*j + b;
				this.map.addPoint(new Point(j,y), angle);
				this.repaint();
			}
		}
		while(true)
		{
			this.repaint();
			try
			{
				Thread.sleep(10);
			}catch(Exception e){}
		}
	}
	
	public void test2(int numberOfPoints)
	{
		this.serial.close();
		Random r = new Random();
		r.setSeed(System.currentTimeMillis());
		ransac.start();
		while(true)
		{
			for(int i = 0; i <numberOfPoints; i++)
			{
				int a = r.nextInt(180);
				map.addPoint(this.findXY(r.nextDouble()*100+100, a), a);
			}
			this.repaint();
		}
		
	}
	
	public void test3(int number)
	{
		this.serial.close();
		ransac.start();
		int angle = 0;
		for(int i = 0; i < number; i++, angle++)
		{
			map.addPoint(new Point((i)*10, i), angle);
			angle++;
			map.addPoint(new Point((i)*10, i*10), angle);
		}
		while(true)
		{
			this.repaint();
			try {
			Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void test()
	{
		this.serial.close();
		ransac.start();
		while(true)
		{
			double angle = 180;
			Random r = new Random();
			r.setSeed(System.currentTimeMillis());
			double hy;
			for(angle=180; angle >= 0; angle--)
			{
				hy = r.nextDouble()*200 + 200;
				this.map.addPoint(this.findXY(hy, angle),(int)angle);
				this.repaint();
			}
			
			try
			{
				Thread.sleep(Main.DELAY);
			}
			catch(Exception e)
			{
			}
		}
		
	}
	
	public RANSAC getRansac()
	{
		return this.ransac;
	}
	
	public Point findXY(double hy, double angle)
	{
		Point p = new Point();
		int x, y, xDir = 1, w = this.map.getW(), h = this.map.getH();
		angle = angle * (Math.PI/180);
		
		if(angle > 90)
		{
			angle = 180 - angle;
			xDir = -1;
		}
		
		x = (int)(hy*Math.cos(angle)) % w * xDir;//cos
		y = (int)(hy*Math.sin(angle)) % h;//sin
		
		p.x = x;
		p.y = y;
		
		return p;
	}

	public static void main(String[] argv)
	{
		int x = 1000;
		int y = 1000;
		if(argv.length >= 1)
		{
			x = Integer.parseInt(argv[0]);
		}
		if(argv.length >= 2)
		{
			y = Integer.parseInt(argv[1]);
		}

		Main m = new Main(x,y);
		m.setLocationRelativeTo(null);
		m.addWindowListener(new WindowListener(){

			@Override
			public void windowActivated(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				m.serial.close();
				
			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowIconified(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowOpened(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		m.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		m.setVisible(true);
		//m.refresh();
		m.testLines();
	}
}
