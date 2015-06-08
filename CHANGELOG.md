# Changelog

## 6.1

* Added `exit-via-teleport` flag (default allow) to control exiting an exit=deny region via teleportation.
* Added a `fall-damage` flag to control player damage caused by falling.
* Added a `time-lock` flag to lock players' time of day. Valid values are from 0 to 24000 for absolute time, or +- any number for relative time.
* Added a `weather-lock` flag to lock players' weather. Valid values are "clear" or "downfall". Unset to restore to normal world weather.
* Added `-s` to the event debugging commands to show a stack trace rather than attempt to detect the causing plugin.
* Added support for using the `-e` argument (sets an empty value for the flag) in the flag set command when the type of flag is of the 'set' type.
* Added NPCs from the Citizens plugin to a whitelist so they are not protected.
* Added support for Spigot's BlockExplodeEvent.
* Changed tripwire to fall under the `use` flag.
* Changed enderchests to fall under the `use` flag.
* Changed vehicles and animals so they are not included in the `interact` flag.
* Changed the display of custom blacklist messages to no longer include a period at the end.
* Changed protection logic to consider connected chests.
* Changed the heal and feed flags to increase values for players who are invincible (or in creative mode) but not decrease them.
* Changed PvP protection to consider both attacker and defender locations.
* Fixed missing protection data for some 1.8-added blocks.
* Fixed compatibility issues with MC 1.7.
* Fixed inverted daylight detectors not being protected.
* Fixed spawn eggs not being included in protection.
* Fixed piston blocking not working due to a bug in Spigot.
* Fixed the blocking of certain invalid entity damage events.
* Fixed creeper explosions not being blocked in certain situations with explosion related flags set.
* Fixed "stickiness" with some position-related flags, sometimes resulting in rubber banding in the exit flags.
* Fixed armor stands so that they are treated more like item frames than mobs.
* Fixed blocks (e.g. sugar canes) adjacent to physics blocks (e.g. sand) not updating.
* Fixed a NullPointerException that occurred sometimes when generating the scheduler section of the report function (`/wg report`).
* Fixed the "no XP drops" configuration not functioning following a previous release.
* Fixed changes to region ownership sometimes not triggering a region database save.
* Fixed bucket protection displaying the "deny effect" even if bucket use was not prevented.
* Fixed CommandFilter matching emoticons and other unwanted characters.
* Fixed an exception occurring sometimes during game mode changes.
* Fixed primed TNT being checked twice for TNT flags.

## 6.0 beta 5

MC 1.7.9, 1.7.10 and 1.8 (Spigot only) are supported.

* Added a feature to detect when another plugin (incorrectly) includes WorldGuard's files, causing errors.
* Added a new `exit-override` flag that can be used on an entry=allow region that players can walk into from an entry=deny region.
* Added a new `entry-deny-message` to change the "entry denied" message.
* Added a new `exit-deny-message` to change the "exit denied" message.
* Changed entry/exit blocking to disembark players from their vehicles.
* Changed the PvP flag to no longer apply to self-attack.
* Fixed [#3220](http://youtrack.sk89q.com/issue/WORLDGUARD-3220): Polygon regions sharing points
* Fixed [#3216](http://youtrack.sk89q.com/issue/WORLDGUARD-3216): Polygon regions 'disappearing' or not working correctly
* Fixed [#3315](http://youtrack.sk89q.com/issue/WORLDGUARD-3315): Invincibility flag and mode not blocking knockback from snowballs, etc.
* Fixed [#3314](http://youtrack.sk89q.com/issue/WORLDGUARD-3314): Protection bypassable by getting a skeleton or creeper to target a player
* Fixed [#3326](http://youtrack.sk89q.com/issue/WORLDGUARD-3326): Enderchests cannot be protected with the use flag
* Fixed [#3327](http://youtrack.sk89q.com/issue/WORLDGUARD-3327): Fake players from some Forge mods are not being detected properly
* Fixed [#3328](http://youtrack.sk89q.com/issue/WORLDGUARD-3328): TypeToken error with CraftBukkit 1.7 in `/wg report`
* Fixed [#3086](http://youtrack.sk89q.com/issue/WORLDGUARD-3086): Region entry / exit messages overlap between seprate worlds
* Fixed [#2542](http://youtrack.sk89q.com/issue/WORLDGUARD-2542): Exit deny regions don't handle respawn / logout consistently
* Fixed [#2731](http://youtrack.sk89q.com/issue/WORLDGUARD-2731): Greeting / farewell messages don't understand region inheritance
* Fixed [#3308](http://youtrack.sk89q.com/issue/WORLDGUARD-3308): Enderchest interaction block not functioning with blacklist.txt
* Fixed [#3312](http://youtrack.sk89q.com/issue/WORLDGUARD-3312): PVP flag blocking enderpearl teleport fall damage
* Fixed [#3309](http://youtrack.sk89q.com/issue/WORLDGUARD-3309): Inventory view commands being blocked in protected regions
* Fixed [#3310](http://youtrack.sk89q.com/issue/WORLDGUARD-3310): USE flag is not allowing workbench usage
* Fixed [#3330](http://youtrack.sk89q.com/issue/WORLDGUARD-3330): Build permissions preventing block placement
* API: Added `BukkitUtil.toRegion(Chunk)`

## 6.0 beta 4

MC 1.7.9, 1.7.10 and 1.8 (Spigot only) are supported.

* Added better support for MC 1.8 blocks (regarding the use flag, etc.).
* Added an an experimental "lite" version of WarmRoast to CPU profile your server (try it: `/wg profile -p`)
* Added permissions for individual flag values (i.e. `worldguard.region.flag.flags.use.allow`).
* Added support for slightly older versions of Bukkit 1.7.9.
* Improved `/wg report`'s output (try it: `/wg report -p`)
* Fixed armor stands not being protected properly.
* Fixed lack of wood plate support with the `USE` flag.
* Fixed left click also being blocked when only right click needed to be blocked.
* Fixed items and paintings/item frames not decaying under some versions of Spigot.
* Fixed parent-child inheritance so the child will always inherit the parent.
* Fixed inheritance issues with a flag is set with a region group on a parent region.
* Fixed high CPU usage when Citizens is also installed.
* Fixed `on-acquire` in the blacklist so it also handles item transfers between inventories.
* Fixed the blacklist not supporting color codes.
* Changed the PvP flag so it applies to self-attack like in WG 5 (this change *may* be reverted).

## 6.0 beta 3

MC 1.7.10 and 1.8 (Spigot only) are supported.

* Support both MC 1.7.10 and 1.8.
* Don't apply the region override permission to PvP.
* Fix incorrect detection of hostile and ambient creatures when protecting against PvE.
* Fix the splash potion flag not working with the other flags.
* Fix potion blocking.
* Fix inability to blacklist the enchantment table and the workbench.
* Make PASSTHROUGH=DENY useful on ``__global__``.
* Fix issues (pistons broken, etc.) caused by adding members to ``__global__``.
* Make the EXP_DROPS flag a build-compat flag and set its deny message.
* Give the player back his/her XP bottle if it was blocked.
* Expand the interaction whitelist configuration to "physical" events.
* Catch command prefixes in the command flags.
* Add a RIDE flag for vehicles.
* Check for names in addition to UUIDs with /rg list.
* Whitelist hoppers and let them cross regions regardless of region protection.
* Revert USE flag's functionality to 5.x, add new INTERACT flag.
* Fix spawn egg blocking.
* Fix failed detection of incorrect blacklist block and item names.
* Fix command prefix bypass with the op command blocking configuration.
* Rewrite and update report generation (wg report).
* Add event cancellation debug commands.
* Fix location flags causing NullPointerExceptions.
* Show more details in error log which world the error belongs to.
* Add missing color codes when parsing input.
* Let players use NPCs always.
* Fix [WorldGuard] in log messages stacking on reloads.
* Better handle empty group / player names due to bad region data.
* Fix players being invincible with BUILD=deny.
* Fix certain entities not being damaged in protected regions.
* Improve entity detection in Entities utility class, add Entities.isNonPlayerCreature().
* Add missing SQL table prefix in statement selecting world names.

## 6.0 beta 2

* Added an `-e` parameter to `/rg flag` that sets the flag to an empty value. You can use this to, for example, make it so the `deny-message` flag is empty, meaning that players won't get a message at all.
* Added a visual smoke effect when an action on a block has been denied.
* Added `event-handling.interaction-whitelist`, a list of items or blocks that should never be protected. For example, adding `wooden_door` to the list would make it so that doors could be used by anyone regardless if they have permission or not. You may still protect the blocks via other means.
* Added `event-handling.emit-block-use-at-feet`, a list of items that, when used, will also be considered as the player using that item on the block at the player's feet. The purpose of this setting is, for example, to allow you to (sort of) prevent the use of a mod-added item that does not emit events.
* Added `-s` to silently turn toggle `/stoplag` and `-i` to show the current state of the setting. (Thanks, [stuntguy3000](https://github.com/stuntguy3000)).
* Added support for Bukkit [Material](http://jd.bukkit.org/rb/apidocs/org/bukkit/Material.html) names in the blacklist.
* Fixed [#3167](http://youtrack.sk89q.com/issue/WORLDGUARD-3167): Saplings can be used without permission although it won't place blocks inside the region
* Fixed [#3168](http://youtrack.sk89q.com/issue/WORLDGUARD-3168): Grown saplings that are partially prevented from placing all of its leaves may not remove the sapling
* Fixed [#3169](http://youtrack.sk89q.com/issue/WORLDGUARD-3169): Bonemeal usage falls under the USE flag when it should really fall under building
* Fixed [#3174](http://youtrack.sk89q.com/issue/WORLDGUARD-3174): Boat placement is not properly prevented in a protected region
* Fixed [#3130](http://youtrack.sk89q.com/issue/WORLDGUARD-3130): Polygonal selections allow unlimited claim volumes with /rg claim
* Fixed [#3137](http://youtrack.sk89q.com/issue/WORLDGUARD-3137): Claiming infinite regions is possible due to integer overflow
* Fixed [#3171](http://youtrack.sk89q.com/issue/WORLDGUARD-3171): Can't pickup XP in protected regions without permission
* Fixed [#3170](http://youtrack.sk89q.com/issue/WORLDGUARD-3170): Boats without a driver can be used to break lily pads
* Fixed [#3172](http://youtrack.sk89q.com/issue/WORLDGUARD-3172): Players cannot be damaged by mobs in protected regions unless they have permission
* Fixed [#3152](http://youtrack.sk89q.com/issue/WORLDGUARD-3152): BUILD overrides all other flags, unlike in WG 5
* Fixed [#3154](http://youtrack.sk89q.com/issue/WORLDGUARD-3154): notify-enterflag doesn't work with horses and other vehicles
* Fixed [#3166](http://youtrack.sk89q.com/issue/WORLDGUARD-3166): Trees do not grow naturally within protected regions

## 6.0 beta 1

### UUID support

Regions now fully support the use of UUIDs rather than names for storing the owners and members of a region. Commands still accept names, but they are translated into UUIDs in the background. For users who already have existing region data, names will be automatically converted to UUIDs on the first run of WorldGuard.

It is still possible to specify names rather than a UUID by using the `-n` flag with the region membership commands.

### 'Deny by default'

Previously, a pre-determined list of blocks and entities was used to determine whether an action by the player should be blocked. However, this has been reversed so that every action on a block or entity is denied (in a protected region) unless it has been deemed safe (such as, for example, right clicking with steak). Mods are now supported as a result.

The `USE` flag has been repurposed as a general "interaction" flag and covers every left click or right click of a block or entity. It also covers interaction that falls outside clicking.

### Exhaustive protection

More events are now handled in the protection code. For example,

* It is no longer possible to drop sand or gravel into a protected region from outside.
* It is no longer possible to grief a region by growing a tree into it.
* The entry and exit flags now handle players sitting on vehicles properly.
* Piston-moved blocks cannot be pushed into a different protected region.
* Owners of tamable animals are now considered in protection code.

Even water and lava flow can also be checked, although this requires that it be explicitly enabled in the configuration (flow events are very frequent, so they incur extra CPU cost).

The goal is to provide extremely exhaustive protection.

### Large performance boost for spatial queries

The performance of spatial queries for regions has been substantially increased at least an order of magnitude. Spatial queries are a necessity for the region code to query the list of regions that apply to a location in the world. There should be a negligible difference between 4, 40, and 4000 regions as long as too many regions are not overlapping.

### Background region operations

Region commands are now executed in the background. Notably, saves and loads will not lock up your server while they complete. Additionally, changes to regions are now saved periodically rather than immediately after every change.

### Updated MySQL region support

MySQL support has been completely overhauled. It should now be faster, no longer crash on foreign key index errors, use transactions, and support automatic creation and migration of tables (including support for table prefixes).

### Partial region data saving

For users of MySQL, WorldGuard now only saves changed and deleted regions when saving region data. No longer does WorldGuard have to rewrite the entire region database on each save.

### Improved handling of related flags

Multiple flags that apply to an event are now evaluated together if they are similar. For example, if a player right clicks a bed to sleep in it, both the `USE` and `SLEEP` flag are checked since they are both interaction-related. If one of them is `DENY`, then sleeping is denied (remember, `DENY` overrides `ALLOW`). If one of them is `ALLOW` and the other is not `DENY`, then sleeping is permitted.

Only one "category" of flags needs to evaluate to true to permit an action. `DENY` will not cross categories. For example, if `BUILD` is deny, it will not override `SLEEP`, so if `SLEEP` is set to `ALLOW`, sleeping will be permitted. This is fairly similar to how it worked on WorldGuard 5.

### Flag groups now work properly

While you could always set a group to a flag's value, the functionality was incomplete and did not work most of the time.

Now that groups are supported, you can change a flag so it only applies to a certain group of players. For example, `/rg flag example sleep deny -g members` would deny sleeping for only members of the `example` region.

### Inheritance now works as expected

Previously, a region only inherited flags and other details from its parents if the parent and the child overlapped at the location queried. This is no longer the case and inheritance is now always applied. 

In addition, a new `-g` flag for `/rg define` lets you create a region with no physical area so that you can assign flags and use this region as a "template region."

### Settable deny message

It is now possible to change the message that users get when they are prevented from interacting with blocks or entities. This message is defined as a region flag, so you can set it on the `__global__` region or override it in a specific region. In addition, the tone and color of the default message has been softened, but you are free to change it entirely.

### Blacklist support for data values (and later, variants)

The blacklist was rewritten to support more than just block and item IDs. The immediate result is that you can now use data values, but as those are in the process of being deprecated by Mojang, it also allows support for Minecraft's new block variants in the future.

### Debounced events

Events that tend to reoccur very frequently (such as the ones that occur when you stand on a pressure plate continuously) have been "debounced" to reduce the number of checks that WorldGuard has to perform, reducing the amount of wasted CPU. In addition, the "you don't have permission" message is rate limited so players are not flooded with the message.

### New block place and break flags

A long requested feature was the availability of block place and break flags. These newly added flags work in tandem with the `BUILD` flag but can override `BUILD`. At the moment, it is not possible to allow explicit types of blocks to be placed or broken yet.

### Build permission nodes

A new optional (disabled by default in the configuration) feature is the checking of build permission nodes. For every block, entity, and item, the following permission nodes are checked:

* Block place: `worldguard.build.block.place.<material>`
* Block break: `worldguard.build.block.remove.<material>`
* Block interact: `worldguard.build.block.interact.<material>`
* Entity spawn: `worldguard.build.entity.place.<type>`
* Entity destroy: `worldguard.build.entity.remove.<type>`
* Entity interact: `worldguard.build.entity.interact.<type>`
* Entity damage: `worldguard.build.entity.damage.<type>`
* Item use: `worldguard.build.item.use.<material>`

In addition, the permissions are also checked in the style of `worldguard.build.block.<material>.<action>`, so `worldguard.build.block.<material>.place` would work too.

The list of usable material names comes from the [Material enumeration in Bukkit](http://jd.bukkit.org/rb/apidocs/org/bukkit/Material.html). For example, the permission for placing the bed block would be `worldguard.build.build.place.bed_block`. Be aware that _Material_ contains both item and block names.

For entity names, see [EntityType](http://jd.bukkit.org/rb/apidocs/org/bukkit/entity/EntityType.html).

### Other changes

* Added a setting to permit "fake players" to bypass protection. Fake players are used by some servers and some mods to allow events to be thrown for mod blocks. WorldGuard considers a player to be a fake player if the name starts with `[` and ends with `]`.
* Added a check so players can no longer disembark in a region from their tamed animal if they don't have permission to get back on.
* Added an item pickup flag to go along with the item drop flag.
* Added a new `//running` command was added to show currently running background tasks.
* Added a blacklist `on-dispense` event.
* Added more detailed error messages when parsing the YAML regions file.
* Added the `-Dworldguard.debug.listener=true` option that can be set on your server's command line to see how WorldGuard processes events, although this will result in a lot of log messages.
* Added log messages that occur on player join when a player had auto-god mode or auto-amphibious mode.
* Fixed the heal command so that it now sets a player's health to the maximum rather than 20.
* Fixed `ProtectedCuboidRegion.getPoints()` returning points in wrong order.
* Fixed the obsidian generator disable option to also apply to tripwire.
* Fixed an error with the blocked commands flag.
* Fixed a connection leak with the MySQL region code.
* Fixed `/wg reload` sometimes not applying new changes right away.
* Changed the region removal command so that child regions are no longer removed without warning when removing the parent region.
* Changed the YAML region data save code to first write the data to a temporary file before replacing the final file.
* Changed how region data is handled when region support is disabled: it is no longer loaded.
* Changed sponges to now be disabled by default. However, if you had previously installed WorldGuard, sponges will still be enabled. Sponges are being added in Minecraft and they work differently than in Minecraft Classic (which WorldGuard emulates). In addition, enabling sponge support in WorldGuard incurs extra CPU cost even if no sponges exist in the world.
* Changed how region data is loaded and saved so failures are now more graceful. If the region data fails to load for a world, then the entire world will be protected by default. In addition, periodic attempts will be made to load the region data in the background.
* Changed the `FEED` flag to maximize the player's saturation level if the hunger level is raised.
* Changed the `wg-invincible` and `wg-amphibious` groups feature so that it now must be enabled in the configuration.
* Removed the ability for WorldGuard to upgrade from a version for Minecraft Alpha.

### API changes

* You can use a new `WorldGuardPlugin.createProtectionQuery()` method to easily check build permissions.
* The listener classes were moved. Consider them internal -- if you want to make use of the listener somehow, please put in a ticket in our issue tracker.
* Much of the protection-related handling was previously scattered throughout the code. Now WorldGuard funnels events into an internal event system and then processes protection queries in a more consistent matter. These events are for internal use so it is strongly recommended that you do not use them (they may change without notice drastically). A lot of the protection-related flag handling was moved there.
* A significant portion of classes under the package of `com.sk89q.worldguard.protection` were changed or removed. However, the basic calls (testing the value of a flag, etc.) should still work, but they may be deprecated. Some of the utility `Domain`-related classes were removed.
* The old region database (load/save) API was removed and replaced with a new one. The new one no longer needs to pointless store a collection of regions on itself -- it has `collection = load()` and `save(collection)` methods.
* Previously, there were subclasses of `RegionManager` that implemented different spatial indices. However, now `RegionManager` is a final class and the spatial indices are separate (see the `RegionIndex` interface).
* A hash table was implemented that caches a list of regions within each loaded chunk. This increases performance of spatial queries substantially.
* A cache of recent spatial queries now exists (a cache of `ApplicableRegionSets`), but you need to make use of `RegionQuery` to make use of this cache. However, if you need to do a spatial query to modify region data, then the old method is better (get a `RegionManager`, etc.).
* `ApplicableRegionSet` was converted into an interface. In addition, there are now "virtual" `ApplicableRegionSet` instances that are returned by `RegionQuery` for certain special cases (such as, for example, region support being disabled by the user or region data being unavailable for a world due to an error). A new `isVirtual()` method is available. This is not an issue if you are getting `ApplicableRegionSet` instances from a `RegionManager`.
* `ApplicableRegionSet` now can accept a pre-sorted collection and its use of a `TreeSet` was removed for performance reasons.
* `FileNotFoundException` is now properly handled in the YAML region file class. It creates the new file and continues on.
* Player names are deprecated by Mojang and so they need to be converted into UUIDs. As this will take some time, you should do this conversion in a background task. To check whether a region contains a player, it recommended that `contains(LocalPlayer)` be used because it's possible that the user may be still using names. A new `WorldGuardPlugin.wrapOfflinePlayer()` method was added for this specific purposes.
* All flags now use `RegionGroup.ALL` as the default region group.
* `StateFlag.getDefault()` now returns a `State` rather than a `boolean`.
* The priority R-tree index now uses the R-tree for region intersection queries. Previously, it was only used for single point containment queries.
* Regions now keep a "dirty" flag and changes it whenever data within the region is changed. When WorldGuard decides to periodically save region data, it may only save changed regions. Calling `save()` on the `RegionManager` is no longer needed as periodical saves happen automatically.
* The old region data migration API was removed in favor of region store drivers. There is no longer a need to have an implementation for every possible conversion (i.e. YAML -> MySQL, MySQL -> YAML, etc.).
* `ProtectedRegion.copyFrom()` was added to copy region settings from another region.
* `@Nullable` annotations were added throughout the code. If you use IntelliJ IDEA, you may notice that it now warns you when a return value may be `null`.

## 5.9

* Added a config option to allow explosion flags to only prevent block damage
* Added allow-tamed-spawns setting, on by default. This will stop WorldGuard from culling tamed animals.
* Added config option and flag to prevent soil from drying naturally.
* Added formatting codes, &k/l/m/n/o and &x (reset).
* Added snow-fall-blocks option to config to restrict snow fall to certain blocks
* Allow players to add newlines (\n) via command, not just manually in yml.
* Allow the console to load/save all region managers with one command.
* Changed entity report format slightly.
* Check bypass perms for item-drop flag.
* Check for entities and projectiles removing items from frames too.
* Fix being able to use bonemeal to turn tall grass into double plants.
* Fix number of arguments for migratedb command.
* Fixed addowner/addmember commands adding command artifacts. Specifically,  "-w <world>" will no longer be added when using it.
* Fixed milking cows in protected areas due to changes in Bukkit.
* Fixed ProtectedCuboidRegion::getPoints() returning points in wrong order.
* Fixed snowman trails being treated as snow fall.
* Make /rg list default to own regions if the player doesn't have permission otherwise.
* Protected items in item frames in protected regions.
* Resolved an issue where explosions of type OTHER_EXPLOSION were ignoring the world  configuration settings.
* Send item-use blacklist event when right-clicking entities.
* Transformed the BlockFadeEvent handler into a switch.

## 5.8

* BREAKING CHANGE: This version has been built for versions of Bukkit for MC 1.6.1 and newer. Do not try to use this version of WG on an older Bukkit version.
* Added support for Minecraft 1.6+. Actual support of new 1.6 additions is dependant on Bukkit. Please use a newer build of Bukkit if, for example, you seem to be unable to add "Horses" to your lists of entities.
* Added mobs.disable-snowman-trails to disable the trails left by walking snowmen.
* Added -a flag to /region removeowner and /region removemember to remove all objects.
* Teleporting is now handled by entry/exit flags. If this is causing people to get entirely stuck inside entry/exit deny regions, set the new setting 'use-player-teleports' to 'false' in your configuration.
* Updated formatting for region commands, especially /region info.
* Changed permissions checking for /region commands to be more consistent, checking for region IDs as a sub-node. This means that if some permissions for some region commands no longer work, add '.*' to the end of permission lines for WG in your permission plugin.

## 5.7.5

* Refixed a few issues from 5.7.4.

## 5.7.4

* BREAKING CHANGE: Removed the cyan "**" in front of greeting and farewell messages to allow more customizability. You can re-add them by adding "&c**" to the flag.
* Fixed an issue enderpearling inside exit/entry deny regions.
* Added checks to players teleporting in and out of exit/entry regions. 
* Fix bad matching in blocked and allowed command flags.
* Fixed heal flag bugs when player max health was increased. 
* Add fishing to xp blocker.
* Applied other-explosion flag to entity damage as well.
* Fixed an issue with interaction that should allow blacklisting item use actions with no block involved (eg fishing rods, potions) 
* Fix witherskull/fireball settings not being differentiated in one case 
* Allow cow milking in no-build areas
* Fix infinite durability breaking items instantly.

## 5.7.3

* Fixed the error caused by Bukkit's change of the TNT minecart API.

## 5.7.2

* Updated to MineCraft 1.5.
* Added TNT Minecart support. They are handled with the same flags/settings as TNT.
* Added mining and smelting xp handling to the xp drops flag and setting.
* Fixed a few edge cases for placing items on region borders.
* Legacy: Fixed issue with //stack not respecting item metadata.

## 5.7.1

* Added a vine growth config option and flag.
* Added support for many commands to be used from console. Usage is with -w flag and a world name e.g. /rg addmem <id> <player> -w <worldname> /rg flag <id> <flag> -w <worldname> <flagvalue>
* Fixed bug that blocks placing plants in flower pots.
* Fixed permissions check for /rg tp
* Fixed enderchests not being accessible (they are now part of the USE flag)
* Fixed an issue with slabs being placable in protected regions. This was also fixed in CraftBukkit but has been left in for users running other versions.
* Fixed wither skulls being handled with other fireballs, despite having their own settings.
* Fixed item frames being placable on region borders.
* Fixed a bug that made explosions more ineffective than they should have been.
* Fixed bonemeal usage in protected regions.
* Fixed special minecarts being placable in protected regions.
* Fixed bugs being placable on region borders.
* Fixed wolves bypassing pvp flags.

## 5.7

* Fixed thread sync issue with FlagStateManager.
* Fixed dragon egg region protection not working in certain cases.
* Added beacon and anvil usage protection in regions.
* Add mobs.block-enderdragon-portal-creation to stop enderdragons from making portals on death.
* Fix TNT being lethal in some case when damage was disabled.
* Fix snowballs and enderpearls being able to knock players back in pvp deny regions.
* Add subcommand support for allowed and blocked command flags.
* Add max-players-allowed and max-players-reject-message flags to limit the numbers of players in a region.
* Fix players being able to plant cocoa beans in protected areas.
* Add an enderpearl flag to prevent players from teleporting to and from regions.
* Added mobs.block-other-explosions config setting and other-explosion flag to block explosions caused by uncovered things, such as other plugins. (It doesn't actually have to be a mob)
* Fix mob damage setting and flag not blocking the DoT wither effect.
* Ghast fireball flag will not block blaze fireballs from lighting things on fire.
* Fixed players without region permissions being able to trigger tripwires, use enderchests, use wooden buttons, and put plants in flower pots.
* Added mobs.block-zombie-door-destruction setting to stop zombies from breaking down doors while difficulty is set to hard.

Thank you to thvortex, Dilandau, tophathacker, and Dark_arc for their contributions this release.

## 5.6.5

* Fixed a regression that caused paintings and item frames to no longer be properly protected.

## 5.6.4

* Added mobs.block-above-ground-slimes to block slimes spawning naturally above ground.
* Added security.deop-everyone-on-join to de-op all players when they join.
* Added security.block-in-game-op-command to prevent usage of /op from in-game.
* Fixed a NullPointerException in PlayerInteract involving water potions and potion blocking.
* Fixed an issue with snowballs being blocked in regions.
* Changed dragon eggs to be checked like it is a broken block in regions.
* Moved developer's WGBukkit class to package com.sk89q.worldguard.bukkit.

## 5.6.3

* Added helper WGBukkit class to get references to WorldGuard from other plugins.
* Added physics.vine-like-rope-ladders to make ladders work like vines. As long as the top ladder block is present, ladder blocks will hold even if they have no block behind them, like a rope ladder.
* Fixed NullPointerException in PlayerInteractEvent caused by potion blocking.
* Fixed mobs.block-{item-frame,painting}-destroy not working.

Thank you to def for testing the above fixes and features.

## 5.6.2

* Fixed potion blocking not working well for splash potions.

## 5.6.1

* Added a mycelium-spread region flag.
* Added a dynamics.disable-mycelium-spread configuration setting.
* Added new gameplay.block-potions option. For example: gameplay: block-potions: [invisibility, speed]
* Fixed a bug in the entity explosion event handler that caused a NullPointerException.

Thank you for gabizou for his contribution in this release.

## 5.6

* Added official support for Minecraft 1.4. Support for 1.3.2 (and below) is still provided in this release.
* Added support for new and existing explosion types.
* Added support for blocking withers with the following settings: mobs: block-wither-explosions: false block-wither-block-damage: false block-wither-skull-explosions: false block-wither-skull-block-damage: true
* Added support for blocking item frames (and their use) in protected regions.
* Added item frame destruction region flags (if you wish to disable item frame  destruction completely).
* (Re-)added support for priority R-tree region indexes. This means quicker region lookup, and better support for servers with thousands of regions.
* Improved /region info output for -g group flags.
* Allowed removing of -g group flags with /region flag.
* Allowed /region flag to set group and value at once.
* Deleting a region flag now also deletes group flag.
* Fixed detection of attack by projectiles to be more accurate.
* Fixed support for saving non-string flags in the MySQL region support.
* Fixed spurious comma in "Flags:" /region info output.
* Fixed players who are not in god mode not receiving damage in PVP areas from enderpearls.
* Players will now be told that they are in a no-PvP area if they are in one and attempting to attack someone, or that the player they are attacking is in a no-PvP area, if that is the case instead.
* Fixed an issue where region.getId() was lowercase'd each pass of a for loop in MySQLDatabase.updatePoly2dPoints().
* Fixed mob damage from projectiles not being properly blocked in regions where applicable.
* Changed default region wand to leather (#334).

Thank you to the following individuals for their contribution:
thvortex, Rutr, Glitchfinder, and DarthAndroid.

## 5.5.4

* Minecraft 1.3 support.
* Fixed /stack dupe bugs.
* Added regions.use-creature-spawn-event.
* Update for Async chat issue.
* Add use flag to cakes.
* Report number of plugins loaded in report writer.
* Updated fireblal item blocking.
* Fixed permissions for /region reload.

## 5.5.3

* Added game mode flag.
* Added item drop pickup flag to regions.
* Added support for fireball item blocking (385).
* Added configuration option to allow creature spawns from plugins.
* Added host key check.
* Added a new line split feature to region greetings.
* Prevented commands in the allow commands list from being blocked and vice versa.

## 5.5.2

* Added potion-splash flag to block splash potions
* Add entity-painting-destroy which can be used to prevent entities from destroying paintings (skeletons firing arrows)
* Added send-chat and receive-chat flags
* Fixed having regions in multiple worlds with the same name not working with MySQL
* A cancellable event is now fired if WorldGuard disallows PvP. (for developers!)
* Made /region setparent check worldguard.region.setparent.own/member.<parent> instead of worldguard.region.setparent.own/member.<child>.
* Allowed passing #<index> in place of a region ID, where <index> is the index as displayed by /region list.
* Fixed an error that occured when clearing a non-existant region's parent.
* Added a -s flag to /region teleport, which sends you to the spawn point instead of the teleport point of a region.
* Added region/role-based permissions to /region teleport.
* BREAKING CHANGE: Added a LocationFlag class and made the teleport and spawn flags use it.
* /region info -s now selects the region being queried.
* Added /region teleport <id>.
* Vector flags can now be set to the current location with the value "here" and to a specific position with x,y,z.
* Made /region info show the region you're in if you don't specify an id explicitly.
* Prevent block ignition from lightning strikes if lightning is blocked in region. Fixes #1175
* Correctly check both WEPIF and superperms in WorldGurdPlugin.broadcastNotification()
* Destroy fire blocks when fire-spread is disallowed
* Added a construct flag that can be used to restrict block placing/destroying in zones to certain roles.
* Added FallingSand to list of intensive entities to be removed with /halt-activiy.
* Replaced all usage of CreatureType by EntityType.
* Updated to support Bukkit 1.2-R0.3 and newer
* Made auto-god-mode disabled by default again.
* Added sign protection check disable.
* Cleaned up javadoc, deprecated duplicate methods, other cleanup
* Added a RegionGroupFlag to evey region flag Using the /region flag command with -g, the region group that the region flag applies to can be set.

## 5.5.1

* Fixed attacking with arrows from non-PVP to PVP areas
* Compatibility with Bukkit 1.1-R5
* Removed broken suppress-tick-sync-warnings config option. See bukkit.yml's setings.warn-on-overload option instead.
* Add region and 'can build' information to CommandBook's /whois

Contributions thanks to: NolanSyKinsley

## 5.5

* Removed WorldEdit.jar from the classpath to stop conflicts with Bukkit's plugin loader
* Now using Bukkit's tagged logging
* Updated to new event system
* Flush player flag state cache on world change
* Improved compatibility with versions of CommandBook without GodComponent and updated pom dep for 2.0
* Added MySQL region storage method.
* Made explosions display their animations even when the event was blocked.
* Added fallback support for those who don't have a version of CommandBook with GodComponent
* Added LIGHTNING flag to DefaultFlag's flagsList Fixes #1026
* Removed /god, /heal, /locate, and /stack from WorldGuard to CommandBook. CommandBook is now checked to see whether a player is godded.
* Removed remaining usages of org.bukkit.util.config.Configuration
* Removed some unused configuration options
* Added permission worldguard.region.wand to the region wand
* Fixed ProtectedRegion.compareTo.
* Fixed FlatRegionManager.getApplicableRegions to return parent regions as well.
* Added exp-drops flag to disable experience drops per-region.
* Fixed some warnings.
* Updated dependency version for WorldGuard from 5.0 to 5.1-SNAPSHOT.
* Added support for fireballs shot by players being blocked by the PVP flag.
* Now using dynamic command registration
* Split off ender dragon block damage from creeper block damage.

Contributions thanks to: narthollis, DarkArc, Wolvereness, and skeight

## 5.4

* Fixed configuration generation on Windows
* Fixed intersection calculation for regions
* Fixed timer and feeding delay for food flags
* Fixed region managers not being created properly (This caused errors related to CREATURE_SPAWN)
* Added support for Minecraft 1.0.1 blocks
* Added support for enchantments and tool damage (worldguard.stack.damaged is the node) with /stack
* Made being in the wg-invincible group respect the configuration's auto-invincible setting
* Added per-group region claim limit maximum
* Added worldguard.region.addowner.unclaimed.* permission for non-economy region 'buying'
* Checking allowed/blocked commands now occurs earlier to override more plugins
* Made PVP flag check whether either player is in a pvp deny region for blocking

Contributions thanks to: epuidokas, wizjany, Turtle9598, and DerFlash

## 5.3

* Added new config options and flags to better handle explosions of all types
* Added config options and flags for snow, ice, mushroom spread, grass spread, and leaf decay
* Added min and max health flags to clamp the values for the existing healing flags
* Added hunger flags similar to the existing healing flags
* Added an option to disable the use of the move event. This can boost performance on large servers but will disable the greeting/farewell and entry/exit flags
* Added flag to prevent pistons (including those that are outside the region, but would affect blocks inside the region)
* Added a /wg flushstates [player] command to free players stuck in a region that was exit-deny but no longer exists or the flag was removed
* Added a config option to disable xp orb dropping
* Added config option to disable redstone wire obsidian generators
* Added a vehicle-destroy flag
* Added enderman protection config and flag
* Allowed and blocked command flags will now fall back to the global region
* The worldguard.region.flag.own/member permissions are now worldguard.region.flag.regions.own/member! Please change your permissions accordingly.
* Made //stack respect max stack sizes unless the player has the worldguard.stack.illegitimate permission
* Setting invincibility flag to deny will override god mode unless the player has worldguard.god.override-regions
* The sleep flag will now prevent beds in the nether from exploding if set to deny
* Existing regions can no longer be accidentally overwritten. Use /region redefine to move an existing region.
* The info command will now sort players alphabetically
* Priming TNT by punching it with flint and steal should throw a blacklist event (consistent with old Minecraft versions)
* The /stoplag command will now remove xp orbs
* Permissions system will use per-world permissions if the provider supports it
* Players in vehicles are now subject to entry/exit and greeting/farewell flags
* Fixed interact blacklist event not logging correctly
* Fixed heal flag taking effect half a block northeast of the region
* Fixed a infinite sign dropping bug
* Fixed players being able to douse fire by punching it in regions they couldn't build it
* Fixed players being able to eat protected cake
* Fence gate usage can be blocked with the use flag
* Fixed chests being unprotected when they should be in 1.8
* Fixed weather config settings not applying when a world was loaded
* Fixed explosion handling in certain cases
* Fixed arrows working in non-pvp zones
* Fixed buckets working on the border of protected regions in certain cases
Contributions thanks to:
wizjany, zml2008, imjake9, Droolio, epuidokas,
EduardBaer, TomyLobo, and halvors

## 5.2.2

* Changed configuration saving so empty lists will be added to the configuration files.

## 5.2.1

* Add region bounds to /region info
* Added the ability to add owners/members to the global region for handling guest groups. Now if there's owners or members on the global region, then those  that are not on the list cannot build.
* Fixed some flag algorithm issues.
* API: Fixed DefaultDomain.size() not counting players.
* API: Undeprecated ApplicableRegionSet.allows(StateFlag).

## 5.2

* Gave the ability to use color colors and macros in greeting and farewell messages.
* Added disable-ice-melting, disable-snow-formation, disable-mushroom-spread, disable-snow-melting, disable-ice-formation.
* Added paintings to blacklist support.
* Added default.disable-health-regain.
* Added auto-invincible-permission setting, which lets you use the permission 'worldguard.auto-invincible' to become invincible on join automatically.
* Flames won't appear if you are /god'ed now. 
* Added blocked-cmds region flag to block commands.
* Added allowed-cmds region flag to whitelist commands.
* Added entry region flag to block entry. Use entry-group to determine who this affects.
* Added exit region flag to block exit. Use exit-group to determine who this affects.
* Made negative healing possible via the healing flag.
* Changed the configuration header messages to be more helpful to new users.
* Added a warning about modifying region files by hand.
* Improved the API for plugin developers.

## 5.1

* A lot of changes.

## 4.0-alpha1

* This release is only to fix WorldGuard for Bukkit. Ignore the 4.0 label. Just move along. Nothing to see.

## 3.2.2

* Changed disable-water-damage to disable-drowning-damage.
* Fixed suffocation disable option.
* Fixed fire spread blocking.
* stopfire and allowfire now work in server console, just like in hMod.

## 3.2.1

* Updated for recent Bukkit ItemStack change.

## 3.2

* Added regions.default.pvp to control default PvP setting.
* Updated WorldGuard for the newer builds of Bukkit.
* Fixed creeper explosion blocking.
* Added redstone support to sponges, removed classic water.
* Fixed item durability disable for hoes.


## 3.1.2

* Increased the priority of WorldGuard's event handling.

## 3.1.1

* Fixed creeper and chest flag conflicting.

## 3.1

* Added ability to disable chest protection. New configuration setting (regions.build.chest-access) and new chest flag.
* Possibly fixed projectile blocking in no-PvP zones.
* Added /regionmembership permission that lets people only change the owners and members of regions they own.

## 3.0b2

* Fixed issue where built-in permissions were not (re)loaded.
* Fixed /god not working for others.

## 3.0b1

* Added parent-child relationships to regions.
* Added global build flag.
* Improved flag support.
* Fixed no-PvP zone messages being sent to the wrong person.
* Fixed commands so that they work in a recent version of Bukkit.
* Fixed NullPointerException in explode hook.

## 2.3.1

* Fixed various cast exceptions with the blacklist loggers.
* Added checks for unchecked exceptions when resolving permissions.
* Fixed the item durability disable setting changing the damage value on an empty hand.

## 2.3

* /god now works very well.
* Item (TNT, flint and steel, etc.) blocking should now work.
* The TNT blocking configuration variable should now work.
* Re-added /heal (not sure where that went), added /slay.
* Implemented contact damage disable toggle.
* Made all player damage disabling configuration parameters work.
* Fixed teleport-on-suffocation setting.
* Added /locate <name>, /locate <x> <y> <z>, and /locate that lets you program/change your compass bearing to point to a player's position (not yet live), a position, or back to spawn.
* Fixed CSVDatabase spitting an error if the owners list was empty.

## 2.2

* Now supports GroupUsers.
* Added support for blocking creeper explosions.
* Fixed a bug where a player's groups were not being loaded correctly, causing the blacklist group ignore option to not work.

## 2.1.1

* Fixed fire spread disable preventing flint and steel too.
* Fixed NullPointerException related to the player item event.
* Made setup simpler.

## 2.1

* Improved Bukkit support.

## 2.0beta3

* Fixed NullPointerException inside the ignite hook.

## 2.0beta2

* Fixed a NullPointerException in the damage hook.

## 2.0beta1

* Added area protection. Use /region or /rg to define regions.

## 1.8.1

* Fixed NullPointerException.

## 1.8

* Updated for Minecraft beta.
* Blacklist hooks have CHANGED. The new hooks are work better. on-create has been replaced with on-place (for block placement) and on-use (for item usage, light buckets and lighters). The old on-use is now on-right-click although it currently does nothing.
* Added coordinates to blacklist file logger.
* Added suffocation damage prevention.

## 1.7.1

* Updated for v0.2.8.

## 1.7

* Updated for v0.2.6_02 and hMod b129.
* Added option to prevent water from damaging particular blocks (Redstone wire, Minecart tracks, etc.). See disable-water-damage-blocks.
* /reload WorldGuard should now reload WorldGuard's configuration.
* Added wg-invincible and wg-amphibious groups that let you give certain players invincibility or underwater breathing.
* Added the ability to disable fall, water, lava, and/or fire damage.
* Water/lava bucket blocking should now be more reliable.
* Added /stack (alias /;) commands to stack items in your inventory into piles up to 64 items in size. Even unstackable items like signs can be stacked.
* /god mode should now let you gain health.
* /god mode should now work more reliably.
* Caught an error message for when the configuration file does not exist.

## 1.6

* Added spawn protection that prevents damage when a player spawns.
* Added login protection that prevents damage when a player joins.
* Added /god. /god can be used on others, provided that the /godother permission is set for the command's user.

## 1.5.1

* Removed the 'java.lang.NoSuchMethodError: Item.setDamage(I)V' error generated.

## 1.5

* Removed the block lag fix.
* WorldGuard now conditionally loads features so you can use any up-to-date build of hMod and the unsupported features won't break the entire plugin. When WG loads, check the server console for a report of features that are not supported by your version of hMod.
* Changed some configuration defaults: disable-lava-fire=true simulate-sponge=true item-durability=false

## 1.4.1

* Fixed issue with the block lag fix dropping item stacks of 0 quantity.

## 1.4

* New on-acquire hook. This does both a total inventory check on inventory change and item pick up denial. The item pick up denial is largely thanks to Dinnerbone for fulfilling my request to add a particular hook.
* Item durability can now be toggled with item-durability. Thanks to Dinnerbone for figuring this out while filfilling my request. The default setting is to fix item durability.
* New no-physics-gravel and no-physics-sand options allow you to prevent gravel and sand from falling.
* New allow-portal-anywhere option allows you to create portal blocks anywhere. 

## 1.3

* UPGRADE WARNING: If you are using database logging, you must add a NULL 'comment' VARCHAR field to the table. The line of SQL that you can run for MySQL can be found in blacklist_table.sql.
* Improved plugin compatibility with the block lag fix. For maximum compatibility, you must now list WorldGuard as the first plugin in server.properties and you cannot reload or enable the plugin while the server is up. This is for bad plugins that unnecessarily use the "critical" priority for its hooks like the LWC. Most plugins don't do that though, so this won't apply for most people.
* Fixed signs being blank if they were denied via the blacklist.
* Added on-break event to the blacklist.
* Added comments and messages. Comments are only for your own use and they are printed with the log and notify actions of the blacklist. The message replaces the message that is shown the user and it is optional.
* The blacklist notification messages are now more compact and are light grey instead of light purple.
* The notify, log, and tell actions will now trigger every time for the on-break, on-create, on-drop events rather than wait 3 seconds between events for the same item or block for a player.

## 1.2

* /stopfire and /allowfire disable and enable fire spreading globally.
* Block lag fix slightly improved in accuracy for item drops.
* Sponge updated to remove water when the sponge block is set. Sponge radius can now be controlled using the 'sponge-radius' parameter and the default is now set to simulate Classic.
* Updated for a newer build of "b126," meaning that lava spread control now works well!
* A new summary of the status of some core protections is now printed on start. Disable this with 'summary-on-start'.
* Blacklist system has been overhauled. Check README.txt for changed configuration settings!
* The blacklist's method of preventing notification repeats is now better, instead waiting 3 seconds before notifying again (before it didn't notify again at all unless the user started using another blocked action).
* To give users the ability to receive notifications, the command to give permission to has been changed to /worldguardnotify, although the old one (that was never mentioned anywhere) still works.
* Water and lava buckets are now psuedo-blocked using an unreliable method that risks the stability of your server (no other plugin does it better though). Use it as your own risk.
* Added on item drop and on item use (i.e. chest) events.
* Chests, signs, and furnaces can now be blocked better with the blacklist system.
* The event names in the blacklist configuration have changed but the old event names should still work. The new names should make "more sense."
* A new "ban" action has been added to the blacklist.
* Action messages have been improved, now longer saying "destroyed" for everything.
* Logging to file has been completely changed, allowing you to use the date and time and the player's username in the log filename. It no longer rotates log files based on size, however.
* Logging to database is now supported.
* Tools can now be destroyed on drop to alleviate the durability cheat. You can do this with either with the blacklist or with the 'item-drop-blacklist' configuration option. The configuration option prints more friendly messages than the 'tell' action of blacklists.

## 1.1.2

* Block lag fix: Snow and glass should no longer drop items.
* Block lag fix: Block object now reset after hook call.

## 1.1.1

* Fixed redstone wire dropping wood blocks with the block lag fix.
* The block lag fix now consults other plugins so that ALL protection mechanisms and plugins should work.

## 1.1

* Added block lag fix.

## 1.0

* Initial release.
