package latmod.cursegraph;

import java.util.Map;

import com.google.gson.annotations.Expose;

public class Curse
{
	public static enum Type
	{
		MOD("mc-mods", "Mod"),
		TEX_PACK("texture-packs", "Texture Pack"),
		WORLD("worlds", "World"),
		MODPACK("modpacks", "[WIP] Modpack"),
		
		; public static final Type[] VALUES = values();
		public final String ID;
		public final String name;
		
		Type(String s, String s1)
		{ ID = s; name = s1; }

		public static Type get(String s)
		{
			for(Type t : VALUES)
				if(s.equals(t.ID)) return t;
			return null;
		}
		
		public String toString()
		{ return ID; }
	}
	
	public static class Project
	{
		public String projectID;
		
		@Expose public Integer typeID;
		@Expose public String title;
		@Expose public String game;
		@Expose public String category;
		@Expose public String url;
		@Expose public String thumbnail;
		@Expose public String[] authors;
		@Expose public Map<String, Integer> downloads;
		@Expose public Integer favorites;
		@Expose public Integer likes;
		@Expose public String updated_at;
		@Expose public String created_at;
		@Expose public String project_url;
		@Expose public String release_type;
		@Expose public String license;
		@Expose public Version download;
		@Expose public Map<String, Version[]> versions;
		
		private int totalDownloads = -1;
		
		public String toString()
		{ return projectID; }
		
		public int hashCode()
		{ return toString().hashCode(); }
		
		public boolean equals(Object o)
		{ return o.toString().equals(toString()); }
		
		public int getTotalDownloads()
		{
			if(totalDownloads == -1)
			{
				int i1 = downloads.get("total");
				int i2 = 0;
				
				for(String s : versions.keySet())
				{
					for(Version v : versions.get(s))
						i2 += v.downloads.intValue();
				}
				
				totalDownloads = Math.max(i1, i2);
			}
			
			return totalDownloads;
		}
		
		public Type getType()
		{ return Type.VALUES[typeID]; }
	}
	
	public static class Version
	{
		@Expose public Integer id;
		@Expose public String url;
		@Expose public String name;
		@Expose public String type;
		@Expose public String version;
		@Expose public Integer downloads;
		@Expose public String created_at;
	}
}