package advanced;

import java.util.ArrayList;
import java.util.List;

record MeetingRoom(Room room, List<Long> userIds) {

    MeetingRoom(final Room room, final long userId) {
        this(room, MatchingHelper.listOf(userId));
    }

    void addUser(final long userId) {
        userIds.add(userId);
    }

    boolean isNotFull() {
        return room.capacity() != userIds.size();
    }

    boolean isNotEmpty() {
        return !userIds.isEmpty();
    }

    void removeLast() {
        userIds.removeLast();
    }

    MeetingRoom copy() {
        return new MeetingRoom(room, new ArrayList<>(userIds));
    }
}
