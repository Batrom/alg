package advanced;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class Snapshots {
    private int currentMaxIndex;
    private List<Snapshot> snapshots;

    Snapshots(final int currentMaxIndex, final Snapshot snapshot) {
        this.currentMaxIndex = currentMaxIndex;
        this.snapshots = MatchingHelper.listOf(snapshot);
    }

    private Snapshots(final List<Snapshot> snapshots) {
        this.currentMaxIndex = 0;
        this.snapshots = snapshots;
    }

    void mergeWith(final List<Snapshots> otherSnapshotsList) {
        final var initialCurrentMaxIndex = currentMaxIndex;
        for (final var otherSnapshots : otherSnapshotsList) {
            if (initialCurrentMaxIndex == otherSnapshots.currentMaxIndex) continue;

            if (currentMaxIndex < otherSnapshots.currentMaxIndex) snapshots.clear();

            if (currentMaxIndex <= otherSnapshots.currentMaxIndex) {
                currentMaxIndex = otherSnapshots.currentMaxIndex;
                snapshots.addAll(otherSnapshots.snapshots);
            }
        }
        snapshots = snapshots.subList(0, Math.min(1000, snapshots.size()));
    }

    static Snapshots initialize(final Map<Long, Set<Long>> usersTimeslots,
                                final Map<Long, Set<Long>> companiesTimeslots,
                                final Map<Long, Map<Integer, Integer>> timeslotsRooms) {

        final var initialSnapshot = new Snapshot(new HashMap<>(), new HashMap<>(), usersTimeslots, companiesTimeslots, timeslotsRooms);
        return new Snapshots(new ArrayList<>(List.of(initialSnapshot)));
    }

    void updateSnapshots(final int index,
                         final Map<Long, Map<Long, MeetingRoom>> soloMeetings,
                         final Map<Long, Map<Long, MeetingRoom>> groupMeetings,
                         final Map<Long, Set<Long>> usersAvailableTimeslots,
                         final Map<Long, Set<Long>> companiesAvailableTimeslots,
                         final Map<Long, Map<Integer, Integer>> timeslotsAvailableRooms) {
        if (currentMaxIndex < index) snapshots.clear();

        if (currentMaxIndex <= index) {
            currentMaxIndex = index;

            snapshots.add(new Snapshot(
                    MatchingHelper.copyMeetings(soloMeetings),
                    MatchingHelper.copyMeetings(groupMeetings),
                    MatchingHelper.copyTimeslots(usersAvailableTimeslots),
                    MatchingHelper.copyTimeslots(companiesAvailableTimeslots),
                    MatchingHelper.copyRooms(timeslotsAvailableRooms)));
        }
    }

    List<Snapshot> snapshots() {
        return snapshots;
    }

    int currentMaxIndex() {
        return currentMaxIndex;
    }

    void incrementCurrentMaxIndex() {
        currentMaxIndex++;
    }
}
