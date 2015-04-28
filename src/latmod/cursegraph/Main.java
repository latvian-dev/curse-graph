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
	public static final int version = 7;
	public static int latestVersion = -1;
	
	public static TrayIcon trayIcon = null;
	public static BufferedImage imageReady, imageBusy;
	
	public static final File folder = getFolder();
	public static File projectsFile, configFile;
	public static Config config;
	
	private static boolean firstRefresh;
	
	public static void main(String[] args) throws Exception
	{
		if(!Desktop.isDesktopSupported())
		{
			System.out.println("Desktop not supported!");
			System.exit(1); return;
		}
		
		System.out.println("Loading CurseGraph, Version: " + version + " @ " + Graph.getTimeString(System.currentTimeMillis()));
		
		{
			File versionFile = new File(folder, "CurseGraph.version");
			versionFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(versionFile);
			fos.write(("" + version).getBytes()); fos.close();
		}
		
		configFile = new File(folder, "config.json");
		config = Utils.fromJsonFile(configFile, Config.class);
		if(!configFile.exists()) configFile.createNewFile();
		
		if(config == null) config = new Config();
		config.setDefaults();
		config.save();
		
		projectsFile = new File(config.projectsFilePath);
		
		try
		{
			String s = Utils.toString(new URL("http://pastebin.com/raw.php?i=RyuQPm4f").openStream());
			if(s != null) latestVersion = Integer.parseInt(s);
		}
		catch(Exception e)
		{ e.printStackTrace(); System.exit(1); }
		
		imageReady = loadImage("trayIcon.png");
		imageBusy = loadImage("trayIconBusy.png");
		
		firstRefresh = true;
		
		trayIcon = new TrayIcon(imageReady);
		trayIcon.setImageAutoSize(true);
		trayIcon.setToolTip("Curse Graph");
		
		if(!Utils.contains(args, "notray"))
		{
			if(!SystemTray.isSupported())
			{
				System.out.println("System tray not supported!");
				System.exit(1); return;
			}
			
			trayIcon.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{ CurseGraphFrame.inst.setVisible(!CurseGraphFrame.inst.isVisible()); }
			});
			
			PopupMenu menu = new PopupMenu();
			
			{
				MenuItem m1 = new MenuItem("Curse Graph v" + version + ((latestVersion > version) ? " (Update available)" : ""));
				
				m1.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{ CurseGraphFrame.inst.setVisible(true); }
				});
				
				menu.add(m1);
			}
			
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
			
			SystemTray.getSystemTray().add(trayIcon);
		}
		
		refresh();
		Projects.save();
		Graph.init();
		
		if(latestVersion > version) info("Update available!", false);
		
		firstRefresh = false;
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
		if(!firstRefresh) CurseGraphFrame.inst.setIconImage(imageBusy);
		Projects.load();
		
		trayIcon.setImage(imageReady);
		if(!firstRefresh) CurseGraphFrame.inst.setIconImage(imageReady);
		CurseGraphFrame.inst.refresh();
	}
	
	public static void info(String string, boolean silent)
	{
		if(!silent) JOptionPane.showMessageDialog(null, string, "Info", JOptionPane.INFORMATION_MESSAGE);
		System.out.println(string);
	}

	public static void error(String string, boolean silent)
	{
		if(!silent) JOptionPane.showMessageDialog(null, string, "Error!", JOptionPane.ERROR_MESSAGE);
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
		@Expose public Integer graphLimit;
		@Expose public Boolean graphRelative;
		@Expose public Boolean startMinimized;
		@Expose public String dataFolderPath;
		@Expose public String projectsFilePath;
		@Expose public Boolean scrollTabs;
		@Expose public Boolean closeToTray;
		@Expose public Integer colorScheme;
		
		public void setDefaults()
		{
			if(refreshMinutes == null) refreshMinutes = 30;
			if(graphLimit == null) graphLimit = -1;
			if(graphRelative == null) graphRelative = false;
			if(startMinimized == null) startMinimized = true;
			if(dataFolderPath == null) dataFolderPath = new File(folder, "data/").getAbsolutePath().replace("\\", "/");
			if(projectsFilePath == null) projectsFilePath = new File(folder, "projects.txt").getAbsolutePath().replace("\\", "/");
			if(scrollTabs == null) scrollTabs = true;
			if(closeToTray == null) closeToTray = true;
			if(colorScheme == null) colorScheme = ColorScheme.DARK_ORANGE.ordinal();
		}
		
		public void save()
		{ Utils.toJsonFile(configFile, Config.this); }
	}
}