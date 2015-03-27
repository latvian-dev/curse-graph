package latmod.cursegraph;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.*;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import com.google.gson.annotations.Expose;

public class Main
{
	public static final int version = 4;
	public static int latestVersion = -1;
	
	public static TrayIcon trayIcon = null;
	public static BufferedImage imageReady, imageBusy;
	
	public static final File folder = getFolder();
	public static File projectsFile, configFile;
	public static Config config;
	
	public static void main(String[] args) throws Exception
	{
		if(!SystemTray.isSupported() || !Desktop.isDesktopSupported())
		{
			System.out.println("Invalid system!");
			System.exit(1); return;
		}
		
		System.out.println("Loading CurseGraph, Version: " + version + ", OS: " + OS.CURRENT);
		
		projectsFile = new File(folder, "projects.json");
		configFile = new File(folder, "config.json");
		
		config = Utils.fromJsonFile(configFile, Config.class);
		if(!configFile.exists()) configFile.createNewFile();
		
		if(config == null) config = new Config();
		config.setDefaults();
		config.save();
		
		try
		{
			String s = Utils.toString(new URL("http://pastebin.com/raw.php?i=RyuQPm4f").openStream());
			if(s != null) latestVersion = Integer.parseInt(s);
		}
		catch(Exception e)
		{ e.printStackTrace(); System.exit(1); }
		
		imageReady = loadImage("trayIcon.png");
		imageBusy = loadImage("trayIconBusy.png");
		
		trayIcon = new TrayIcon(imageReady);
		trayIcon.setImageAutoSize(true);
		trayIcon.setToolTip("Curse Graph");
		SystemTray.getSystemTray().add(trayIcon);
		
		trayIcon.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String[] ml = Projects.getTitles();
				
				String lastID = null;
				
				if(ml.length > 0)
				{
					if(config.lastProjectID != null)
					{
						Curse.Project p = Projects.getProject(config.lastProjectID);
						if(p != null) lastID = p.title;
					}
					
					if(lastID == null) lastID = ml[0];
					
					String s = (String)JOptionPane.showInputDialog(null, "Select the mod:", "Display Graph", JOptionPane.PLAIN_MESSAGE, null, ml, lastID);
					
					if(s != null)
					{
						Curse.Project p = Projects.getProjectFromTitle(s);
						if(p != null)
						{
							config.lastProjectID = p.projectID;
							config.save();
							Graph.displayGraph(p);
						}
					}
				}
			}
		});
		
		refresh();
		Projects.save();
		Graph.init();
		
		if(latestVersion > version) info("Update available!");
	}
	
	private static File getFolder()
	{
		File f = new File(System.getProperty("user.home"), "/LatMod/CurseGraph/");
		if(!f.exists()) f.mkdirs();
		return f;
	}

	public static BufferedImage loadImage(String s) throws Exception
	{ return ImageIO.read(Main.class.getResource("/latmod/cursegraph/" + s)); }
	
	public static void refresh()
	{
		trayIcon.setImage(imageBusy);
		Projects.load();
		
		PopupMenu menu = new PopupMenu();
		
		{
			MenuItem m1 = new MenuItem("Curse Graph v" + version + ((latestVersion > version) ? " (Update available)" : ""));
			
			m1.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{ openURL("https://github.com/LatvianModder/CurseGraph/"); }
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
			MenuItem m1 = new MenuItem("Set refresh interval");
			
			m1.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					String input = JOptionPane.showInputDialog("Set refresh minutes:", "" + config.refreshMinutes);
					if(input != null && !input.isEmpty())
					{
						try
						{
							int i = Integer.parseInt(input);
							config.refreshMinutes = Math.max(1, i);
							config.save();
							
							Graph.checker.start();
						}
						catch(Exception ex)
						{ error("Invalid number!"); }
					}
				}
			});
			
			menu.add(m1);
		}
		
		{
			MenuItem m1 = new MenuItem("Open Data Folder");
			
			m1.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{ try { Desktop.getDesktop().open(folder); }
				catch(Exception ex) { ex.printStackTrace(); } }
			});
			
			menu.add(m1);
		}
		
		{
			PopupMenu m1 = new PopupMenu("Clear older than...");
			
			{
				MenuItem m2 = new MenuItem("Month");
				
				m2.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{ Graph.clearData(getH(24 * 30)); }
				});
				
				m1.add(m2);
			}
			
			{
				MenuItem m2 = new MenuItem("Week");
				
				m2.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{ Graph.clearData(getH(24 * 7)); }
				});
				
				m1.add(m2);
			}
			
			{
				MenuItem m2 = new MenuItem("Day");
				
				m2.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{ Graph.clearData(getH(24)); }
				});
				
				m1.add(m2);
			}
			
			{
				MenuItem m2 = new MenuItem("Hour");
				
				m2.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{ Graph.clearData(getH(1)); }
				});
				
				m1.add(m2);
			}
			
			{
				MenuItem m2 = new MenuItem("X Minues");
				
				m2.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						String input = JOptionPane.showInputDialog("X Minutes:", "10");
						if(input != null && !input.isEmpty())
						{
							try
							{
								int i = Integer.parseInt(input);
								i = Math.max(1, i);
								Graph.clearData(i * 60000L);
							}
							catch(Exception ex)
							{ error("Invalid number!"); }
						}
					}
				});
				
				m1.add(m2);
			}
			
			menu.add(m1);
		}
		
		{
			PopupMenu m1 = new PopupMenu("Add");
			
			for(final Curse.Type t : Curse.Type.VALUES)
			{
				MenuItem m2 = new MenuItem(t.name);
				
				m2.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						try
						{
							String input = JOptionPane.showInputDialog("Enter " + t.name + "'s Curse ID here:", "");
							if(input != null && !input.isEmpty())
							{
								Projects.add(t, input, false);
								refresh();
							}
						}
						catch(Exception ex)
						{ ex.printStackTrace(); }
					}
				});
				
				m1.add(m2);
			}
			
			menu.add(m1);
		}
		
		menu.addSeparator();
		
		try { refresh0(menu); }
		catch(Exception e)
		{ e.printStackTrace(); menu.add("No mods loaded"); }
		
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
		trayIcon.setImage(imageReady);
	}
	
	private static long getH(int i)
	{ return 1000L * 60L * 60L * i; }
	
	public static void info(String string)
	{
		JOptionPane.showMessageDialog(null, string, "Info", JOptionPane.INFORMATION_MESSAGE);
		System.out.println(string);
	}

	public static void error(String string)
	{
		JOptionPane.showMessageDialog(null, string, "Error!", JOptionPane.ERROR_MESSAGE);
		System.out.println(string);
	}
	
	public static boolean showYesNo(String title, String question)
	{
		int i = JOptionPane.showConfirmDialog(null, question, title, JOptionPane.YES_NO_OPTION);
		return i == JOptionPane.YES_OPTION;
	}

	public static void refresh0(PopupMenu menu) throws Exception
	{
		if(!Projects.load()) menu.add("No projects loaded");
		else
		{
			for(final Curse.Type type : Curse.Type.VALUES)
			{
				ArrayList<Curse.Project> pl = Projects.getByType(type);
				
				for(final Curse.Project p : pl)
				{
					PopupMenu info = new PopupMenu(p.title);
					
					{
						MenuItem m1 = new MenuItem("Open Project");
						m1.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent e)
							{ openURL(p.url); }
						});
						
						info.add(m1);
					}
					
					{
						MenuItem m1 = new MenuItem("Open Graph");
						m1.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent e)
							{ Graph.displayGraph(p); }
						});
						
						info.add(m1);
					}
					
					PopupMenu info1 = new PopupMenu("Info");
					
					{
						if(p.authors.length == 1)
						{
							MenuItem m1 = new MenuItem("Author: " + p.authors[0]);
							m1.addActionListener(new ActionListener()
							{
								public void actionPerformed(ActionEvent e)
								{ openURL("http://minecraft.curseforge.com/members/" + p.authors[0]); }
							});
							
							info1.add(m1);
						}
						else
						{
							PopupMenu authors = new PopupMenu("Authors:");
							for(final String s : p.authors)
							{
								MenuItem m1 = new MenuItem(s);
								m1.addActionListener(new ActionListener()
								{
									public void actionPerformed(ActionEvent e)
									{ openURL("http://minecraft.curseforge.com/members/" + s); }
								});
								authors.add(m1);
							}
							
							info1.add(authors);
						}
						
						info1.add("Likes: " + p.likes);
						info1.add("Favorites: " + p.favorites);
					}
					
					info1.add("All downloads: " + p.getTotalDownloads());
					Integer mo = p.downloads.get("monthly");
					if(mo != null && mo.intValue() > 0) info1.add("Monthly downloads: " + mo);
					info1.add("Last file downloads: " + p.download.downloads);
					
					info.add(info1);
					
					{
						PopupMenu downloads = new PopupMenu("Files:");
						
						MenuItem all = new MenuItem("All");
						all.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent e)
							{ openURL("http://minecraft.curseforge.com/" + p.getType().ID + "/" + p.projectID + "/files"); }
						});
						
						downloads.add(all);
						
						MenuItem latest = new MenuItem("Latest");
						latest.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent e)
							{ openURL(p.download.url); }
						});
						
						downloads.add(latest);
						
						for(String s : p.versions.keySet())
						{
							PopupMenu m1 = new PopupMenu(s);
							
							final Curse.Version[] v = p.versions.get(s);
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
					
					{
						MenuItem remove = new MenuItem("Remove");
						remove.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent e)
							{
								if(showYesNo("Confirm", "Remove " + p.title + "?"))
								{
									Projects.list.remove(p);
									Projects.save();
									refresh();
								}
							}
						});
						
						info.add(remove);
					}
					
					menu.add(info);
				}
				
				if(!pl.isEmpty()) menu.addSeparator();
			}
		}
	}
	
	public static void openURL(String s)
	{ try { Desktop.getDesktop().browse(new URI(s)); }
	catch (Exception e) { e.printStackTrace(); } }
	
	public static class Config
	{
		@Expose public Integer refreshMinutes;
		@Expose public String lastProjectID;
		@Expose public Boolean graphRelative;
		@Expose public Integer graphLimit;
		
		public void setDefaults()
		{
			if(refreshMinutes == null) refreshMinutes = 30;
			if(lastProjectID == null) lastProjectID = "";
			if(graphRelative == null) graphRelative = false;
			if(graphLimit == null) graphLimit = -1;
		}
		
		public void save()
		{ Utils.toJsonFile(configFile, Config.this); }
	}
}