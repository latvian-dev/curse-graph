package latmod.cursegraph;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

public class Utils
{
	public static String toString(InputStream is) throws Exception
	{ byte b[] = new byte[is.available()]; is.read(b); is.close(); return new String(b); }
	
	public static File newFile(File f)
	{
		if(!f.exists())
		{
			try { f.createNewFile(); }
			catch(Exception e)
			{
				f.getParentFile().mkdirs();
				try { f.createNewFile(); }
				catch(Exception e1)
				{ e1.printStackTrace(); }
			}
		}
		return f;
	}
	
	public static <T> T fromJson(String s, Type t)
	{
		if(s == null || s.length() < 2) return null;
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		return gson.fromJson(s, t);
	}
	
	public static <T> T fromJsonFile(File f, Type t)
	{
		if(f == null || !f.exists()) return null;
		
		try
		{
			FileInputStream fis = new FileInputStream(f);
			String s = toString(fis);
			return fromJson(s, t);
		}
		catch(Exception e)
		{ e.printStackTrace(); return null; }
	}
	
	public static String toJson(Object o, boolean asTree)
	{
		GsonBuilder gb = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();
		if(asTree) gb.setPrettyPrinting(); Gson gson = gb.create(); return gson.toJson(o);
	}
	
	public static void toJsonFile(File f, Object o)
	{
		String s = toJson(o, true);
		
		try
		{
			FileOutputStream fos = new FileOutputStream(newFile(f));
			fos.write(s.getBytes()); fos.close();
		}
		catch(Exception e)
		{ e.printStackTrace(); }
	}
	
	public static <K, V> Type getMapType(Type K, Type V)
	{ return new TypeToken<Map<K, V>>() {}.getType(); }
	
	public static <E> Type getListType(Type E)
	{ return new TypeToken<List<E>>() {}.getType(); }
	
	public static double map(double val, double min1, double max1, double min2, double max2)
	{ return min2 + (max2 - min2) * ((val - min1) / (max1 - min1)); }
	
	public static int mapInt(int val, int min1, int max1, int min2, int max2)
	{ return min2 + (max2 - min2) * ((val - min1) / (max1 - min1)); }
	
	public static long mapLong(long val, long min1, long max1, long min2, long max2)
	{ return min2 + (max2 - min2) * ((val - min1) / (max1 - min1)); }
}