package latmod.cursegraph;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.swing.*;

import latmod.cursegraph.Main.Mod;

import com.google.gson.annotations.Expose;

public class Graph
{
	public static long checkMillis = 900000L;
	
	public static File dataFile;
	public static GraphData graphData;
	public static BufferedImage image_graph;
	
	public static void init() throws Exception
	{
		dataFile = new File(Main.folder, "data.json");
		
		graphData = null;
		
		if(dataFile.exists())
		{
			try
			{
				FileInputStream fis = new FileInputStream(dataFile);
				String graphDataS = Utils.toString(fis);
				fis.close();
				
				if(graphDataS != null && !graphDataS.isEmpty())
					graphData = Utils.fromJson(graphDataS, GraphData.class);
			}
			catch(Exception e) { e.printStackTrace(); }
		}
		
		image_graph = Main.loadImage("graph_background.png");
		
		new Checker().start();
	}
	
	public static void logData() throws Exception
	{
		if(graphData == null)
		{
			graphData = new GraphData();
			graphData.graph = new HashMap<String, Map<Long, Integer>>();
		}
		
		long ms = System.currentTimeMillis();
		
		for(int i = 0; i < Main.loadedMods.size(); i++)
		{
			Mod m = Main.loadedMods.get(i);
			
			Map<Long, Integer> map = graphData.graph.get(m.modID);
			if(map == null)
			{
				map = new HashMap<Long, Integer>();
				graphData.graph.put(m.modID, map);
			}
			
			map.put(ms, m.getTotalDownloads());
		}
		
		String s = Utils.toJson(graphData, true);
		FileOutputStream fos = new FileOutputStream(dataFile);
		fos.write(s.getBytes()); fos.close();
	}
	
	public static class GraphData
	{
		@Expose public Map<String, Map<Long, Integer>> graph;
	}
	
	public static class Checker implements Runnable
	{
		public Thread thread = null;
		
		public void start()
		{
			thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
		}
		
		public void run()
		{
			try
			{
				while(true)
				{
					logData();
					Thread.sleep(checkMillis);
				}
			}
			catch(Exception e)
			{ e.printStackTrace(); thread = null; }
		}
	}
	
	public static void displayGraph(final Mod mod)
	{
		final JLabel picLabel = new JLabel(new ImageIcon(image_graph))
		{
			private static final long serialVersionUID = 1L;
			
			public void paint(Graphics g)
			{
				super.paint(g);
				
				int w = getWidth();
				int h = getHeight();
				
				Map<Long, Integer> map = graphData.graph.get(mod.modID);
				
				TimedValue values[] = new TimedValue[map.size()];
				
				long minTime = -1;
				long maxTime = -1;
				int minDown = -1;
				int maxDown = -1;
				
				int index = -1;
				for(Long l : map.keySet())
					values[++index] = new TimedValue(l, map.get(l));
				
				//values[values.length - 1] = new TimedValue(System.currentTimeMillis(), mod.getTotalDownloads());
				
				Arrays.sort(values);
				
				for(int i = 0; i < values.length; i++)
				{
					if(minTime == -1 || values[i].time < minTime) minTime = values[i].time;
					if(maxTime == -1 || values[i].time > maxTime) maxTime = values[i].time;
					if(minDown == -1 || values[i].down < minDown) minDown = values[i].down;
					if(maxDown == -1 || values[i].down > maxDown) maxDown = values[i].down;
				}
				
				g.drawString("" + maxDown, 4, 16);
				g.drawString("" + ((maxDown + minDown) / 2), 4, h / 2 + 4);
				g.drawString("" + minDown, 4, h - 8);
				
				ArrayList<Point> points = new ArrayList<Point>();
				
				for(int i = 0; i < values.length; i++)
				{
					double x = Utils.map(values[i].time, minTime, maxTime, 0D, w);
					double y = h - 1 - Utils.map(values[i].down, minDown, maxDown, 0D, h);
					points.add(new Point((int)x, (int)y));
					
					//System.out.println(x + ", " + y + "; " + values[i].time + ", " + values[i].down);
				}
				
				for(int i = 0; i < points.size(); i++)
				{
					Point p = points.get(i);
					Point pp = new Point(0, h - 1);
					
					if(i > 0) pp = points.get(i - 1);
					
					g.drawLine(pp.x, pp.y, p.x, p.y);
					g.drawLine(p.x, h - 4, p.x, h);
				}
			}
		};
		
		picLabel.addMouseListener(new MouseListener()
		{
			public void mouseReleased(MouseEvent e) { }
			public void mousePressed(MouseEvent e) { }
			public void mouseExited(MouseEvent e) { }
			public void mouseEntered(MouseEvent e) { }
			
			public void mouseClicked(MouseEvent e)
			{ try { logData(); } catch(Exception ex) {} picLabel.repaint(); }
		});
		
		JOptionPane.showMessageDialog(null, picLabel, "Graph: " + mod.title, JOptionPane.PLAIN_MESSAGE, null);
	}
	
	private static class TimedValue implements Comparable<TimedValue>
	{
		public Long time;
		public Integer down;
		
		public TimedValue(Long t, Integer v)
		{ time = t; down = v; }
		
		public int compareTo(TimedValue o)
		{ return time.compareTo(o.time); }
	}
}