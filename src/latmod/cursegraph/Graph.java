package latmod.cursegraph;

import java.io.*;
import java.util.*;

public class Graph
{
	public static ColorScheme colors = ColorScheme.DARK_ORANGE;
	
	public static class GraphData
	{
		public final String modID;
		public final ArrayList<TimedDown> downloads;
		public int lastDownloads;
		
		public GraphData(String s)
		{
			modID = s;
			downloads = new ArrayList<TimedDown>();
			lastDownloads = 0;
		}
		
		public String toString()
		{ return modID; }
		
		public boolean equals(Object o)
		{
			if(o == this) return true;
			return o.toString().equals(toString());
		}
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
	
	public static class TimedDown implements Comparable<TimedDown>
	{
		public long time;
		public int down;
		
		public TimedDown(long t, int v)
		{ time = t; down = v; }
		
		public int compareTo(TimedDown o)
		{ return (time < o.time ? -1 : (time == o.time ? 0 : 1)); }
	}
	
	public static final ArrayList<GraphData> allData = new ArrayList<GraphData>();
	public static final Checker checker = new Checker();
	
	public static void init() throws Exception
	{
		if(!OldGraphDataLoader.init())
		{
			for(Curse.Project p : Projects.list)
			{
				GraphData data = getData(p.projectID);
				data.downloads.clear();
				
				File f = new File(Main.config.dataFolderPath, p.projectID + ".txt");
				
				if(f.exists())
				{
					FileInputStream fis = new FileInputStream(f);
					String txt = Utils.toString(fis);
					fis.close();
					
					String[] s = txt.split("\n");
					if(s == null || s.length == 0)
						s = new String[] { txt };
					
					for(String s1 : s)
					{
						String[] s2 = s1.split(":", 2);
						if(s2 != null && s2.length == 2)
						{
							long t = Long.parseLong(s2[0]);
							int d = Integer.parseInt(s2[1]);
							
							Graph.TimedDown td = new Graph.TimedDown(t, d);
							data.downloads.add(td);
						}
					}
				}
			}
		}
		
		saveGraph();
		checker.start();
	}
	
	public static void saveGraph() throws Exception
	{
		for(Curse.Project p : Projects.list)
		{
			GraphData data = getData(p.projectID);
			Graph.TimedDown[] downs = data.downloads.toArray(new Graph.TimedDown[0]);
			
			if(downs.length > 0)
			{
				FileOutputStream fos = new FileOutputStream(Utils.newFile(new File(Main.config.dataFolderPath, p.projectID + ".txt")));
				Arrays.sort(downs);
				for(Graph.TimedDown t : downs) fos.write((t.time + ":" + t.down + "\n").getBytes());
				fos.flush(); fos.close();
			}
		}
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
	
	public static GraphData getData(String mod)
	{
		int idx = allData.indexOf(mod);
		if(idx < 0)
		{
			GraphData data = new GraphData(mod);
			data.lastDownloads = -1;
			allData.add(data);
			return data;
		}
		
		return allData.get(idx);
	}
	
	public static ArrayList<TimedDown> getDownloads(String mod)
	{
		ArrayList<TimedDown> alist = new ArrayList<TimedDown>();
		GraphData data = getData(mod);
		if(data == null || data.downloads.isEmpty()) return alist;
		alist.addAll(data.downloads);
		return alist;
	}
	
	public static ArrayList<TimedDown> getDownloads(String mod, long min, long max)
	{
		ArrayList<TimedDown> al = getDownloads(mod);
		
		for(int i = 0; i < al.size(); i++)
		{
			long v = al.get(i).time;
			if(v < min && v > max)
				al.remove(i);
		}
		
		return al;
	}
	
	public static void logData() throws Exception
	{
		long ms = System.currentTimeMillis();
		
		if(Projects.hasProjects()) for(Curse.Project p : Projects.list)
		{
			GraphData data = getData(p.projectID);
			
			int d = p.getTotalDownloads();
			
			if(data.lastDownloads <= -1 || d > data.lastDownloads)
			{
				data.downloads.add(new TimedDown(ms, d));
				data.lastDownloads = d;
			}
		}
		
		saveGraph();
	}
	
	/* if(i > 0)
			{
				Main.info("Removed " + i + " values!", false);
				logData();
				Main.refresh();
			}
	 */
	
	public static int clearData(long l)
	{
		int i = 0;
		for(Curse.Project p : Projects.list)
			i += clearData(p.projectID, l);
		return i;
	}
	
	public static int clearData(String s, long l)
	{
		long ms = System.currentTimeMillis();
		int ret = 0;
		
		GraphData data = getData(s);
		
		data.lastDownloads = -1;
		
		for(int i = 0; i < data.downloads.size(); i++)
		{
			TimedDown t = data.downloads.get(i);
			if((ms - t.time) >= l)
			{
				data.downloads.remove(i);
				ret++;
			}
		}
		
		return ret;
	}
}