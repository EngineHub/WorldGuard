package com.sk89q.worldguard.protection.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is fired when PVP is disallowed between players due to a "pvp deny" flag.
 * Cancelling this event allows the PVP in spite of this.
 * 
 * @author Score_Under
 */
public class DisallowedPVPEvent extends Event implements Cancellable {

    private static final long serialVersionUID = 1L;
    private static final HandlerList handlers = new HandlerList();
    
    private boolean cancelled = false;
    private final Player attacker;
    private final Player defender;

    public DisallowedPVPEvent(final Player attacker, final Player defender) {
        super("DisallowedPVPEvent");
        this.attacker = attacker;
        this.defender = defender;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * @return the attacking player.
     */
    public Player getAttacker() {
        return attacker;
    }

    /**
     * @return the defending player.
     */
    public Player getDefender() {
        return defender;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
