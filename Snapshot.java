import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

record Snapshot(
        Map<Integer, Map<Integer, Meeting>> meetings,
        Map<Integer, Set<Integer>> usersTimeslots,
        Map<Integer, Set<Integer>> companiesTimeslots,
        Map<Integer, Integer> timeslotsRoomsCount) {

    Set<Meeting> uniqueMeetings() {
        return meetings
                .values()
                .stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .collect(toSet());
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Snapshot snapshot = (Snapshot) o;
        final var uniqueMeetings = uniqueMeetings();
        final var otherUniquerMeetings = snapshot.uniqueMeetings();
        return uniqueMeetings.size() == otherUniquerMeetings.size() && uniqueMeetings.containsAll(otherUniquerMeetings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueMeetings());
    }
}