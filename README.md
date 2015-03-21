# CurseGraph

This small application creates a background process in SystemTray (On Windows, its the small icon bar on bottom right) and tracks and records your projects total downloads every 15 minutes. By default, "LatCoreMC", "Tinker's Construct" and "Mine & Blade: Battlegear 2" are added, but you can press "Remove" to remove them and "Add" in main menu to add a new project. Files are saved in C:/Users/username/LatMod/CurseGraph/, formatted in Json

Valid ProjectIDs are "224778-latcoremc" or "tinkers-construct" (the ending of your Curse project's link). Older projects don't have the number. (It's still a mystery, maybe someone can explain this to me)

This is still WIP for MacOSX, you can still doubleclick to open Graph selector, but you have to edit ProjectIDs in /user home/CurseGraph/projects.json

Get latest version here: https://github.com/LatvianModder/CurseGraph/releases