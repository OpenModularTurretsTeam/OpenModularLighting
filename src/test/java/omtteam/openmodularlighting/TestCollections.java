package omtteam.openmodularlighting;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Java 8 stand-ins for {@code List.of}, {@code Set.of} and {@code Map.of} (Java 9+) in the tests, so the test code
 * stays close to the main branch's.
 */
public final class TestCollections {
    private TestCollections() {
    }

    @SafeVarargs
    public static <T> List<T> listOf(T... elements) {
        return Collections.unmodifiableList(Arrays.asList(elements));
    }

    @SafeVarargs
    public static <T> Set<T> setOf(T... elements) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(elements)));
    }

    /** Alternating keys and values. */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> mapOf(Object... keysAndValues) {
        Map<K, V> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keysAndValues.length; i += 2) {
            map.put((K) keysAndValues[i], (V) keysAndValues[i + 1]);
        }
        return Collections.unmodifiableMap(map);
    }
}
