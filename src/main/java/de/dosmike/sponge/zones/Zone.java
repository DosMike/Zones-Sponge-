package de.dosmike.sponge.zones;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public abstract class Zone {

	UUID zoneid;
	public UUID getID() {
		return zoneid;
	}
	Map<String, Serializable> data;
	
	public Zone() {
		zoneid = UUID.randomUUID();
		data = new HashMap<>();	
	}
	
	public abstract boolean contains(Location<World> dyn);
	
	public void put(String key, Serializable value) {
		data.put(key, value);
	}
	public boolean containsKey(String key) {
		return data.containsKey(key);
	}
	public void remove(String key) {
		data.remove(key);
	}
	public Serializable get(String key) {
		return data.get(key);
	}
	public Set<String> getKeys() {
		return data.keySet();
	}
	
	public Collection<Player> getPlayers() {
		Set<Player> result = new HashSet<>();
		for (PlayerExtra ex : Zones.extra.values())
			if (ex.inzone.contains(this))
				result.add(ex.getPlayer());
		return result;
	}
	
	public abstract void serialize(ConfigurationNode cfg) throws ObjectMappingException;
	public abstract World getWorld();
}
