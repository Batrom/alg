package advanced;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class GigaMatcher2000 {
    private int index;
    private final Map<Long, Map<Long, MeetingRoom>> soloMeetings;
    private final Map<Long, Map<Long, MeetingRoom>> groupMeetings;
    private final Map<Long, Set<Long>> usersAvailableTimeslots;
    private final Map<Long, Set<Long>> companiesAvailableTimeslots;
    private final RoomsHolder roomsHolder;

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
        this.roomsHolder = snapshot.roomsHolder();
    }

    Snapshots match() {
        matchRecursively();
        return snapshots;
    }

    private void matchRecursively() {
        if (index < context.pairs().size()) {
            final var pair = context.pairs().get(index);
            final var userId = pair.userId();
            final var companyId = pair.companyId();
            final var userAvailableTimeslots = usersAvailableTimeslots.get(userId);
            final var canBeGroupMeeting = canBeGroupMeeting(userId, companyId);
            var timeslotsCounter = 0;

            if (canBeGroupMeeting) {
                final var timeslots = context.timeslotsHolder().groupMeetingsTimeslots(userId, companyId);

                for (final var timeslot : timeslots) {
                    if (timeslotIsUsed(timeslot, userAvailableTimeslots)) continue;

                    final boolean success = joinExistingGroupMeeting(timeslot, pair) || createNewGroupMeeting(timeslot, pair);
                    if (success) timeslotsCounter++;
                    if (timeslotsCounter > 10) break;
                }

            } else {
                final var timeslots = context.timeslotsHolder().soloMeetingsTimeslots(userId, companyId);

                for (final var timeslot : timeslots) {
                    if (timeslotIsUsed(timeslot, userAvailableTimeslots)) continue;

                    final boolean success = createNewSoloMeeting(timeslot, pair);
                    if (success) timeslotsCounter++;
                    if (timeslotsCounter > 10) break;
                }
            }
        }
        if (updateSnapshots) {
            snapshots.updateSnapshots(index, soloMeetings, groupMeetings, usersAvailableTimeslots, companiesAvailableTimeslots, roomsHolder);
            updateSnapshots = false;
        }
    }

    private static boolean timeslotIsUsed(final Long timeslot, final Set<Long> userAvailableTimeslots) {
        return userAvailableTimeslots == null || !userAvailableTimeslots.contains(timeslot);
    }

    private boolean joinExistingGroupMeeting(final long timeslot, final Pair pair) {
        final var companyId = pair.companyId();
        final var userId = pair.userId();
        final var meetingRoom = existingGroupMeetingRoom(timeslot, companyId);

        if (meetingRoom != null) {
            final var userAvailableTimeslots = usersAvailableTimeslots.get(userId);

            userAvailableTimeslots.remove(timeslot);
            meetingRoom.addUser(userId);
            index++;

            updateSnapshots = true;
            matchRecursively();

            userAvailableTimeslots.add(timeslot);
            meetingRoom.removeLast();
            index--;

            return true;
        }
        return false;
    }

    private boolean createNewSoloMeeting(final long timeslot, final Pair pair) {
        final var companyId = pair.companyId();
        final var userId = pair.userId();
        final var companyAvailableTimeslots = companiesAvailableTimeslots.get(companyId);
        final var userAvailableTimeslots = usersAvailableTimeslots.get(userId);

        if (companyAvailableTimeslots != null && companyAvailableTimeslots.contains(timeslot)) {
            final var room = roomsHolder.roomForSoloMeeting(timeslot);
            if (room != null) {

                companyAvailableTimeslots.remove(timeslot);
                userAvailableTimeslots.remove(timeslot);
                final var meetingRoom = new MeetingRoom(room, userId);
                index++;

                newSoloMeeting(timeslot, companyId, meetingRoom);

                companyAvailableTimeslots.add(timeslot);
                userAvailableTimeslots.add(timeslot);
                roomsHolder.addRoomForSoloMeeting(timeslot, room);
                index--;
                return true;

            }
        }
        return false;
    }

    private boolean createNewGroupMeeting(final long timeslot, final Pair pair) {
        final var companyId = pair.companyId();
        final var userId = pair.userId();
        final var companyAvailableTimeslots = companiesAvailableTimeslots.get(companyId);
        final var userAvailableTimeslots = usersAvailableTimeslots.get(userId);

        if (companyAvailableTimeslots != null && companyAvailableTimeslots.contains(timeslot)) {
            final var room = roomsHolder.roomForGroupMeeting(timeslot);
            if (room != null) {

                companyAvailableTimeslots.remove(timeslot);
                userAvailableTimeslots.remove(timeslot);
                final var meetingRoom = new MeetingRoom(room, userId);
                index++;

                newGroupMeeting(timeslot, companyId, meetingRoom);

                companyAvailableTimeslots.add(timeslot);
                userAvailableTimeslots.add(timeslot);
                roomsHolder.addRoomForGroupMeeting(timeslot, room);
                index--;
                return true;

            }
        }
        return false;
    }

    private void newSoloMeeting(final long timeslot, final long companyId, final MeetingRoom meetingRoom) {
        final var meetingRooms = timeslotSoloMeetingRooms(timeslot);
        meetingRooms.put(companyId, meetingRoom);

        updateSnapshots = true;
        matchRecursively();

        meetingRooms.remove(companyId);
    }

    private void newGroupMeeting(final long timeslot, final long companyId, final MeetingRoom meetingRoom) {
        final var meetingRooms = timeslotGroupMeetingRooms(timeslot);
        meetingRooms.put(companyId, meetingRoom);

        updateSnapshots = true;
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
