import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

class SnapshotsTracker {
    private final Lock snapshotLock;
    private int currentMaxIndex;
    private List<Snapshot> snapshots;

    static SnapshotsTracker initialize(final Map<Integer, Set<Integer>> usersTimeslots,
                                       final Map<Integer, Set<Integer>> companiesTimeslots,
                                       final Map<Integer, Integer> timeslotsRoomsCount) {

        final var initialSnapshot = new Snapshot(Map.of(), usersTimeslots, companiesTimeslots, timeslotsRoomsCount);
        return new SnapshotsTracker(new ArrayList<>(List.of(initialSnapshot)));
    }

    private SnapshotsTracker(final List<Snapshot> snapshots) {
        this.snapshots = snapshots;
        this.currentMaxIndex = 0;
        this.snapshotLock = new ReentrantLock();
    }

    void updateSnapshots(final int index,
                         final Map<Integer, Map<Integer, Meeting>> meetings,
                         final Map<Integer, Set<Integer>> usersTimeslots,
                         final Map<Integer, Set<Integer>> companiesTimeslots,
                         final Map<Integer, Integer> timeslotsRoomsCount) {
        snapshotLock.lock();

        try {
            if (currentMaxIndex < index) snapshots.clear();

            if (currentMaxIndex <= index) {
                currentMaxIndex = index;
                snapshots.add(new Snapshot(meetings, usersTimeslots, companiesTimeslots, timeslotsRoomsCount));
            }
        } finally {
            snapshotLock.unlock();
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
