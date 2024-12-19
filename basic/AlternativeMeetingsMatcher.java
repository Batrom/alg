package basic;

import java.util.HashSet;
import java.util.Set;

final class AlternativeMeetingsMatcher extends MeetingsMatcher {

    private AlternativeMeetingsMatcher(final MeetingsMatcherContext context) {
        super(context);
    }

    static AlternativeMeetingsMatcher basedOn(final MeetingsMatcher matcher) {
        return new AlternativeMeetingsMatcher(matcher.context.deepCopy());
    }

    private final Set<Pair> lockedPairs = new HashSet<>();

    @Override
    protected Boolean findAlternatives(final long userId, final Meeting meeting) {
        final var pair = Pair.from(meeting);
        lockPair(pair);
        removeMeeting(meeting);
        if (!rematchMembers(meeting)) return false;

        unlockPair(pair);
        return match(userId, meeting.companyId());
    }

    @Override
    protected boolean pairIsLocked(final long timeslot, final long companyId) {
        return lockedPairs.contains(new Pair(timeslot, companyId));
    }

    private record Pair(long timeslot, long companyId) {
        private static Pair from(final Meeting meeting) {
            return new Pair(meeting.timeslot(), meeting.companyId());
        }
    }

    private boolean rematchMembers(final Meeting meeting) {
        meeting.userIds().sort(context.usersComparator());
        for (final Long userId : meeting.userIds()) {
            if (!match(userId, meeting.companyId())) return false;
        }
        return true;
    }

    private void lockPair(final Pair pair) {
        lockedPairs.add(pair);
    }

    private void unlockPair(final Pair pair) {
        lockedPairs.remove(pair);
    }

    private void removeMeeting(final Meeting meeting) {
        context.roomsHolder().add(meeting.timeslot(), meeting.room());
        (meeting.solo() ? context.soloMeetings() : context.groupMeetings()).get(meeting.timeslot()).remove(meeting.companyId());
        meeting.userIds().stream().map(context.usersAvailableTimeslots()::get).forEach(timeslots -> timeslots.add(meeting.timeslot()));
        context.companiesAvailableTimeslots().get(meeting.companyId()).add(meeting.timeslot());
    }
}
