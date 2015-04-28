package latmod.cursegraph;

import java.awt.Color;

public enum ColorScheme
{
	DARK_ORANGE(0x000000, 0x1E1E1E, 0xFF9D00, 0xFFC900),
	DARK_BLUE(0x000000, 0x1E1E1E, 0x00A4ED, 0x9BDDFF),
	DARK_GREEN(0x000000, 0x1E1E1E, 0x46E837, 0x80FF75),
	PAPER(0xFFECAF, 0xD8C995, 0x333333, 0x333333);
	
	public final Color background;
	public final Color grid;
	public final Color nodes;
	public final Color text;
	
	ColorScheme(int c1, int c2, int c3, int c4)
	{
		background = new Color(c1);
		grid = new Color(c2);
		nodes = new Color(c3);
		text = new Color(c4);
	}
}