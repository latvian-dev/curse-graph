package latmod.cursegraph;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import com.google.gson.annotations.Expose;

public class Main
{
	public static final int version = 2;
	public static boolean updateAvailable = false;
	
	public static TrayIcon trayIcon = null;
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
		
		projectsFile = new File(folder, "projects.json");
		configFile = new File(folder, "config.json");
		
		config = Utils.fromJsonFile(configFile, Config.class);
		if(!configFile.exists()) configFile.createNewFile();
		
		if(config == null)
		{
			config = new Config();
			config.setDefaults();
			config.save();
		}
		
		try
		{
			String s = Utils.toString(new URL("http://pastebin.com/raw.php?i=RyuQPm4f").openStream());
			
			if(s != null)
			{
				int v = Integer.parseInt(s);
				if(v > version) updateAvailable = true;
			}
		}
		catch(Exception e)
		{ e.printStackTrace(); System.exit(1); }
		
		trayIcon = new TrayIcon(loadImage("trayIcon.png"));
		trayIcon.setImageAutoSize(true);
		trayIcon.setToolTip("Curse Graph");
		SystemTray.getSystemTray().add(trayIcon);
		
		trayIcon.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String[] ml = Projects.getTitles();
				
				if(ml.length > 0)
				{
					String s = (String)JOptionPane.showInputDialog(null, "Select the mod:", "Display Graph", JOptionPane.PLAIN_MESSAGE, null, ml, ml[0]);
					
					if(s != null)
					{
						Curse.Project p = Projects.getProject(s);
						if(p != null) Graph.displayGraph(p);
					}
				}
			}
		});
		
		refresh();
		Projects.save();
		Graph.init();
		
		if(updateAvailable) showInfo("Update available!");
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
		Projects.load();
		
		PopupMenu menu = new PopupMenu();
		
		{
			MenuItem m1 = new MenuItem("Curse Graph" + (updateAvailable ? " (Update available)" : ""));
			
			m1.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{ openURL("https://github.com/LatvianModder/CurseGraph/releases"); }
			});
			
			menu.add(m1);
		}
		
		menu.addSeparator();
		
		{
			MenuItem m1 = new MenuItem("Refresh");
			
			m1.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{ try { refresh(); /*Graph.logData();*/ } catch(Exception ex)
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
						{ showError("Invalid number!"); }
					}
				}
			});
			
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
							if(input != null && !input.isEmpty()) Projects.add(t, input, false);
						}
						catch(Exception ex)
						{ ex.printStackTrace(); }
					}
				});
				
				m1.add(m2);
			}
			
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
	
	public static void showInfo(String string)
	{
		JOptionPane.showMessageDialog(null, string, "Info", JOptionPane.INFORMATION_MESSAGE);
		System.out.println(string);
	}

	public static void showError(String string)
	{
		JOptionPane.showMessageDialog(null, string, "Error!", JOptionPane.ERROR_MESSAGE);
		System.out.println(string);
	}

	public static void refresh0(PopupMenu menu) throws Exception
	{
		if(!Projects.load()) menu.add("No projects loaded");
		else
		{
			for(final Curse.Project p : Projects.list)
			{
				PopupMenu info = new PopupMenu(p.title);
				
				{
					MenuItem m1 = new MenuItem("ProjectID: " + p.modID);
					m1.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{ openURL(p.url); }
					});
					
					info.add(m1);
				}
				
				if(p.authors.length == 1)
				{
					MenuItem m1 = new MenuItem("Author: " + p.authors[0]);
					m1.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{ openURL("http://minecraft.curseforge.com/members/" + p.authors[0]); }
					});
					
					info.add(m1);
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
					info.add(authors);
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
				
				{
					MenuItem remove = new MenuItem("Remove");
					remove.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{
							Projects.list.remove(p);
							Projects.save();
							refresh();
						}
					});
					
					info.add(remove);
				}
				
				info.addSeparator();
				
				info.add("Likes / Favorites: " + p.likes + " / " + p.favorites);
				
				{
					PopupMenu downloads = new PopupMenu("Downloads:");
					downloads.add("All: " + p.getTotalDownloads());
					downloads.add("Monthly: " + p.downloads.get("monthly"));
					downloads.add("Last file: " + p.download.downloads);
					info.add(downloads);
				}
				
				{
					PopupMenu downloads = new PopupMenu("Files:");
					
					MenuItem all = new MenuItem("All");
					all.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{ openURL("http://minecraft.curseforge.com/" + p.getType().ID + "/" + p.modID + "/files"); }
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
				
				menu.add(info);
			}
		}
	}
	
	public static void openURL(String s)
	{ try { Desktop.getDesktop().browse(new URI(s)); }
	catch (Exception e) { e.printStackTrace(); } }
	
	public static class Config
	{
		@Expose public Integer refreshMinutes;
		
		public void setDefaults()
		{
			refreshMinutes = 15;
		}
		
		public void save()
		{
			try
			{
				String s = Utils.toJson(this, true);
				FileOutputStream fos = new FileOutputStream(configFile);
				fos.write(s.getBytes());
				fos.close();
			}
			catch(Exception e)
			{ e.printStackTrace(); }
		}
	}
}