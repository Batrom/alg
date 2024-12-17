package advanced;

import java.util.HashSet;
import java.util.Set;

record MeetingRoom(Room room, Set<Long> userIds) {

    MeetingRoom(final Room room, final long userId) {
        this(room, MatchingHelper.setOf(userId));
    }

    void addUser(final long userId) {
        userIds.add(userId);
    }

    void removeUser(final long userId) {
        userIds.remove(userId);
    }

    boolean isNotFull() {
        return room.capacity() != userIds.size();
    }

    boolean isNotEmpty() {
        return !userIds.isEmpty();
    }

    MeetingRoom copy() {
        return new MeetingRoom(room, new HashSet<>(userIds));
    }
}
