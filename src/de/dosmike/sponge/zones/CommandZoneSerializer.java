package de.dosmike.sponge.zones;

import java.util.List;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.common.reflect.TypeToken;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class CommandZoneSerializer extends ZoneSerializer<CommandZone> {
	
	@Override
	public void serialize(CommandZone z, ConfigurationNode cfg) throws ObjectMappingException {
		cfg.getNode("Low").setValue(loc2str(z.low));
		cfg.getNode("High").setValue(loc2str(z.high));
		//I wanna die, why did noone tell me that lists are such a pain to serialize >.> took me why to look to dig up a solution on the undocumentet piece of serializer called configurate
		TypeToken<List<CommandZoneAction>> token = new TypeToken<List<CommandZoneAction>>() {
			private static final long serialVersionUID = -2818095595522682842L;
		};
		if (!z.enter.isEmpty()) cfg.getNode("Enter").setValue(token, z.enter);
		if (!z.leave.isEmpty()) cfg.getNode("Leave").setValue(token, z.leave);
	}

	@Override
	public CommandZone deserialize(ConfigurationNode cfg) throws ObjectMappingException {
		Location<World> low = str2loc(cfg.getNode("Low").getString());
		Location<World> high = str2loc(cfg.getNode("High").getString());
		CommandZone result = new CommandZone(low, high);
		result.enter = cfg.getNode("Enter").getList(TypeToken.of(CommandZoneAction.class));
		result.leave = cfg.getNode("Leave").getList(TypeToken.of(CommandZoneAction.class));
		return result;
	}
	
	private Location<World> str2loc (String s) {
		String[] p = s.split("/"); return Sponge.getServer().getWorld(p[0]).get().getLocation(Double.parseDouble(p[1]), Double.parseDouble(p[2]), Double.parseDouble(p[3]));
	}
	private String loc2str (Location<World> l) {
		return l.getExtent().getName() + "/" + l.getX() + "/" + l.getY() + "/" + l.getZ();
	}
}
