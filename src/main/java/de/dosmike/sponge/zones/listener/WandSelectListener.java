package de.dosmike.sponge.zones.listener;

import java.util.Optional;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;

import de.dosmike.sponge.zones.handler.ZoneCommand;

public class WandSelectListener {

	@Listener
	public void onInteractBlock(InteractBlockEvent event) {
		Optional<Player> source = event.getCause().first(Player.class);
		if (!source.isPresent()) return;
		if (!source.get().hasPermission("dosmike.zones.edit")) return;
		Optional<ItemStack> tool = source.get().getItemInHand(HandTypes.MAIN_HAND);
		if (!tool.isPresent() || !tool.get().getItem().equals(ItemTypes.LEAD)) return; //getType() does not work in the used version of spongevanilla
		if (!event.getTargetBlock().getLocation().isPresent()) return;
		
		if (event instanceof InteractBlockEvent.Primary) {
			ZoneCommand.set1(source.get(), event.getTargetBlock().getLocation().get().sub(0, 0.1, 0));
		} else if (event instanceof InteractBlockEvent.Secondary) {
			if (!source.get().get(Keys.IS_SNEAKING).orElse(false)) {
				ZoneCommand.set2(source.get(), event.getTargetBlock().getLocation().get().add(0, 0.9, 0)); //at the opposite corner
				source.get().sendMessage(ChatTypes.ACTION_BAR, Text.of(TextColors.GOLD, "Shift + Secondary Click = Create Zone"));
			} else {
				ZoneCommand.create(source.get());
			}
		}
	}
	
}
