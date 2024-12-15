import java.util.List;

class Solver {
    private final Context context;
    private final BestMeetingsPicker bestMeetingsPicker;
    private final Snapshots snapshots;

    Solver(final Context context,
           final BestMeetingsPicker bestMeetingsPicker,
           final Snapshots snapshots) {
        this.bestMeetingsPicker = bestMeetingsPicker;
        this.context = context;
        this.snapshots = snapshots;
    }

    List<Meeting> match() {
        findAllPossibleMeetingsConfigurations();
        return bestMeetingsPicker.pickMeetings();
    }

    private void findAllPossibleMeetingsConfigurations() {
        final var collector = new SnapshotsCollector(snapshots);
        while (snapshots.currentMaxIndex() < context.pairs().size()) {
            final var index = snapshots.currentMaxIndex();
            for (final var snapshot : snapshots.snapshots()) {
                collector.submit(() -> new Matcher(context, snapshot, index).match());
            }
            collector.collect();
            snapshots.removeDuplicates();
            snapshots.incrementCurrentMaxIndex();
        }
    }
}
