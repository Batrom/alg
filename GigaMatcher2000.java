import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class GigaMatcher2000 {
    private int index;
    private final Map<Long, Map<Long, MeetingRoom>> soloMeetings;
    private final Map<Long, Map<Long, MeetingRoom>> groupMeetings;
    private final Map<Long, Set<Long>> usersAvailableTimeslots;
    private final Map<Long, Set<Long>> companiesAvailableTimeslots;
    private final Map<Long, Map<Integer, Integer>> timeslotsFreeRooms;

    private boolean updateSnapshots = false;
    private final Snapshots snapshots;
    private final Context context;

    GigaMatcher2000(final Context context, final Snapshot snapshot, final int index) {
        this.context = context;
        this.index = index;
        this.snapshots = new Snapshots(index, snapshot);
        this.soloMeetings = snapshot.soloMeetings();
        this.groupMeetings = snapshot.groupMeetings();
        this.usersAvailableTimeslots = snapshot.usersAvailableTimeslots();
        this.companiesAvailableTimeslots = snapshot.companiesAvailableTimeslots();
        this.timeslotsFreeRooms = snapshot.timeslotsFreeRooms();
    }

    Snapshots match() {
        matchRecursively();
        return snapshots;
    }

    private void matchRecursively() {
        if (index < context.pairs().size()) {
            final var pair = context.pairs().get(index);
            final var userId = pair.userId();
            final var userTimeslots = context.usersTimeslots().get(userId);
            final var userAvailableTimeslots = usersAvailableTimeslots.get(userId);

            for (final var timeslot : userTimeslots) {
                if (timeslotIsUsed(timeslot, userAvailableTimeslots)) continue;

                addToExistingGroupMeeting(timeslot, pair);
                createNewMeeting(timeslot, pair);
            }
        }
        if (updateSnapshots) {
            snapshots.updateSnapshots(index, soloMeetings, groupMeetings, usersAvailableTimeslots, companiesAvailableTimeslots, timeslotsFreeRooms);
            updateSnapshots = false;
        }
    }

    private static boolean timeslotIsUsed(final Long timeslot, final Set<Long> userAvailableTimeslots) {
        return userAvailableTimeslots == null || !userAvailableTimeslots.contains(timeslot);
    }

    private void addToExistingGroupMeeting(final long timeslot, final Pair pair) {
        final var companyId = pair.companyId();
        final var userId = pair.userId();
        final var meetingRoom = existingGroupMeetingRoom(timeslot, companyId);
        final var canBeGroupMeeting = canBeGroupMeeting(userId, companyId);

        if (canBeGroupMeeting && meetingRoom != null) {
            final var userAvailableTimeslots = usersAvailableTimeslots.get(userId);

            userAvailableTimeslots.remove(timeslot);
            meetingRoom.addUser(userId);
            index++;

            updateSnapshots = true;
            matchRecursively();

            userAvailableTimeslots.add(timeslot);
            meetingRoom.removeLast();
            index--;
        }
    }

    private void createNewMeeting(final long timeslot, final Pair pair) {
        final var companyId = pair.companyId();
        final var userId = pair.userId();
        final var companyAvailableTimeslots = companiesAvailableTimeslots.get(companyId);
        final var userAvailableTimeslots = usersAvailableTimeslots.get(userId);
        final var timeslotRooms = timeslotsFreeRooms.get(timeslot);

        if (companyAvailableTimeslots != null && companyAvailableTimeslots.contains(timeslot) && timeslotRooms != null) {
            for (final var capacityToRoomsCount : timeslotRooms.entrySet()) {
                final var capacity = capacityToRoomsCount.getKey();
                final var roomsCount = capacityToRoomsCount.getValue();
                if (roomsCount == 0) continue;

                companyAvailableTimeslots.remove(timeslot);
                userAvailableTimeslots.remove(timeslot);

                final var meetingRoom = new MeetingRoom(capacity, userId);
                capacityToRoomsCount.setValue(roomsCount - 1);
                index++;

                updateSnapshots = true;
                if (canBeGroupMeeting(userId, companyId)) {
                    newGroupMeeting(timeslot, companyId, meetingRoom);
                } else {
                    newSoloMeeting(timeslot, companyId, meetingRoom);
                }


                companyAvailableTimeslots.add(timeslot);
                userAvailableTimeslots.add(timeslot);
                capacityToRoomsCount.setValue(roomsCount + 1);
                index--;
            }
        }
    }

    private void newSoloMeeting(final long timeslot, final long companyId, final MeetingRoom meetingRoom) {
        final var meetingRooms = timeslotSoloMeetingRooms(timeslot);
        meetingRooms.put(companyId, meetingRoom);

        matchRecursively();

        meetingRooms.remove(companyId);
    }

    private void newGroupMeeting(final long timeslot, final long companyId, final MeetingRoom meetingRoom) {
        final var meetingRooms = timeslotGroupMeetingRooms(timeslot);
        meetingRooms.put(companyId, meetingRoom);

        matchRecursively();

        meetingRooms.remove(companyId);
    }

    private Map<Long, MeetingRoom> timeslotMeetingRooms(final long timeslot, final Map<Long, Map<Long, MeetingRoom>> meetings) {
        final var map = meetings.get(timeslot);
        if (map != null) {
            return map;
        } else {
            final var newMap = new HashMap<Long, MeetingRoom>();
            meetings.put(timeslot, newMap);
            return newMap;
        }
    }

    private Map<Long, MeetingRoom> timeslotSoloMeetingRooms(final long timeslot) {
        return timeslotMeetingRooms(timeslot, soloMeetings);
    }

    private Map<Long, MeetingRoom> timeslotGroupMeetingRooms(final long timeslot) {
        return timeslotMeetingRooms(timeslot, groupMeetings);
    }

    private boolean canBeGroupMeeting(final long userId, final long companyId) {
        return context.usersThatAllowGroupMeetings().contains(userId) && context.companiesThatAllowGroupMeetings().contains(companyId);
    }

    private MeetingRoom existingGroupMeetingRoom(final long timeslot, final long companyId) {
        final var timeslotGroupMeetings = groupMeetings.get(timeslot);
        if (timeslotGroupMeetings != null) {
            final var meetingRoom = timeslotGroupMeetings.get(companyId);
            if (meetingRoom != null && meetingRoom.isNotFull() && meetingRoom.isNotEmpty()) return meetingRoom;
        }
        return null;
    }
}
