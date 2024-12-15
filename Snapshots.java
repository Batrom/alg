import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class Snapshots {
    private int currentMaxIndex;
    private List<Snapshot> snapshots;

    Snapshots(final int currentMaxIndex, final Snapshot snapshot) {
        this.currentMaxIndex = currentMaxIndex;
        this.snapshots = MatchingHelper.listOf(snapshot);
    }

    void mergeWith(final List<Snapshots> otherSnapshotsList) {
        for (final var otherSnapshots : otherSnapshotsList) {
            if (currentMaxIndex < otherSnapshots.currentMaxIndex) snapshots.clear();

            if (currentMaxIndex <= otherSnapshots.currentMaxIndex) {
                currentMaxIndex = otherSnapshots.currentMaxIndex;
                snapshots.addAll(otherSnapshots.snapshots);
            }
        }
    }

    static Snapshots initialize(final Map<Long, Set<Long>> usersTimeslots,
                                final Map<Long, Set<Long>> companiesTimeslots,
                                final Map<Long, Map<Integer, Integer>> timeslotsRooms) {

        final var initialSnapshot = new Snapshot(Map.of(), Map.of(), usersTimeslots, companiesTimeslots, timeslotsRooms);
        return new Snapshots(new ArrayList<>(List.of(initialSnapshot)));
    }

    private Snapshots(final List<Snapshot> snapshots) {
        this.snapshots = snapshots;
        this.currentMaxIndex = 0;
    }

    void updateSnapshots(final int index,
                         final Map<Long, Map<Long, MeetingRoom>> soloMeetings,
                         final Map<Long, Map<Long, MeetingRoom>> groupMeetings,
                         final Map<Long, Set<Long>> usersAvailableTimeslots,
                         final Map<Long, Set<Long>> companiesAvailableTimeslots,
                         final Map<Long, Map<Integer, Integer>> timeslotsFreeRooms) {
        if (currentMaxIndex < index) snapshots.clear();

        if (currentMaxIndex <= index) {
            currentMaxIndex = index;

            snapshots.add(new Snapshot(
                    MatchingHelper.copyMeetings(soloMeetings),
                    MatchingHelper.copyMeetings(groupMeetings),
                    MatchingHelper.copyTimeslots(usersAvailableTimeslots),
                    MatchingHelper.copyTimeslots(companiesAvailableTimeslots),
                    MatchingHelper.copyRooms(timeslotsFreeRooms)));
        }
    }

    void removeDuplicates() {
        // todo fix distinct by hashcode/equals?
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
