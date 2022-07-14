package sn.sn.Npc;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Memory implements ConfigurationSerializable {
/*
    final List<Event> eventlist;
    final Map<Event , Time> event_time;
    final Map<Event , Integer> event_level;
    final Map<Event, Integer> event_favorability;

    final List<Npc> peoplelist;
    final Map<Npc, Intege/* people_favorability;
    final Map<Npc, Characters> people_impressions;

    final List<Player> playerlist;
    final Map<Player, Integer> player_favorability;
    final Map<Player, Characters> player_impressions;

    final List<ItemStack> itemlist;
    final Map<ItemStack, Integer> item_favorability;

    final List<Location> locationlist;
    final Map<Location, Integer> location_favorability;


*/


    /**
     * Creates a Map representation of this class.
     * <p>
     * This class must provide a method to restore this class, as defined in
     * the {@link ConfigurationSerializable} interface javadocs.
     *
     * @return Map containing the current state of this class
     */
    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return null;
    }
}
