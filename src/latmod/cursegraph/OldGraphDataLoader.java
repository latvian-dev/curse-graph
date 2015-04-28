package latmod.cursegraph;

import java.io.File;
import java.util.*;

import latmod.cursegraph.Graph.GraphData;

import com.google.gson.annotations.Expose;

public class OldGraphDataLoader
{
	public static class OldGraphData
	{
		@Expose public Map<String, Integer> lastDownloads;
		@Expose public Map<String, Map<Long, Integer>> projects;
	}
	
	public static boolean init() throws Exception
	{
		File oldDataFile = new File(Main.folder, "data.json");
		
		if(oldDataFile.exists())
		{
			//oldDataFile.deleteOnExit();
			
			OldGraphData graphData = Utils.fromJsonFile(oldDataFile, OldGraphData.class);
			
			if(graphData != null)
			{
				if(graphData.projects == null) graphData.projects = new HashMap<String, Map<Long, Integer>>();
				if(graphData.lastDownloads == null) graphData.lastDownloads = new HashMap<String, Integer>();
			}
			
			for(Curse.Project p : Projects.list)
			{
				Graph.GraphData data = new GraphData(p.projectID);
				Integer i = graphData.lastDownloads.get(p.projectID);
				Map<Long, Integer> m = graphData.projects.get(p.projectID);
				if(i == null) data.lastDownloads = -1;
				else data.lastDownloads = i.intValue();
				
				if(m != null)
				{
					for(Long l : m.keySet())
						data.downloads.add(new Graph.TimedDown(l.longValue(), m.get(l).intValue()));
					
					Graph.allData.add(data);
				}
			}
			
			return true;
		}
		
		return false;
	}
}