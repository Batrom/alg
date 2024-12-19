package basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
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
    private final Comparator<Long> usersComparator;
    private final Set<Pair> lockedPairs = new HashSet<>();

    MeetingsHolder(final RoomsHolder roomsHolder, final TimeslotsHolder timeslotsHolder, final Map<Long, Map<Long, Meeting>> soloMeetings, final Map<Long, Map<Long, Meeting>> groupMeetings, final Map<Long, Set<Long>> usersAvailableTimeslots, final Map<Long, Set<Long>> companiesAvailableTimeslots, final Set<Long> usersThatAllowGroupMeetings, final Set<Long> companiesThatAllowGroupMeetings, final Comparator<Long> usersComparator) {
        this.roomsHolder = roomsHolder;
        this.timeslotsHolder = timeslotsHolder;
        this.soloMeetings = soloMeetings;
        this.groupMeetings = groupMeetings;
        this.usersAvailableTimeslots = usersAvailableTimeslots;
        this.companiesAvailableTimeslots = companiesAvailableTimeslots;
        this.usersThatAllowGroupMeetings = usersThatAllowGroupMeetings;
        this.companiesThatAllowGroupMeetings = companiesThatAllowGroupMeetings;
        this.usersComparator = usersComparator;
    }

    List<Meeting> meetings() {
        return Stream.concat(
                        soloMeetings.values().stream().map(Map::values).flatMap(Collection::stream),
                        groupMeetings.values().stream().map(Map::values).flatMap(Collection::stream))
                .toList();
    }

    boolean match(final long userId, final long companyId) {
        return joinOrCreateMeeting(userId, companyId) || shiftPreviousMeetingAndRetry(userId, companyId);
    }

    private boolean joinOrCreateMeeting(final long userId, final long companyId) {
        return addUserToExistingMeeting(userId, companyId) || createMeeting(userId, companyId);
    }

    private boolean addUserToExistingMeeting(final long userId, final long companyId) {
        final var userAvailableTimeslots = usersAvailableTimeslots.get(userId);
        final var canJoinGroupMeeting = canJoinGroupMeeting(userId, companyId);

        if (canJoinGroupMeeting) {
            final var timeslots = timeslotsHolder.groupMeetingsTimeslots(userId, companyId);
            for (final var timeslot : timeslots) {
                if (lockedPairs.contains(new Pair(companyId, timeslot))) continue;

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

    private boolean createMeeting(final long userId, final long companyId) {
        final var userAvailableTimeslots = usersAvailableTimeslots.get(userId);
        final var companyAvailableTimeslots = companiesAvailableTimeslots.get(companyId);
        final var canJoinGroupMeeting = canJoinGroupMeeting(userId, companyId);

        if (canJoinGroupMeeting) {
            final var timeslots = timeslotsHolder.groupMeetingsTimeslots(userId, companyId);
            for (final var timeslot : timeslots) {
                if (lockedPairs.contains(new Pair(companyId, timeslot))) continue;

                if (userAvailableTimeslots.contains(timeslot) && companyAvailableTimeslots.contains(timeslot)) {
                    final var room = roomsHolder.takeRoomForGroupMeeting(timeslot);
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
            if (lockedPairs.contains(new Pair(companyId, timeslot))) continue;

            if (userAvailableTimeslots.contains(timeslot) && companyAvailableTimeslots.contains(timeslot)) {
                final var room = roomsHolder.takeRoomForSoloMeeting(timeslot);
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

    private boolean shiftPreviousMeetingAndRetry(final long userId, final long companyId) {
        return shiftPreviousMeetingAndRetryForGroupMeeting(userId, companyId)
               || shiftPreviousMeetingAndRetryForSoloMeeting(userId, companyId);
    }

    private boolean shiftPreviousMeetingAndRetryForSoloMeeting(final long userId, final long companyId) {
        return shiftPreviousMeetingAndRetry(userId, companyId, timeslotsHolder.soloMeetingsTimeslots(userId, companyId));
    }

    private boolean shiftPreviousMeetingAndRetryForGroupMeeting(final long userId, final long companyId) {
        final var canJoinGroupMeeting = canJoinGroupMeeting(userId, companyId);
        if (canJoinGroupMeeting) {
            return shiftPreviousMeetingAndRetry(userId, companyId, timeslotsHolder.groupMeetingsTimeslots(userId, companyId));
        }
        return false;
    }

    private boolean shiftPreviousMeetingAndRetry(final long userId, final long companyId, final List<Long> timeslots) {
        final var userAvailableTimeslots = usersAvailableTimeslots.get(userId);
        for (final var timeslot : timeslots) {
            if (userAvailableTimeslots.contains(timeslot)) {
                final var roomExists = roomsHolder.checkIfThereIsAnyFreeRoom(timeslot);
                if (roomExists) {
                    final var meeting = findMeeting(timeslot, companyId);
                    if (meeting != null) {
                        final var pair = Pair.from(meeting);
                        lockPair(pair);
                        removeMeeting(meeting);
                        final var success = rematchMembers(meeting);
                        unlockPair(pair);

                        if (success) {
                            return match(userId, companyId);
                        } else {
                            meeting.userIds().forEach(otherUserId -> match(otherUserId, companyId));
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean rematchMembers(final Meeting meeting) {
        meeting.userIds().sort(usersComparator);
        boolean success;
        for (final Long userId : meeting.userIds()) {
            success = match(userId, meeting.companyId());
            if (!success) return false;
        }
        return true;
    }

    private void lockPair(final Pair pair) {
        lockedPairs.add(pair);
    }

    private void unlockPair(final Pair pair) {
        lockedPairs.remove(pair);
    }

    private Meeting findMeeting(final long timeslot, final long companyId) {
        final var timeslotSoloMeetings = soloMeetings.get(timeslot);
        final Meeting soloMeeting;
        if (timeslotSoloMeetings != null && (soloMeeting = timeslotSoloMeetings.get(companyId)) != null) {
            return soloMeeting;
        } else {
            final var timeslotGroupMeetings = groupMeetings.get(timeslot);
            return timeslotGroupMeetings != null ? timeslotGroupMeetings.get(companyId) : null;
        }
    }

    private void removeMeeting(final Meeting meeting) {
        roomsHolder.add(meeting.timeslot(), meeting.room());
        (meeting.solo() ? soloMeetings : groupMeetings).get(meeting.timeslot()).remove(meeting.companyId());
        usersAvailableTimeslots.get(meeting.userIds().getFirst()).add(meeting.timeslot());
        companiesAvailableTimeslots.get(meeting.companyId()).add(meeting.timeslot());
    }

    private boolean canJoinGroupMeeting(final long userId, final Long companyId) {
        return usersThatAllowGroupMeetings.contains(userId) && companiesThatAllowGroupMeetings.contains(companyId);
    }

    private record Pair(long companyId, long timeslot) {
        private static Pair from(final Meeting meeting) {
            return new Pair(meeting.companyId(), meeting.timeslot());
        }
    }
}
