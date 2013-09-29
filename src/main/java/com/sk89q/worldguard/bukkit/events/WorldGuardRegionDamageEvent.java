package com.sk89q.worldguard.bukkit.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * @author Silthus
 */
public class WorldGuardRegionDamageEvent extends EntityDamageEvent {

    public WorldGuardRegionDamageEvent(Entity damagee, DamageCause cause, double damage) {

        super(damagee, cause, damage);
    }

    @Override
    public void setDamage(double damage) {

        // we dont want to modify the damage
    }
}
