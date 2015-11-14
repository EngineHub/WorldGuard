# Upgrading to WorldGuard 6

This is a **major upgrade**. However, it is relatively well-tested because many people have *accidentally* updated to version 6 when the Spigot team updated WorldGuard for 1.8 but did not update v5.9.

The version that you downloaded supports Minecraft 1.7.10 and 1.8.

**NEW DOCUMENTATION:** There is [new work-in-progress documentation](http://docs.enginehub.org/manual/worldguard/latest/). However, refer to the [older wiki](http://wiki.sk89q.com/wiki/WorldGuard) for missing pages.

## Downgrading to 5.9

When updating to v6, your region data will be updated to use player UUIDs rather than their names. That means that you **cannot downgrade** to WG 5.9 because it does not understand UUIDs and will remove all ownership data. However, you can use [Six2Five](https://github.com/sk89q/Six2Five/releases/tag/release-1.0) to downgrade region data stored as YAML.

Remember to make backups.

If you do not use region protection, then downgrading requires no extra steps.

## What's Changed?

There have been many changes, but the most important (and breaking ones) are listed below.

### Region Protection

The region protection has been optimized and aggressive caching has been added. That means that the impact of region protection when hundreds or thousands of regions exist has been minimized. 

* UUID support was added. On first server start, your region data will be converted to use UUIDs. Names that lack a UUID (i.e. they refer to accounts that don't exist) will remain, but can be removed by re-running the conversion (see the config) after changing the configuration to remove unconverted names. 
* Build protection for regions is now much more complete, and WorldGuard protects against entities and blocks making changes as if they were players. For example, TNT cannot be flung into a protected region and piston machines cannot push into a protected region. Liquid flow (lava and water) can also be checked, although this is disabled by default.
* Setting the `build` flag to `deny` will break pistons and Redstone. When you set the `build` flag to `deny`, you are essentially saying that *no one* can build at all. Now that blocks and entities are considered the same as players, they get blocked in that case. What's the solution? First of all, you probably do **not** want to set the build flag: remember, when you create a region, only members can build in it, so there's no need to change the `build` flag.
* If you want to deny building "in the wilderness," use `/rg flag __global__ passthrough deny`. Unset the `build` flag if you had set it to `deny`. As you may know, when you create a region, protection is automatically turned on (only members can build). If you don't want that, you can set a region's `passthrough` flag to `allow`. In the case of the global region, it *defaults* to `allow`, so you have to set it to `deny` to turn that off.
* Earlier versions of WorldGuard 6 changed the ``use`` flag to be much more encompassing, but this is **no longer the case**. The ``use`` flag now works like it did before in 5.x, only applying to things like doors, pressure plates, and levers. A new ``interact`` flag was added instead that controls all right clicks of blocks and entities.
* By default, the ``use`` flag is now set so only members of a region can use levers and doors within a region. If you want to disable this type of protection in all protected regions, use `/rg flag __global__ use allow`.
* Region groups for flags now work properly. You can set a certain flag to apply to only a certain group (owners, members, nonmembers, nonowners, all). Before, it only worked correctly for some flags. Region groups can be set like this: `/rg flag spawn pvp -g nonmembers deny`.
* In WG 5.9, some flags had a default region group of "non-members." That means that if you did `/rg flag spawn chest-access deny`, only non-members would be unable to open chests. In WG 6, you have to explicitly specify this: `/rg flag spawn chest-access -g nonmembers deny`.
* It is now possible to change the message that users get when they are prevented from interacting with blocks or entities. This message is defined as a region flag, so you can set it on the `__global__` region or override it in a specific region. In addition, the tone and color of the default message has been softened, but you are free to change it entirely.
* The MySQL code was substantially rewritten to be faster and more efficient. Some changes are needed to the table structure, but WorldGuard is now capable of performing those automatically. However, those who use table prefixes *may* run into trouble.

### Blacklist

* If you want to blacklist only water and lava blocks and not buckets, you can no longer apply `on-place` to water or lava blocks because it will also deny the use of buckets. If you wish to deny the use of just the liquid blocks, use `on-use`. This is because WorldGuard now considers the use of a bucket also the placement of a liquid block.

### Miscellaneous

* In the rare situation that you are user of the "auto-invincibility" and "auto-no-drowning" groups (`wg-invincible` and `wg-amphibious`), you now have to enable these features in the config (`auto-invincible-group: true` and `auto-no-drowning-group: true`). This is because some permission plugins have been causing severe hang ups whenever it is queried for a player's groups, which, in this case, happens to include when the player joins.

## Known Incompatibilities

Plugins that utilize WorldGuard's API may not work anymore. They may crash with errors. The API has changed somewhat.

* Spigot:
    * `settings.global-api-cache`, if enabled, will crash WG UUID lookups (fixed in Spigot build #1625)

## Other Changes

The rest of the changes can be found in the CHANGELOG file.

In addition, there is [new work-in-progress documentation](http://docs.enginehub.org/manual/worldguard/latest/) that describes some of the new features.