package basic;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;

record RoomsHolder(Map<Long, LinkedList<Room>> timeslotRooms) {

    Room takeRoomForSoloMeeting(final long timeslot) {
        return timeslotRooms.get(timeslot).pollFirst();
    }

    Room takeRoomForGroupMeeting(final long timeslot) {
        return timeslotRooms.get(timeslot).pollLast();
    }

    boolean checkIfThereIsAnyFreeRoom(final long timeslot) {
        return !timeslotRooms.get(timeslot).isEmpty();
    }

    void add(final long timeslot, final Room room) {
        final var rooms = timeslotRooms.get(timeslot);
        rooms.push(room);
        rooms.sort(Comparator.comparing(Room::capacity));
    }
}
