package basic;

import java.util.Deque;
import java.util.Map;

record RoomsHolder(Map<Long, Deque<Room>> timeslotRooms) {

    Room roomForSoloMeeting(final long timeslot) {
        return timeslotRooms.get(timeslot).pollFirst();
    }

    Room roomForGroupMeeting(final long timeslot) {
        return timeslotRooms.get(timeslot).pollLast();
    }
}