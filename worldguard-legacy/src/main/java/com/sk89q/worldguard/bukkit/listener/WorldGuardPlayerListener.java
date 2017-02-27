/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.bukkit.listener;

import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.event.player.ProcessPlayerEvent;
import com.sk89q.worldguard.bukkit.util.Events;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.GameModeFlag;
import com.sk89q.worldguard.util.command.CommandFilter;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Handles all events thrown in relation to a player.
 */
public class WorldGuardPlayerListener implements Listener {

    private static final Logger log = Logger.getLogger(WorldGuardPlayerListener.class.getCanonicalName());
    private static final Pattern opPattern = Pattern.compile("^/(?:bukkit:)?op(?:\\s.*)?$", Pattern.CASE_INSENSITIVE);
    private WorldGuardPlugin plugin;

    /**
     * Construct the object;
     *
     * @param plugin
     */
    public WorldGuardPlayerListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register events.
     */
    public void registerEvents() {
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        WorldConfiguration wcfg = plugin.getGlobalStateManager().get(player.getWorld());
        Session session = plugin.getSessionManager().getIfPresent(player);
        if (session != null) {
            GameModeFlag handler = session.getHandler(GameModeFlag.class);
            if (handler != null && wcfg.useRegions && !plugin.getGlobalRegionManager().hasBypass(player, player.getWorld())) {
                GameMode expected = handler.getSetGameMode();
                if (handler.getOriginalGameMode() != null && expected != null && expected != event.getNewGameMode()) {
                    log.info("Смена режима игры " + player.getName() + " была заблокирована в этом регионе");
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (cfg.activityHaltToggle) {
            player.sendMessage(ChatColor.YELLOW
                    + "Интенсивная активность на сервере ОТКЛЮЧЕНА.");

            int removed = 0;

            for (Entity entity : world.getEntities()) {
                if (BukkitUtil.isIntensiveEntity(entity)) {
                    entity.remove();
                    removed++;
                }
            }

            if (removed > 10) {
                log.info("Блокировка активности: " + removed + " (>10) было удалено в мире "
                        + player.getWorld().toString());
            }
        }

        if (wcfg.fireSpreadDisableToggle) {
            player.sendMessage(ChatColor.YELLOW
                    + "Распространение огня было отключено в данном мире.");
        }

        Events.fire(new ProcessPlayerEvent(player));

        plugin.getSessionManager().get(player); // Initializes a session
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        WorldConfiguration wcfg = plugin.getGlobalStateManager().get(player.getWorld());
        if (wcfg.useRegions) {
            if (!plugin.getGlobalRegionManager().allows(DefaultFlag.SEND_CHAT, player.getLocation())) {
                player.sendMessage(ChatColor.RED + "Вы не можете писать в чат из этого региона!");
                event.setCancelled(true);
                return;
            }

            for (Iterator<Player> i = event.getRecipients().iterator(); i.hasNext();) {
                if (!plugin.getGlobalRegionManager().allows(DefaultFlag.RECEIVE_CHAT, i.next().getLocation())) {
                    i.remove();
                }
            }
            if (event.getRecipients().size() == 0) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        ConfigurationManager cfg = plugin.getGlobalStateManager();

        String hostKey = cfg.hostKeys.get(player.getName().toLowerCase());
        if (hostKey != null) {
            String hostname = event.getHostname();
            int colonIndex = hostname.indexOf(':');
            if (colonIndex != -1) {
                hostname = hostname.substring(0, colonIndex);
            }

            if (!hostname.equals(hostKey)
                    && !(cfg.hostKeysAllowFMLClients && hostname.equals(hostKey + "\u0000FML\u0000"))) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                        "Вы вошли с неправильным ключем сервера!");
                log.warning("WorldGuard проводит проверку ключа: " +
                        player.getName() + " присоединился '" + hostname +
                        "' но '" + hostKey + "' ожидалось. Кикнут!");
                return;
            }
        }

        if (cfg.deopOnJoin) {
            player.setOp(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            handleBlockRightClick(event);
        } else if (event.getAction() == Action.PHYSICAL) {
            handlePhysicalInteract(event);
        }

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (wcfg.removeInfiniteStacks
                && !plugin.hasPermission(player, "worldguard.override.infinite-stack")) {
            int slot = player.getInventory().getHeldItemSlot();
            ItemStack heldItem = player.getInventory().getItem(slot);
            if (heldItem != null && heldItem.getAmount() < 0) {
                player.getInventory().setItem(slot, null);
                player.sendMessage(ChatColor.RED + "Бесконечный стак удален.");
            }
        }
    }

    /**
     * Called when a player right clicks a block.
     *
     * @param event Thrown event
     */
    private void handleBlockRightClick(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Block block = event.getClickedBlock();
        World world = block.getWorld();
        int type = block.getTypeId();
        Player player = event.getPlayer();
        @Nullable ItemStack item = event.getItem();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        // Infinite stack removal
        if ((type == BlockID.CHEST
                || type == BlockID.JUKEBOX
                || type == BlockID.DISPENSER
                || type == BlockID.FURNACE
                || type == BlockID.BURNING_FURNACE
                || type == BlockID.BREWING_STAND
                || type == BlockID.ENCHANTMENT_TABLE)
                && wcfg.removeInfiniteStacks
                && !plugin.hasPermission(player, "worldguard.override.infinite-stack")) {
            for (int slot = 0; slot < 40; slot++) {
                ItemStack heldItem = player.getInventory().getItem(slot);
                if (heldItem != null && heldItem.getAmount() < 0) {
                    player.getInventory().setItem(slot, null);
                    player.sendMessage(ChatColor.RED + "Бесконечные стак в слоте #" + slot + " удален.");
                }
            }
        }

        if (wcfg.useRegions) {
            //Block placedIn = block.getRelative(event.getBlockFace());
            ApplicableRegionSet set = plugin.getRegionContainer().createQuery().getApplicableRegions(block.getLocation());
            //ApplicableRegionSet placedInSet = plugin.getRegionContainer().createQuery().getApplicableRegions(placedIn.getLocation());
            LocalPlayer localPlayer = plugin.wrapPlayer(player);

            if (item != null && item.getTypeId() == wcfg.regionWand && plugin.hasPermission(player, "worldguard.region.wand")) {
                if (set.size() > 0) {
                    player.sendMessage(ChatColor.YELLOW + "Вы можете здесь строить? "
                            + (set.canBuild(localPlayer) ? "Да" : "Нет"));

                    StringBuilder str = new StringBuilder();
                    for (Iterator<ProtectedRegion> it = set.iterator(); it.hasNext();) {
                        str.append(it.next().getId());
                        if (it.hasNext()) {
                            str.append(", ");
                        }
                    }

                    player.sendMessage(ChatColor.YELLOW + "Размещенные здесь регионы: " + str.toString());
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Здесь не обнаружено ни одного региона!");
                }

                event.setCancelled(true);
            }
        }
    }

    /**
     * Called when a player steps on a pressure plate or tramples crops.
     *
     * @param event Thrown event
     */
    private void handlePhysicalInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock(); //not actually clicked but whatever
        //int type = block.getTypeId();
        World world = player.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (block.getTypeId() == BlockID.SOIL && wcfg.disablePlayerCropTrampling) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(player.getWorld());

        if (wcfg.useRegions) {
            ApplicableRegionSet set = plugin.getRegionContainer().createQuery().getApplicableRegions(location);

            LocalPlayer localPlayer = plugin.wrapPlayer(player);
            com.sk89q.worldedit.Location spawn = set.getFlag(DefaultFlag.SPAWN_LOC, localPlayer);

            if (spawn != null) {
                event.setRespawnLocation(com.sk89q.worldedit.bukkit.BukkitUtil.toLocation(spawn));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(player.getWorld());

        if (wcfg.removeInfiniteStacks
                && !plugin.hasPermission(player, "worldguard.override.infinite-stack")) {
            int newSlot = event.getNewSlot();
            ItemStack heldItem = player.getInventory().getItem(newSlot);
            if (heldItem != null && heldItem.getAmount() < 0) {
                player.getInventory().setItem(newSlot, null);
                player.sendMessage(ChatColor.RED + "Бесконечный стак удален.");
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        World world = event.getFrom().getWorld();
        Player player = event.getPlayer();
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (wcfg.useRegions) {
            ApplicableRegionSet set = plugin.getRegionContainer().createQuery().getApplicableRegions(event.getTo());
            ApplicableRegionSet setFrom = plugin.getRegionContainer().createQuery().getApplicableRegions(event.getFrom());
            LocalPlayer localPlayer = plugin.wrapPlayer(player);

            if (cfg.usePlayerTeleports) {
                if (null != plugin.getSessionManager().get(player).testMoveTo(player, event.getTo(), MoveType.TELEPORT)) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (event.getCause() == TeleportCause.ENDER_PEARL) {
                if (!plugin.getGlobalRegionManager().hasBypass(localPlayer, world)
                        && !(set.allows(DefaultFlag.ENDERPEARL, localPlayer)
                                && setFrom.allows(DefaultFlag.ENDERPEARL, localPlayer))) {
                    player.sendMessage(ChatColor.DARK_RED + "Вы не можете телепортироваться на данную територию.");
                    event.setCancelled(true);
                    return;
                }
            }
            try {
                if (event.getCause() == TeleportCause.CHORUS_FRUIT) {
                    if (!plugin.getGlobalRegionManager().hasBypass(localPlayer, world)) {
                        boolean allowFrom = setFrom.allows(DefaultFlag.CHORUS_TELEPORT, localPlayer);
                        boolean allowTo = set.allows(DefaultFlag.CHORUS_TELEPORT, localPlayer);
                        if (!allowFrom || !allowTo) {
                            player.sendMessage(ChatColor.DARK_RED + "Вы не можете телепортироваться " +
                                    (!allowFrom ? "отсюда." : "сюда."));
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            } catch (NoSuchFieldError ignored) {}
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.getTo() == null) { // apparently this counts as a cancelled event, implementation specific though
            return;
        }
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getTo().getWorld());
        if (!wcfg.regionNetherPortalProtection) return;
        if (event.getCause() != TeleportCause.NETHER_PORTAL) {
            return;
        }
        if (!event.useTravelAgent()) { // either end travel (even though we checked cause) or another plugin is fucking with us, shouldn't create a portal though
            return;
        }
        TravelAgent pta = event.getPortalTravelAgent();
        if (pta == null) { // possible, but shouldn't create a portal
            return;
        }
        if (pta.findPortal(event.getTo()) != null) {
            return; // portal exists...it shouldn't make a new one
        }
        // HOPEFULLY covered everything the server can throw at us...proceed protection checking
        // a lot of that is implementation specific though

        // hackily estimate the area that could be effected by this event, since the server refuses to tell us
        int radius = pta.getCreationRadius();
        Location min = event.getTo().clone().subtract(radius, radius, radius);
        Location max = event.getTo().clone().add(radius, radius, radius);
        World world = event.getTo().getWorld();

        ProtectedRegion check = new ProtectedCuboidRegion("__portalcheck__", BukkitUtil.toVector(min.getBlock()), BukkitUtil.toVector(max.getBlock()));

        if (wcfg.useRegions && !plugin.getGlobalRegionManager().hasBypass(event.getPlayer(), world)) {
            RegionManager mgr = plugin.getRegionContainer().get(event.getTo().getWorld());
            if (mgr == null) return;
            ApplicableRegionSet set = mgr.getApplicableRegions(check);
            if (!set.testState(plugin.wrapPlayer(event.getPlayer()), DefaultFlag.BUILD)) {
                event.getPlayer().sendMessage(ChatColor.RED + "Место назначения находится в защищенном регионе.");
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        World world = player.getWorld();
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (wcfg.useRegions && !plugin.getGlobalRegionManager().hasBypass(player, world)) {
            ApplicableRegionSet set = plugin.getRegionContainer().createQuery().getApplicableRegions(player.getLocation());

            Set<String> allowedCommands = set.queryValue(localPlayer, DefaultFlag.ALLOWED_CMDS);
            Set<String> blockedCommands = set.queryValue(localPlayer, DefaultFlag.BLOCKED_CMDS);
            CommandFilter test = new CommandFilter(allowedCommands, blockedCommands);

            if (!test.apply(event.getMessage())) {
                player.sendMessage(ChatColor.RED + event.getMessage() + " не разрешена в этом регионе.");
                event.setCancelled(true);
                return;
            }
        }

        if (cfg.blockInGameOp) {
            if (opPattern.matcher(event.getMessage()).matches()) {
                player.sendMessage(ChatColor.RED + "/op можно использовать только в консоли (так установлено в настройках WG).");
                event.setCancelled(true);
                return;
            }
        }
    }
}
