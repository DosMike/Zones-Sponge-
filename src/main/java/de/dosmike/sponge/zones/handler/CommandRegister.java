package de.dosmike.sponge.zones.handler;

import org.spongepowered.api.Sponge;

import de.dosmike.sponge.zones.Zones;

public class CommandRegister {
	public static void RegisterCommands(Zones plugin) {
		Sponge.getCommandManager().register(plugin, ZoneCommand.getSpec(), "/zones", "/zone", "/z");
	}
}
