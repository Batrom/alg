package basic;

import java.util.HashSet;
import java.util.Set;

record Meeting(long timeslot, long companyId, Set<Long> userIds, Room room, boolean solo) {

    static Meeting solo(final long timeslot, final long companyId, final Long userId, final Room room) {
        return new Meeting(timeslot, companyId, Set.of(userId), room, true);
    }

    static Meeting group(final long timeslot, final long companyId, final Long userId, final Room room) {
        final var userIds = new HashSet<Long>();
        userIds.add(userId);
        return new Meeting(timeslot, companyId, userIds, room, false);
    }

    void addUser(final long userId) {
        userIds.add(userId);
    }

    boolean isNotFull() {
        return room.capacity() > userIds.size();
    }
}