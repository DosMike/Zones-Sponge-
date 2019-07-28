package de.dosmike.sponge.zones;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.common.reflect.TypeToken;

import de.dosmike.sponge.zones.events.ZoneEvent.Direction;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class CommandZone extends BlockZone {
	List<CommandZoneAction> enter = new ArrayList<>();
	List<CommandZoneAction> leave = new ArrayList<>();
	
	public CommandZone(Location<World> a, Location<World> b) {
		super(a,b);
	}
	
	@Override
	public void serialize(ConfigurationNode cfg) throws ObjectMappingException {
		cfg.setValue(TypeToken.of(CommandZone.class), this);
	}
	
	public void fire(Player target, Direction direction) {
		if (direction.equals(Direction.ENTER)) {
			for (CommandZoneAction action : enter) action.fire(target);
		} else if (direction.equals(Direction.LEAVE)) {
			for (CommandZoneAction action : leave) action.fire(target);
		}
	}
	
	public static CommandZone fromBlockZone(BlockZone block) {
		CommandZone result = new CommandZone(block.low, block.high);
		result.zoneid = block.zoneid;
		result.data = block.data;
		return result;
	}
}
