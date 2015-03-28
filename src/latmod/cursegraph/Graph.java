package latmod.cursegraph;

import java.awt.Color;
import java.io.File;
import java.util.*;

import com.google.gson.annotations.Expose;

public class Graph
{
	public static final Color C_BG = new Color(0, 0, 0);
	public static final Color C_GRID = new Color(30, 30, 30);
	public static final Color C_NODE = new Color(255, 157, 0);
	public static final Color C_TEXT = new Color(255, 201, 0);
	
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
		s += formNum(date.getDate());
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