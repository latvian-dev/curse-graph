package latmod.cursegraph;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import com.google.gson.annotations.Expose;

public class Main
{
	public static TrayIcon trayIcon = null;
	public static final ArrayList<Mod> loadedMods = new ArrayList<Mod>();
	public static File folder, modsFile;
	
	public static void main(String[] args) throws Exception
	{
		if(!SystemTray.isSupported() || !Desktop.isDesktopSupported())
		{
			System.out.println("Invalid system!");
			System.exit(1); return;
		}
		
		folder = new File(System.getProperty("user.home"), "/LatMod/CurseGraph/");
		if(!folder.exists()) folder.mkdirs();
		modsFile = new File(folder, "mods.json");
		
		try
		{
			trayIcon = new TrayIcon(loadImage("trayIcon.png"));
			trayIcon.setImageAutoSize(true);
			trayIcon.setToolTip("Curse Graph");
			SystemTray.getSystemTray().add(trayIcon);
			
			trayIcon.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					String[] ml = new String[loadedMods.size()];
					
					if(ml.length > 0)
					{
						for(int i = 0; i < ml.length; i++)
							ml[i] = loadedMods.get(i).title;
						
						String s = (String)JOptionPane.showInputDialog(null, "Select the mod:", "Display Graph", JOptionPane.PLAIN_MESSAGE, null, ml, ml[0]);
						
						if(s != null)
						{
							for(int i = 0; i < loadedMods.size(); i++)
								if(loadedMods.get(i).title.equals(s))
									Graph.displayGraph(loadedMods.get(i));
						}
					}
				}
			});
		}
		catch(Exception e)
		{ e.printStackTrace(); }
		
		refresh();
		
		Graph.init();
	}
	
	public static BufferedImage loadImage(String s) throws Exception
	{ return ImageIO.read(Main.class.getResource("/latmod/cursegraph/" + s)); }
	
	public static void refresh()
	{
		loadMods();
		
		PopupMenu menu = new PopupMenu();
		
		{
			MenuItem m1 = new MenuItem("Curse Graph");
			
			m1.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{ openURL("https://github.com/LatvianModder/CurseGraph"); }
			});
			
			menu.add(m1);
		}
		
		menu.addSeparator();
		
		{
			MenuItem m1 = new MenuItem("Refresh");
			
			m1.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{ try { refresh(); Graph.logData(); } catch(Exception ex)
				{ ex.printStackTrace(); } }
			});
			
			menu.add(m1);
		}
		
		{
			MenuItem m1 = new MenuItem("Add a Mod");
			
			m1.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						String input = JOptionPane.showInputDialog("Enter Mod's Curse ID here:", "");
						
						if(input != null && !input.isEmpty())
						{
							try
							{
								Scanner sc = new Scanner(new URL("http://widget.mcf.li/mc-mods/minecraft/" + input + ".json").openStream(), "UTF-8");
								String s = sc.useDelimiter("\\A").next(); sc.close();
								
								Mod m = Utils.fromJson(s, Mod.class);
								if(m != null) { m.modID = input; loadedMods.add(m); }
								
								saveMods();
								refresh();
							}
							catch(Exception ex)
							{ System.out.println("Mod with ID '" + input + "' can't be loaded!"); }
						}
					}
					catch(Exception ex)
					{ ex.printStackTrace(); }
				}
			});
			
			menu.add(m1);
		}
		
		{
			MenuItem m1 = new MenuItem("Open Graph Folder");
			
			m1.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{ try { Desktop.getDesktop().open(folder); }
				catch(Exception ex) { ex.printStackTrace(); } }
			});
			
			menu.add(m1);
		}
		
		menu.addSeparator();
		
		try { refresh0(menu); }
		catch(Exception e)
		{ e.printStackTrace(); menu.add("No mods loaded"); }
		
		menu.addSeparator();
		
		{
			MenuItem m1 = new MenuItem("Exit");
			
			m1.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					SystemTray.getSystemTray().remove(trayIcon);
					System.exit(0);
				}
			});
			
			menu.add(m1);
		}
		
		trayIcon.setPopupMenu(menu);
	}
	
	public static void refresh0(PopupMenu menu) throws Exception
	{
		loadMods();
		
		if(loadedMods.isEmpty()) menu.add("No mods loaded");
		else for(int i = 0; i < loadedMods.size(); i++)
		{
			final Mod m = loadedMods.get(i);
			
			PopupMenu info = new PopupMenu(m.title);
			
			{
				MenuItem m1 = new MenuItem("ProjectID: " + m.modID);
				m1.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{ openURL(m.url); }
				});
				
				info.add(m1);
			}
			
			if(m.authors.length == 1)
			{
				MenuItem m1 = new MenuItem("Author: " + m.authors[0]);
				m1.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{ openURL("http://minecraft.curseforge.com/members/" + m.authors[0]); }
				});
				
				info.add(m1);
			}
			else
			{
				PopupMenu authors = new PopupMenu("Authors:");
				for(final String s : m.authors)
				{
					MenuItem m1 = new MenuItem(s);
					m1.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{ openURL("http://minecraft.curseforge.com/members/" + s); }
					});
					authors.add(m1);
				}
				info.add(authors);
			}
			
			{
				MenuItem m1 = new MenuItem("Open Graph");
				m1.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{ Graph.displayGraph(m); }
				});
				
				info.add(m1);
			}
			
			{
				MenuItem remove = new MenuItem("Remove");
				remove.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						loadedMods.remove(m);
						saveMods();
						refresh();
					}
				});
				
				info.add(remove);
			}
			
			info.addSeparator();
			
			info.add("Likes / Favorites: " + m.likes + " / " + m.favorites);
			
			{
				PopupMenu downloads = new PopupMenu("Downloads:");
				downloads.add("All: " + m.getTotalDownloads());
				downloads.add("Monthly: " + m.downloads.get("monthly"));
				downloads.add("Last file: " + m.download.downloads);
				info.add(downloads);
			}
			
			{
				PopupMenu downloads = new PopupMenu("Files:");
				
				MenuItem all = new MenuItem("All");
				all.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{ openURL("http://minecraft.curseforge.com/mc-mods/" + m.modID + "/files"); }
				});
				
				downloads.add(all);
				
				MenuItem latest = new MenuItem("Latest");
				latest.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{ openURL(m.download.url); }
				});
				
				downloads.add(latest);
				
				for(String s : m.versions.keySet())
				{
					PopupMenu m1 = new PopupMenu(s);
					
					final Version[] v = m.versions.get(s);
					for(int j = 0; j < v.length; j++)
					{
						final int fj = j;
						MenuItem m2 = new MenuItem(v[j].name);
						
						m2.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent e)
							{ openURL(v[fj].url); }
						});
						
						m1.add(m2);
					}
					
					downloads.add(m1);
				}
				
				info.add(downloads);
			}
			
			menu.add(info);
		}
	}
	
	public static void loadMods()
	{
		loadedMods.clear();
		
		List<String> l;
		
		try
		{
			if(modsFile.exists())
			{
				FileInputStream fis = new FileInputStream(modsFile);
				String modsToLoad0 = Utils.toString(fis);
				fis.close();
				l = Utils.fromJson(modsToLoad0, Utils.getListType(String.class));
			}
			else
			{
				l = new ArrayList<String>();
				l.add("224778-latcoremc");
				l.add("225733-latblocks");
				l.add("225907-emc-condenser");
				l.add("228434-theconstruct");
			}
			
			for(int i = 0; i < l.size(); i++)
			{
				String id = l.get(i);
				
				try
				{
					Scanner sc = new Scanner(new URL("http://widget.mcf.li/mc-mods/minecraft/" + id + ".json").openStream(), "UTF-8");
					String s = sc.useDelimiter("\\A").next(); sc.close();
					
					Mod m = Utils.fromJson(s, Mod.class);
					if(m != null) { m.modID = id; loadedMods.add(m); }
				}
				catch(Exception e)
				{ System.out.println("Mod with ID '" + id + "' can't be loaded!"); }
			}
		}
		catch(Exception e)
		{ e.printStackTrace(); }
	}
	
	public static void saveMods()
	{
		try
		{
			if(!modsFile.exists()) modsFile.createNewFile();
			
			ArrayList<String> l = new ArrayList<String>();
			
			for(int i = 0; i < loadedMods.size(); i++)
				l.add(loadedMods.get(i).modID);
			
			String s = Utils.toJson(l, true);
			FileOutputStream fos = new FileOutputStream(modsFile);
			fos.write(s.getBytes());
			fos.close();
		}
		catch(Exception e)
		{ e.printStackTrace(); }
	}
	
	public static void openURL(String s)
	{ try { Desktop.getDesktop().browse(new URI(s)); }
	catch (Exception e) { e.printStackTrace(); } }
	
	public static class Mod
	{
		public String modID;
		@Expose public String title;
		@Expose public String game;
		@Expose public String category;
		@Expose public String url;
		@Expose public String thumbnail;
		@Expose public String[] authors;
		@Expose public Map<String, Integer> downloads;
		@Expose public Integer favorites;
		@Expose public Integer likes;
		@Expose public String updated_at;
		@Expose public String created_at;
		@Expose public String project_url;
		@Expose public String release_type;
		@Expose public String license;
		@Expose public Version download;
		@Expose public Map<String, Version[]> versions;
		
		public String toString()
		{ return modID; }
		
		public int hashCode()
		{ return toString().hashCode(); }
		
		public boolean equals(Object o)
		{ return o.toString().equals(toString()); }
		
		public int getTotalDownloads()
		{
			Integer i = downloads.get("total");
			return (i == null) ? download.downloads.intValue() : i.intValue();
		}
	}
	
	public static class Version
	{
		@Expose public Integer id;
		@Expose public String url;
		@Expose public String name;
		@Expose public String type;
		@Expose public String version;
		@Expose public Integer downloads;
		@Expose public String created_at;
	}
}