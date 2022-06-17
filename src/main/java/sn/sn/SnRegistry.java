package sn.sn;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;

public interface SnRegistry<T extends Keyed> extends Iterable<T> {

    /**
     * Sn Setting Type
     *
     * @see quest.SettingType
     */
    SnRegistry<quest.SettingType> SETTINGTYPE = new SimpleSnRegistry<>(quest.SettingType.class);


    /**
     * Sn QuestType
     *
     * @see quest.QuestType
     */
    SnRegistry<quest.QuestType> QUESTTYPE = new SimpleSnRegistry<>(quest.QuestType.class);

    /**
     * Sn QuestType
     *
     * @see quest.QuestActionType
     */
    SnRegistry<quest.QuestActionType> QUESTACTIONTYPE = new SimpleSnRegistry<>(quest.QuestActionType.class);


    /**
     * Get the object by its key.
     *
     * @param key non-null key
     * @return item or null if does not exist
     */
    @Nullable
    T get(@NotNull NamespacedKey key);

    final class SimpleSnRegistry<T extends Enum<T> & Keyed> implements SnRegistry<T> {

        private final Map<NamespacedKey, T> map;

        private SimpleSnRegistry(@NotNull Class<T> type) {
            this(type, t -> true);
        }

        private SimpleSnRegistry(@NotNull Class<T> type, @NotNull Predicate<T> predicate) {
            ImmutableMap.Builder<NamespacedKey, T> builder = ImmutableMap.builder();

            for (T entry : type.getEnumConstants()) {
                if (predicate.test(entry)) {
                    builder.put(entry.getKey(), entry);
                }
            }

            map = builder.build();
        }

        @Nullable
        @Override
        public T get(@NotNull NamespacedKey key) {
            return map.get(key);
        }

        @NotNull
        @Override
        public Iterator<T> iterator() {
            return map.values().iterator();
        }
    }
}
