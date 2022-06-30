package sn.sn.Quest;

import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SerializableAs("SnQuestType")
public enum QuestType implements Cloneable, Serializable, ConfigurationSerializable, Keyed {
    MAIN(1, "main", Material.RAIL),
    SIDE(2, "side", Material.POWERED_RAIL),
    TRIGGER(3, "trigger", Material.ACTIVATOR_RAIL),
    DAILY(4, "daily", Material.DETECTOR_RAIL),
    REWARD(5, "reward", Material.EMERALD),
    DIY(6, "diy", Material.WOODEN_PICKAXE);

    final private int number;
    final private NamespacedKey key;
    final private Material symbol;

    QuestType(int number, String key, Material symbol) {
        this.number = number;
        this.key = NamespacedKey.fromString(key);
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return "QuestType{" +
                "number=" + number +
                ", key=" + key +
                ", symbol=" + symbol +
                '}';
    }

    public @NotNull
    NamespacedKey getKey() {
        return key;
    }

    public int getNumber() {
        return number;
    }

    public Material getSymbol() {
        return symbol;
    }

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
        Map<String, Object> tmp = new HashMap<>();
        tmp.put("name", this.key);
        return tmp;
    }
}
