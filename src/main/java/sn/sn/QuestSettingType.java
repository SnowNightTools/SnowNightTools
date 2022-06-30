package sn.sn;

import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SerializableAs("SnQuestSettingType")
public enum QuestSettingType implements Keyed, Cloneable, Serializable, ConfigurationSerializable {
    QUEST(1, "quest", Material.BOOK),
    QUESTPOSITION(2, "qusetposition", Material.PINK_GLAZED_TERRACOTTA),
    QUESTACTION(3, "qusetaction", Material.WRITABLE_BOOK),
    QUESTACTIONDATA(4, "qusetactiondata", Material.WRITTEN_BOOK),
    QUESTREWARD(5, "qusetreward", Material.EMERALD),
    QUESTTYPE(6, "questtype", Material.ARROW),
    ENTITY(7, "entity", Material.SPAWNER);

    final int number;
    final NamespacedKey key;
    final Material symbol;


    QuestSettingType(int number, String key, Material symbol) {
        this.number = number;
        this.key = NamespacedKey.fromString(key);
        this.symbol = symbol;
    }

    @Override
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
