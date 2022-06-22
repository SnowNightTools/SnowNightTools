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
     * @see Quest_CE.SettingType
     */
    SnRegistry<Quest_CE.SettingType> SETTINGTYPE = new SimpleSnRegistry<>(Quest_CE.SettingType.class);


    /**
     * Sn QuestType
     *
     * @see Quest_CE.QuestType
     */
    SnRegistry<Quest_CE.QuestType> QUESTTYPE = new SimpleSnRegistry<>(Quest_CE.QuestType.class);

    /**
     * Sn QuestType
     *
     * @see Quest_CE.QuestActionType
     */
    SnRegistry<Quest_CE.QuestActionType> QUESTACTIONTYPE = new SimpleSnRegistry<>(Quest_CE.QuestActionType.class);


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
