import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class SnapshotsTracker2 {
    private int currentMaxIndex;
    private List<Snapshot> snapshots;

    static SnapshotsTracker2 initialize(final Map<Integer, Set<Integer>> usersTimeslots,
                                        final Map<Integer, Set<Integer>> companiesTimeslots,
                                        final Map<Integer, Integer> timeslotsRoomsCount) {

        final var initialSnapshot = new Snapshot(Map.of(), usersTimeslots, companiesTimeslots, timeslotsRoomsCount);
        return new SnapshotsTracker2(new ArrayList<>(List.of(initialSnapshot)));
    }

    private SnapshotsTracker2(final List<Snapshot> snapshots) {
        this.snapshots = snapshots;
        this.currentMaxIndex = 0;
    }

    void updateSnapshots(final int index,
                         final Map<Integer, Map<Integer, Meeting2>> meetings,
                         final Map<Integer, Set<Integer>> usersTimeslots,
                         final Map<Integer, Set<Integer>> companiesTimeslots,
                         final Map<Integer, Integer> timeslotsRoomsCount) {
        if (currentMaxIndex < index) snapshots.clear();

        if (currentMaxIndex <= index) {
            currentMaxIndex = index;

            //todo deep copy of all of the fields

            snapshots.add(new Snapshot(meetings, usersTimeslots, companiesTimeslots, timeslotsRoomsCount));
        }
    }

    void removeDuplicates() {
        snapshots = snapshots.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
    }

    List<Snapshot> snapshots() {
        return snapshots;
    }

    int currentMaxIndex() {
        return currentMaxIndex;
    }

    void incrementCurrentMaxIndex() {
        currentMaxIndex += 2;
    }
}
