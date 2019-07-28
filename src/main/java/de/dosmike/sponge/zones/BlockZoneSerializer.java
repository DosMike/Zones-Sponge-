package de.dosmike.sponge.zones;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class BlockZoneSerializer extends ZoneSerializer<BlockZone> {
	
	@Override
	public void serialize(BlockZone z, ConfigurationNode cfg) throws ObjectMappingException {
		cfg.getNode("Low").setValue(loc2str(z.low));
		cfg.getNode("High").setValue(loc2str(z.high));
	}

	@Override
	public BlockZone deserialize(ConfigurationNode cfg) throws ObjectMappingException {
		Location<World> low = str2loc(cfg.getNode("Low").getString());
		Location<World> high = str2loc(cfg.getNode("High").getString());
		return new BlockZone(low, high);
	}
	
	private Location<World> str2loc (String s) {
		String[] p = s.split("/"); return Sponge.getServer().getWorld(p[0]).get().getLocation(Double.parseDouble(p[1]), Double.parseDouble(p[2]), Double.parseDouble(p[3]));
	}
	private String loc2str (Location<World> l) {
		return l.getExtent().getName() + "/" + l.getX() + "/" + l.getY() + "/" + l.getZ();
	}
	
}
