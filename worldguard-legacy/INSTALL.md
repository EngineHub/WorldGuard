# New User Guide

Thank you for installing WorldGuard! By default, when you first install WorldGuard, most features will be disabled until you enable the ones that you want to use.

## Requirements

You will need to be using [Bukkit](http://dl.bukkit.org/) for your server or one of its derivatives (such as Spigot or Cauldron). If you are using Minecraft Forge, you will have to use Cauldron in order to use WorldGuard.

It is not possible to use WorldGuard with the plain vanilla server from Mojang.

## Installation

Installation is simple!

1. Extract the `WorldGuard.jar` file and put it into your server's `plugins` folder. If the folder does not exist yet, you can create it yourself.
2. Run the Bukkit server.
3. Look inside `plugins/WorldGuard` and configure the plugin as necessary. There are also in-game commands.

By default, only "ops" can use WorldGuard commands. If you install a permissions plugin, then you can assign fine grained permissions to your server's trusted users.

## Documentation

To learn how to use WorldGuard, check out [the wiki](http://wiki.sk89q.com/wiki/WorldGuard).

## Frequently Asked Questions

### How do I protect my spawn?

Check out the [region protection tutorial](http://wiki.sk89q.com/wiki/$%7Bproject.name%7D/Regions/Tutorial) on the wiki.

### Players can't do anything!

WorldGuard will typically not block something without also telling the user that he or she does not have permission. Please make sure that it's not another plugin that is preventing players from interacting with the world.

Also, be aware that spawn protection is a feature of vanilla Minecraft and you must disable that in `bukkit.yml`. It only allows ops to do anything in a specified radius around the world's spawn point.

### I made a region but anyone can build in it!

1. If players get a "you can't do that here" message but they are still able to build, it's because another plugin is likely undoing the protection offered by WorldGuard.
2. If no message is being sent, make sure that there is a region where you think one is. 

### Where can I ask questions?

You can [ask questions on our forum](http://forum.sk89q.com) or visit us on IRC (irc.esper.net #sk89q).

### Where can I file a bug report or request a feature?

Visit our [issue tracker](http://youtrack.sk89q.com/dashboard). It's also used for WorldEdit and our other projects.

### Is WorldGuard open source?

Yes! [View the source code on GitHub](https://github.com/sk89q/worldguard).
