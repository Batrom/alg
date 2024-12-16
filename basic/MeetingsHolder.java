package basic;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

class MeetingsHolder {
    private final RoomsHolder roomsHolder;
    private final TimeslotsHolder timeslotsHolder;
    private final Map<Long, Map<Long, Meeting>> soloMeetings;
    private final Map<Long, Map<Long, Meeting>> groupMeetings;
    private final Map<Long, Set<Long>> usersAvailableTimeslots;
    private final Map<Long, Set<Long>> companiesAvailableTimeslots;
    private final Set<Long> usersThatAllowGroupMeetings;
    private final Set<Long> companiesThatAllowGroupMeetings;

    MeetingsHolder(final RoomsHolder roomsHolder, final TimeslotsHolder timeslotsHolder, final Map<Long, Map<Long, Meeting>> soloMeetings, final Map<Long, Map<Long, Meeting>> groupMeetings, final Map<Long, Set<Long>> usersAvailableTimeslots, final Map<Long, Set<Long>> companiesAvailableTimeslots, final Set<Long> usersThatAllowGroupMeetings, final Set<Long> companiesThatAllowGroupMeetings) {
        this.roomsHolder = roomsHolder;
        this.timeslotsHolder = timeslotsHolder;
        this.soloMeetings = soloMeetings;
        this.groupMeetings = groupMeetings;
        this.usersAvailableTimeslots = usersAvailableTimeslots;
        this.companiesAvailableTimeslots = companiesAvailableTimeslots;
        this.usersThatAllowGroupMeetings = usersThatAllowGroupMeetings;
        this.companiesThatAllowGroupMeetings = companiesThatAllowGroupMeetings;
    }

    List<Meeting> meetings() {
        return Stream.concat(
                        soloMeetings.values().stream().map(Map::values).flatMap(Collection::stream),
                        groupMeetings.values().stream().map(Map::values).flatMap(Collection::stream))
                .toList();
    }

    boolean addUserToExistingMeeting(final long userId, final long companyId) {
        final var userAvailableTimeslots = usersAvailableTimeslots.get(userId);
        final var canJoinGroupMeeting = canJoinGroupMeeting(userId, companyId);

        if (canJoinGroupMeeting) {
            final var timeslots = timeslotsHolder.groupMeetingsTimeslots(userId, companyId);
            for (final var timeslot : timeslots) {
                if (userAvailableTimeslots.contains(timeslot)) {
                    final var meeting = groupMeetings.get(timeslot).get(companyId);
                    if (meeting != null && meeting.isNotFull()) {
                        userAvailableTimeslots.remove(timeslot);
                        meeting.addUser(userId);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    boolean createMeeting(final long userId, final long companyId) {
        final var userAvailableTimeslots = usersAvailableTimeslots.get(userId);
        final var companyAvailableTimeslots = companiesAvailableTimeslots.get(companyId);
        final var canJoinGroupMeeting = canJoinGroupMeeting(userId, companyId);

        if (canJoinGroupMeeting) {
            final var timeslots = timeslotsHolder.groupMeetingsTimeslots(userId, companyId);
            for (final var timeslot : timeslots) {
                if (userAvailableTimeslots.contains(timeslot) && companyAvailableTimeslots.contains(timeslot)) {
                    final var room = roomsHolder.roomForGroupMeeting(timeslot);
                    if (room != null) {
                        userAvailableTimeslots.remove(timeslot);
                        companyAvailableTimeslots.remove(timeslot);
                        final var meeting = Meeting.group(timeslot, companyId, userId, room);
                        groupMeetings.get(timeslot).put(companyId, meeting);
                        return true;
                    }
                }
            }
        }

        final var timeslots = timeslotsHolder.soloMeetingsTimeslots(userId, companyId);
        for (final var timeslot : timeslots) {
            if (userAvailableTimeslots.contains(timeslot) && companyAvailableTimeslots.contains(timeslot)) {
                final var room = roomsHolder.roomForSoloMeeting(timeslot);
                if (room != null) {
                    userAvailableTimeslots.remove(timeslot);
                    companyAvailableTimeslots.remove(timeslot);
                    final var meeting = Meeting.solo(timeslot, companyId, userId, room);
                    soloMeetings.get(timeslot).put(companyId, meeting);
                    return true;
                }
            }
        }

        return false;
    }

    private boolean canJoinGroupMeeting(final long userId, final Long companyId) {
        return usersThatAllowGroupMeetings.contains(userId) && companiesThatAllowGroupMeetings.contains(companyId);
    }
}