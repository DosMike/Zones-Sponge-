package de.dosmike.sponge.zones.listener;

import org.spongepowered.api.event.Listener;

import de.dosmike.sponge.zones.CommandZone;
import de.dosmike.sponge.zones.events.ZoneEvent;

public class CommandZoneListener {
	
	@Listener
	public void onZoneChange(ZoneEvent event) {
		if (!(event.getZone() instanceof CommandZone)) return;
		
		((CommandZone)event.getZone()).fire(event.getTargetEntity(), event.getDirection());
	}
	
}
