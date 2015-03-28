package latmod.cursegraph;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class CurseGraphFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	public static final CurseGraphFrame inst = new CurseGraphFrame();
	
	public final JTabbedPane pane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
	
	public CurseGraphFrame()
	{
		setTitle("CurseGraph v" + Main.version);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		add(pane);
		pane.setSize(700, 500);
		setResizable(true);
		refresh();
		pack();
		setLocationRelativeTo(null);
	}
	
	public void refresh()
	{
		pane.removeAll();
		
		JPanel settingsPanel = new JPanel(false);
		
		//settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
		BorderLayout layout = new BorderLayout();
		layout.setHgap(40);
		layout.setVgap(50);
		settingsPanel.setLayout(layout);
		
		{
			JButton b = new JButton("Refresh");
			b.setAlignmentX(Component.CENTER_ALIGNMENT);
			b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{ try { refresh(); Graph.logData(); } catch(Exception ex)
				{ ex.printStackTrace(); } }
			});
			
			settingsPanel.add(b, BorderLayout.LINE_END);
		}
		
		{
			JButton b = new JButton("Set refresh interval");
			b.setAlignmentX(Component.CENTER_ALIGNMENT);
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
						{ Main.error("Invalid number!"); }
					}
				}
			});
			
			settingsPanel.add(b, BorderLayout.LINE_START);
		}
		
		{
			JButton b = new JButton("Add");
			b.setAlignmentX(Component.CENTER_ALIGNMENT);
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
			
			settingsPanel.add(b, BorderLayout.CENTER);
		}
		
		{
			JButton b = new JButton("Open data folder");
			b.setAlignmentX(Component.CENTER_ALIGNMENT);
			b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{ try { Desktop.getDesktop().open(Main.folder); }
				catch(Exception ex) { ex.printStackTrace(); } }
			});
			
			settingsPanel.add(b, BorderLayout.PAGE_START);
		}
		
		{
			JButton b = new JButton("Exit");
			b.setAlignmentX(Component.CENTER_ALIGNMENT);
			b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{ System.exit(0); }
			});
			
			settingsPanel.add(b, BorderLayout.PAGE_END);
		}
		
		addPanel("Settings", settingsPanel);
		
		for(final Curse.Project p : Projects.list)
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
							refresh();
						}
					}
				});
				
				menu.add(remove);
			}
			
			panel.setComponentPopupMenu(menu);
			
			panel.add(new JCurseGraph(panel, p));
			addPanel(p.title, panel);
		}
	}
	
	public void addPanel(String title, JComponent c)
	{ pane.addTab(title, null, c, title); }
}