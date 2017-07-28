package de.dosmike.sponge.zones;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

import de.dosmike.sponge.zones.events.ZoneEvent;
import de.dosmike.sponge.zones.events.ZoneEvent.Direction;

public class PlayerExtra {
	Set<Zone> inzone = new HashSet<>();
	Player player;
	
	PlayerExtra(Player forPlayer) {
		player=forPlayer;
	}
	
	void updateZones(Collection<Zone> in, Collection<Zone> out) {
		Cause eventcause = 
				Cause.builder().suggestNamed("LocationChanged", 
				Sponge.getPluginManager().getPlugin("dosmike_zones").orElse(null)).build();
		Set<Zone> change = new HashSet<>();
		//find zones player left
		for (Zone z : inzone) if (!in.contains(z)) change.add(z);
		for (Zone z : change) {
			inzone.remove(z);
			//create event
			Sponge.getEventManager().post(new ZoneEvent(player, z, Direction.LEAVE, eventcause));
		}
		change.clear();
		if (!player.isOnline() || !player.isLoaded()) return; //do not add zones to players disconnected
		//find zones player entered
		for (Zone z : in) if (!inzone.contains(z)) change.add(z);
		for (Zone z : change) {
			inzone.add(z);
			//create event
			Sponge.getEventManager().post(new ZoneEvent(player, z, Direction.ENTER, eventcause));
		}
	}
	
	public boolean isInZone(UUID zoneid) {
		for (Zone z : inzone) {
			if (z.zoneid.equals(zoneid)) return true;
		}
		return false;
	}
	public boolean isInZone(Zone zone) {
		 return inzone.contains(zone);
	}
	public Collection<Zone> getZonesByData(String key, Serializable value) {
		Set<Zone> zones = new HashSet<>();
		for (Zone z : inzone) {
			if (z.data.containsKey(key) && z.data.get(key).equals(value)) zones.add(z);
		}
		return zones;
	}
	public Collection<Zone> getZones() {
		return inzone;
	}
	
	/** manually calling this will force re-enter a Player to all zones he's currently in */
	public void loosePlayer(Cause cause) {
		Zones.loosePlayer(player.getUniqueId());
		inzone.forEach(new Consumer<Zone>() {
			public void accept(Zone z) {
				//create event
				Sponge.getEventManager().post(new ZoneEvent(player, z, Direction.LEAVE, cause));
			}
		});
	}
}
