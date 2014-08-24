# Upgrading to WorldGuard 6

Version 6 is a __MAJOR UPGRADE__ to WorldGuard so we recommend:

* Making backups of your worlds
* Making backups of your WorldGuard configuration

(Periodical backups are recommended anyway.)

You can upgrade from any version of WorldGuard 5.

It is __NOT POSSIBLE TO DOWNGRADE__ after upgrading to WorldGuard 6 without loss of data due to the conversion to UUIDs. Do NOT downgrade WorldGuard once you have installed version 6.

If downgrading becomes necessary, please file a ticket on our issue tracker.

If this is a __BETA BUILD__ or __SNAPSHOT BUILD__, then you should re-read this document every new release until after you have upgraded to a final release. Breaking changes may be added until the final release.

You have downloaded version ${project.version}.

## Breaking changes

Be aware of these breaking changes:

* Children regions now inherit from their parent even if there is no overlap.
* Flags are now evaluated together so a `DENY` on `SLEEP` will override a `BUILD` set to `ALLOW`. (The sleep flag only applies to beds.)
* MySQL support now features the ability to migrate tables. If you are using tables without a prefix, then the new migration code should be able to handle your table. However, if you are using a table prefix, you may encounter trouble with migration.
* Plugins that utility WorldGuard's API may not work anymore.
* Protection is now a lot more exhaustive so it is no longer, for example, possible to fling sand into a region.
* In the rare situation that you are user of the "auto-invincibility" and "auto-no-drowning" groups (`wg-invincible` and `wg-amphibious`), you now have to enable these features in the config (`auto-invincible-group: true` and `auto-no-drowning-group: true`). This is because some permission plugins have been causing severe hang ups whenever it is queried for a player's groups, which, in this case, happens to include when the player joins.

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

### Build permission nodes

A new optional (disabled by default in the configuration) feature is the checking of build permission nodes. For every block, entity, and item, the following permission nodes are checked:

* Block place: `worldguard.build.block.place.<material>`
* Block break: `worldguard.build.block.remove.<material>`
* Block interact: `worldguard.build.block.interact.<material>`
* Entity spawn: `worldguard.build.block.place.<type>`
* Entity destroy: `worldguard.build.block.remove.<type>`
* Entity interact: `worldguard.build.block.interact.<type>`
* Entity damage: `worldguard.build.block.damage.<type>`
* Item use: `worldguard.build.item.interact.<material>`

In addition, the permissions are also checked in the style of `worldguard.build.block.<material>.<action>`, so `worldguard.build.block.<material>.place` would work too.

The list of usable material names comes from the [Material enumeration in Bukkit](http://jd.bukkit.org/rb/apidocs/org/bukkit/Material.html). For example, the permission for placing the bed block would be `worldguard.build.build.place.bed_block`. Be aware that _Material_ contains both item and block names.

For entity names, see [EntityType](http://jd.bukkit.org/rb/apidocs/org/bukkit/entity/EntityType.html).

# Other changes

The rest of the changes can be found in the CHANGELOG file.