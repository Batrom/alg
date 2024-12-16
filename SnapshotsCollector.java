import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class SnapshotsCollector {
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final Queue<Future<Snapshots>> futures = new ConcurrentLinkedQueue<>();
    private final CountDownLatch latch;
    private final Snapshots initialSnapshots;

    SnapshotsCollector(final Snapshots initialSnapshots) {
        this.latch = new CountDownLatch(initialSnapshots.snapshots().size());
        this.initialSnapshots = initialSnapshots;
    }

    void submit(final Callable<Snapshots> task) {
        futures.add(executor.submit(task));
    }

    void collect() {
        initialSnapshots.mergeWith(otherSnapshots());
        waitForAllSubmission();
        futures.clear();
    }

    private List<Snapshots> otherSnapshots() {
        return futures.stream().map(this::tryToGet).toList();
    }

    private void waitForAllSubmission() {
        try {
            latch.await();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Snapshots tryToGet(final Future<Snapshots> future) {
        try {
            final var snapshots = future.get();
            latch.countDown();
            return snapshots;
        } catch (final InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }
}
