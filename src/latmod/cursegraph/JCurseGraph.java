package latmod.cursegraph;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import javax.swing.*;

public class JCurseGraph extends JLabel implements MouseMotionListener, MouseListener
{
	private static final long serialVersionUID = 1L;
	private static final int fontSize = 12;
	
	private static class GraphPoint
	{
		public final int x;
		public final int y;
		public final long time;
		public final int downs;
		
		public GraphPoint(double px, double py, long t, int d)
		{ x = (int)px; y = (int)py; time = t; downs = d; }
	}
	
	public final JPanel parent;
	public final Curse.Project project;
	
	private int lastWidth = -1;
	private int lastHeight = -1;
	private int mouseX, mouseY;
	
	public JCurseGraph(JPanel jp, Curse.Project p)
	{
		super(getIcon(p, 700, 500));
		parent = jp;
		project = p;
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	private static Icon getIcon(Curse.Project p, int w, int h)
	{
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		int pixels[] = new int[w * h];
		
		for(int i = 0; i < pixels.length; i++)
		{
			int x = i % w;
			int y = i / w;
			
			pixels[i] = Graph.colors.background.getRGB();
			
			if((y % 24 == 0) || (x % 24 == 0)) pixels[i] = Graph.colors.grid.getRGB();
		}
		
		img.setRGB(0, 0, w, h, pixels, 0, w);
		
		return new ImageIcon(img);
	}

	public void paint(Graphics g)
	{
		int parentW = parent.getWidth();
		int parentH = parent.getHeight() - 8;
		
		if(lastWidth != parentW || lastHeight != parentH)
		{
			setIcon(getIcon(project, parentW, parentH));
			lastWidth = parentW;
			lastHeight = parentH;
		}
		
		int w = getWidth();
		int h = getHeight() - 8;
		
		super.paint(g);
		
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		List<Graph.TimedDown> values = Graph.getDownloads(project.projectID);
		//values.add(new Graph.TimedDown(System.currentTimeMillis(), project.getTotalDownloads()));
		
		if(Main.config.graphLimit.intValue() > 0)
		{
			long l = System.currentTimeMillis() - (Main.config.graphLimit.intValue() * 3600000L);
			
			//System.out.println(l);
			
			for(int i = 0; i < values.size(); i++)
			{ if(values.get(i).time < l) values.remove(i); }
		}
		
		ArrayList<GraphPoint> points = new ArrayList<GraphPoint>();
		
		long minTime = -1;
		long maxTime = -1;
		int minDown = -1;
		int maxDown = -1;
		
		boolean isRelative = Main.config.graphRelative.booleanValue();
		
		if(isRelative)
		{
			if(values.size() >= 2)
			{
				Graph.TimedDown min = values.get(0);
				Graph.TimedDown max = values.get(values.size() - 1);
				
				minTime = min.time;
				maxTime = max.time;
				minDown = 0;
				maxDown = 0;
				
				for(int i = 1; i < values.size(); i++)
				{
					int d0 = values.get(i - 1).down;
					int d = values.get(i).down;
					if(maxDown == -1 || (d - d0) > maxDown) maxDown = (d - d0);
				}
				
				for(int i = 0; i < values.size(); i++)
				{
					Graph.TimedDown t = values.get(i);
					Graph.TimedDown t0 = (i > 0 ? values.get(i - 1) : new Graph.TimedDown(minTime, minDown));
					
					int down = (t.down - t0.down);
					
					double x = Utils.map(t.time, minTime, maxTime, 0D, w);
					double y = Utils.map(down, minDown, maxDown, h, 0D);
					
					y = Math.max(2, Math.min(y, h - 2));
					points.add(new GraphPoint(x, y, t.time, t.down));
				}
			}
			
		}
		else
		{
			if(values.size() >= 2)
			{
				Graph.TimedDown min = values.get(0);
				Graph.TimedDown max = values.get(values.size() - 1);
				
				minTime = min.time;
				maxTime = max.time;
				minDown = min.down;
				maxDown = max.down;
				
				for(int i = 0; i < values.size(); i++)
				{
					Graph.TimedDown t = values.get(i);
					
					double x = Utils.map(t.time, minTime, maxTime, 0D, w);
					double y = Utils.map(t.down, minDown, maxDown, h, 0D);
					
					y = Math.max(2, Math.min(y, h - 2));
					points.add(new GraphPoint(x, y, t.time, t.down));
				}
			}
		}
		
		boolean isOver = false;
		
		for(int i = points.size() - 1; i >= 0; i--)
		{
			GraphPoint p = points.get(i);
			GraphPoint pp = new GraphPoint(0, h - 1, 0, 0);
			
			if(i > 0) pp = points.get(i - 1);
			
			g.setColor(Graph.colors.nodes);
			g.drawLine(pp.x, pp.y, p.x, p.y);
			//g.drawLine(p.x, h - 4, p.x, h);
			
			if(!isRelative)
				g.drawOval(p.x - 2, p.y - 2, 4, 4);
			else
				g.drawOval(p.x - 1, p.y - 1, 2, 2);
			
			if(!isOver && p.time > 0L && Utils.distSq(mouseX, mouseY, p.x, p.y) <= (6 * 6))
			{
				isOver = true;
				g.setColor(Graph.colors.text);
				
				if(i > 0 && isRelative)
					g.drawString(Graph.getTimeString(p.time) + " :: " + p.downs + " :: +" + (p.downs - pp.downs), 4, 16);
				else
					g.drawString(Graph.getTimeString(p.time) + " :: " + p.downs, 4, 16);
				g.setColor(Graph.colors.nodes);
				
				if(!isRelative)
					g.drawOval(p.x - 1, p.y - 1, 2, 2);
				g.drawOval(p.x - 2, p.y - 2, 4, 4);
			}
		}
		
		if(!isOver)
		{
			g.setColor(Graph.colors.text);
			drawString("" + maxDown, 4, 4, g);
			drawString("" + ((maxDown + minDown) / 2), 4, h / 2 + fontSize / 2 - 6, g);
			drawString("" + minDown, 4, h - fontSize + 6, g);
			
			if(!isRelative)
			{
				String ns = points.size() + " Nodes";
				drawString(ns, w - (g.getFontMetrics().stringWidth(ns) + 3), h - fontSize + 6, g);
			}
		}
		
		repaint();
	}
	
	public void drawString(String s, int x, int y, Graphics g)
	{
		FontMetrics fm = g.getFontMetrics();
		int y1 = fm.getAscent() + y - fm.getDescent();// + fontSize / 2;
		g.drawString(s, x, y1);
	}
	
	public void mouseDragged(MouseEvent e) { }
	
	public void mouseMoved(MouseEvent e)
	{
		mouseX = e.getX();
		mouseY = e.getY();
		repaint();
	}
	
	public void mouseReleased(MouseEvent e) { }
	public void mousePressed(MouseEvent e) {  }
	public void mouseExited(MouseEvent e) { }
	public void mouseEntered(MouseEvent e) { }
	
	public void mouseClicked(MouseEvent e)
	{ try { Graph.logData(); } catch(Exception ex) {} repaint(); }
}