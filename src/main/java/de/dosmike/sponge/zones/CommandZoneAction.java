package de.dosmike.sponge.zones;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
		for (String cmd : command) {
			int ios=cmd.indexOf('/');
			if (ios<0) {
				cmd = parseCustomTarget(cmd, target);
				Sponge.getServer().getConsole().sendMessage(Text.of("Zone triggerd by "+target.getName()+" running \""+cmd+"\""));
				Sponge.getCommandManager().process(Sponge.getServer().getConsole(), cmd);
			} else {
				long delay=0l;
				
				String prefix = cmd.substring(0, ios);
				cmd = parseCustomTarget(cmd.substring(ios+1), target);
				Sponge.getServer().getConsole().sendMessage(Text.of("Zone triggerd by "+target.getName()+" running \""+cmd+"\"" + (prefix.isEmpty()?"":" with meta "+prefix)));
				if (!prefix.isEmpty()) {
					String[] metas = prefix.split(";");
					for (String meta : metas) {
						ios = meta.indexOf(':'); String key = meta.substring(0, ios); String value = meta.substring(ios+1);
						
						if (key.equalsIgnoreCase("delay")) {
							try {
								delay = (long)(Double.parseDouble(value)*1000);
							} catch (Exception e) {} //ignore if the value is invalid
						}
					}
				}
				if (delay>0) {
					Sponge.getScheduler().createSyncExecutor(Zones.instance).schedule(new CmdRunner(cmd), (long)delay, TimeUnit.MILLISECONDS);
				} else {
					Sponge.getCommandManager().process(Sponge.getServer().getConsole(), cmd);
				}
			}
		}
	}
	class CmdRunner implements Runnable {
		String arg;
		public CmdRunner(String cmd) { arg=cmd; }
		@Override public void run() {
			Sponge.getCommandManager().process(Sponge.getServer().getConsole(), arg); 
		}
	}
	String parseCustomTarget(String command, Player target) {
		Vector3d pos = target.getLocation().getPosition();
		//String.format uses localized decimal point (germany uses comma, minecraft can't work with that as input)
		command = command.replace("@t.x", String.valueOf((double)((int)pos.getX()*100)/100.0));
		command = command.replace("@t.y", String.valueOf((double)((int)pos.getY()*100)/100.0));
		command = command.replace("@t.z", String.valueOf((double)((int)pos.getZ()*100)/100.0));
		command = command.replace("@t", target.getName());
		return command;
	}
}
