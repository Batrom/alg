import java.util.ArrayList;
import java.util.List;

record MeetingRoom(int capacity, List<Long> userIds) {
    MeetingRoom(final int capacity, final long userId) {
        this(capacity, MatchingHelper.listOf(userId));
    }

    void addUser(final long userId) {
        userIds.add(userId);
    }

    boolean isNotFull() {
        return capacity != userIds.size();
    }

    boolean isNotEmpty() {
        return !userIds.isEmpty();
    }

    void removeLast() {
        userIds.removeLast();
    }

    MeetingRoom copy() {
        return new MeetingRoom(capacity, new ArrayList<>(userIds));
    }
}
