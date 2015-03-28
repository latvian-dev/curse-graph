package latmod.cursegraph;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.*;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import com.google.gson.annotations.Expose;

public class Main
{
	public static final int version = 5;
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
		
		System.out.println("Loading CurseGraph, Version: " + version + " @ " + Graph.getTimeString(System.currentTimeMillis()));
		
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
			{ CurseGraphFrame.inst.setVisible(!CurseGraphFrame.inst.isVisible()); }
		});
		
		refresh();
		Projects.save();
		Graph.init();
		
		if(latestVersion > version) info("Update available!");
		
		CurseGraphFrame.inst.setVisible(!config.startMinimized.booleanValue());
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
		CurseGraphFrame.inst.refresh();
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
	
	public static void openURL(String s)
	{ try { Desktop.getDesktop().browse(new URI(s)); }
	catch (Exception e) { e.printStackTrace(); } }
	
	public static class Config
	{
		@Expose public Integer refreshMinutes;
		@Expose public String lastProjectID;
		@Expose public Boolean graphRelative;
		@Expose public Integer graphLimit;
		@Expose public Boolean startMinimized;
		
		public void setDefaults()
		{
			if(refreshMinutes == null) refreshMinutes = 30;
			if(lastProjectID == null) lastProjectID = "";
			if(graphRelative == null) graphRelative = false;
			if(graphLimit == null) graphLimit = -1;
			if(startMinimized == null) startMinimized = true;
		}
		
		public void save()
		{ Utils.toJsonFile(configFile, Config.this); }
	}
}