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

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.event.player.ProcessPlayerEvent;
import com.sk89q.worldguard.bukkit.util.Events;
import com.sk89q.worldguard.bukkit.util.Materials;
import com.sk89q.worldguard.config.ConfigurationManager;
import com.sk89q.worldguard.config.WorldConfiguration;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.GameModeFlag;
import com.sk89q.worldguard.util.Entities;
import com.sk89q.worldguard.util.command.CommandFilter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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
    private static final Pattern opPattern = Pattern.compile("^/(?:minecraft:)?(?:bukkit:)?(?:de)?op(?:\\s.*)?$", Pattern.CASE_INSENSITIVE);
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
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        WorldConfiguration wcfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(localPlayer.getWorld());
        Session session = WorldGuard.getInstance().getPlatform().getSessionManager().getIfPresent(localPlayer);
        if (session != null) {
            GameModeFlag handler = session.getHandler(GameModeFlag.class);
            if (handler != null && wcfg.useRegions && !WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer,
                    localPlayer.getWorld())) {
                GameMode expected = handler.getSetGameMode();
                if (handler.getOriginalGameMode() != null && expected != null && expected != BukkitAdapter.adapt(event.getNewGameMode())) {
                    log.info("Изменение режима игры для игрока " + player.getName() + " было заблокировано в связи с флагом GameMode данного региона");
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        World world = player.getWorld();

        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(localPlayer.getWorld());

        if (cfg.activityHaltToggle) {
            player.sendMessage(ChatColor.YELLOW
                    + "Интенсивная активность сервера была ОСТАНОВЛЕНА.");

            int removed = 0;

            for (Entity entity : world.getEntities()) {
                if (Entities.isIntensiveEntity(BukkitAdapter.adapt(entity))) {
                    entity.remove();
                    removed++;
                }
            }

            if (removed > 10) {
                log.info("Интенсивная активность: " + removed + " энтити (>10) автоматически удалены в мире "
                        + player.getWorld());
            }
        }

        if (wcfg.fireSpreadDisableToggle) {
            player.sendMessage(ChatColor.YELLOW
                    + "Распространение огня в настоящее время глобально отключено для этого мира.");
        }

        Events.fire(new ProcessPlayerEvent(player));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        WorldConfiguration wcfg =
                WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(localPlayer.getWorld());
        if (wcfg.useRegions) {
            RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            ApplicableRegionSet chatFrom = query.getApplicableRegions(localPlayer.getLocation());

            if (!chatFrom.testState(localPlayer, Flags.SEND_CHAT)) {
                String message = chatFrom.queryValue(localPlayer, Flags.DENY_MESSAGE);
                RegionProtectionListener.formatAndSendDenyMessage("chat", localPlayer, message);
                event.setCancelled(true);
                return;
            }

            boolean anyRemoved = false;
            for (Iterator<Player> i = event.getRecipients().iterator(); i.hasNext();) {
                Player rPlayer = i.next();
                LocalPlayer rLocal = plugin.wrapPlayer(rPlayer);
                if (!query.testState(rLocal.getLocation(), rLocal, Flags.RECEIVE_CHAT)) {
                    i.remove();
                    anyRemoved = true;
                }
            }
            if (anyRemoved && event.getRecipients().isEmpty() && wcfg.regionCancelEmptyChatEvents) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();

        String hostKey = cfg.hostKeys.get(player.getUniqueId().toString());
        if (hostKey == null) {
            hostKey = cfg.hostKeys.get(player.getName().toLowerCase());
        }

        if (hostKey != null) {
            String hostname = event.getHostname();
            int colonIndex = hostname.indexOf(':');
            if (colonIndex != -1) {
                hostname = hostname.substring(0, colonIndex);
            }

            if (!hostname.equals(hostKey)
                    && !(cfg.hostKeysAllowFMLClients && hostname.equals(hostKey + "\u0000FML\u0000"))) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                        "Вы не присоединились к действительному ключу хоста!");
                log.warning("Проверка ключа хоста WorldGuard: " +
                        player.getName() + " присоединился с '" + hostname +
                        "', но ожидался '" + hostKey + "'. Кикнут!");
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

        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(BukkitAdapter.adapt(world));

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
        if (event.useItemInHand() == Event.Result.DENY) {
            return;
        }

        Block block = event.getClickedBlock();
        World world = block.getWorld();
        Material type = block.getType();
        Player player = event.getPlayer();
        @Nullable ItemStack item = event.getItem();

        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(BukkitAdapter.adapt(world));

        // Infinite stack removal
        if (Materials.isInventoryBlock(type)
                && wcfg.removeInfiniteStacks
                && !plugin.hasPermission(player, "worldguard.override.infinite-stack")) {
            for (int slot = 0; slot < 40; slot++) {
                ItemStack heldItem = player.getInventory().getItem(slot);
                if (heldItem != null && heldItem.getAmount() < 0) {
                    player.getInventory().setItem(slot, null);
                    player.sendMessage(ChatColor.RED + "Бесконечный стак в слоте #" + slot + " удален.");
                }
            }
        }

        if (wcfg.useRegions) {
            //Block placedIn = block.getRelative(event.getBlockFace());
            ApplicableRegionSet set =
                    WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(block.getLocation()));
            //ApplicableRegionSet placedInSet = plugin.getRegionContainer().createQuery().getApplicableRegions(placedIn.getLocation());
            LocalPlayer localPlayer = plugin.wrapPlayer(player);

            if (item != null && item.getType().getKey().toString().equals(wcfg.regionWand) && plugin.hasPermission(player, "worldguard.region.wand")) {
                if (set.size() > 0) {
                    player.sendMessage(ChatColor.YELLOW + "Можно ли строить? " + (set.testState(localPlayer, Flags.BUILD) ? "Да" : "Нет"));

                    StringBuilder str = new StringBuilder();
                    for (Iterator<ProtectedRegion> it = set.iterator(); it.hasNext();) {
                        str.append(it.next().getId());
                        if (it.hasNext()) {
                            str.append(", ");
                        }
                    }

                    localPlayer.print("Соответствующие регионы: " + str);
                } else {
                    localPlayer.print("WorldGuard: Здесь нет определенных регионов!");
                }

                event.setUseItemInHand(Event.Result.DENY);
            }
        }
    }

    /**
     * Called when a player steps on a pressure plate or tramples crops.
     *
     * @param event Thrown event
     */
    private void handlePhysicalInteract(PlayerInteractEvent event) {
        if (event.useInteractedBlock() == Event.Result.DENY) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock(); //not actually clicked but whatever
        //int type = block.getTypeId();
        World world = player.getWorld();

        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(BukkitAdapter.adapt(world));

        if (block.getType() == Material.FARMLAND && wcfg.disablePlayerCropTrampling) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        LocalPlayer localPlayer = plugin.wrapPlayer(player);

        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(localPlayer.getWorld());

        if (wcfg.useRegions) {
            ApplicableRegionSet set =
                    WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(localPlayer.getLocation());

            com.sk89q.worldedit.util.Location spawn = set.queryValue(localPlayer, Flags.SPAWN_LOC);

            if (spawn != null) {
                event.setRespawnLocation(BukkitAdapter.adapt(spawn));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(BukkitAdapter.adapt(player.getWorld()));

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
        Player player = event.getPlayer();
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(localPlayer.getWorld());

        if (wcfg.useRegions && cfg.usePlayerTeleports) {
            ApplicableRegionSet set =
                    WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(event.getTo()));
            ApplicableRegionSet setFrom =
                    WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(event.getFrom()));

            if (event.getCause() == TeleportCause.ENDER_PEARL) {
                if (!WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld())) {
                    boolean cancel = false;
                    String message = null;
                    if (!setFrom.testState(localPlayer, Flags.ENDERPEARL)) {
                        cancel = true;
                        message = setFrom.queryValue(localPlayer, Flags.EXIT_DENY_MESSAGE);
                    } else if (!set.testState(localPlayer, Flags.ENDERPEARL)) {
                        cancel = true;
                        message = set.queryValue(localPlayer, Flags.ENTRY_DENY_MESSAGE);
                    }
                    if (cancel) {
                        if (message != null) {
                            player.sendMessage(message);
                        }
                        event.setCancelled(true);
                        return;
                    }
                }
            } else if (event.getCause() == TeleportCause.CHORUS_FRUIT) {
                if (!WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld())) {
                    boolean cancel = false;
                    String message = null;
                    if (!setFrom.testState(localPlayer, Flags.CHORUS_TELEPORT)) {
                        cancel = true;
                        message = setFrom.queryValue(localPlayer, Flags.EXIT_DENY_MESSAGE);
                    } else if (!set.testState(localPlayer, Flags.CHORUS_TELEPORT)) {
                        cancel = true;
                        message = set.queryValue(localPlayer, Flags.ENTRY_DENY_MESSAGE);
                    }
                    if (cancel) {
                        if (message != null) {
                            player.sendMessage(message);
                        }
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            if (null != WorldGuard.getInstance().getPlatform().getSessionManager().get(localPlayer)
                    .testMoveTo(localPlayer, BukkitAdapter.adapt(event.getTo()), MoveType.TELEPORT)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(localPlayer.getWorld());

        if (wcfg.useRegions && !WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld())) {
            ApplicableRegionSet set =
                    WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(localPlayer.getLocation());

            Set<String> allowedCommands = set.queryValue(localPlayer, Flags.ALLOWED_CMDS);
            Set<String> blockedCommands = set.queryValue(localPlayer, Flags.BLOCKED_CMDS);
            CommandFilter test = new CommandFilter(allowedCommands, blockedCommands);

            if (!test.apply(event.getMessage())) {
                String message = set.queryValue(localPlayer, Flags.DENY_MESSAGE);
                RegionProtectionListener.formatAndSendDenyMessage("use " + event.getMessage(), localPlayer, message);
                event.setCancelled(true);
                return;
            }
        }

        if (cfg.blockInGameOp) {
            if (opPattern.matcher(event.getMessage()).matches()) {
                player.sendMessage(ChatColor.RED + "/op и / deop можно использовать только в консоли (в соответствии с настройкой WG).");
                event.setCancelled(true);
                return;
            }
        }
    }
}
