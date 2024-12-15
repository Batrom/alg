import java.util.Set;

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

    Set<Meeting> createMeetings() {
        findAllPossibleMeetingsConfigurations();
        return meetingsCreator.createMeetings();
    }

    private void findAllPossibleMeetingsConfigurations() {
        final var collector = new SnapshotsCollector(snapshots);
        while (snapshots.currentMaxIndex() < context.pairs().size()) {
            final var index = snapshots.currentMaxIndex();
            for (final var snapshot : snapshots.snapshots()) {
                collector.submit(() -> new GigaMatcher2000(context, snapshot, index).match());
            }
            collector.collect();
            snapshots.removeDuplicates();
            snapshots.incrementCurrentMaxIndex();
        }
    }
}
