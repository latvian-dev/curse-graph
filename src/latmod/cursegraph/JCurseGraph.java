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
			
			pixels[i] = Graph.C_BG.getRGB();
			
			if((y % 24 == 0) || (x % 24 == 0)) pixels[i] = Graph.C_GRID.getRGB();
		}
		
		img.setRGB(0, 0, w, h, pixels, 0, w);
		
		return new ImageIcon(img);
	}

	public void paint(Graphics g)
	{
		int parentW = parent.getWidth();
		int parentH = parent.getHeight();
		
		if(lastWidth != parentW || lastHeight != parentH)
		{
			setIcon(getIcon(project, parentW, parentH));
			lastWidth = parentW;
			lastHeight = parentH;
		}
		
		int w = getWidth();
		int h = getHeight();
		
		super.paint(g);
		
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		List<Graph.TimedDown> values = Graph.getAllKeys(project.projectID);
		
		long minTime = -1;
		long maxTime = -1;
		int minDown = -1;
		int maxDown = -1;
		
		//values.add(new Graph.TimedDown(System.currentTimeMillis(), project.getTotalDownloads()));
		
		for(int i = 0; i < values.size(); i++)
		{
			Graph.TimedDown t = values.get(i);
			if(minTime == -1 || t.time < minTime) minTime = t.time;
			if(maxTime == -1 || t.time > maxTime) maxTime = t.time;
			if(minDown == -1 || t.down < minDown) minDown = t.down;
			if(maxDown == -1 || t.down > maxDown) maxDown = t.down;
		}
		
		ArrayList<GraphPoint> points = new ArrayList<GraphPoint>();
		
		if(Main.config.graphLimit.intValue() > 0)
		{
			int pc = Main.config.graphLimit.intValue() * 4;
			
			long currentTime = System.currentTimeMillis();
			long h24 = (24000L * 3600L);
			
			for(int i = 0; i < pc; i++)
			{
				long time = currentTime - h24 + (i * 3600000L);
				
				//long time = values[i].time.longValue();
				//int downs = values[i].down.intValue();
				
				double x = Utils.map(minTime, minTime, maxTime, 0D, w);
				double y = Utils.map(minDown, minDown, maxDown, h, 0D);
				
				y = Math.max(2, Math.min(y, h - 2));
				points.add(new GraphPoint(x, y, time, 0));
			}
			
		}
		else
		{
			for(int i = 0; i < values.size(); i++)
			{
				Graph.TimedDown t = values.get(i);
				
				double x = Utils.map(t.time, minTime, maxTime, 0D, w);
				double y = Utils.map(t.down, minDown, maxDown, h, 0D);
				
				y = Math.max(2, Math.min(y, h - 2));
				points.add(new GraphPoint(x, y, t.time, t.down));
			}
			
		}
		
		boolean isOver = false;
		
		for(int i = points.size() - 1; i >= 0; i--)
		{
			GraphPoint p = points.get(i);
			GraphPoint pp = new GraphPoint(0, h - 1, 0, 0);
			
			if(i > 0) pp = points.get(i - 1);
			
			g.setColor(Graph.C_NODE);
			g.drawLine(pp.x, pp.y, p.x, p.y);
			//g.drawLine(p.x, h - 4, p.x, h);
			g.drawOval(p.x - 3, p.y - 3, 6, 6);
			
			if(!isOver && p.time > 0L && Utils.distSq(mouseX, mouseY, p.x, p.y) <= 100D)
			{
				isOver = true;
				g.setColor(Graph.C_TEXT);
				g.drawString(Graph.getTimeString(p.time) + " :: " + p.downs, 4, 16);
				g.setColor(Graph.C_NODE);
				g.drawOval(p.x - 1, p.y - 1, 2, 2);
				g.drawOval(p.x - 2, p.y - 2, 4, 4);
			}
		}
		
		if(!isOver)
		{
			g.setColor(Graph.C_TEXT);
			g.drawString("" + maxDown, 4, 16);
			g.drawString("" + ((maxDown + minDown) / 2), 4, h / 2 + 4);
			g.drawString("" + minDown, 4, h - 8);
		}
		
		repaint();
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