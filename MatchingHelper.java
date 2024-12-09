import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class MatchingHelper {

    static Set<Integer> remove(final Set<Integer> origin, final Integer value) {
        final var set = new HashSet<>(origin);
        set.remove(value);
        return set;
    }

    static <K, V> Map<K, V> update(final Map<K, V> origin, final K key, final V value) {
        final var map = new HashMap<>(origin);
        map.put(key, value);
        return map;
    }

    static int[] add(final int[] origin, final int newValue) {
        final var newArray = Arrays.copyOf(origin, origin.length + 1);
        newArray[newArray.length - 1] = newValue;
        return newArray;
    }
}
