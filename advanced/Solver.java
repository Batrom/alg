package advanced;

import java.util.List;

class Solver {
    private final Context context;
    private final MeetingsCreator meetingsCreator;
    private final Snapshots snapshots;

    Solver(final Context context,
           final MeetingsCreator meetingsCreator,
           final Snapshots snapshots) {
        this.meetingsCreator = meetingsCreator;
        this.context = context;
        this.snapshots = snapshots;
    }

    List<Meeting> createMeetings() {
        findPossibleMeetingsConfigurations();
        return meetingsCreator.createMeetings();
    }

    private void findPossibleMeetingsConfigurations() {
        while (snapshots.currentMaxIndex() < context.pairs().size()) {
            final var newSnapshots = generateSnapshots();
            snapshots.mergeWith(newSnapshots);
            snapshots.incrementCurrentMaxIndex();
        }
    }

    private List<Snapshots> generateSnapshots() {
        final var index = snapshots.currentMaxIndex();
        final var snapshotCount = snapshots.snapshots().size();
        return snapshots.snapshots()
                .parallelStream()
                .map(snapshot -> new GigaMatcher2000(context, snapshot, index, snapshotCount))
                .map(GigaMatcher2000::match)
                .toList();
    }
}
