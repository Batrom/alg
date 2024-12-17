package advanced;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

record RoomsHolder(Map<Long, Deque<Room>> timeslotRooms) {

    Room roomForSoloMeeting(final long timeslot) {
        return timeslotRooms.get(timeslot).pollFirst();
    }

    Room roomForGroupMeeting(final long timeslot) {
        return timeslotRooms.get(timeslot).pollLast();
    }

    void addRoomForSoloMeeting(final long timeslot, final Room room) {
        timeslotRooms.get(timeslot).addFirst(room);
    }

    void addRoomForGroupMeeting(final long timeslot, final Room room) {
        timeslotRooms.get(timeslot).addFirst(room);
    }

    RoomsHolder copy() {
        final var copy = new HashMap<Long, Deque<Room>>(timeslotRooms.size());

        for (final var entry : timeslotRooms.entrySet()) {
            copy.put(entry.getKey(), new LinkedList<>(entry.getValue()));
        }

        return new RoomsHolder(copy);
    }
}