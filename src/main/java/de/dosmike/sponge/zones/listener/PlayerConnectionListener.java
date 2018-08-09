package de.dosmike.sponge.zones.listener;

import java.util.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import de.dosmike.sponge.zones.PlayerExtra;
import de.dosmike.sponge.zones.Zones;

public class PlayerConnectionListener {
	
	@Listener
	public void onClientConnectionEvent(ClientConnectionEvent event) {
		if (event instanceof ClientConnectionEvent.Disconnect) {
			Player player = ((ClientConnectionEvent.Disconnect) event).getTargetEntity();
			Optional<PlayerExtra> extra = Zones.getPlayer(player.getUniqueId());
			if (!extra.isPresent()) return; // already gone - maybe through timer
			extra.get().loosePlayer(event.getCause()); //creates zone left events and removes player
		}
	}
}
