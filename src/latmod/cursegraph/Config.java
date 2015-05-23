package latmod.cursegraph;

import java.io.File;

import latmod.cursegraph.JCurseGraph.Colors;

import com.google.gson.annotations.Expose;

public class Config
{
	@Expose public Integer refreshMinutes;
	@Expose public Integer graphLimit;
	@Expose public Boolean graphRelative;
	@Expose public Boolean startMinimized;
	@Expose public String dataFolderPath;
	@Expose public Boolean scrollTabs;
	@Expose public Boolean closeToTray;
	@Expose public String colorBackground;
	@Expose public String colorGrid;
	@Expose public String colorLines;
	@Expose public String colorText;
	@Expose public Integer[] exportGraph;
	@Expose public Integer displayTabs;
	
	public void setDefaults()
	{
		if(refreshMinutes == null) refreshMinutes = 30;
		if(graphLimit == null) graphLimit = -1;
		if(graphRelative == null) graphRelative = false;
		if(startMinimized == null) startMinimized = true;
		if(dataFolderPath == null) dataFolderPath = new File(Main.folder, "data/").getAbsolutePath().replace("\\", "/");
		if(scrollTabs == null) scrollTabs = true;
		if(closeToTray == null) closeToTray = true;
		if(colorBackground == null) colorBackground = "#000000";
		if(colorGrid == null) colorGrid = "#1E1E1E";
		if(colorLines == null) colorLines = "#FF9D00";
		if(colorText == null) colorText = "#FFC900";
		if(exportGraph == null) exportGraph = new Integer[]{ 800, 600 };
		if(displayTabs == null) displayTabs = 1;
		
		Colors.update();
		if(refreshMinutes < 1) refreshMinutes = 1;
		if(exportGraph.length != 2) exportGraph = new Integer[]{ 800, 600 };
	}
	
	public void save()
	{ Utils.toJsonFile(Main.configFile, Config.this); }
}