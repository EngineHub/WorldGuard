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

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldguard.protection.AreaFlags;
import static com.sk89q.worldguard.bukkit.BukkitUtil.*;


public class WorldGuardEntityListener extends EntityListener
{
	/**
	 * Plugin.
	 */
	private WorldGuardPlugin plugin;

	/**
	 * Construct the object;
	 *
	 * @param plugin
	 */
	public WorldGuardEntityListener(WorldGuardPlugin plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public void onEntityDamage(EntityDamageEvent event)
	{
		Entity defender = event.getEntity();
		DamageCause type = event.getCause();

		if (defender instanceof Player)
		{
			Player player = (Player)defender;

			if (plugin.invinciblePlayers.contains(player.getName()))
			{
				event.setCancelled(true);
				return;
			}

			if (plugin.disableLavaDamage && type == DamageCause.LAVA)
			{
				event.setCancelled(true);
				return;
			}

			if (plugin.disableContactDamage && type == DamageCause.CONTACT)
			{
				event.setCancelled(true);
				return;
			}

			if (type == DamageCause.ENTITY_ATTACK)
			{
				Entity attacker = ((EntityDamageByEntityEvent)event).getDamager();

				if (plugin.invinciblePlayers.contains(player.getName()))
				{
					event.setCancelled(true);
					return;
				}

				if (attacker != null && attacker instanceof Player)
				{
					if (plugin.useRegions)
					{
						Vector pt = toVector(defender.getLocation());

						if (!plugin.regionManager.getApplicableRegions(pt).allowsFlag(AreaFlags.FLAG_PVP))
						{
							((Player)attacker).sendMessage(ChatColor.DARK_RED + "You are in a no-PvP area.");
							event.setCancelled(true);
							return;
						}
					}
				}

				if (attacker != null && attacker instanceof Monster)
				{
					if (attacker instanceof Creeper && plugin.blockCreeperExplosions)
					{
						event.setCancelled(true);
						return;
					}

					if (plugin.useRegions)
					{
						Vector pt = toVector(defender.getLocation());

						if (!plugin.regionManager.getApplicableRegions(pt).allowsFlag(AreaFlags.FLAG_MOB_DAMAGE))
						{
							event.setCancelled(true);
							return;
						}

						if (attacker instanceof Creeper)
						{
							if (!plugin.regionManager.getApplicableRegions(pt).allowsFlag(AreaFlags.FLAG_CREEPER_EXPLOSION))
							{
								event.setCancelled(true);
								return;
							}
						}
					}
				}
			}

			if (plugin.disableFallDamage && type == DamageCause.FALL)
			{
				event.setCancelled(true);
				return;
			}

			if (plugin.disableFireDamage && (type == DamageCause.FIRE
											 || type == DamageCause.FIRE_TICK))
			{
				event.setCancelled(true);
				return;
			}

			if (plugin.disableDrowningDamage && type == DamageCause.DROWNING)
			{
				event.setCancelled(true);
				return;
			}

			if (plugin.teleportOnSuffocation && type == DamageCause.SUFFOCATION)
			{
				findFreePosition(player);
				event.setCancelled(true);
				return;
			}

			if (plugin.disableSuffocationDamage && type == DamageCause.SUFFOCATION)
			{
				event.setCancelled(true);
				return;
			}

			if (type == DamageCause.DROWNING
				&& plugin.amphibiousPlayers.contains(player.getName()))
			{
				event.setCancelled(true);
				return;
			}
		}
	}

	@Override
	public void onEntityExplode(EntityExplodeEvent event)
	{
		if (event.getEntity() instanceof LivingEntity)
		{
			if (plugin.blockCreeperBlockDamage)
			{
				event.setCancelled(true);
				return;
			}

			if (plugin.blockCreeperExplosions)
			{
				event.setCancelled(true);
				return;
			}

			if (plugin.useRegions)
			{
				Vector pt = toVector(event.getEntity().getLocation());

				if (!plugin.regionManager.getApplicableRegions(pt).allowsFlag(AreaFlags.FLAG_CREEPER_EXPLOSION))
				{
					event.setCancelled(true);
					return;
				}
			}
		}
		else
		{ // Shall assume that this is TNT
			if (plugin.blockTNT)
			{
				event.setCancelled(true);
				return;
			}

			if (plugin.useRegions && event.getEntity() != null)
			{
				Vector pt = toVector(event.getEntity().getLocation());

				if (!plugin.regionManager.getApplicableRegions(pt).allowsFlag(AreaFlags.FLAG_TNT))
				{
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	/**
	 * Find a position for the player to stand that is not inside a block.
	 * Blocks above the player will be iteratively tested until there is
	 * a series of two free blocks. The player will be teleported to
	 * that free position.
	 *
	 * @param player
	 */
	public void findFreePosition(Player player)
	{
		Location loc = player.getLocation();
		int x = loc.getBlockX();
		int y = Math.max(0, loc.getBlockY());
		int origY = y;
		int z = loc.getBlockZ();
		World world = player.getWorld();

		byte free = 0;

		while (y <= 129)
		{
			if (BlockType.canPassThrough(world.getBlockTypeIdAt(x, y, z)))
			{
				free++;
			}
			else
			{
				free = 0;
			}

			if (free == 2)
			{
				if (y - 1 != origY)
				{
					loc.setX(x + 0.5);
					loc.setY(y);
					loc.setZ(z + 0.5);
					player.teleportTo(loc);
				}

				return;
			}

			y++;
		}
	}
}
