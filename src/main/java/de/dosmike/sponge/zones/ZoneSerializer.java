package de.dosmike.sponge.zones;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.BiConsumer;

import com.google.common.reflect.TypeToken;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

public abstract class ZoneSerializer<T extends Zone> implements TypeSerializer<T> {
	/** convert any serializable into a string */
	private String serser(Serializable o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}
	/** convert serialized string data into a class object */
	private Object serdes(String s) throws IOException, ClassNotFoundException {
		byte[] bdata = Base64.getDecoder().decode(s);
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bdata));
		Object o = ois.readObject();
		ois.close();
		return o;
	}
	
	@Override
	public void serialize(TypeToken<?> token, T zone, ConfigurationNode cfg) throws ObjectMappingException {
		cfg.getNode("class").setValue(zone.getClass().getName());
		cfg.getNode("UUID").setValue(TypeToken.of(UUID.class), zone.zoneid);
		ConfigurationNode data = cfg.getNode("Data");
		for (Entry<String, Serializable> e : zone.data.entrySet()) {
			try {
				data.getNode(e.getKey()).setValue(serser(e.getValue()));
			} catch (IOException ex) {
				throw new ObjectMappingException("Unable to write data for '"+e.getKey()+"'", ex);
			}
		}
		serialize(zone, cfg);
	}
	@Override
	public T deserialize(TypeToken<?> token, ConfigurationNode cfg) throws ObjectMappingException {
		try {
			UUID uid = UUID.fromString(cfg.getNode("UUID").getString());
			Map<String, Serializable> data = new HashMap<>();
			ConfigurationNode dn = cfg.getNode("Data");
			/*for (ConfigurationNode n : dn.getChildrenList()) {
				data.put((String)n.getKey(), (Serializable)serdes(n.getString()));
			}*/
			dn.getChildrenMap().forEach(new BiConsumer<Object, ConfigurationNode>() {
				@Override
				public void accept(Object t, ConfigurationNode u) {
					try {
						data.put((String)t, (Serializable)serdes(u.getString()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			
			T res = deserialize(cfg);
			res.zoneid=uid;
			res.data=data;
			return res;
		} catch (Exception e) {
			throw new ObjectMappingException("Failed at restoring zone", e);
		}
	}
	
	/** serialize zone sub-class data. UUID and data are already done */
	public abstract void serialize(T z, ConfigurationNode cfg) throws ObjectMappingException;
	/** create and return a zone sub-class instance with sub-class data restored */
	public abstract T deserialize(ConfigurationNode cfg) throws ObjectMappingException;
}
