package advanced;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class MatchingHelper {

    static <T> List<T> listOf(final T obj) {
        final var list = new ArrayList<T>();
        list.add(obj);
        return list;
    }

    static Map<Long, Set<Long>> copyTimeslots(final Map<Long, Set<Long>> original) {
        final var copy = new HashMap<Long, Set<Long>>(original.size());

        for (final var entry : original.entrySet()) {
            final var originalSet = entry.getValue();
            if (!originalSet.isEmpty()) {
                final var newSet = new HashSet<>(originalSet);
                copy.put(entry.getKey(), newSet);
            }
        }

        return copy;
    }

    static Map<Long, Map<Integer, Integer>> copyRooms(final Map<Long, Map<Integer, Integer>> original) {
        final var copy = new HashMap<Long, Map<Integer, Integer>>(original.size());

        for (final var entry : original.entrySet()) {
            var allRoomsCountsAreZero = true;
            final var innerMap = entry.getValue();

            final var innerMapCopy = new HashMap<Integer, Integer>(innerMap.size());
            for (final var innerEntry : innerMap.entrySet()) {
                final var roomsCount = innerEntry.getValue();
                if (roomsCount != 0) {
                    allRoomsCountsAreZero = false;
                    innerMapCopy.put(innerEntry.getKey(), roomsCount);
                }
            }

            if (!allRoomsCountsAreZero) copy.put(entry.getKey(), innerMapCopy);
        }

        return copy;
    }

    static Map<Long, Map<Long, MeetingRoom>> copyMeetings(final Map<Long, Map<Long, MeetingRoom>> original) {
        final var copy = new HashMap<Long, Map<Long, MeetingRoom>>(original.size());

        for (final var entry : original.entrySet()) {
            var allRoomsAreEmpty = true;
            final var innerMap = entry.getValue();

            final var innerMapCopy = new HashMap<Long, MeetingRoom>(innerMap.size());
            for (final var innerEntry : innerMap.entrySet()) {
                final var room = innerEntry.getValue();
                if (room.isNotEmpty()) {
                    allRoomsAreEmpty = false;
                    innerMapCopy.put(innerEntry.getKey(), room.copy());
                }
            }

            if (!allRoomsAreEmpty) copy.put(entry.getKey(), innerMapCopy);
        }

        return copy;
    }

    static <T> boolean isEmpty(final Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }
}
