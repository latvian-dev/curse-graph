package latmod.cursegraph;

public enum OS
{
	WINDOWS,
	MACOSX,
	OTHER;
	
	public static final OS CURRENT = get();
	public final String filePath = toString().toLowerCase();
	
	private static OS get()
	{
		String s = System.getProperty("os.name");
		if (s.startsWith("Windows")) return WINDOWS;
		else if ((s.startsWith("Mac OS X"))
				|| (s.startsWith("Darwin"))) return MACOSX;
		return OTHER;
	}
}