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

package com.sk89q.worldguard.bukkit;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.Style;
import com.sk89q.worldedit.util.paste.ActorCallbackPaste;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.event.debug.CancelLogging;
import com.sk89q.worldguard.bukkit.event.debug.LoggingBlockBreakEvent;
import com.sk89q.worldguard.bukkit.event.debug.LoggingBlockPlaceEvent;
import com.sk89q.worldguard.bukkit.event.debug.LoggingEntityDamageByEntityEvent;
import com.sk89q.worldguard.bukkit.event.debug.LoggingPlayerInteractEvent;
import com.sk89q.worldguard.bukkit.util.report.CancelReport;
import com.sk89q.worldguard.internal.platform.DebugHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.BlockIterator;

import java.util.logging.Logger;

public class BukkitDebugHandler implements DebugHandler {

    private static final Logger log = Logger.getLogger(BukkitDebugHandler.class.getCanonicalName());
    private static final int MAX_TRACE_DISTANCE = 20;

    private final WorldGuardPlugin plugin;

    BukkitDebugHandler(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Simulate an event and print its report.
     *
     * @param receiver The receiver of the messages
     * @param target The target
     * @param event The event
     * @param stacktraceMode Whether stack traces should be generated and posted
     * @param <T> The type of event
     */
    private <T extends Event & CancelLogging> void testEvent(CommandSender receiver, Player target, T event, boolean stacktraceMode) throws
            CommandPermissionsException {
        boolean isConsole = receiver instanceof ConsoleCommandSender;

        if (!receiver.equals(target)) {
            if (!isConsole) {
                log.info(receiver.getName() + " is simulating an event on " + target.getName());
            }

            target.sendMessage(
                    ChatColor.RED + "(Please ignore any messages that may immediately follow.)");
        }

        Bukkit.getPluginManager().callEvent(event);
        int start = new Exception().getStackTrace().length;
        CancelReport report = new CancelReport(event, event.getCancels(), start);
        report.setDetectingPlugin(!stacktraceMode);
        String result = report.toString();

        if (stacktraceMode) {
            receiver.sendMessage(ChatColor.GRAY + "The report was printed to console.");
            log.info("Event report for " + receiver.getName() + ":\n\n" + result);

            plugin.checkPermission(receiver, "worldguard.debug.pastebin");
            ActorCallbackPaste
                    .pastebin(WorldGuard.getInstance().getSupervisor(), plugin.wrapCommandSender(receiver), result,
                            "Event debugging report: %s.txt", WorldGuard.getInstance().getExceptionConverter());
        } else {
            receiver.sendMessage(result.replaceAll("(?m)^", ChatColor.AQUA.toString()));

            if (result.length() >= 500 && !isConsole) {
                receiver.sendMessage(ChatColor.GRAY + "The report was also printed to console.");
                log.info("Event report for " + receiver.getName() + ":\n\n" + result);
            }
        }
    }

    /**
     * Get the source of the test.
     *
     * @param sender The message sender
     * @param target The provided target
     * @param fromTarget Whether the source should be the target
     * @return The source
     * @throws CommandException Thrown if a condition is not met
     */
    private Player getSource(CommandSender sender, Player target, boolean fromTarget) throws CommandException {
        if (fromTarget) {
            return target;
        } else {
            if (sender instanceof Player) {
                return (Player) sender;
            } else {
                throw new CommandException(
                        "If this command is not to be used in-game, use -t to run the test from the viewpoint of the given player rather than yourself.");
            }
        }
    }

    /**
     * Find the first non-air block in a ray trace.
     *
     * @param sender The sender
     * @param target The target
     * @param fromTarget Whether the trace should originate from the target
     * @return The block found
     * @throws CommandException Throw on an incorrect parameter
     */
    private Block traceBlock(CommandSender sender, Player target, boolean fromTarget) throws CommandException {
        Player source = getSource(sender, target, fromTarget);

        BlockIterator it = new BlockIterator(source);
        int i = 0;
        while (it.hasNext() && i < MAX_TRACE_DISTANCE) {
            Block block = it.next();
            if (block.getType() != Material.AIR) {
                return block;
            }
            i++;
        }

        throw new CommandException("Not currently looking at a block that is close enough.");
    }

    /**
     * Find the first nearby entity in a ray trace.
     *
     * @param sender The sender
     * @param target The target
     * @param fromTarget Whether the trace should originate from the target
     * @return The entity found
     * @throws CommandException Throw on an incorrect parameter
     */
    private Entity traceEntity(CommandSender sender, Player target, boolean fromTarget) throws CommandException {
        Player source = getSource(sender, target, fromTarget);

        BlockIterator it = new BlockIterator(source);
        int i = 0;
        while (it.hasNext() && i < MAX_TRACE_DISTANCE) {
            Block block = it.next();

            // A very in-accurate and slow search
            Entity[] entities = block.getChunk().getEntities();
            for (Entity entity : entities) {
                if (!entity.equals(target) && entity.getLocation().distanceSquared(block.getLocation()) < 10) {
                    return entity;
                }
            }

            i++;
        }

        throw new CommandException("Not currently looking at an entity that is close enough.");
    }

    @Override
    public void testBreak(Actor sender, LocalPlayer target, boolean fromTarget, boolean stackTraceMode) throws CommandException {
        CommandSender bukkitSender = plugin.unwrapActor(sender);
        Player bukkitTarget = BukkitAdapter.adapt(target);

        Block block = traceBlock(bukkitSender, bukkitTarget, fromTarget);
        sender.printRaw(Style.CYAN + "Testing BLOCK BREAK at " + Style.CYAN_DARK + block);
        LoggingBlockBreakEvent event = new LoggingBlockBreakEvent(block, bukkitTarget);
        testEvent(bukkitSender, bukkitTarget, event, stackTraceMode);
    }

    @Override
    public void testPlace(Actor sender, LocalPlayer target, boolean fromTarget, boolean stackTraceMode) throws CommandException {
        CommandSender bukkitSender = plugin.unwrapActor(sender);
        Player bukkitTarget = BukkitAdapter.adapt(target);

        Block block = traceBlock(bukkitSender, bukkitTarget, fromTarget);
        sender.printRaw(Style.CYAN + "Testing BLOCK PLACE at " + Style.CYAN_DARK + block);
        LoggingBlockPlaceEvent event = new LoggingBlockPlaceEvent(block, block.getState(), block.getRelative(BlockFace.DOWN), bukkitTarget.getItemInHand(), bukkitTarget, true);
        testEvent(bukkitSender, bukkitTarget, event, stackTraceMode);
    }

    @Override
    public void testInteract(Actor sender, LocalPlayer target, boolean fromTarget, boolean stackTraceMode) throws CommandException {
        CommandSender bukkitSender = plugin.unwrapActor(sender);
        Player bukkitTarget = BukkitAdapter.adapt(target);

        Block block = traceBlock(bukkitSender, bukkitTarget, fromTarget);
        sender.printRaw(Style.CYAN + "Testing BLOCK INTERACT at " + Style.CYAN_DARK + block);
        LoggingPlayerInteractEvent event = new LoggingPlayerInteractEvent(bukkitTarget, Action.RIGHT_CLICK_BLOCK, bukkitTarget.getItemInHand(), block, BlockFace.SOUTH);
        testEvent(bukkitSender, bukkitTarget, event, stackTraceMode);
    }

    @Override
    public void testDamage(Actor sender, LocalPlayer target, boolean fromTarget, boolean stackTraceMode) throws CommandException {
        CommandSender bukkitSender = plugin.unwrapActor(sender);
        Player bukkitTarget = BukkitAdapter.adapt(target);
        Entity entity = traceEntity(bukkitSender, bukkitTarget, fromTarget);
        sender.printRaw(Style.CYAN + "Testing ENTITY DAMAGE on " + Style.CYAN_DARK + entity);
        LoggingEntityDamageByEntityEvent event = new LoggingEntityDamageByEntityEvent(bukkitTarget, entity, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 1);
        testEvent(bukkitSender, bukkitTarget, event, stackTraceMode);
    }
}
