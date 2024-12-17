package basic;

import java.util.ArrayList;
import java.util.List;

record Meeting(long timeslot, long companyId, List<Long> userIds, Room room, boolean solo) {

    static Meeting solo(final long timeslot, final long companyId, final Long userId, final Room room) {
        return new Meeting(timeslot, companyId, List.of(userId), room, true);
    }

    static Meeting group(final long timeslot, final long companyId, final Long userId, final Room room) {
        final var userIds = new ArrayList<Long>();
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