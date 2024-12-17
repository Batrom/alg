package basic;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    private Long lockedCompany;
    private Long lockedTimeslot;

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
                .filter(Objects::nonNull)
                .toList();
    }

    void match(final long userId, final long companyId) {
        final var success = addUserToExistingMeeting(userId, companyId) || createMeeting(userId, companyId);
        if (!success) shiftPreviousMeetingAndRetry(userId, companyId);
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
                if (Objects.equals(timeslot, lockedTimeslot) && Objects.equals(companyId, lockedCompany)) continue;

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
                if (Objects.equals(timeslot, lockedTimeslot) && Objects.equals(companyId, lockedCompany)) continue;

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
            if (Objects.equals(timeslot, lockedTimeslot) && Objects.equals(companyId, lockedCompany)) continue;

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

    private void shiftPreviousMeetingAndRetry(final long userId, final long companyId) {
        final var success = shiftPreviousMeetingAndRetryForGroupMeeting(userId, companyId);
        if (!success) shiftPreviousMeetingAndRetryForSoloMeeting(userId, companyId);
    }

    private void shiftPreviousMeetingAndRetryForSoloMeeting(final long userId, final long companyId) {
        shiftPreviousMeetingAndRetry(userId, companyId, timeslotsHolder.soloMeetingsTimeslots(userId, companyId));
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
                    final var meeting = findSoloMeeting(timeslot, companyId);
                    if (meeting != null) {
                        removeSoloMeeting(meeting);
                        lockCompanyAndTimeslot(meeting.companyId(), meeting.timeslot());
                        final var otherUserId = meeting.userIds().getFirst();
                        final var otherUserSuccess = joinOrCreateMeeting(otherUserId, companyId);
                        unlockCompanyAndTimeslot();

                        if (otherUserSuccess) {
                            return joinOrCreateMeeting(userId, companyId);
                        } else {
                            joinOrCreateMeeting(otherUserId, companyId);
                        }
                    }
                }
            }
        }
        return false;
    }

    private void lockCompanyAndTimeslot(final long company, final long timeslot) {
        lockedCompany = company;
        lockedTimeslot = timeslot;
    }

    private void unlockCompanyAndTimeslot() {
        lockedTimeslot = null;
        lockedCompany = null;
    }

    private Meeting findSoloMeeting(final long timeslot, final long companyId) {
        final var meetingsMap = soloMeetings.get(timeslot);
        return meetingsMap != null ? meetingsMap.get(companyId) : null;
    }

    private void removeSoloMeeting(final Meeting meeting) {
        roomsHolder.add(meeting.timeslot(), meeting.room());
        soloMeetings.get(meeting.timeslot()).remove(meeting.companyId());
        usersAvailableTimeslots.get(meeting.userIds().getFirst()).add(meeting.timeslot());
        companiesAvailableTimeslots.get(meeting.companyId()).add(meeting.timeslot());
    }

    private boolean canJoinGroupMeeting(final long userId, final Long companyId) {
        return usersThatAllowGroupMeetings.contains(userId) && companiesThatAllowGroupMeetings.contains(companyId);
    }
}