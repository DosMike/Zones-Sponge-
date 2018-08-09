package de.dosmike.sponge.zones;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.Cause;

import de.dosmike.sponge.zones.events.ZoneEvent;
import de.dosmike.sponge.zones.events.ZoneEvent.Direction;

public class PlayerExtra {
	Set<Zone> inzone = new HashSet<>();
	//Player player;
	UUID plid;
	Player getPlayer() { return Sponge.getServer().getPlayer(plid).orElse(null); } //should not be able to return null, disconnected player are removed
	PlayerExtra(Player forPlayer) {
		plid=forPlayer.getUniqueId();
	}
	
	void updateZones(Collection<Zone> in, Collection<Zone> out) {
		Player player = getPlayer();

		try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
			frame.pushCause("LocationChanged");
			Set<Zone> change = new HashSet<>();
			//find zones player left
			for (Zone z : inzone) if (!in.contains(z)) change.add(z);
			for (Zone z : change) {
				inzone.remove(z);
				//create event
				Sponge.getEventManager().post(new ZoneEvent(player, z, Direction.LEAVE, frame.getCurrentCause()));
			}
			change.clear();
			if (!player.isOnline() || !player.isLoaded()) return; //do not add zones to players disconnected
			//find zones player entered
			for (Zone z : in) if (!inzone.contains(z)) change.add(z);
			for (Zone z : change) {
				inzone.add(z);
				//create event
				Sponge.getEventManager().post(new ZoneEvent(player, z, Direction.ENTER, frame.getCurrentCause()));
			}
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
		Player player = getPlayer();
		inzone.forEach(new Consumer<Zone>() {
			public void accept(Zone z) {
				//create event
				Sponge.getEventManager().post(new ZoneEvent(player, z, Direction.LEAVE, cause));
			}
		});
		Zones.loosePlayer(player.getUniqueId());
	}
}
