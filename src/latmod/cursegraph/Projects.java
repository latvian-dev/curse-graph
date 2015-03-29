package latmod.cursegraph;

import java.io.File;
import java.net.URL;
import java.util.*;

public class Projects
{
	public static final ArrayList<Curse.Project> list = new ArrayList<Curse.Project>();
	
	public static ArrayList<Curse.Project> getByType(Curse.Type t)
	{
		ArrayList<Curse.Project> l = new ArrayList<Curse.Project>();
		for(Curse.Project p : list)
		{ if(p.typeID.intValue() == t.ordinal()) l.add(p); }
		return l;
	}
	
	public static boolean hasProjects()
	{ return !list.isEmpty(); }
	
	public static Curse.Project getProject(String id)
	{
		for(Curse.Project p : list)
			if(p.projectID.equals(id)) return p;
		return null;
	}
	
	public static Curse.Project getProjectFromTitle(String title)
	{
		for(Curse.Project p : list)
			if(p.title.equals(title)) return p;
		return null;
	}
	
	public static String[] getTitles()
	{
		ArrayList<String> l = new ArrayList<String>();
		
		for(Curse.Project p : list)
			l.add(p.title);
		return l.toArray(new String[0]);
	}
	
	public static boolean load()
	{
		list.clear();
		
		List<String> l = Utils.fromJsonFile(Main.projectsFile, Utils.getListType(String.class));
		
		if(l == null)
		{
			l = new ArrayList<String>();
			l.add(Curse.Type.MOD + "@224778-latcoremc");
		}
		
		boolean addedAll = true;
		
		for(String s : l)
		{
			String[] s1 = s.split("@", 2);
			if(s1 != null && s1.length == 2)
			{ if(!add(Curse.Type.get(s1[0]), s1[1], true)) addedAll = false; }
		}
		
		if(!loadOld()) addedAll = false;
		
		if(!addedAll) Main.error("Some projects failed to load!", false);
		
		return hasProjects();
	}
	
	private static boolean loadOld()
	{
		File f = new File(Main.folder, "mods.json");
		
		if(f.exists())
		{
			List<String> l = Utils.fromJsonFile(f, Utils.getListType(String.class));
			
			boolean addedAll = true;
			
			if(l != null) for(int i = 0; i < l.size(); i++)
				if(!add(Curse.Type.MOD, l.get(i), true))
					addedAll = false;
			
			f.delete();
			return addedAll;
		}
		
		return true;
	}
	
	public static boolean add(Curse.Type t, String id, boolean silent)
	{
		try
		{
			Scanner sc = new Scanner(new URL("http://widget.mcf.li/" + t.ID + "/minecraft/" + id + ".json").openStream(), "UTF-8");
			String s = sc.useDelimiter("\\A").next(); sc.close();
			
			Curse.Project m = Utils.fromJson(s, Curse.Project.class);
			if(m != null && m.checkValid())
			{
				m.projectID = id;
				m.typeID = t.ordinal();
				
				if(list.contains(m))
				{
					Main.error("Duplicate ProjectID '" + id + "'!", silent);
					return false;
				}
				
				list.add(m);
				
				if(!silent)
				{
					save();
					Graph.logData();
				}
				
				Main.info("Added '" + m.title + "'!", silent);
				
				return true;
			}
		}
		catch(Exception ex)
		{ ex.printStackTrace(); }
		
		Main.error("Project with ID '" + id + "' failed to load!", silent);
		return false;
	}
	
	public static void save()
	{
		ArrayList<String> l = new ArrayList<String>();
		
		for(Curse.Project p : list)
			l.add(p.getType() + "@" + p.projectID);
		
		try
		{
			if(!Main.projectsFile.exists())
				Main.projectsFile.createNewFile();
			Utils.toJsonFile(Main.projectsFile, l);
		}
		catch(Exception e)
		{ e.printStackTrace(); }
	}
}