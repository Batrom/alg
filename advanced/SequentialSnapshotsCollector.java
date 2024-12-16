package advanced;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

class SequentialSnapshotsCollector {
    private final List<Snapshots> allSnapshots = new ArrayList<>();
    private final Snapshots initialSnapshots;

    SequentialSnapshotsCollector(final Snapshots initialSnapshots) {
        this.initialSnapshots = initialSnapshots;
    }

    void submit(final Supplier<Snapshots> task) {
        allSnapshots.add(task.get());
    }

    void collect() {
        initialSnapshots.mergeWith(allSnapshots);
    }
}
