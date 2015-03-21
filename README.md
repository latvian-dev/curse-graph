# CurseGraph

This small application creates a background process in SystemTray (On Windows, its the small icon bar on bottom right) and tracks and records your mods total downloads every 15 minutes. By default, my mods are added, but you can press "Remove" to remove them and "Add a Mod" in main menu to add a new mod. Files are saved in C:/Users/username/LatMod/CurseGraph/, formatted in Json

Valid ModIDs are "224778-latcoremc" or "tinkers-construct" (the ending of your Curse project's link). Older projects dont have the number.

This is still WIP for MacOSX, you can still doubleclick to open Graph selector, but you have to edit ModIDs in /user home/CurseGraph/mods.json