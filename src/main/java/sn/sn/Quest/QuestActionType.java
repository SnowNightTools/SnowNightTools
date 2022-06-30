package sn.sn.Quest;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SerializableAs("SnQuestActionType")
public enum QuestActionType implements Keyed, Cloneable, Serializable, ConfigurationSerializable {
    COLLECT(1, "collect"),
    CRUSADE(2, "crusade"),
    FIND_NPC(3, "find_npc"),
    FIND_ITEM(4, "find_item"),
    FIND_POSITION(5, "find_position"),
    BUILD(6, "build"),
    ACCOMPLISHMENT(7, "accomplishment"),
    HUSBANDRY(8, "husbandry"),//养殖动物
    AGRICULTURE(9, "agriculture");

    final private int number;
    final private NamespacedKey key;

    QuestActionType(int number, String key) {
        this.number = number;
        this.key = NamespacedKey.fromString(key);
    }

    @Contract(pure = true)
    public static @Nullable
    QuestActionType getFromInt(int number) {
        switch (number) {
            case 1:
                return QuestActionType.COLLECT;
            case 2:
                return QuestActionType.CRUSADE;
            case 3:
                return QuestActionType.FIND_NPC;
            case 4:
                return QuestActionType.FIND_ITEM;
            case 5:
                return QuestActionType.FIND_POSITION;
            case 6:
                return QuestActionType.BUILD;
            case 7:
                return QuestActionType.ACCOMPLISHMENT;
            case 8:
                return QuestActionType.HUSBANDRY;
            case 9:
                return QuestActionType.AGRICULTURE;
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return "QuestActionType{" +
                "number=" + number +
                ", key=" + key +
                '}';
    }

    @Override
    public @NotNull
    NamespacedKey getKey() {
        return key;
    }

    public int getNumber() {
        return number;
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
