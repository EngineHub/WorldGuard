WorldGuard
Copyright (c) 2010 sk89q <http://www.sk89q.com>
Licensed under the GNU Lesser General Public License v3

Introduction
------------

WorldGuard has a number of features, any of which you can choose to
use or not:

- Enforce only one session for a player on a server (can't login twice).
- Block creeper explosions.
- Block TNT explosions.
- Block lighters from setting fires.
- Block all fires.
- Allow fire but prevent it from burning certain blocks.
- Prevent lava from starting fires.
- Restrict lava spreading to only some block types.
- Simulate classic-esque water by letting water infinitely expand in
    area (only if there is a block underneath).
- Simulate the function of the sponge from Minecraft Classic.
- Notify admins, block, log, kick or ban for the use of certain block types
    or items.
- Temporarily stop fire globally with some commands.
- Fix the block lag.
- Destroy tools on drop to alleviate the durability cheat.
- OR just fix the durability bug.
- Prevent gravel and sand from falling.
- Allow portal blocks to be placed anywhere.

hMod is required as WorldGuard is a plugin for hMod.

Configuration
-------------

A "worldguard.properties" will be created the first the time that you load
WorldGuard on your server. You can either restart your server or use
/reloadplugin WorldGuard to reload the configuraton file after editing it.

- item-durability (def. true)
    Enables item durability.

- classic-water (def. false)
    Toggle use of classic water. Be foreward that your world may be
    flooded if you are not careful. If you have WorldEdit, you can use
    //drain in such an event, although that will drain the entire pool
    as well. This classic-esque water will only spread infinitely over
    ground, but if there are air blocks underneath, the water will not
    spread (preventing waterfalls from becoming tsunamis).

- simulate-sponge (def. false)
    Toggle simulation of the sponge from Classic. It blocks water from
    flowing near the sponge.

- sponge-radius (def. 3)
    The radius of the sponge's effect. The default is that of Classic,
    creating a 5x5x5 cuboid free of water. Increasing the number will
    increase server load exponentially, although 3-5 has fairly low impact.

- no-physics-gravel (def. false)
    Prevents gravel from falling due to gravity.

- no-physics-sand (def. false)
    Prevents sand from falling due to gravity.

- allow-portal-anywhere (def. false)
    Allows you to place portal blocks anywhere.

- enforce-single-session (def. true)
    Enforce single sessions. If the player is already found to be on
    the server when s/he logs in, the other player will be kicked with
    an informative message. The inventory will have been saved before
    the new session starts.
    
- block-tnt (def. false)
    Block TNT explosions. TNT will still explode client-side but the blocks
    will be "restored" in a few seconds afterwards.
    
- block-lighter (def. false)
    Block flint and steel fires. Those with access to the commands
    /uselighter or /lighter can bypass this. /uselighter is not a real
    command; it is only used for permissions.

- block-creepers (def. false)
    Block creeper explosions from destroying terrain (and possibly from
    causing damage).

- disable-lava-fire (def. false)
    Prevent lava from starting fires.

- allowed-lava-spread-blocks (no default)
    List of block names/IDs to allow lava to spread to. Leave blank
    to disable this feature.

- disable-all-fire-spread (def. false)
    Disable all fire from spreading.

- disallowed-fire-spread-blocks (no default)
    List of block names/IDs to prevent fire from spreading to. Leave blank
    to disable this feature. Enabling disable-all-fire-spread will
    override this function.

- item-drop-blacklist (no default)
    List of block names/IDs to destroy on drop. This can alleviate the
    durability cheat that allows you to drop your tools to fix their
    durability. You can also enforce this with the blacklist but
    this is an easier way to do it (this way also prints a more
    friendly message than if you used the 'tell' action of the blacklist).

- block-lag-fix (def. false)
    Attempts to fix block lag. For maximum compatibility with other plugins,
    you must list WorldGuard as the first plugin in server.properties and
    you cannot reload or enable WorldGuard while the server is up.
    This is for bad plugins that unnecessarily use the "critical" priority
    for its hooks like the LWC. Most plugins, the popular protection ones
    included, don't do that though so this won't apply for most people.

- log-console (def. true)
    For blacklist log actions, print to console.

- log-file (def. false)
    For blacklist log actions, log to file.
    
- log-file-path (def. "worldguard/logs/%Y-%m-%d.log")
    Log blacklist events to file. You can use these patterns in the path:
        %Y the year (YYYY)
        %m the month (MM)
        %d the day (DD)
        %W the week of the year (00-52)
        %H 24-hour time (HH)
        %h 12-hour time (HH)
        %i the minute (mm)
        %s the second (ss)
        %u the user's name
        %% translates to a single percent sign "%"
    The files are not automatically rotated if they get large so you should
    perhaps put a week in the filename at least.
    
    Escape backslashes with another backslash, like so:
    C:\path\to\log.txt -> C:\\path\\to\\log.txt

- log-file-open-files (def. 10)
    The number of log files to keep open at once. Unless you use dynamic
    patterns in the path, the value of this variable won't matter. However,
    if, for example, you use a player's username in the filename and you
    have many users who cause logged events to occur, files would have
    to be opened for each user and this parameter would matter.

- log-database (def. false)
    Log blacklist events to database.

- log-database-dsn (def. "jdbc:mysql://localhost:3306/minecraft")
    Connection string. A string to use for MySQL would be:
    jdbc:mysql://localhost:3306/minecraft
    The "minecraft" part at the end is the database name.
    
    Escape colons with a backslash, like so:
    jdbc:mysql://localhost:3306/minecraft
    ->
    jdbc\:mysql\://localhost\:3306/minecraft

- log-database-user (def. "root")
    Database username.

- log-database-pass (def. "")
    Database password.

- log-database-table (def. "blacklist_events")
    Database table to use.

Commands
--------

- /stopfire and /allowfire
    Disables and enables fire spread globally. Both commands require the
    permission to use only "/stopfire. Re-enabling fire spread with this
    command will not override other fire spread control features of
    WorldGuard. Note that disabling fire does not disable fire damage
    but fire at least won't spread.

Server Commands
---------------

- fire-stop and fire-allow
    Disables and enables fire spread globally. Re-enabling fire spread
    with this command will not override other fire spread control features
    of WorldGuard. Note that disabling fire does not disable fire damage
    but fire at least won't spread.

Blacklists
----------

Blacklists let you control the use of blocks and items. You can have
certain rules apply to only some groups.

Edit the included worldguard-blacklist.txt file and follow the
instructions inside. The file comes with the download for WorldGuard.

Thanks
------

While I would like to thank everyone for their support, I would like to
say thanks to the following individuals for their direct
contributions to WorldGuard:

- Sturmeh, for contributing the original durability workaround
- Meaglin, for changing the on flow hook at my request
- Dinnerbone, for implementing the on item pick up hook at my request,
    and also for figuring out the durability bug in the process
