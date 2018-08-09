package de.dosmike.sponge.zones;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

import de.dosmike.sponge.zones.handler.CommandRegister;
import de.dosmike.sponge.zones.listener.CommandZoneListener;
import de.dosmike.sponge.zones.listener.PlayerConnectionListener;
import de.dosmike.sponge.zones.listener.WandSelectListener;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.DefaultObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;

@Plugin(id = "dosmike_zones", name = "Zones", version = "1.2", description = "Framework for zone, fires events when entering/leaving a zone. Use //z to edit zones", authors = "DosMike")
public class Zones {
	/** Not doing anything, empty Java main */
	public static void main(String[] args) {
		System.out.println("This jar can not be run as executable!");
	}
	public static boolean verboseEvents=false;
	Task motionGuard=null;
	static Zones instance;
	
	static void log(Object... message) {
		Text.Builder tb = Text.builder();
		tb.color(TextColors.AQUA);
		tb.append(Text.of("[Zones] "));
		if (!(message[0] instanceof TextColor)) tb.color(TextColors.RESET);
		for (Object o : message) {
			if (o instanceof TextColor) 
				tb.color((TextColor)o);
			else
				tb.append(Text.of(o));
		}
		if (verboseEvents)
			Sponge.getServer().getBroadcastChannel().send(tb.build());
		else
			Sponge.getServer().getConsole().sendMessage(tb.build());
	}
	
	static Map<UUID, PlayerExtra> extra = new HashMap<>();
	/** Retrieve a PlayerExtra containing information on the Zones the player is currently in */
	public static Optional<PlayerExtra> getPlayer(UUID player) {
		if (!extra.containsKey(player)) {
			Optional<Player> from = Sponge.getServer().getPlayer(player);
			if (from.isPresent()) {
				Player fp = from.get();
				if (fp.isOnline()&&fp.isLoaded())
					extra.put(player, new PlayerExtra(Sponge.getServer().getPlayer(player).get()));
			}
		}
		
		return extra.containsKey(player)?Optional.of(extra.get(player)):Optional.empty(); 
	} 
	public static void loosePlayer(UUID player) { extra.remove(player); }
	
	@Listener
	public void onServerStart(GameStartedServerEvent event) {
		log(TextColors.YELLOW, "Wellcome to the zone!");
		log(TextColors.YELLOW, "Registering commands and loading zones...");
		
		instance = this;
		//add event listener
		Sponge.getEventManager().registerListeners(this, new PlayerConnectionListener());
		Sponge.getEventManager().registerListeners(this, new WandSelectListener());
		
		Sponge.getEventManager().registerListeners(this, new CommandZoneListener());
		
		//register commands
		CommandRegister.RegisterCommands(this);
		
		//load zones
		zoneSerializer.registerType(TypeToken.of(BlockZone.class), new BlockZoneSerializer());
		zoneSerializer.registerType(TypeToken.of(CommandZone.class), new CommandZoneSerializer());
		loadZones();
		
		//start timer for locations
		Task.builder().execute(() -> {
			long t0 = System.currentTimeMillis();
			
			Sponge.getServer().getOnlinePlayers().forEach(p -> {
				if (p.isOnline() && p.isLoaded()) {
					Set<Zone> inside = new HashSet<>(), outside = new HashSet<>();
					zonelist.forEach(z -> {
						if (z.contains(p.getLocation())) {
							inside.add(z);
						} else {
							outside.add(z);
						}
					});
					getPlayer(p.getUniqueId()).get().updateZones(inside, outside);
				}
			});
			
			//timing
			t0 = System.currentTimeMillis()-t0;
			if (t0>50) {
				log(TextColors.YELLOW, "! Location monitoring took "+t0+"ms");
			}
		}).interval(50, TimeUnit.MILLISECONDS).name("Zones - Motion Guard").submit(this);
		
		log(TextColors.YELLOW, "We're done loading");
	}
	
	@Listener
	public void onServerHalt(GameStoppingEvent event) {
		if (motionGuard != null) motionGuard.cancel();
		Set<UUID> keys = extra.keySet();
		for (UUID key : keys)
			extra.get(key).loosePlayer(event.getCause());
	}
	
	static Set<Zone> zonelist = new HashSet<>();
	public static Iterator<Zone> getZones() {
		return zonelist.iterator();
	}
	public static Optional<Zone> getZone(UUID zoneid) {
		for (Zone z : zonelist)
			if (z.zoneid.equals(zoneid)) return Optional.of(z);
		return Optional.empty();
	}
	
	public static void addZone(Zone z) {
		zonelist.add(z);
	}
	public static void removeZone(Class<? extends Zone> clz) {
		Set<Zone> mark = new HashSet<>();
		for (Zone z : zonelist)
			if (clz.isAssignableFrom(z.getClass()))
				mark.add(z);
		zonelist.removeAll(mark);
	}
	public static void removeZone(UUID zoneid) {
		for (Zone z : zonelist)
			if (z.zoneid.equals(zoneid)) {
				zonelist.remove(z);
				return;
			}
	}
	
	/** Convenience function for getPlayer(p.getUniqueId()).get().getZones(); */
	public static Collection<Zone> getZonesFor(Player p) {
		Optional<PlayerExtra> ex = getPlayer(p.getUniqueId());
		return ex.isPresent()?ex.get().getZones():new HashSet<>(); 
	}
	/** return all zones kontaining a certain Key in their data map<br>
	 * If you intend to get player from selected zones it is faster to<br>
	 * <pre>getZonesFor(player).forEach(new Consumer&lt;Zone&gt;() {
	 *   &#x40;Override public void accept(Zone zone) {
	 *     if (zone.containsKey(key)) {
	 *       //Do something
	 *     }
	 *   }</pre> */
	public static Collection<Zone> getZonesFor(String key) {
		Set<Zone> result = new HashSet<>();
		for (Zone z : zonelist)
			if (z.containsKey(key))
				result.add(z);
		return result;
	}
	/** return all zones kontaining a certain Key in their data map<br>
	 * If you intend to get player from selected zones it is faster to<br>
	 * <pre>getZonesFor(player).forEach(new Consumer&lt;Zone&gt;() {
	 *   &#x40;Override public void accept(Zone zone) {
	 *     if (zone.containsKey(key) && zone.get(key).equals(value)) {
	 *       //Do something
	 *     }
	 *   }</pre> */
	public static Collection<Zone> getZonesFor(String key, Serializable value) {
		Set<Zone> result = new HashSet<>();
		for (Zone z : zonelist)
			if (z.containsKey(key) && z.get(key).equals(value))
				result.add(z);
		return result;
	}
	public static Collection<Zone> getZonesAt(Location<World> location) {
		Set<Zone> result = new HashSet<>();
		for (Zone z : zonelist)
			if (z.contains(location))
				result.add(z);
		return result;
	}
	
	@Listener
	public void reload(GameReloadEvent event) {
		Zones.removeZone(Zone.class); //remove all zones
		Set<UUID> keys = extra.keySet();
		for (UUID key : keys)
			extra.get(key).loosePlayer(event.getCause());
		loadZones();
	}
	
	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;
	
	public static TypeSerializerCollection zoneSerializer = TypeSerializers.getDefaultSerializers().newChild();
	
	void loadZones() {
		Path zoneFile = configDir.resolve("saved.zones");
		ConfigurationLoader<CommentedConfigurationNode> loader =
				  HoconConfigurationLoader.builder().setPath(zoneFile).build();
		
		ConfigurationOptions options = ConfigurationOptions.defaults().setSerializers(zoneSerializer);
		ConfigurationNode root;
		try {
			root = loader.load(options);
			int i=0; ConfigurationNode n;
			for(;;) { n=root.getNode("Zones", String.valueOf(++i)); if (n.isVirtual()) break; //sloppy for each in virtual environment
				try {
					@SuppressWarnings("unchecked") // we expect to only load zones, if it fails a plugin is missing and we catch that, so no need to worry
					Class<? extends Zone> clz = (Class<? extends Zone>)Class.forName(n.getNode("class").getString());
					zonelist.add(n.getValue(TypeToken.of(clz)));
				} catch (Exception e) {
					log(TextColors.RED, "Plugin-zone of type " + n.getNode("class").getString() + " can't be loaded. Is the plugin missing?");
				}
			}
			log ("Loaded "+zonelist.size()+" Zones.");
		} catch (Exception e) {
			log(TextColors.RED, "Failed to load config!");
			e.printStackTrace();
		}
	}
	public void saveZones() {
		if (!configDir.toFile().exists()) configDir.toFile().mkdirs(); //prepare directory as Configurate does not do this?
		Path zoneFile = configDir.resolve("saved.zones");
		ConfigurationLoader<CommentedConfigurationNode> loader =
				  HoconConfigurationLoader.builder().setPath(zoneFile).build();
		
		ConfigurationOptions options = ConfigurationOptions.defaults().setSerializers(zoneSerializer);
		options.setObjectMapperFactory(DefaultObjectMapperFactory.getInstance());
		ConfigurationNode root = loader.createEmptyNode(options);
		try {
			Iterator<Zone> it = zonelist.iterator();
			for (int i = 0; i < zonelist.size() && it.hasNext(); i++) {
				it.next().serialize(root.getNode("Zones", String.valueOf(i+1)));
			}
			loader.save(root);
		} catch (Exception e) {
			log(TextColors.RED, "Failed to save config!");
			e.printStackTrace();
		}
	}
}
