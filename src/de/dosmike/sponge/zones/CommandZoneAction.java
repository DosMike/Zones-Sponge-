package de.dosmike.sponge.zones;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class CommandZoneAction {
	public CommandZoneAction() {} //ObjectMapper access
	
	@Setting
	List<String> permission = new ArrayList<>();

	@Setting(comment="Format: world/x/y/z or world/x/y/z/rotation")
	String location=null;
	Location<World> loc=null;
	Vector3d rotation=null;
	
	@Setting
	List<String> message = new ArrayList<>();
	
	@Setting
	List<String> command = new ArrayList<>();
	
	public void fire(Player target) {
		boolean permissionOR = permission.isEmpty();
		for (String perm : permission)
			if (target.hasPermission(perm)) {
				permissionOR = true;
				break;
			}
		if (!permissionOR) return;
		
		if (loc==null && location!=null && !location.isEmpty()) {
			String[]p=location.split("/");
			try {
				if (p.length>5) throw new IndexOutOfBoundsException("Found more that 5 elements");
				loc = Sponge.getServer().getWorld(p[0]).get().getLocation(Double.parseDouble(p[1]), Double.parseDouble(p[2]), Double.parseDouble(p[3]));
				if (p.length==5) {
					rotation = new Vector3d(Double.valueOf(p[4]), 0, 0);
				}
			} catch (Exception e) {
				throw new RuntimeException("Location for zone action in config has to be formatted world/x/y/z or world/x/y/z/rotation");
			}
		}
		if (loc!=null) if(rotation!=null) target.setLocationAndRotationSafely(loc, rotation); else target.setLocationSafely(loc);
		
		for (String msg : message) target.sendMessage(Text.of(msg));
		for (String cmd : command) Sponge.getCommandManager().process(target, cmd.replace("@t", target.getName()));
	}
}
