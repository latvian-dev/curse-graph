package latmod.cursegraph;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

public class CurseGraphFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	public static final CurseGraphFrame inst = new CurseGraphFrame();
	
	public final JTabbedPane pane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
	
	public CurseGraphFrame()
	{
		setTitle("CurseGraph v" + Main.version);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		add(pane);
		this.setSize(700, 500);
		pane.setSize(700, 500);
		setResizable(true);
		refresh();
		pack();
		setLocationRelativeTo(null);
		
		setVisible(!Main.config.startMinimized.booleanValue());
		setIconImage(Main.imageReady);
	}
	
	public void dispose()
	{
		if(Main.config.closeToTray) setVisible(false);
		else System.exit(0);
	}
	
	public void refresh()
	{
		pane.removeAll();
		pane.setTabLayoutPolicy(Main.config.scrollTabs.booleanValue() ? JTabbedPane.SCROLL_TAB_LAYOUT : JTabbedPane.WRAP_TAB_LAYOUT);
		
		ArrayList<String> componentsAdded = new ArrayList<String>();
		componentsAdded.add("Settings");
		
		JPanel settingsPanel = new JPanel(false);
		
		GridLayout layout = new GridLayout();
		layout.setColumns(1);
		layout.setRows(0);
		layout.setVgap(5);
		settingsPanel.setLayout(layout);
		
		{
			JButton b = new JButton("Add");
			b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						String types[] = Curse.Type.getAllNames();
						String type0 = (String)JOptionPane.showInputDialog(null, "Select the mod:", "Add new Project", JOptionPane.PLAIN_MESSAGE, null, types, Curse.Type.MOD.name);
						if(type0 == null || type0.isEmpty()) return;
						Curse.Type t = Curse.Type.getFromName(type0);
						
						String input = JOptionPane.showInputDialog("Enter " + t.name + "'s ProjectID here:", "");
						if(input != null && !input.trim().isEmpty())
						{
							Projects.add(t, input.trim(), false);
							Main.refresh();
						}
					}
					catch(Exception ex)
					{ ex.printStackTrace(); }
				}
			});
			
			settingsPanel.add(b);
		}
		
		{
			JButton b = new JButton("Refresh");
			b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{ try { Main.refresh(); Graph.logData(); } catch(Exception ex)
				{ ex.printStackTrace(); } }
			});
			
			settingsPanel.add(b);
		}
		
		{
			JButton b = new JButton("Set graph limit");
			b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						int times[] = { -1, 1, 24, 24 * 7, 24 * 30 };
						String types[] = { "None", "Hour", "Day", "Week", "Month" };
						
						String type0 = (String)JOptionPane.showInputDialog(null, "Select graph type:", "Graph type", JOptionPane.PLAIN_MESSAGE, null, types, types[0]);
						if(type0 == null || type0.isEmpty()) return;
						
						for(int i = 0; i < types.length; i++)
						{
							if(type0.equals(types[i]))
							{
								Main.config.graphLimit = times[i];
								Main.refresh();
							}
						}
					}
					catch(Exception ex)
					{ ex.printStackTrace(); }
				}
			});
			
			settingsPanel.add(b);
		}
		
		{
			JButton b = new JButton("Graph type: " + (Main.config.graphRelative.booleanValue() ? "Relative" : "Default"));
			b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					Main.config.graphRelative = !Main.config.graphRelative.booleanValue();
					Main.config.save();
					refresh();
				}
			});
			
			settingsPanel.add(b);
		}
		
		{
			final boolean scrollTabs = Main.config.scrollTabs.booleanValue();
			final JButton b = new JButton("Scrolling Tabs: " + (scrollTabs ? "ON" : "OFF"));
			b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					Main.config.scrollTabs = !Main.config.scrollTabs.booleanValue();
					Main.config.save();
					refresh();
				}
			});
			
			settingsPanel.add(b);
		}
		
		{
			JButton b = new JButton("Set refresh interval");
			b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					String input = JOptionPane.showInputDialog("Set refresh minutes:", "" + Main.config.refreshMinutes);
					if(input != null && !input.isEmpty())
					{
						try
						{
							int i = Integer.parseInt(input);
							Main.config.refreshMinutes = Math.max(1, i);
							Main.config.save();
							
							Graph.checker.start();
						}
						catch(Exception ex)
						{ Main.error("Invalid number!", false); }
					}
				}
			});
			
			settingsPanel.add(b);
		}
		
		{
			JButton b = new JButton("Open data folder");
			b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{ try { Desktop.getDesktop().open(Main.folder); }
				catch(Exception ex) { ex.printStackTrace(); } }
			});
			
			settingsPanel.add(b);
		}
		
		/*
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
							{ error("Invalid number!", false); }
						}
					}
				});
				
				m1.add(m2);
			}
			
			menu.add(m1);
		}
		*/
		{
			JButton b = new JButton("Exit");
			b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{ System.exit(0); }
			});
			
			settingsPanel.add(b);
		}
		
		pane.addTab("Settings", null, settingsPanel, null);
		
		Curse.Project[] projectsList = Projects.list.toArray(new Curse.Project[0]);
		Arrays.sort(projectsList);
		
		for(final Curse.Project p : projectsList)
		{
			final JPanel panel = new JPanel(false);
			
			JPopupMenu menu = new JPopupMenu();
			
			{
				JMenuItem m1 = new JMenuItem("Open Project");
				m1.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{ Main.openURL(p.url); }
				});
				
				menu.add(m1);
			}
			
			JMenu info = new JMenu("Info");
			
			{
				if(p.authors.length == 1)
				{
					JMenuItem m1 = new JMenuItem("Author: " + p.authors[0]);
					m1.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{ Main.openURL("http://minecraft.curseforge.com/members/" + p.authors[0]); }
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
							{ Main.openURL("http://minecraft.curseforge.com/members/" + s); }
						});
						authors.add(m1);
					}
					
					info.add(authors);
				}
				
				info.add("Likes: " + p.likes);
				info.add("Favorites: " + p.favorites);
			}
			
			info.add("All downloads: " + p.getTotalDownloads());
			Integer mo = p.downloads.get("monthly");
			if(mo != null && mo.intValue() > 0) info.add("Monthly downloads: " + mo);
			info.add("Last file downloads: " + p.download.downloads);
			
			menu.add(info);
			
			{
				JMenu downloads = new JMenu("Files:");
				
				JMenuItem all = new JMenuItem("All");
				all.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{ Main.openURL("http://minecraft.curseforge.com/" + p.getType().ID + "/" + p.projectID + "/files"); }
				});
				
				downloads.add(all);
				
				JMenuItem latest = new JMenuItem("Latest");
				latest.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{ Main.openURL(p.download.url); }
				});
				
				downloads.add(latest);
				
				for(String s : p.versions.keySet())
				{
					JMenu m1 = new JMenu(s);
					
					final Curse.Version[] v = p.versions.get(s);
					for(int j = 0; j < v.length; j++)
					{
						final int fj = j;
						JMenuItem m2 = new JMenuItem(v[j].name);
						
						m2.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent e)
							{ Main.openURL(v[fj].url); }
						});
						
						m1.add(m2);
					}
					
					downloads.add(m1);
				}
				
				menu.add(downloads);
			}
			
			{
				JMenuItem remove = new JMenuItem("Remove");
				remove.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						if(Main.showYesNo("Confirm", "Remove " + p.title + "?"))
						{
							Projects.list.remove(p);
							Projects.save();
							Main.refresh();
						}
					}
				});
				
				menu.add(remove);
			}
			
			panel.setComponentPopupMenu(menu);
			
			panel.add(new JCurseGraph(panel, p));
			pane.addTab(p.title, null, panel, null);
			componentsAdded.add(p.title);
		}
		
		JPopupMenu menuAll = new JPopupMenu();
		
		for(int i = 0; i < componentsAdded.size(); i++)
		{
			final JMenuItem item = new JMenuItem(componentsAdded.get(i));
			final int index = i;
			item.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					pane.setSelectedIndex(index);
				}
			});
			
			menuAll.add(item);
		}
		
		pane.setComponentPopupMenu(menuAll);
	}
	
	//private static long getH(int i)
	//{ return 1000L * 60L * 60L * i; }
}