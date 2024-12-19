package basic;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

sealed abstract class MeetingsMatcher permits DefaultMeetingsMatcher, AlternativeMeetingsMatcher {
    protected MeetingsMatcherContext context;

    protected MeetingsMatcher(final MeetingsMatcherContext context) {
        this.context = context;
    }

    abstract protected Boolean findAlternatives(final long userId, final Meeting meeting);

    abstract protected boolean pairIsLocked(final long timeslot, final long companyId);

    List<Meeting> meetings() {
        return Stream.concat(
                        context.soloMeetings().values().stream().map(Map::values).flatMap(Collection::stream),
                        context.groupMeetings().values().stream().map(Map::values).flatMap(Collection::stream))
                .toList();
    }

    boolean match(final long userId, final long companyId) {
        return createOrJoinMeeting(userId, companyId) || shiftPreviousMeetingAndRetry(userId, companyId);
    }

    private boolean createOrJoinMeeting(final long userId, final long companyId) {
        return createMeeting(userId, companyId) || addUserToExistingMeeting(userId, companyId);
    }

    private boolean addUserToExistingMeeting(final long userId, final long companyId) {
        final var userAvailableTimeslots = context.usersAvailableTimeslots().get(userId);
        final var canJoinGroupMeeting = canJoinGroupMeeting(userId, companyId);

        if (canJoinGroupMeeting) {
            final var timeslots = context.timeslotsHolder().groupMeetingsTimeslots(userId, companyId);
            for (final var timeslot : timeslots) {
                if (pairIsLocked(timeslot, companyId)) continue;

                if (userAvailableTimeslots.contains(timeslot)) {
                    final var meeting = context.groupMeetings().get(timeslot).get(companyId);
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
        final var userAvailableTimeslots = context.usersAvailableTimeslots().get(userId);
        final var companyAvailableTimeslots = context.companiesAvailableTimeslots().get(companyId);

        // try to create a new solo meeting
        final var soloTimeslots = context.timeslotsHolder().soloMeetingsTimeslots(userId, companyId);
        for (final var timeslot : soloTimeslots) {
            if (pairIsLocked(timeslot, companyId)) continue;

            if (userAvailableTimeslots.contains(timeslot) && companyAvailableTimeslots.contains(timeslot)) {
                final var room = context.roomsHolder().takeRoomForSoloMeeting(timeslot);
                if (room != null) {
                    userAvailableTimeslots.remove(timeslot);
                    companyAvailableTimeslots.remove(timeslot);
                    final var meeting = Meeting.solo(timeslot, companyId, userId, room);
                    context.soloMeetings().get(timeslot).put(companyId, meeting);
                    return true;
                }
            }
        }

        // try to create a new group meeting
        if (canJoinGroupMeeting(userId, companyId)) {
            final var groupTimeslots = context.timeslotsHolder().groupMeetingsTimeslots(userId, companyId);
            for (final var timeslot : groupTimeslots) {
                if (pairIsLocked(timeslot, companyId)) continue;

                if (userAvailableTimeslots.contains(timeslot) && companyAvailableTimeslots.contains(timeslot)) {
                    final var room = context.roomsHolder().takeRoomForGroupMeeting(timeslot);
                    if (room != null) {
                        userAvailableTimeslots.remove(timeslot);
                        companyAvailableTimeslots.remove(timeslot);
                        final var meeting = Meeting.group(timeslot, companyId, userId, room);
                        context.groupMeetings().get(timeslot).put(companyId, meeting);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean shiftPreviousMeetingAndRetry(final long userId, final long companyId) {
        return shiftPreviousMeetingAndRetryForSoloMeeting(userId, companyId) || shiftPreviousMeetingAndRetryForGroupMeeting(userId, companyId);
    }

    private boolean shiftPreviousMeetingAndRetryForSoloMeeting(final long userId, final long companyId) {
        return shiftPreviousMeetingAndRetry(userId, companyId, context.timeslotsHolder().soloMeetingsTimeslots(userId, companyId));
    }

    private boolean shiftPreviousMeetingAndRetryForGroupMeeting(final long userId, final long companyId) {
        if (canJoinGroupMeeting(userId, companyId)) {
            return shiftPreviousMeetingAndRetry(userId, companyId, context.timeslotsHolder().groupMeetingsTimeslots(userId, companyId));
        }
        return false;
    }

    private boolean shiftPreviousMeetingAndRetry(final long userId, final long companyId, final List<Long> timeslots) {
        final var userAvailableTimeslots = context.usersAvailableTimeslots().get(userId);
        for (final var timeslot : timeslots) {
            if (userAvailableTimeslots.contains(timeslot)) {
                final var meeting = findMeeting(timeslot, companyId);
                if (meeting != null) {
                    final var result = findAlternatives(userId, meeting);
                    if (result != null) return result;
                }
            }
        }
        return false;
    }

    private Meeting findMeeting(final long timeslot, final long companyId) {
        final var timeslotSoloMeetings = context.soloMeetings().get(timeslot);
        final Meeting soloMeeting;
        if (timeslotSoloMeetings != null && (soloMeeting = timeslotSoloMeetings.get(companyId)) != null) {
            return soloMeeting;
        } else {
            final var timeslotGroupMeetings = context.groupMeetings().get(timeslot);
            return timeslotGroupMeetings != null ? timeslotGroupMeetings.get(companyId) : null;
        }
    }

    private boolean canJoinGroupMeeting(final long userId, final Long companyId) {
        return context.usersThatAllowGroupMeetings().contains(userId) && context.companiesThatAllowGroupMeetings().contains(companyId);
    }
}
