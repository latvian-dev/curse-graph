package latmod.cursegraph;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.swing.*;

import com.google.gson.annotations.Expose;

public class Graph
{
	public static File dataFile;
	public static GraphData graphData;
	public static BufferedImage image_graph;
	public static Checker checker;
	
	public static void init() throws Exception
	{
		dataFile = new File(Main.folder, "data.json");
		graphData = Utils.fromJsonFile(dataFile, GraphData.class);
		
		if(!dataFile.exists()) dataFile.createNewFile();
		
		if(graphData == null)
		{
			graphData = new GraphData();
			checkNull();
		}
		
		image_graph = Main.loadImage("graph_background.png");
		
		checker = new Checker();
		checker.start();
	}
	
	public static void checkNull()
	{
		if(graphData.projects == null) graphData.projects = new HashMap<String, Map<Long, Integer>>();
		if(graphData.lastCheck == null) graphData.lastCheck = -1L;
	}
	
	public static void logData() throws Exception
	{
		if(graphData == null) graphData = new GraphData();
		
		long ms = System.currentTimeMillis();
		
		//System.out.println("Data logged with ID " + ms + "!");
		
		checkNull();
		
		if(Projects.hasProjects()) for(Curse.Project p : Projects.list)
		{
			Map<Long, Integer> map = graphData.projects.get(p.modID);
			
			if(map == null)
			{
				map = new HashMap<Long, Integer>();
				graphData.projects.put(p.modID, map);
			}
			
			int d = p.getTotalDownloads();
			
			if(graphData.lastCheck == -1L)
				map.put(ms, d);
			else
			{
				Integer i = map.get(graphData.lastCheck);
				if(i != null && i.longValue() != d)
					map.put(ms, d);
			}
		}
		
		checkNull();
		graphData.lastCheck = ms;
		
		String s = Utils.toJson(graphData, true);
		FileOutputStream fos = new FileOutputStream(dataFile);
		fos.write(s.getBytes()); fos.close();
	}
	
	public static class GraphData
	{
		@Expose public Map<String, Map<Long, Integer>> projects;
		@Expose public Long lastCheck;
	}
	
	public static class Checker implements Runnable
	{
		public Thread thread = null;
		
		public void start()
		{
			if(thread != null) stop();
			
			if(thread == null)
			{
				thread = new Thread(this);
				thread.setDaemon(true);
				thread.start();
			}
		}
		
		@SuppressWarnings("deprecation")
		public void stop()
		{
			if(thread != null)
			{
				thread.stop();
				thread = null;
			}
		}
		
		public void run()
		{
			try
			{
				while(true)
				{
					logData();
					
					if(Main.config.refreshMinutes <= 0)
					{ Main.config.refreshMinutes = 15; Main.config.save(); }
					
					Thread.sleep(Main.config.refreshMinutes * 60L * 1000L);
				}
			}
			catch(Exception e)
			{ e.printStackTrace(); thread = null; }
		}
	}
	
	public static void displayGraph(final Curse.Project mod)
	{
		final JLabel picLabel = new JLabel(new ImageIcon(image_graph))
		{
			private static final long serialVersionUID = 1L;
			
			public void paint(Graphics g)
			{
				super.paint(g);
				
				Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				
				int w = getWidth();
				int h = getHeight();
				
				Map<Long, Integer> map = graphData.projects.get(mod.modID);
				
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