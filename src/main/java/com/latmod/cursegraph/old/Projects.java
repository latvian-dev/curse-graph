package com.latmod.cursegraph.old;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

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
		
		boolean addedAll = true;

        File f = new File(CurseGraph.dataFolder, "projects.txt");

        try
		{
			BufferedReader br = new BufferedReader(new FileReader(Utils.newFile(f)));
			String s = null;
			
			while((s = br.readLine()) != null)
			{
				String[] s1 = s.split(": ", 2);
				
				if(s1 != null && s1.length == 2)
				{
					Curse.Type t = Curse.Type.getFromChar(s1[0].charAt(0));
					
					if(t == null) { addedAll = false; continue; }
					else add(t, s1[1], true);
				}
			}
			
			br.close();
		}
		catch(Exception e) { }

        if(!addedAll)
        {
            CurseGraph.error("Some projects failed to load!", false);
        }
		return hasProjects();
	}
	
	public static Curse.Project add(Curse.Type t, String id, boolean silent)
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

                if(m.thumbnail != null && !m.thumbnail.isEmpty())
                {
                    m.image = CurseGraph.loadImageURL(m.thumbnail);
                }
				
				if(list.contains(m))
				{
                    CurseGraph.error("Duplicate ProjectID '" + id + "'!", silent);
                    return list.get(list.indexOf(m));
				}
				
				list.add(m);
				
				if(!silent)
				{
					save();
					Graph.logData();
				}

                CurseGraph.info("Added '" + m.title + "'!", silent);

                return m;
			}
		}
		catch(Exception ex)
		{ ex.printStackTrace(); }

        CurseGraph.error(t.name + " with ID '" + id + "' failed to load!", silent);
        return null;
	}
	
	public static void save()
	{
		String[] l = new String[list.size()];
		for(int i = 0; i < l.length; i++)
		{
			Curse.Project p = Projects.list.get(i);
			l[i] = (p.getType().charID + ": " + p.projectID + "\n");
		}
		
		Arrays.sort(l);
		
		try
		{
            BufferedWriter bw = new BufferedWriter(new FileWriter(Utils.newFile(new File(CurseGraph.dataFolder, "projects.txt"))));
            for(String s : l) bw.append(s); bw.flush(); bw.close();
		}
		catch(Exception e)
		{ e.printStackTrace(); }
	}
}