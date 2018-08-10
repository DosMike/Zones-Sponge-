package de.dosmike.sponge.zones;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.common.reflect.TypeToken;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class BlockZone extends Zone {
	
	Location<World> low, high;
	public BlockZone(Location<World> a, Location<World> b) {
		super(); //give this zone a uuid and prepare data-map
		
		World ea = a.getExtent();
		if (ea==null) throw new RuntimeException("Can't figure world for location 1");
		if (!ea.equals(b.getExtent())) throw new RuntimeException("Worlds for Location 1 and 2 do not match");
		
		//sort locations
		double l,h,x1,x2,y1,y2,z1,z2;
		l = a.getX();
		h = b.getX();
		x1 = l<h ? l : h;
		x2 = l>h ? l : h;
		l = a.getY();
		h = b.getY();
		y1 = l<h ? l : h;
		y2 = l>h ? l : h;
		l = a.getZ();
		h = b.getZ();
		z1 = l<h ? l : h;
		z2 = l>h ? l : h;
		
		//fill locations
		low=a.getExtent().getLocation(x1, y1, z1);
		high=a.getExtent().getLocation(x2, y2, z2);
	}
	
	public boolean contains(Location<World> dyn) {
		if (low.getExtent()==null || !low.getExtent().equals(dyn.getExtent())) return false;
		return (dyn.getX()>=low.getX() && dyn.getY()>=low.getY() && dyn.getZ()>=low.getZ() &&
				dyn.getX()<=high.getX() && dyn.getY()<=high.getY() && dyn.getZ()<=high.getZ());
	}

	public Location<World> getLow() {
		return low;
	}

	public Location<World> getHigh() {
		return high;
	}

	@Override
	public World getWorld() {
		return low==null?null:low.getExtent();
	}
	
	@Override
	public void serialize(ConfigurationNode cfg) throws ObjectMappingException {
		cfg.setValue(TypeToken.of(BlockZone.class), this);
	}
}
