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
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
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
                log.info(message("debug.handler.log.simulating-event", receiver.getName(), target.getName()));
            }

            target.sendMessage(colorMessage("debug.handler.ignore-following"));
        }

        Bukkit.getPluginManager().callEvent(event);
        int start = new Exception().getStackTrace().length;
        CancelReport report = new CancelReport(event, event.getCancels(), start);
        report.setDetectingPlugin(!stacktraceMode);
        String result = report.toString();

        if (stacktraceMode) {
            receiver.sendMessage(colorMessage("debug.handler.console-only"));
            log.info(message("debug.handler.log.event-report", receiver.getName(), result));

            plugin.checkPermission(receiver, "worldguard.debug.pastebin");
            ActorCallbackPaste.pastebin(WorldGuard.getInstance().getSupervisor(), plugin.wrapCommandSender(receiver),
                    result, "Event debugging report: %s.txt");
        } else {
            receiver.sendMessage(result.replaceAll("(?m)^", ChatColor.AQUA.toString()));

            if (result.length() >= 500 && !isConsole) {
                receiver.sendMessage(colorMessage("debug.handler.console-also"));
                log.info(message("debug.handler.log.event-report", receiver.getName(), result));
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
                throw new CommandException(message("debug.handler.error.console-use-target"));
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

        throw new CommandException(message("debug.handler.error.block-too-far"));
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

        throw new CommandException(message("debug.handler.error.entity-too-far"));
    }

    @Override
    public void testBreak(Actor sender, LocalPlayer target, boolean fromTarget, boolean stackTraceMode) throws CommandException {
        CommandSender bukkitSender = plugin.unwrapActor(sender);
        Player bukkitTarget = BukkitAdapter.adapt(target);

        Block block = traceBlock(bukkitSender, bukkitTarget, fromTarget);
        sender.print(buildTestComponent("debug.handler.test.block-break", block.toString()));
        LoggingBlockBreakEvent event = new LoggingBlockBreakEvent(block, bukkitTarget);
        testEvent(bukkitSender, bukkitTarget, event, stackTraceMode);
    }

    @Override
    public void testPlace(Actor sender, LocalPlayer target, boolean fromTarget, boolean stackTraceMode) throws CommandException {
        CommandSender bukkitSender = plugin.unwrapActor(sender);
        Player bukkitTarget = BukkitAdapter.adapt(target);

        Block block = traceBlock(bukkitSender, bukkitTarget, fromTarget);
        sender.print(buildTestComponent("debug.handler.test.block-place", block.toString()));
        LoggingBlockPlaceEvent event = new LoggingBlockPlaceEvent(block, block.getState(), block.getRelative(BlockFace.DOWN), bukkitTarget.getItemInHand(), bukkitTarget, true);
        testEvent(bukkitSender, bukkitTarget, event, stackTraceMode);
    }

    @Override
    public void testInteract(Actor sender, LocalPlayer target, boolean fromTarget, boolean stackTraceMode) throws CommandException {
        CommandSender bukkitSender = plugin.unwrapActor(sender);
        Player bukkitTarget = BukkitAdapter.adapt(target);

        Block block = traceBlock(bukkitSender, bukkitTarget, fromTarget);
        sender.print(buildTestComponent("debug.handler.test.block-interact", block.toString()));
        LoggingPlayerInteractEvent event = new LoggingPlayerInteractEvent(bukkitTarget, Action.RIGHT_CLICK_BLOCK, bukkitTarget.getItemInHand(), block, BlockFace.SOUTH);
        testEvent(bukkitSender, bukkitTarget, event, stackTraceMode);
    }

    @Override
    public void testDamage(Actor sender, LocalPlayer target, boolean fromTarget, boolean stackTraceMode) throws CommandException {
        CommandSender bukkitSender = plugin.unwrapActor(sender);
        Player bukkitTarget = BukkitAdapter.adapt(target);
        Entity entity = traceEntity(bukkitSender, bukkitTarget, fromTarget);
        sender.print(buildTestComponent("debug.handler.test.entity-damage", entity.toString()));
        LoggingEntityDamageByEntityEvent event = new LoggingEntityDamageByEntityEvent(bukkitTarget, entity, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 1);
        testEvent(bukkitSender, bukkitTarget, event, stackTraceMode);
    }

    private String message(String key, Object... arguments) {
        return WorldGuard.getInstance().getLocalization().format(key, arguments);
    }

    private String colorMessage(String key, Object... arguments) {
        return ChatColor.translateAlternateColorCodes('&', message(key, arguments));
    }

    private TextComponent buildTestComponent(String key, String value) {
        String template = message(key);
        String[] segments = template.split("%s", -1);
        TextComponent.Builder builder = TextComponent.builder("");
        if (segments.length == 1) {
            builder.append(TextComponent.of(template, TextColor.AQUA));
            builder.append(TextComponent.of(value, TextColor.DARK_AQUA));
        } else {
            for (int i = 0; i < segments.length; i++) {
                if (!segments[i].isEmpty()) {
                    builder.append(TextComponent.of(segments[i], TextColor.AQUA));
                }
                if (i < segments.length - 1) {
                    builder.append(TextComponent.of(value, TextColor.DARK_AQUA));
                }
            }
        }
        return builder.build();
    }
}
