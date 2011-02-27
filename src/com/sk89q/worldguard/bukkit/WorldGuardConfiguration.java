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

import com.nijiko.coelho.iConomy.iConomy;
import com.sk89q.bukkit.migration.PermissionsResolverManager;
import com.sk89q.bukkit.migration.PermissionsResolverServerListener;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.TickSyncDelayLoggerFilter;
import com.sk89q.worldguard.blacklist.Blacklist;
import com.sk89q.worldguard.bukkit.commands.CommandHandler.InsufficientPermissionsException;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.dbs.CSVDatabase;
import com.sk89q.worldguard.protection.regionmanager.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Filter;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

/**
 *
 * @author Michael
 */
public class WorldGuardConfiguration {

    private static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");
    private PermissionsResolverServerListener permsListener;
    private PermissionsResolverManager perms;
    private WorldGuardPlugin wg;
    private Map<String, WorldGuardWorldConfiguration> worldConfig;
    private Set<String> invinciblePlayers = new HashSet<String>();
    private Set<String> amphibiousPlayers = new HashSet<String>();
    private boolean suppressTickSyncWarnings;

    private static Pattern groupPattern = Pattern.compile("^[gG]:(.+)$");
    private iConomy iConomy;

    public WorldGuardConfiguration(WorldGuardPlugin wg) {
        this.wg = wg;
        this.worldConfig = new HashMap<String, WorldGuardWorldConfiguration>();
        this.iConomy = null;
    }

    public WorldGuardWorldConfiguration getWorldConfig(String worldName) {
        WorldGuardWorldConfiguration ret = worldConfig.get(worldName);
        if (ret == null) {
            ret = createWorldConfig(worldName);
            worldConfig.put(worldName, ret);
        }

        return ret;
    }

    private WorldGuardWorldConfiguration createWorldConfig(String worldName) {
        File configFile = new File(wg.getDataFolder(), worldName + ".yml");
        File blacklistFile = new File(wg.getDataFolder(), worldName + "_blacklist.txt");

        return new WorldGuardWorldConfiguration(wg, worldName, configFile, blacklistFile);
    }

    public void onEnable() {
        checkOldConfigFiles();
        checkOldCSVDB();

        perms = new PermissionsResolverManager(wg.getConfiguration(), wg.getServer(), "WorldGuard", logger);
        permsListener = new PermissionsResolverServerListener(perms);

        invinciblePlayers.clear();
        amphibiousPlayers.clear();

        try {
            for (Player player : wg.getServer().getOnlinePlayers()) {
                if (inGroup(player, "wg-invincible")) {
                    invinciblePlayers.add(player.getName());
                }

                if (inGroup(player, "wg-amphibious")) {
                    amphibiousPlayers.add(player.getName());
                }
            }
        } catch (NullPointerException e) { // Thrown if loaded too early
        }


        File configFile = new File(wg.getDataFolder(), "global.yml");
        WorldGuardWorldConfiguration.createDefaultConfiguration(configFile, "global.yml");
        Configuration config = new Configuration(configFile);
        config.load();

        suppressTickSyncWarnings = config.getBoolean("suppress-tick-sync-warnings", false);

        if (suppressTickSyncWarnings) {
            Logger.getLogger("Minecraft").setFilter(new TickSyncDelayLoggerFilter());
        } else {
            Filter filter = Logger.getLogger("Minecraft").getFilter();
            if (filter != null && filter instanceof TickSyncDelayLoggerFilter) {
                Logger.getLogger("Minecraft").setFilter(null);
            }
        }

        worldConfig.clear();
        ;
        for (World w : wg.getServer().getWorlds()) {
            String worldName = w.getName();
            worldConfig.put(worldName, createWorldConfig(worldName));
        }

        permsListener.register(wg);
        perms.load();
    }

    private void checkOldConfigFiles() {
        try {

            File oldFile = new File(wg.getDataFolder(), "config.yml");
            if (oldFile.exists()) {
                logger.info("WorldGuard: WARNING: config.yml is outdated, please reapply your configuration in <world>.yml and global.yml");
                logger.info("WorldGuard: WARNING: config.yml renamed to config.yml.old");
                oldFile.renameTo(new File(wg.getDataFolder(), "config.yml.old"));
            }
            oldFile = new File(wg.getDataFolder(), "blacklist.txt");
            if (oldFile.exists()) {
                logger.info("WorldGuard: WARNING: blacklist.txt is outdated, please reapply your configuration in <world>_blacklist.txt");
                logger.info("WorldGuard: WARNING: blacklist.txt renamed to blacklist.txt.old");
                oldFile.renameTo(new File(wg.getDataFolder(), "blacklist.txt.old"));
            }

        } catch (Exception e) {
        }
    }

    private void checkOldCSVDB() {
        try {
            File CSVfile = new File(wg.getDataFolder(), "regions.txt");
            if (CSVfile.exists()) {

                logger.info("WorldGuard: Converting old regions.txt to new format....");

                World w = wg.getServer().getWorlds().get(0);
                RegionManager mgr = wg.getGlobalRegionManager().getRegionManager(w.getName());

                CSVDatabase db = new CSVDatabase(CSVfile);
                db.load();

                for (Map.Entry<String, ProtectedRegion> entry : db.getRegions().entrySet()) {
                    mgr.addRegion(entry.getValue());
                }

                mgr.save();
                CSVfile.renameTo(new File(wg.getDataFolder(), "regions.txt.old"));

                logger.info("WorldGuard: Done.");
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            logger.warning("WorldGuard: Failed to load regions: "
                    + e.getMessage());
        }
    }

    public void onDisable() {
    }

    public boolean canBuild(Player player, int x, int y, int z) {

        if (getWorldConfig(player.getWorld().getName()).useRegions) {
            Vector pt = new Vector(x, y, z);
            LocalPlayer localPlayer = BukkitPlayer.wrapPlayer(this, player);

            if (!hasPermission(player, "region.bypass")) {
                RegionManager mgr = wg.getGlobalRegionManager().getRegionManager(player.getWorld().getName());

                if (!mgr.getApplicableRegions(pt).canBuild(localPlayer)) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    public boolean canBuild(Player player, Vector pt) {

        if (getWorldConfig(player.getWorld().getName()).useRegions) {
            LocalPlayer localPlayer = BukkitPlayer.wrapPlayer(this, player);

            if (!hasPermission(player, "region.bypass")) {
                RegionManager mgr = wg.getGlobalRegionManager().getRegionManager(player.getWorld().getName());

                if (!mgr.getApplicableRegions(pt).canBuild(localPlayer)) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    public boolean inGroup(Player player, String group) {
        try {
            return perms.inGroup(player.getName(), group);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    public String[] getGroups(Player player) {
        try {
            return perms.getGroups(player.getName());
        } catch (Throwable t) {
            t.printStackTrace();
            return new String[0];
        }
    }

    
    public boolean hasPermission(Player player, String perm) {
        try {
            return player.isOp() || perms.hasPermission(player.getName(), "worldguard." + perm);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }


    /**
     * Checks to see if there are sufficient permissions, otherwise an exception
     * is raised in that case.
     *
     * @param player
     * @param permission
     * @throws InsufficientPermissionsException
     */
    public void checkRegionPermission(CommandSender sender, String permission)
            throws InsufficientPermissionsException {
        
        if (!(sender instanceof Player)) {
            return;
        }
        Player player = (Player)sender;
        if (!hasPermission(player, permission)) {
            throw new InsufficientPermissionsException();
        }
    }

    /**
     * Checks to see if there are sufficient permissions, otherwise an exception
     * is raised in that case.
     *
     * @param sender
     * @param permission
     * @throws InsufficientPermissionsException
     */
    public void checkPermission(CommandSender sender, String permission)
            throws InsufficientPermissionsException {
        if (!(sender instanceof Player)) {
            return;
        }
        if (!hasPermission((Player)sender, permission)) {
            throw new InsufficientPermissionsException();
        }
    }

     /**
     * Parse a group/player DefaultDomain specification for areas.
     *
     * @param domain
     * @param split
     * @param startIndex
     */
    public static void addToDomain(DefaultDomain domain,
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
    public static void removeFromDomain(DefaultDomain domain,
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
    public static DefaultDomain parseDomainString(String[] split, int startIndex) {
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


    public boolean isAmphibiousPlayer(String playerName) {

        if (amphibiousPlayers.contains(playerName)) {
            return true;
        }

        return false;
    }

    public boolean isInvinciblePlayer(String playerName) {

        if (invinciblePlayers.contains(playerName)) {
            return true;
        }

        return false;
    }

    public void addAmphibiousPlayer(String playerName) {
        amphibiousPlayers.add(playerName);
    }

    public void addInvinciblePlayer(String playerName) {
        invinciblePlayers.add(playerName);
    }

    public void removeAmphibiousPlayer(String playerName) {
        amphibiousPlayers.remove(playerName);
    }

    public void removeInvinciblePlayer(String playerName) {
        invinciblePlayers.remove(playerName);
    }

    public void forgetPlayerAllBlacklists(LocalPlayer player) {

        for (Map.Entry<String, WorldGuardWorldConfiguration> entry : worldConfig.entrySet()) {
            Blacklist bl = entry.getValue().getBlacklist();
            if (bl != null) {
                bl.forgetPlayer(player);
            }
        }
    }

    public WorldGuardPlugin getWorldGuardPlugin()
    {
        return this.wg;
    }


    public iConomy getiConomy()
    {
        return this.iConomy;
    }

    public void setiConomy(iConomy newVal)
    {
         this.iConomy = newVal;
    }
}
