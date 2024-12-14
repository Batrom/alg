import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class ParallelExecutor {
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final List<Future<?>> futures = new CopyOnWriteArrayList<>();
    private final CountDownLatch latch;

    ParallelExecutor(final int threadsCount) {
        this.latch = new CountDownLatch(threadsCount);
    }

    void submit(final Runnable task) {
        futures.add(executor.submit(task));
        latch.countDown();
    }

    void waitForAll() {
        waitForAllSubmission();
        futures.forEach(ParallelExecutor::tryToGet);
        futures.clear();
    }

    private void waitForAllSubmission() {
        try {
            latch.await();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void tryToGet(final Future<?> future) {
        try {
            future.get();
        } catch (final InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }
}
