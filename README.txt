WorldProtect
Copyright (c) 2010 sk89q <http://www.sk89q.com>
Licensed under the GNU Lesser General Public License v3

Introduction
------------

WorldProtect has a number of features, any of which you can choose to
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
- Simulate the sponge.
- Block the use, desturction and/or placement of some items or block types.
- Notify admins when a certain block type or item is used, destroyed,
    or place.
- Log the use, destruction, and/or placement of some blocks/items.
- Kick for the use, destruction, and/or placement of some blocks.

hMod is required as WorldProtect is a plugin for hMod.

Configuration
-------------

A "worldprotect.properties" will be created the first the time that you load
WorldProtect on your server. You can either restart your server or use
/reloadplugin WorldProtect to reload the configuraton file after editing it.

- classic-water (def. false)
    Toggle use of classic water. Be foreward that your world may be
    flooded if you are not careful. If you have WorldEdit, you can use
    //drain in such an event, although that will drain the entire pool
    as well. This classic-esque water will only spread infinitely over
    ground, but if there are air blocks underneath, the water will not
    spread (preventing waterfalls from becoming tsunamis).

- simulate-sponge (def. false)
    Toggle simulation of the sponge. The sponge only prevents water
    from spreading. It will not take away existing water. Simulation of
    the sponge requires iterating over a 9x9x9 cuboid around the water
    block and the resource impact of this has not been quantified.

- enforce-single-session (def. true)
    Enforce single sessions. If the player is already found to be on
    the server when s/he logs in, the other player will be kicked with
    an informative message. The inventory will have been saved before
    the new session starts.

- blacklist-log-console (def. true)
    For log actions, print to console as well.

- blacklist-log-file (no default)
    File to log to. Log files are automatically rotated based on size, and
    you can use these patterns in the path:
        %t the system temporary directory
        %h the value of the "user.home" system property
        %g the generation number to distinguish rotated logs
        %u a unique number to resolve conflicts
        %% translates to a single percent sign "%"
    There is no way to put the date in the filename or rotate by date.

- blacklist-log-file-limit (def. 5242880)
    Size in bytes before the log file is rotated.

- blacklist-log-file-count (def. 10)
    Number of log files to keep in rotation at maximum.

- block-tnt (def. false)
    Block TNT explosions. TNT will still explode client-side but the blocks
    will be "restored" in a few seconds afterwards.

- block-lighter (def. false)
    Block flint and steel fires. Those with access to the commands
    /uselighter or /lighter can bypass this. /uselighter is not a real
    command; it is only used for permissions.

- block-creepers (def. false)
    Block creeper explosions. They will not explode client-side.

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

Blacklists
----------

Edit the included worldprotect-blacklist.txt file and follow the
instructions inside.
