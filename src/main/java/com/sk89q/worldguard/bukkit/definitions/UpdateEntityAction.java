// $Id$
/*
 * This file is a part of WorldGuard.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.bukkit.definitions;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.sk89q.rulelists.Action;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldguard.bukkit.BukkitContext;
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.resolvers.EntityResolver;

public class UpdateEntityAction implements Action<BukkitContext> {

    private final WorldGuardPlugin wg;
    private final EntityResolver entityResolver;

    private Boolean explode;
    private Float fallDistance;
    private Integer fireTicks;

    private Integer health;
    private Integer damage;
    private Integer remainingAir;

    private Integer foodLevel;
    private Float exhaustionLevel;
    private Float saturationLevel;
    private Float walkSpeed;
    private Boolean allowFlight;
    private Boolean flying;
    private Float flySpeed;
    private Boolean sneaking;
    private Boolean sprinting;
    private Boolean invincible;
    private Boolean openWorkbench;
    private String performCommand;

    private Boolean teleportSafely;

    public UpdateEntityAction(WorldGuardPlugin wg, EntityResolver entityResolver) {
        this.wg = wg;
        this.entityResolver = entityResolver;
    }

    public Boolean getExplode() {
        return explode;
    }

    public void setExplode(Boolean explode) {
        this.explode = explode;
    }

    public Float getFallDistance() {
        return fallDistance;
    }

    public void setFallDistance(Float fallDistance) {
        this.fallDistance = fallDistance;
    }

    public Integer getFireTicks() {
        return fireTicks;
    }

    public void setFireTicks(Integer fireTicks) {
        this.fireTicks = fireTicks;
    }

    public Integer getHealth() {
        return health;
    }

    public void setHealth(Integer health) {
        this.health = health;
    }

    public Integer getDamage() {
        return damage;
    }

    public void setDamage(Integer damage) {
        this.damage = damage;
    }

    public Integer getRemainingAir() {
        return remainingAir;
    }

    public void setRemainingAir(Integer remainingAir) {
        this.remainingAir = remainingAir;
    }

    public Integer getFoodLevel() {
        return foodLevel;
    }

    public void setFoodLevel(Integer foodLevel) {
        this.foodLevel = foodLevel;
    }

    public Float getExhaustionLevel() {
        return exhaustionLevel;
    }

    public void setExhaustionLevel(Float exhaustionLevel) {
        this.exhaustionLevel = exhaustionLevel;
    }

    public Float getSaturationLevel() {
        return saturationLevel;
    }

    public void setSaturationLevel(Float saturationLevel) {
        this.saturationLevel = saturationLevel;
    }

    public Float getWalkSpeed() {
        return walkSpeed;
    }

    public void setWalkSpeed(Float walkSpeed) {
        this.walkSpeed = walkSpeed;
    }

    public Boolean getAllowFlight() {
        return allowFlight;
    }

    public void setAllowFlight(Boolean allowFlight) {
        this.allowFlight = allowFlight;
    }

    public Boolean getFlying() {
        return flying;
    }

    public void setFlying(Boolean flying) {
        this.flying = flying;
    }

    public Float getFlySpeed() {
        return flySpeed;
    }

    public void setFlySpeed(Float flySpeed) {
        this.flySpeed = flySpeed;
    }

    public Boolean getSneaking() {
        return sneaking;
    }

    public void setSneaking(Boolean sneaking) {
        this.sneaking = sneaking;
    }

    public Boolean getSprinting() {
        return sprinting;
    }

    public void setSprinting(Boolean sprinting) {
        this.sprinting = sprinting;
    }

    public Boolean getInvincible() {
        return invincible;
    }

    public void setInvincible(Boolean invincible) {
        this.invincible = invincible;
    }

    public Boolean getOpenWorkbench() {
        return openWorkbench;
    }

    public void setOpenWorkbench(Boolean openWorkbench) {
        this.openWorkbench = openWorkbench;
    }

    public String getPerformCommand() {
        return performCommand;
    }

    public void setPerformCommand(String performCommand) {
        this.performCommand = performCommand;
    }

    public Boolean getTeleportSafely() {
        return teleportSafely;
    }

    public void setTeleportSafely(Boolean teleportSafely) {
        this.teleportSafely = teleportSafely;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void apply(BukkitContext context) {
        Entity entity = entityResolver.resolve(context);

        if (explode != null)
            entity.getWorld().createExplosion(null, 4);

        if (fireTicks != null)
            entity.setFireTicks(fireTicks);

        if (fallDistance != null)
            entity.setFallDistance(fallDistance);

        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;

            if (health != null)
                living.setHealth(health);

            if (damage != null)
                living.damage(damage);

            if (remainingAir != null)
                living.setRemainingAir(remainingAir);
        }

        if (entity instanceof Player) {
            Player player = (Player) entity;

            if (foodLevel != null)
                player.setFoodLevel(foodLevel);

            if (saturationLevel != null)
                player.setSaturation(saturationLevel);

            if (exhaustionLevel != null)
                player.setExhaustion(exhaustionLevel);

            if (walkSpeed != null)
                player.setWalkSpeed(walkSpeed);

            if (allowFlight != null)
                player.setAllowFlight(allowFlight);

            if (flying != null)
                player.setFlying(flying);

            if (flySpeed != null)
                player.setFlySpeed(flySpeed);

            if (sneaking != null)
                player.setSneaking(sneaking);

            if (sprinting != null)
                player.setSneaking(sprinting);

            if (invincible != null) {
                ConfigurationManager cfg = wg.getGlobalStateManager();

                if (invincible) {
                    cfg.enableGodMode(player);
                } else {
                    cfg.disableGodMode(player);
                }
            }

            if (openWorkbench != null && openWorkbench)
                player.openWorkbench(player.getLocation(), true);

            if (performCommand != null)
                player.performCommand(performCommand);
        }

        if (teleportSafely != null && teleportSafely) {
            Location loc = entity.getLocation();
            World world = loc.getWorld();

            // In case we're in void!
            if (loc.getY() < 1) {
                loc.setY(1);
            }

            int maxY = world.getMaxHeight();
            int x = loc.getBlockX();
            int y = Math.max(0, loc.getBlockY());
            int z = loc.getBlockZ();

            byte free = 0;

            loop:
            while (y <= maxY + 2) {
                if (BlockType.canPassThrough(new Location(world, x, y, z).getBlock().getTypeId())) {
                    ++free;
                } else {
                    free = 0;
                }

                if (free == 2) {
                    Block block = world.getBlockAt(x, y - 2, z);
                    int id = block.getTypeId();
                    int data = block.getData();
                    Location newLoc = new Location(world, x + 0.5,
                            y - 2 + BlockType.centralTopLimit(id, data), z + 0.5);

                    // Don't want the player falling into void
                    if (newLoc.getBlockY() <= 1) {
                        Block bottomBlock = world.getBlockAt(x, 0, z);
                        bottomBlock.setType(Material.GLASS);
                        newLoc.add(0, 1, 0);
                    }

                    entity.teleport(newLoc);
                    entity.setFallDistance(0);

                    break loop;
                }

                ++y;
            }
        }
    }

}