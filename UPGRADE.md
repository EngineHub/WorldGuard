# Upgrading to WorldGuard 6

Version 6 is a __MAJOR UPGRADE__ to WorldGuard so we recommend:

* Making backups of your worlds
* Making backups of your WorldGuard configuration

(Periodical backups are recommended anyway.)

You can upgrade from any version of WorldGuard 5.

It is __NOT POSSIBLE TO DOWNGRADE__ after upgrading to WorldGuard 6 without loss of data due to the conversion to UUIDs. Do NOT downgrade WorldGuard once you have installed version 6.

If downgrading becomes necessary, please file a ticket on our issue tracker.

## Breaking changes

Be aware of these breaking changes:

* Children regions now inherit from their parent even if there is no overlap.
* Flags are now evaluated together so a `DENY` on `SLEEP` will override a `BUILD` set to `ALLOW`. (The sleep flag only applies to beds.)
* MySQL support now features the ability to migrate tables. If you are using tables without a prefix, then the new migration code should be able to handle your table. However, if you are using a table prefix, you may encounter trouble with migration.
* Plugins that utility WorldGuard's API may not work anymore.
* Protection is now a lot more exhaustive so it is no longer, for example, possible to fling sand into a region.

## Major features

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

### Improved handling of multiple related-flags

Multiple flags that apply to an event are now evaluated together. For example, if a player right clicks a bed to sleep in it, both the `BUILD` flag, the `SLEEP` flag, and the player's membership level is considered. If one of those flags is set to `DENY`, then the player will be prevented from sleeping. If none of the flags or membership gives the player permission to sleep, it is possible to set `SLEEP` to `ALLOW` and allow players to _only_ sleep.

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

# Other changes

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
* Changed the region removal command so that child regions are no longer removed without warning when removing the parent region.
* Changed the YAML region data save code to first write the data to a temporary file before replacing the final file.
* Changed how region data is handled when region support is disabled: it is no longer loaded.
* Changed sponges to now be disabled by default. However, if you had previously installed WorldGuard, sponges will still be enabled. Sponges are being added in Minecraft and they work differently than in Minecraft Classic (which WorldGuard emulates). In addition, enabling sponge support in WorldGuard incurs extra CPU cost even if no sponges exist in the world.
* Changed how region data is loaded and saved so failures are now more graceful. If the region data fails to load for a world, then the entire world will be protected by default. In addition, periodic attempts will be made to load the region data in the background.
* Changed the `FEED` flag to maximize the player's saturation level if the hunger level is raised.
* Removed the ability for WorldGuard to upgrade from a version for Minecraft Alpha.

# API changes

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