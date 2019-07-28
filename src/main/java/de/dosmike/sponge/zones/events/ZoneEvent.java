package de.dosmike.sponge.zones.events;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.living.humanoid.player.TargetPlayerEvent;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.text.Text;

import de.dosmike.sponge.zones.Zone;
import de.dosmike.sponge.zones.Zones;

public class ZoneEvent extends AbstractEvent implements TargetPlayerEvent {

	public enum Direction {
		ENTER, LEAVE
	}
	
	private final Cause cause;
    private final Player target;
    private final Zone zone;
    private final Direction dir;
	
    public ZoneEvent(Player target, Zone zone, Direction direction, Cause cause) {
		this.cause = cause;
		this.target = target;
		this.zone = zone;
		this.dir = direction;
		if (Zones.verboseEvents) {
			Sponge.getServer().getBroadcastChannel().send(Text.of(target.getName() + " " + direction + " [" +
						zone.getID().toString() + "]"));
		}
	}
    
	@Override
	public Cause getCause() {
		return cause;
	}

	@Override
	public Player getTargetEntity() {
		return target;
	}
	
	public Zone getZone() {
		return zone;
	}
	
	public Direction getDirection() {
		return dir;
	}
}
