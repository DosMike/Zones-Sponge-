package de.dosmike.sponge.zones.handler;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import de.dosmike.sponge.zones.BlockZone;
import de.dosmike.sponge.zones.PlayerExtra;
import de.dosmike.sponge.zones.Zone;
import de.dosmike.sponge.zones.Zones;

public class ZoneCommand implements CommandExecutor {
	
	public static CommandSpec getSpec() {
		return CommandSpec.builder()
				.description(Text.of("Work with Zones, see //z help for more!"))
				.permission("dosmike.zones.edit")
				.executor(new ZoneCommand())
				.arguments(
					GenericArguments.optional(
						GenericArguments.string(Text.of("Action"))
					),
					GenericArguments.optional(
						GenericArguments.string(Text.of("ZoneID"))
					)
				)
				.build();
	}
	
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Optional<String> arg0 = args.getOne("Action");
		if (!arg0.isPresent()) {
			src.sendMessage(Text.of(TextColors.AQUA, "-= Zones 1.0 by DosMike =-"));
			return CommandResult.success();
		}
		if (!(src instanceof Player)) {
			src.sendMessage(Text.of("This command is Player only"));
			return CommandResult.empty();
		}
		Player p = (Player)src;
		Optional<PlayerExtra> extra = Zones.getPlayer(p.getUniqueId());
		
		String tmp;
		
		switch (arg0.get()) {
		case "pos1":
		case "from":
		case "1":
			set1(p, p.getWorld().getLocation(p.getLocation().getBlockPosition().toDouble().sub(0, 0.1, 0)));
			break;
		case "pos2":
		case "to":
		case "2":
			set2(p, p.getWorld().getLocation(p.getLocation().getBlockPosition().toDouble().add(0, 1.9, 0)));
			break;
		case "create":
		case "add":
		case "c":
			create(p);
			break;
		case "info":
		case "i":
			if (!extra.isPresent()) break;
			Collection<Zone> inzones = extra.get().getZones();
			msg(p, "You're currently in "+inzones.size()+" zones:");
			for (Zone z : inzones) {
				src.sendMessage(Text.of(z.getClass().getSimpleName(), " [",
						Text.builder(z.getID().toString()).onShiftClick(TextActions.insertText(z.getID().toString())).build(),
						"]"
						));
			}
			break;
		case "delete":
		case "remove":
			if (!extra.isPresent()) break;
			tmp = (String)args.getOne("ZoneID").orElse("#");
			if (tmp.equals("*")) {
				Collection<Zone> inzone = extra.get().getZones();
				msg(p, inzone.size()+" Zones at your location deleted:");
				for (Zone z : inzone) {
					Zones.removeZone(z.getID());
					src.sendMessage(Text.of(z.getClass().getSimpleName(), " [", z.getID().toString(), "]"));
				}
			} else {
				try {
					UUID uid = UUID.fromString(tmp);
					Zones.removeZone(uid);
					msg(p, "Zone deleted");
				} catch (Exception e) {
					msg(p, "Zone with this UUID does not exist");
				}
			}
			break;
		case "save":
			((Zones)Sponge.getPluginManager().getPlugin("dosmike_zones").get().getInstance().get()).saveZones();
			msg(p, TextColors.GREEN, "Save Complete");
			break;
		case "help":
		case "?":
			msg(p, "To create a Zone select a area with //z 1 and //z 2, then confirm creation with //z c");
			msg(p, "To get a list of zones you're currently inside use //z i");
			msg(p, "To delete a zone use //z delete ZONEID (use * as ZONEID to delete all zones you're in)");
			break;
		case "verbose":
		case "v":
			Zones.verboseEvents=!Zones.verboseEvents;
			break;
		default:
			return CommandResult.empty(); 
		}
		return CommandResult.success();
	}
	
	private static Location<World> str2loc (String s) {
		String[] p = s.split("/"); return Sponge.getServer().getWorld(p[0]).get().getLocation(Double.parseDouble(p[1]), Double.parseDouble(p[2]), Double.parseDouble(p[3]));
	}
	private static String loc2str (Location<World> l) {
		return l.getExtent().getName() + "/" + l.getX() + "/" + l.getY() + "/" + l.getZ();
	}
	private static void msg(CommandSource src, Object... msg) {
		Text result = Text.of(TextColors.AQUA, "[Zones] ");
		Text.Builder builder = Text.builder();
		builder.append(result);
		builder.append(Text.of(msg));
		src.sendMessage(builder.build());
	}
	
	public static void set1(Player source, Location<World> from) {
		String tmp = loc2str(from);
		source.getProfile().getPropertyMap().removeAll("dosmike_zones_selection");
		source.getProfile().getPropertyMap().put("dosmike_zones_selection", ProfileProperty.of("low", tmp));
		msg(source, "Position 1 set to " + tmp);
	}
	public static void set2(Player source, Location<World> to) {
		Location<World> low=null;
		ProfileProperty hi=null;
		String tmp = loc2str(to);
		for (ProfileProperty pp : source.getProfile().getPropertyMap().get("dosmike_zones_selection")) {
			if (pp.getName().equals("low")) {
				//msg (p, "L1 was " + pp.getValue());
				low = str2loc(pp.getValue());
			} else if (pp.getName().equals("high"))
				hi = pp;
		}
		if (low==null) {
			msg(source, "Set position 1 first");
			return;
		}
		if (!low.getExtent().equals(to.getExtent())) {
			msg(source, "Selection can not span multiple worlds");
			return;
		}
		if (hi!=null) source.getProfile().getPropertyMap().get("dosmike_zones_selection").remove(hi); //delete hi to prevent duplicates
		source.getProfile().getPropertyMap().put("dosmike_zones_selection", ProfileProperty.of("high", tmp));
		msg(source, "Position 2 set to " + tmp);
		msg(source, "Selection: x"+(Math.abs(to.getBlockX()-low.getBlockX()))+
						" y"+(Math.abs(to.getBlockY()-low.getBlockY()))+
						" z"+(Math.abs(to.getBlockZ()-low.getBlockZ())));
	}
	public static void create(Player source) {
		Location<World> low=null, high=null;
		for (ProfileProperty pp : source.getProfile().getPropertyMap().get("dosmike_zones_selection")) {
			if (pp.getName().equals("low")) {
				low = str2loc(pp.getValue());
			} else if (pp.getName().equals("high")) {
				high = str2loc(pp.getValue());
			}
		}
		if (low==null) {
			msg(source, "Set position 1 first");
			return;
		} else if (high==null) {
			msg(source, "Set position 2 first");
			return;
		} else {
			//sort locations
			double l,h,x1,x2,y1,y2,z1,z2;
			l = low.getBlockX();
			h = high.getBlockX();
			x1 = l<h ? l : h;
			x2 = l>h ? l : h;
			l = low.getBlockY();
			h = high.getBlockY();
			y1 = l<h ? l : h;
			y2 = l>h ? l : h;
			l = low.getBlockZ();
			h = high.getBlockZ();
			z1 = l<h ? l : h;
			z2 = l>h ? l : h;
			
			//fill locations
			low=low.getExtent().getLocation(x1, y1, z1);
			high=high.getExtent().getLocation(x2+1.0, y2, z2+1.0);
		}
		BlockZone newblockzone = new BlockZone(low,high);
		source.getProfile().getPropertyMap().removeAll("dosmike_zones_selection");
		Zones.addZone(newblockzone);
		msg(source, "Created new Block Zone: [", 
				Text.builder(newblockzone.getID().toString()).onShiftClick(TextActions.insertText(newblockzone.getID().toString())).build(),
				"]");
	}
}
