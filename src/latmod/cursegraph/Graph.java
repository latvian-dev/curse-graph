package latmod.cursegraph;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import com.google.gson.annotations.Expose;

public class Graph
{
	private static int mouseX;
	private static int mouseY;
	
	public static class GraphData
	{
		@Expose public Map<String, Integer> lastDownloads;
		@Expose public Map<String, Map<Long, Integer>> projects;
	}
	
	public static class Checker implements Runnable
	{
		public Thread thread = null;
		private boolean first = true;
		
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
					if(!first) Main.refresh();
					else first = false;
					
					logData();
					
					if(Main.config.refreshMinutes <= 0)
					{ Main.config.refreshMinutes = 15; Main.config.save(); }
					
					Thread.sleep(Main.config.refreshMinutes * 60000L);
				}
			}
			catch(Exception e)
			{ e.printStackTrace(); thread = null; }
		}
	}
	
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
		
		image_graph = Main.loadImage("graph.png");
		
		checker = new Checker();
		checker.start();
	}
	
	public static void checkNull()
	{
		if(graphData.projects == null) graphData.projects = new HashMap<String, Map<Long, Integer>>();
		if(graphData.lastDownloads == null) graphData.lastDownloads = new HashMap<String, Integer>();
	}
	
	@SuppressWarnings("deprecation")
	public static String getTimeString(long time)
	{
		Date date = new Date(time);
		String s = "";
		s += formNum(date.getDate() + 1);
		s += ".";
		s += formNum(date.getMonth() + 1);
		s += ".";
		s += formNum(date.getYear() + 1900);
		s += " ";
		s += formNum(date.getHours());
		s += ":";
		s += formNum(date.getMinutes());
		s += ":";
		s += formNum(date.getSeconds());
		return s;
	}
	
	private static String formNum(int i)
	{ return (i < 10) ? ("0" + i) : ("" + i); }
	
	public static void logData() throws Exception
	{
		if(graphData == null) graphData = new GraphData();
		
		long ms = System.currentTimeMillis();
		
		//System.out.println("Data logged with ID " + ms + "!");
		
		checkNull();
		
		if(Projects.hasProjects()) for(Curse.Project p : Projects.list)
		{
			Map<Long, Integer> map = graphData.projects.get(p.projectID);
			
			if(map == null)
			{
				map = new HashMap<Long, Integer>();
				graphData.projects.put(p.projectID, map);
			}
			
			Integer lastDowns = graphData.lastDownloads.get(p.projectID);
			int d = p.getTotalDownloads();
			
			if(lastDowns == null || lastDowns.intValue() < d)
			{
				map.put(ms, d);
				graphData.lastDownloads.put(p.projectID, d);
			}
		}
		
		saveGraph();
	}
	
	public static void saveGraph() throws Exception
	{ checkNull(); Utils.toJsonFile(dataFile, graphData); }
	
	private static class GraphPoint
	{
		public final int x;
		public final int y;
		public final long time;
		public final int downs;
		
		public GraphPoint(double px, double py, long t, int d)
		{ x = (int)px; y = (int)py; time = t; downs = d; }
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
				
				Map<Long, Integer> map = graphData.projects.get(mod.projectID);
				
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
				
				ArrayList<GraphPoint> points = new ArrayList<GraphPoint>();
				
				for(int i = 0; i < values.length; i++)
				{
					long time = values[i].time.longValue();
					int downs = values[i].down.intValue();
					
					double x=0, y=0;
					
					if(Main.config.graphRelative.booleanValue())
					{
						x = Utils.map(time, minTime, maxTime, 0D, w);
						y = Utils.map(downs, minDown, maxDown, h, 0D);
					}
					else
					{
						x = Utils.map(time, minTime, maxTime, 0D, w);
						y = Utils.map(downs, minDown, maxDown, h, 0D);
					}
					
					y = Math.max(2, Math.min(y, h - 2));
					points.add(new GraphPoint(x, y, time, downs));
				}
				
				boolean isOver = false;
				
				for(int i = points.size() - 1; i >= 0; i--)
				{
					GraphPoint p = points.get(i);
					GraphPoint pp = new GraphPoint(0, h - 1, 0, 0);
					
					if(i > 0) pp = points.get(i - 1);
					
					g.drawLine(pp.x, pp.y, p.x, p.y);
					//g.drawLine(p.x, h - 4, p.x, h);
					g.drawOval(p.x - 3, p.y - 3, 6, 6);
					
					if(!isOver && p.time > 0L && Utils.distSq(mouseX, mouseY, p.x, p.y) <= 100D)
					{
						isOver = true;
						g.drawString(getTimeString(p.time) + " :: " + p.downs, 4, 16);
						Color c = g.getColor();
						g.setColor(Color.red);
						g.drawOval(p.x - 1, p.y - 1, 2, 2);
						g.drawOval(p.x - 2, p.y - 2, 4, 4);
						g.setColor(c);
					}
				}
				
				if(!isOver)
				{
					g.drawString("" + maxDown, 4, 16);
					g.drawString("" + ((maxDown + minDown) / 2), 4, h / 2 + 4);
					g.drawString("" + minDown, 4, h - 8);
				}
			}
		};
		
		picLabel.addMouseListener(new MouseListener()
		{
			public void mouseReleased(MouseEvent e) { }
			public void mousePressed(MouseEvent e) {  }
			public void mouseExited(MouseEvent e) { }
			public void mouseEntered(MouseEvent e) { }
			
			public void mouseClicked(MouseEvent e)
			{ try { logData(); } catch(Exception ex) {} picLabel.repaint(); }
		});
		
		picLabel.addMouseMotionListener(new MouseMotionListener()
		{
			public void mouseDragged(MouseEvent e) { }
			
			public void mouseMoved(MouseEvent e)
			{
				mouseX = e.getX();
				mouseY = e.getY();
				picLabel.repaint();
			}
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

	public static void clearData(long l)
	{
		long ms = System.currentTimeMillis();
		
		try
		{
			int i = 0;
			
			if(graphData.lastDownloads != null) graphData.lastDownloads.clear();
			
			if(graphData.projects != null) for(String s : graphData.projects.keySet())
			{
				if(graphData.projects != null)
				{
					Map<Long, Integer> m = graphData.projects.get(s);
					
					List<Long> keys = new ArrayList<Long>();
					keys.addAll(m.keySet());
					
					for(Long l1 : keys)
					{
						if((ms - l1.longValue()) >= l)
						{
							m.remove(l1);
							i++;
						}
					}
				}
			}
			
			if(i > 0)
			{
				logData();
				Main.refresh();
			}
			
			Main.info("Removed " + i + " values!");
		}
		catch(Exception e)
		{ e.printStackTrace(); }
	}
}