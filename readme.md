Zones
=====

With this plugin you are able to receive events whenever a player enters or leaves a certain area. Additionally you plugin can save any type of serializable data in each Zone (be carefull, those data will be written into the Zones config file)...

Here are the admin commands (Permission: dosmike.zones.edit):
- //z <i|info> - List all zones you're in (IDs shift clickable)
- //z <v|verbose> - Toggle verbose chat-messages (spamming the server, off by default)
- //z <1|pos1|from> - Mark the first position for a new zone (Taken at your feet)
- //z <2|pos2|to> - Mark the second position for a new zone (Taken at your head)
- //z <c|create|ass> - Create a new Zone of type BlockZone and output UUID (shift clickable)
- //z <delete|remove> <UUID|*> - Remove zone with given UUID or all Zones you're currently standing in if using *

The API
-----

Include Zones as additional Reference Libaray (like SpongeAPI) and use the static methods in Zones to access the data. API functions explained:

| Signature  | Description  |
|------------|--------------|
| PlayerExtra getPlayer(UUID)  | Retrieve a PlayerExtra for this player's uuid containing Zones related information (see below)  |
| loosePlayer(UUID)  | Internally used to reset a PlayerExtra, used internally, otherwise no big use  |
| Iterator<Zone> getZones()  | Get a iterator to itterate through all zones  |
| Optional<Zone> getZone(UUID)  | Retrieve a Zone by it's UUID. Any references in you Plugin should be using UUIDs  |
| addZone(Zone)  | Add a Zone. Please remember to add (registering global should be the easiest) a custom ZoneSerializer in order for Zones to save it, if you add custom ZoneTypes.  |
| removeZone(? extends Zone)  | Remove ALL Zones of this subtype! - This is handy if you want to "uninstall" your plugin  |
| removeZone(UUID)  | Remove a zone with the given UUID  |
| getZones... | Various functions that probably should be static?  |

The event triggered when a player changes a Zone is called ZoneEvent. For more information on how to listen to events visit the Spongepowered website.

### Event details:

| Signature  | Description  |
|------------|--------------|
| Cause getCause  | Pretty dirty (I recommend you not to use this :s)  |
| Player getTargetEntity  | Receive the player that triggered this event  |
| Zone getZone  | Receive the zone the player entered or left  |
| ZoneEvent.Direction getDirection  | Wether the player entered or left the zone (enum values)  |

### Zones have the following methods:

| Signature  | Description  |
|------------|--------------|
| UUID getID()  | Get the UUID for this Zone  |
| put(String, Serializable)  | Add data to the persitent Zone data map  |
| containsKey(String)  | Check if the data map contains such a key value mapping  |
| remove(String)  | Remove a key value mapping from the data map  |
| get(String)  | Receive a seraializable value for this key  |
| getkeys()  | Receive all keys for the data map  |
| getPlayers()  | Looks up all players currently inside this zone (not cached)  |
| serialize(ConfigurationNode)  | Called by the save function to allow propper usage of the TypeToken  |
| getWorld()  | Returns the world this Zones is valid for  |
| contains(Location<World>)  | Detects if a given location is inside this Zone  |

### By receiving a PlayerExtra you can better filter the Zones a player is currently inside:

| Signature  | Description  |
|------------|--------------|
| boolean isInZone(UUID)  | Check if a player is within this Zone  |
| booelan isInZone(Zone)  | Check if a player is within this Zone  |
| Collection<Zone> getZonesByData(String, Serializable)  | Returns all zones the player is currently in with a existing and matching data mapping  |
| Collection<Zone> getZones()  | Rectieve all Zones the player is currently inside  |

### About saving zones

I recommend you letting the server admin create zones, link them to your plugin with commands you write and then save them with //z save.
If you think that's a bad design choice go ahead and get my plugin instance to call the save Function manually:

```((Zones)(Sponge.getPluginManager().getPlugin("dosmike_zones").get().getInstance().get())).saveZones();```

Be warned tho, this function will always write a confirmation message into chat!
