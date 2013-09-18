package com.sk89q.worldguard.protection.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityRegainHealthEvent;

/**
 * @author Silthus
 */
public class WorldGuardRegionHealEvent extends EntityRegainHealthEvent {

    public WorldGuardRegionHealEvent(Entity entity, double amount, RegainReason regainReason) {

        super(entity, amount, regainReason);
    }

    @Override
    public void setAmount(double amount) {

        // we dont want to modify the amount
    }
}
