package latmod.cursegraph;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.JPanel;

public class JCurseGraph extends JPanel implements MouseMotionListener, MouseListener
{
	private static final long serialVersionUID = 1L;
	private static final int fontSize = 16;
	public static Color colorBackground, colorGrid, colorLines, colorText;
	public static boolean mouse = true;
	
	private static class GraphPoint
	{
		public final int x;
		public final int y;
		public final long time;
		public final int downs;
		
		public GraphPoint(double px, double py, long t, int d)
		{ x = (int)px; y = (int)py; time = t; downs = d; }
	}
	
	public static class Colors
	{
		public static Color background, grid, lines, text;
		
		public static void update()
		{
			try
			{
				background = new Color(Integer.decode(Main.config.colorBackground));
				grid = new Color(Integer.decode(Main.config.colorGrid));
				lines = new Color(Integer.decode(Main.config.colorText));
				text = new Color(Integer.decode(Main.config.colorText));
			}
			catch(Exception e)
			{ e.printStackTrace(); }
		}
	}
	
	public final Curse.Project project;
	
	private static int mouseX, mouseY;
	
	public JCurseGraph(Curse.Project p)
	{
		super(false);
		project = p;
		addMouseListener(this);
		addMouseMotionListener(this);
		setBackground(Colors.background);
	}
	
	public static void drawString(String s, int x, int y, Graphics g)
	{
		FontMetrics fm = g.getFontMetrics();
		int y1 = fm.getAscent() + y - fm.getDescent();// + fontSize / 2;
		g.drawString(s, x, y1);
	}
	
	public void paint(Graphics g0)
	{
		super.paint(g0);
		
		if(!(g0 instanceof Graphics2D)) return;
		Graphics2D g = (Graphics2D)g0;
		
		int w = getWidth();
		int h = getHeight();
		
		if(w < 0 || h < 0) return;
		
		g.setBackground(Colors.background);
		g.clearRect(0, 0, w, h);
		
		Colors.update();
		
		g.setColor(Colors.grid);
		
		for(int i = 0; i <= w; i += 24)
			g.drawLine(i, 0, i, h);
		
		for(int i = 0; i <= h; i += 24)
			g.drawLine(0, i, w, i);
		
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		List<Graph.TimedDown> values0 = Graph.getDownloads(project.projectID);
		List<Graph.TimedDown> values = new ArrayList<Graph.TimedDown>();
		//values.add(new Graph.TimedDown(System.currentTimeMillis(), project.getTotalDownloads()));
		
		if(Main.config.graphLimit.intValue() > 0)
		{
			long l = System.currentTimeMillis() - (Main.config.graphLimit.intValue() * 3600 * 1000L);
			
			//System.out.println(l);
			
			for(int i = 0; i < values0.size(); i++)
			{ if(values0.get(i).time >= l) values.add(values0.get(i)); }
		}
		else values = values0;
		
		ArrayList<GraphPoint> points = new ArrayList<GraphPoint>();
		
		long minTime = -1;
		long maxTime = -1;
		int minDown = -1;
		int maxDown = -1;
		
		boolean isRelative = Main.config.graphRelative.booleanValue();
		
		if(isRelative) // double angle = (Math.atan2(x - prevX, prevY - y) * 180D / Math.PI);
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
					if(i == 0) y = h - 2;
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
		
		int pointOver = -1;
		
		for(int i = points.size() - 1; i >= 0; i--)
		{
			GraphPoint p = points.get(i);
			GraphPoint pp = new GraphPoint(0, h - 1, 0, 0);
			
			if(i > 0) pp = points.get(i - 1);
			
			g.setColor(Colors.lines);
			g.drawLine(pp.x, pp.y, p.x, p.y);
			//g.drawLine(p.x, h - 4, p.x, h);
			
			if(!isRelative)
				g.drawOval(p.x - 2, p.y - 2, 4, 4);
			else
				g.drawOval(p.x - 1, p.y - 1, 2, 2);
			
			if(mouse && pointOver == -1 && p.time > 0L && Utils.distSq(mouseX, mouseY, p.x, p.y) <= (6 * 6))
				pointOver = i;
		}
		
		g.setColor(Colors.text);
		
		{
			drawString("" + maxDown, 4, 4, g);
			drawString("" + ((maxDown + minDown) / 2), 4, h / 2 - fontSize / 2, g);
			drawString("" + minDown, 4, h - fontSize, g);
		}
		
		if(!isRelative)
		{
			String ns = (pointOver != -1) ? ("Node #" + (pointOver + 1)) : (points.size() + " Nodes");
			drawString(ns, w - (g.getFontMetrics().stringWidth(ns) + 3), h - fontSize, g);
		}
		
		if(mouse && pointOver != -1)
		{
			GraphPoint p = points.get(pointOver);
			
			int x = mouseX - 10;
			int y = mouseY + 20;
			
			String txt = Graph.getTimeString(p.time) + " :: " + p.downs;
			
			if(isRelative && pointOver > 0)
			{
				GraphPoint pp = points.get(pointOver - 1);
				txt += " :: +" + (p.downs - pp.downs);
			}
			
			int ts = g.getFontMetrics().stringWidth(txt);
			
			if(x + ts + 18 > w)
				x = w - ts - 18;
			
			if(y + 18 > h)
				y = h - 18;
			
			if(!isRelative)
				g.drawOval(p.x - 1, p.y - 1, 2, 2);
			g.drawOval(p.x - 2, p.y - 2, 4, 4);
			
			g.setColor(Colors.grid);
			g.fillRect(x + 6, y, ts + 12, fontSize);
			g.setColor(Colors.text);
			g.drawString(txt, x + 12, y + 12);
		}
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