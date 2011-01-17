// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.bukkit;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldedit.bukkit.WorldEditAPI;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.*;
import com.sk89q.worldguard.blacklist.events.ItemUseBlacklistEvent;
import com.sk89q.worldguard.domains.*;
import com.sk89q.worldguard.protection.*;
import static com.sk89q.worldguard.bukkit.BukkitUtil.*;

/**
 * Handles all events thrown in relation to a Player
 */
public class WorldGuardPlayerListener extends PlayerListener {
    /**
     * Plugin.
     */
    private WorldGuardPlugin plugin;
    /**
     * Group pattern used to specify groups for the region command.
     */
    private static Pattern groupPattern = Pattern.compile("^[gG]:(.+)$");

    private static int CMD_LIST_SIZE = 9;
    
    /**
     * Construct the object;
     * 
     * @param plugin
     */
    public WorldGuardPlayerListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Called when a player joins a server
     *
     * @param event Relevant event details
     */
    @Override
    public void onPlayerJoin(PlayerEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.fireSpreadDisableToggle) {
            player.sendMessage(ChatColor.YELLOW
                    + "Fire spread is currently globally disabled.");
        }

        if (plugin.inGroup(player, "wg-invincible")) {
            plugin.invinciblePlayers.add(player.getName());
        }

        if (plugin.inGroup(player, "wg-amphibious")) {
            plugin.amphibiousPlayers.add(player.getName());
        }
    }

    /**
     * Called when a player leaves a server
     *
     * @param event Relevant event details
     */
    @Override
    public void onPlayerQuit(PlayerEvent event) {
        Player player = event.getPlayer();
        plugin.invinciblePlayers.remove(player.getName());
        plugin.amphibiousPlayers.remove(player.getName());
        if (plugin.blacklist != null) {
            plugin.blacklist.forgetPlayer(plugin.wrapPlayer(player));
        }
    }
    
    /**
     * Called when a player uses an item
     * 
     * @param event Relevant event details
     */
    @Override
    public void onPlayerItem(PlayerItemEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockClicked();
        
        if (plugin.useRegions && !event.isBlock() && event.getBlockClicked() != null) {
            Vector pt = toVector(block.getRelative(event.getBlockFace()));
            LocalPlayer localPlayer = plugin.wrapPlayer(player);
            
            if (!plugin.hasPermission(player, "/regionbypass")
                    && !plugin.regionManager.getApplicableRegions(pt).canBuild(localPlayer)) {
                player.sendMessage(ChatColor.DARK_RED
                        + "You don't have permission for this area.");
                event.setCancelled(true);
                return;
            }
        }
        
        if (block != null && plugin.blacklist != null) {
            if (!plugin.blacklist.check(
                    new ItemUseBlacklistEvent(plugin.wrapPlayer(player), toVector(block),
                            block.getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Called when a player attempts to log in to the server
     *
     * @param event Relevant event details
     */
    @Override
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.enforceOneSession) {
            String name = player.getName();
            
            for (Player pl : plugin.getServer().getOnlinePlayers()) {
                if (pl.getName().equalsIgnoreCase(name)) {
                    pl.kickPlayer("Logged in from another location.");
                }
            }
        }
    }

    /**
     * Called when a player attempts to use a command
     *
     * @param event Relevant event details
     */
    @Override
    public void onPlayerCommand(PlayerChatEvent event) {
        if (handleCommand(event)) {
            event.setCancelled(true);
        }
    }

    /**
     * Called when a player attempts to use a command
     *
     * @param event Relevant event details
     */
    public boolean handleCommand(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String[] split = event.getMessage().split(" ");
        
        if (split[0].equalsIgnoreCase("/stopfire") &&
                plugin.hasPermission(player, "/stopfire")) {
            if (!plugin.fireSpreadDisableToggle) {
                plugin.getServer().broadcastMessage(ChatColor.YELLOW
                        + "Fire spread has been globally disabled by " + player.getName() + ".");
            } else {
                player.sendMessage(ChatColor.YELLOW + "Fire spread was already globally disabled.");
            }
            
            plugin.fireSpreadDisableToggle = true;
        } else if (split[0].equalsIgnoreCase("/allowfire")
                    && plugin.hasPermission(player, "/stopfire")) {
            if (plugin.fireSpreadDisableToggle) {
                plugin.getServer().broadcastMessage(ChatColor.YELLOW
                        + "Fire spread has been globally re-enabled by " + player.getName() + ".");
            } else {
                player.sendMessage(ChatColor.YELLOW + "Fire spread was already globally enabled.");
            }
            
            plugin.fireSpreadDisableToggle = false;
        } else if (split[0].equalsIgnoreCase("/god")
                    && plugin.hasPermission(player, "/god")) {
            // Allow setting other people invincible
            if (split.length > 1) {
                if (!plugin.hasPermission(player, "/godother")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to make others invincible.");
                    return true;
                }

                Player other = matchSinglePlayer(plugin.getServer(), split[1]);
                if (other == null) {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                } else {
                    if (!plugin.invinciblePlayers.contains(other.getName())) {
                        plugin.invinciblePlayers.add(other.getName());
                        player.sendMessage(ChatColor.YELLOW + other.getName() + " is now invincible!");
                        other.sendMessage(ChatColor.YELLOW + player.getName() + " has made you invincible!");
                    } else {
                        plugin.invinciblePlayers.remove(other.getName());
                        player.sendMessage(ChatColor.YELLOW + other.getName() + " is no longer invincible.");
                        other.sendMessage(ChatColor.YELLOW + player.getName() + " has taken away your invincibility.");
                    }
                }
            // Invincibility for one's self
            } else {
                if (!plugin.invinciblePlayers.contains(player.getName())) {
                    plugin.invinciblePlayers.add(player.getName());
                    player.sendMessage(ChatColor.YELLOW + "You are now invincible!");
                } else {
                    plugin.invinciblePlayers.remove(player.getName());
                    player.sendMessage(ChatColor.YELLOW + "You are no longer invincible.");
                }
            }
        } else if ((split[0].equalsIgnoreCase("/stack")
                || split[0].equalsIgnoreCase("/;"))
                    && plugin.hasPermission(player, "/stack")) {
            ItemStack[] items = player.getInventory().getContents();
            int len = items.length;

            int affected = 0;
            
            for (int i = 0; i < len; i++) {
                ItemStack item = items[i];

                // Avoid infinite stacks and stacks with durability
                if (item == null || item.getAmount() <= 0
                        || ItemType.shouldNotStack(item.getTypeId())) {
                    continue;
                }

                // Ignore buckets
                if (item.getTypeId() >= 325 && item.getTypeId() <= 327) {
                    continue;
                }

                if (item.getAmount() < 64) {
                    int needed = 64 - item.getAmount(); // Number of needed items until 64

                    // Find another stack of the same type
                    for (int j = i + 1; j < len; j++) {
                        ItemStack item2 = items[j];

                        // Avoid infinite stacks and stacks with durability
                        if (item2 == null || item2.getAmount() <= 0
                                || ItemType.shouldNotStack(item.getTypeId())) {
                            continue;
                        }

                        // Same type?
                        // Blocks store their color in the damage value
                        if (item2.getTypeId() == item.getTypeId() &&
                                (!ItemType.usesDamageValue(item.getTypeId())
                                        || item.getDamage() == item2.getDamage())) {
                            // This stack won't fit in the parent stack
                            if (item2.getAmount() > needed) {
                                item.setAmount(64);
                                item2.setAmount(item2.getAmount() - needed);
                                break;
                            // This stack will
                            } else {
                                items[j] = null;
                                item.setAmount(item.getAmount() + item2.getAmount());
                                needed = 64 - item.getAmount();
                            }

                            affected++;
                        }
                    }
                }
            }

            if (affected > 0) {
                player.getInventory().setContents(items);
            }

            player.sendMessage(ChatColor.YELLOW + "Items compacted into stacks!");
        } else if (split[0].equalsIgnoreCase("/rg")
                || split[0].equalsIgnoreCase("/region")) {
            if (split.length < 2) {
                player.sendMessage(ChatColor.RED + "/region <define|flag|delete|info|add|remove|list|save|load> ...");
                return true;
            }
            
            String action = split[1];
            String[] args = new String[split.length - 1];
            System.arraycopy(split, 1, args, 0, split.length - 1);
            
            handleRegionCommand(player, action, args);
        } else if (split[0].equalsIgnoreCase("/reload")
                && plugin.hasPermission(player, "/reload")
                && split.length > 1) {
            if (split[1].equalsIgnoreCase("WorldGuard")) {
                LoggerToChatHandler handler = new LoggerToChatHandler(player);
                handler.setLevel(Level.ALL);
                Logger minecraftLogger = Logger.getLogger("Minecraft");
                minecraftLogger.addHandler(handler);

                try {
                    plugin.loadConfiguration();
                    plugin.postReload();
                    player.sendMessage("WorldGuard configuration reloaded.");
                } catch (Throwable t) {
                    player.sendMessage("Error while reloading: "
                            + t.getMessage());
                } finally {
                    minecraftLogger.removeHandler(handler);
                }

                return true;
            }
        } else {
            return false;
        }

        return true;
    }
    
    /**
     * Handles a region command.
     * 
     * @param player
     * @param action
     * @param args
     */
    private void handleRegionCommand(Player player, String action, String[] args) {
        if (!plugin.useRegions) {
            player.sendMessage(ChatColor.RED + "Regions are disabled.");
            return;
        }
        
        if (action.equalsIgnoreCase("define")) {
            if (!canUseRegionCommand(player, "/regiondefine")) {
                player.sendMessage(ChatColor.RED + "You don't have the /regiondefine permission.");
                return;
            }
            
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "/region define <id> [owner1 [owner2 [owners...]]]");
                return;
            }
            
            try {
                String id = args[1].toLowerCase();
                Plugin wePlugin = plugin.getServer().getPluginManager().getPlugin("WorldEdit");
                if (plugin == null) {
                    player.sendMessage(ChatColor.RED + "WorldEdit must be installed and enabled as a plugin.");
                    return;
                }
                
                WorldEditPlugin worldEdit = (WorldEditPlugin)wePlugin;
                WorldEditAPI api = worldEdit.getAPI();
                
                LocalSession session = api.getSession(player);
                Region weRegion = session.getRegion();
                
                BlockVector min = weRegion.getMinimumPoint().toBlockVector();
                BlockVector max = weRegion.getMaximumPoint().toBlockVector();
                
                ProtectedRegion region = new ProtectedCuboidRegion(min, max);
                if (args.length >= 3) {
                    region.setOwners(parseDomainString(args, 2));
                }
                plugin.regionManager.addRegion(id, region);
                plugin.regionLoader.save(plugin.regionManager);
                player.sendMessage(ChatColor.YELLOW + "Region saved as " + id + ".");
            } catch (IncompleteRegionException e) {
                player.sendMessage(ChatColor.RED + "You must first define an area in WorldEdit.");
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Region database failed to save: "
                        + e.getMessage());
            }
        } else if (action.equalsIgnoreCase("claim")) {
            if (!canUseRegionCommand(player, "/regionclaim")) {
                player.sendMessage(ChatColor.RED + "You don't have the /regionclaim permission.");
                return;
            }
            
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "/region claim <id>");
                return;
            }
            
            try {
                String id = args[1].toLowerCase();
                Plugin wePlugin = plugin.getServer().getPluginManager().getPlugin("WorldEdit");
                if (plugin == null) {
                    player.sendMessage(ChatColor.RED + "WorldEdit must be installed and enabled as a plugin.");
                    return;
                }

                ProtectedRegion existing = plugin.regionManager.getRegion(id);
                
                if (existing != null) {
                    if (!existing.getOwners().contains(plugin.wrapPlayer(player))) {
                        player.sendMessage(ChatColor.RED + "You don't own this region.");
                        return;
                    }
                }
                
                WorldEditPlugin worldEdit = (WorldEditPlugin)wePlugin;
                WorldEditAPI api = worldEdit.getAPI();
                
                LocalSession session = api.getSession(player);
                Region weRegion = session.getRegion();
                
                BlockVector min = weRegion.getMinimumPoint().toBlockVector();
                BlockVector max = weRegion.getMaximumPoint().toBlockVector();
                
                ProtectedRegion region = new ProtectedCuboidRegion(min, max);
                
                if (plugin.regionManager.overlapsUnownedRegion(region, plugin.wrapPlayer(player))) {
                    player.sendMessage(ChatColor.RED + "This region overlaps with someone else's region.");
                    return;
                }
                
                region.getOwners().addPlayer(player.getName());
                
                plugin.regionManager.addRegion(id, region);
                plugin.regionLoader.save(plugin.regionManager);
                player.sendMessage(ChatColor.YELLOW + "Region saved as " + id + ".");
            } catch (IncompleteRegionException e) {
                player.sendMessage(ChatColor.RED + "You must first define an area in WorldEdit.");
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Region database failed to save: "
                        + e.getMessage());
            }
        } else if (action.equalsIgnoreCase("flag")) {
            if (!canUseRegionCommand(player, "/regiondefine")) {
                player.sendMessage(ChatColor.RED + "You don't have the /regiondefine permission.");
                return;
            }
            
            if (args.length < 4) {
                player.sendMessage(ChatColor.RED + "/region flag <id> <lighter> <none|allow|deny>");
                player.sendMessage(ChatColor.RED + "Other flags not supported in Bukkit at the moment.");
                return;
            }
            
            try {
                String id = args[1].toLowerCase();
                String flagStr = args[2];
                String stateStr = args[3];
                ProtectedRegion region = plugin.regionManager.getRegion(id);
                
                if (region == null) {
                    player.sendMessage(ChatColor.RED + "Could not find a region by that ID.");
                    return;
                }
                
                AreaFlags.State state = null;
    
                if (stateStr.equalsIgnoreCase("allow")) {
                    state = AreaFlags.State.ALLOW;
                } else if (stateStr.equalsIgnoreCase("deny")) {
                    state = AreaFlags.State.DENY;
                } else if (stateStr.equalsIgnoreCase("none")) {
                    state = AreaFlags.State.NONE;
                } else {
                    player.sendMessage(ChatColor.RED + "Acceptable states: allow, deny, none");
                    return;
                }
                
                AreaFlags flags = region.getFlags();
                
                /*if (flagStr.equalsIgnoreCase("build")) {
                    flags.allowBuild = state;
                } else if (flagStr.equalsIgnoreCase("pvp")) {
                    flags.allowPvP = state;
                } else if (flagStr.equalsIgnoreCase("tnt")) {
                    flags.allowTNT = state;
                } else*/ if (flagStr.equalsIgnoreCase("lighter")) {
                    flags.allowLighter = state;
                } else {
                    player.sendMessage(ChatColor.RED + "Acceptable flags: build, pvp, tnt, lighter");
                    return;
                }
                
                plugin.regionLoader.save(plugin.regionManager);
                player.sendMessage(ChatColor.YELLOW + "Region '" + id + "' updated.");
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Region database failed to save: "
                        + e.getMessage());
            }
        } else if (action.equalsIgnoreCase("info")) {
            if (!canUseRegionCommand(player, "/regioninfo")) {
                player.sendMessage(ChatColor.RED + "You don't have the /regioninfo permission.");
                return;
            }
            
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "/region info <id>");
                return;
            }
    
            String id = args[1].toLowerCase();
            if (!plugin.regionManager.hasRegion(id)) {
                player.sendMessage(ChatColor.RED + "A region with ID '"
                        + id + "' doesn't exist.");
                return;
            }
    
            ProtectedRegion region = plugin.regionManager.getRegion(id);
            AreaFlags flags = region.getFlags();
            DefaultDomain domain = region.getOwners();
            
            player.sendMessage(ChatColor.YELLOW + "Region ID: " + id);
            player.sendMessage(ChatColor.GRAY + "Type: " + region.getClass().getCanonicalName());
            player.sendMessage(ChatColor.BLUE + "Build: " + flags.allowBuild.name());
            player.sendMessage(ChatColor.BLUE + "PvP: " + flags.allowPvP.name());
            player.sendMessage(ChatColor.BLUE + "TNT: " + flags.allowTNT.name());
            player.sendMessage(ChatColor.BLUE + "Lighter: " + flags.allowLighter.name());
            player.sendMessage(ChatColor.LIGHT_PURPLE + "Players: " + domain.toPlayersString());
            player.sendMessage(ChatColor.LIGHT_PURPLE + "Groups: " + domain.toGroupsString());
        } else if (action.equalsIgnoreCase("addowner")) {
            if (!canUseRegionCommand(player, "/regiondefine")
                    && !canUseRegionCommand(player, "/regionclaim")) {
                player.sendMessage(ChatColor.RED + "You don't have the /regiondefine permission.");
                return;
            }
            
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "/region addowner <id>");
                return;
            }
    
            try {
                String id = args[1].toLowerCase();
                if (!plugin.regionManager.hasRegion(id)) {
                    player.sendMessage(ChatColor.RED + "A region with ID '"
                            + id + "' doesn't exist.");
                    return;
                }
                
                ProtectedRegion existing = plugin.regionManager.getRegion(id);
                
                if (!canUseRegionCommand(player, "/regiondefine")
                        && !existing.getOwners().contains(plugin.wrapPlayer(player))) {
                    player.sendMessage(ChatColor.RED + "You don't own this region.");
                    return;
                }
                
                addToDomain(existing.getOwners(), args, 2);
                
                plugin.regionLoader.save(plugin.regionManager);
                player.sendMessage(ChatColor.YELLOW + "Region updated!");
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Region database failed to save: "
                        + e.getMessage());
            }
        } else if (action.equalsIgnoreCase("removeowner")) {
            if (!canUseRegionCommand(player, "/regiondefine")
                    && !canUseRegionCommand(player, "/regionclaim")) {
                player.sendMessage(ChatColor.RED + "You don't have the /regiondefine permission.");
                return;
            }
            
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "/region removeowner <id>");
                return;
            }
    
            try {
                String id = args[1].toLowerCase();
                if (!plugin.regionManager.hasRegion(id)) {
                    player.sendMessage(ChatColor.RED + "A region with ID '"
                            + id + "' doesn't exist.");
                    return;
                }
                
                ProtectedRegion existing = plugin.regionManager.getRegion(id);
                
                if (!canUseRegionCommand(player, "/regiondefine")
                        && !existing.getOwners().contains(plugin.wrapPlayer(player))) {
                    player.sendMessage(ChatColor.RED + "You don't own this region.");
                    return;
                }
                
                removeFromDomain(existing.getOwners(), args, 2);
                
                plugin.regionLoader.save(plugin.regionManager);
                player.sendMessage(ChatColor.YELLOW + "Region updated!");
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Region database failed to save: "
                        + e.getMessage());
            }
        } else if (action.equalsIgnoreCase("list")) {
            if (!canUseRegionCommand(player, "/regiondefine")) {
                player.sendMessage(ChatColor.RED + "You don't have the /regiondefine permission.");
                return;
            }
            
            int page = 0;
            
            if (args.length >= 2) {
                try {
                    page = Math.max(0, Integer.parseInt(args[1]) - 1);
                } catch (NumberFormatException e) {
                    page = 0;
                }
            }
    
            Map<String,ProtectedRegion> regions = plugin.regionManager.getRegions();
            int size = regions.size();
            int pages = (int)Math.ceil(size / (float)CMD_LIST_SIZE);
            
            String[] regionIDList = new String[size];
            int index = 0;
            for (String id : regions.keySet()) {
                regionIDList[index] = id;
                index++;
            }
            Arrays.sort(regionIDList);
            
            
            player.sendMessage(ChatColor.RED + "Regions (page "
                    + (page + 1) + " of " + pages + "):");
            
            if (page < pages) {
                for (int i = page * CMD_LIST_SIZE; i < page * CMD_LIST_SIZE + CMD_LIST_SIZE; i++) {
                    if (i >= size) break;
                    player.sendMessage(ChatColor.YELLOW.toString() + (i + 1) + ". " + regionIDList[i]);
                }
            }
        } else if (action.equalsIgnoreCase("delete")) {
            if (!canUseRegionCommand(player, "/regiondelete")
                    && !canUseRegionCommand(player, "/regionclaim")) {
                player.sendMessage(ChatColor.RED + "You don't have the /regiondelete permission.");
                return;
            }
            
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "/region delete <id>");
                return;
            }
    
            try {
                String id = args[1].toLowerCase();
                if (!plugin.regionManager.hasRegion(id)) {
                    player.sendMessage(ChatColor.RED + "A region with ID '"
                            + id + "' doesn't exist.");
                    return;
                }

                ProtectedRegion existing = plugin.regionManager.getRegion(id);
                
                if (!canUseRegionCommand(player, "/regiondelete")
                        && !existing.getOwners().contains(plugin.wrapPlayer(player))) {
                    player.sendMessage(ChatColor.RED + "You don't own this region.");
                    return;
                }
                
                plugin.regionManager.removeRegion(id);
                plugin.regionLoader.save(plugin.regionManager);
                player.sendMessage(ChatColor.YELLOW + "Region removed!");
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Region database failed to save: "
                        + e.getMessage());
            }
        } else if (action.equalsIgnoreCase("save")) {
            if (!canUseRegionCommand(player, "/regionsave")) {
                player.sendMessage(ChatColor.RED + "You don't have the /regionsave permission.");
                return;
            }
            
            try {
                plugin.regionLoader.save(plugin.regionManager);
                player.sendMessage(ChatColor.YELLOW + "Region database saved to file!");
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Region database failed to save: "
                        + e.getMessage());
            }
        } else if (action.equalsIgnoreCase("load")) {
            if (!canUseRegionCommand(player, "/regionload")) {
                player.sendMessage(ChatColor.RED + "You don't have the /regionload permission.");
                return;
            }
            
            try {
                plugin.regionLoader.load(plugin.regionManager);
                player.sendMessage(ChatColor.YELLOW + "Region database loaded from file!");
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Region database failed to load: "
                        + e.getMessage());
            }
        } else {
            player.sendMessage(ChatColor.RED + "/region <define|claim|flag|delete|info|addowner|removeowner|list|save|load> ...");
        }
    }
    
    /**
     * Checks for the command or /region.
     * 
     * @param player
     * @param cmd
     * @return
     */
    private boolean canUseRegionCommand(Player player, String cmd) {
        return plugin.hasPermission(player, "/region")
                || plugin.hasPermission(player, cmd);
    }
    
    /**
     * Parse a group/player DefaultDomain specification for areas.
     * 
     * @param domain
     * @param split
     * @param startIndex
     */
    private static void addToDomain(DefaultDomain domain,
            String[] split, int startIndex) {        
        for (int i = startIndex; i < split.length; i++) {
            String s = split[i];
            Matcher m = groupPattern.matcher(s);
            if (m.matches()) {
                domain.addGroup(m.group(1));
            } else {
                domain.addPlayer(s);
            }
        }
    }
    
    /**
     * Parse a group/player DefaultDomain specification for areas.
     * 
     * @param domain
     * @param split
     * @param startIndex
     */
    private static void removeFromDomain(DefaultDomain domain,
            String[] split, int startIndex) {        
        for (int i = startIndex; i < split.length; i++) {
            String s = split[i];
            Matcher m = groupPattern.matcher(s);
            if (m.matches()) {
                domain.removeGroup(m.group(1));
            } else {
                domain.removePlayer(s);
            }
        }
    }
    
    /**
     * Parse a group/player DefaultDomain specification for areas.
     * 
     * @param split
     * @param startIndex
     * @return
     */
    private static DefaultDomain parseDomainString(String[] split, int startIndex) {
        DefaultDomain domain = new DefaultDomain();
        
        for (int i = startIndex; i < split.length; i++) {
            String s = split[i];
            Matcher m = groupPattern.matcher(s);
            if (m.matches()) {
                domain.addGroup(m.group(1));
            } else {
                domain.addPlayer(s);
            }
        }
        
        return domain;
    }
}
