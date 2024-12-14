import java.util.Map;
import java.util.Set;

class Matcher2 {
    private int index;
    private final Map<Integer, Map<Integer, Meeting2>> meetings; //todo initialize empty Map (each timeslot of company with empty map of meetings)
    private final Map<Integer, Set<Integer>> usersTimeslots;
    private final Map<Integer, Set<Integer>> usersUsedTimeslots;
    private final Map<Integer, Set<Integer>> companiesAvailableTimeslots;
    private final Map<Integer, Integer> timeslotsRoomsCount;

    private final SnapshotsTracker2 snapshotsTracker;
    private final MatcherContext context;
    private final BestMeetingsPicker bestMeetingsPicker;

    Matcher2(final SnapshotsTracker2 snapshotsTracker,
             final MatcherContext context,
             final BestMeetingsPicker bestMeetingsPicker) {
        this.snapshotsTracker = snapshotsTracker;
        this.bestMeetingsPicker = bestMeetingsPicker;
        this.context = context;
    }

    private void match() {
        if (index < context.pairs().size()) {
            final var pair = context.pairs().get(index);
            final var userId = pair.userId();
            final var companyId = pair.companyId();
            final var userTimeslots = usersTimeslots.get(userId);
            final var userUsedTimeslots = usersUsedTimeslots.get(userId);
            final var companyTimeslots = companiesAvailableTimeslots.get(companyId);

            for (final var timeslot : userTimeslots) {
                if (userUsedTimeslots.contains(timeslot)) continue;

                final var timeslotMeetings = meetings.get(timeslot);
                final var existingGroupMeeting = findExistingGroupMeeting(timeslotMeetings, companyId);
                final var canBeGroupMeeting = canBeGroupMeeting(userId, companyId);
                final int roomsCount = timeslotsRoomsCount.get(timeslot);

                if (canBeGroupMeeting && existingGroupMeeting != null) {
                    userUsedTimeslots.add(timeslot);
                    existingGroupMeeting.addUser(userId);
                    //todo implement rooms
                    index++;

                    match();

                    userUsedTimeslots.remove(timeslot);
                    existingGroupMeeting.removeUser(userId);
                    //todo implement rooms
                    index--;
                }

                if (roomsCount > 0 && companyTimeslots.contains(timeslot)) {
                    userUsedTimeslots.add(timeslot);
                    if (canBeGroupMeeting) {
                        final var meeting = Meeting2.group(userId, companyId, timeslot);
                        timeslotMeetings.put(companyId, meeting);

                    } else {
                        final var meeting = Meeting2.solo(userId, companyId, timeslot);
                        timeslotMeetings.put(companyId, meeting);
                        companyTimeslots.remove(timeslot);
                    }

                    //todo implement rooms

                    index++;

                    match();

                    userUsedTimeslots.remove(timeslot);
                    timeslotMeetings.remove(companyId);
                    //todo implement rooms
                    index--;
                }
            }
            snapshotsTracker.updateSnapshots(index, meetings, usersTimeslots, companiesTimeslots, timeslotsRoomsCount);
        }
        snapshotsTracker.updateSnapshots(index - 1, meetings, usersTimeslots, companiesTimeslots, timeslotsRoomsCount);
    }

    private static Meeting2 findExistingGroupMeeting(final Map<Integer, Meeting2> timeslotMeetings, final int companyId) {
        final var meeting = timeslotMeetings.get(companyId);
        return (meeting != null && meeting.allowGroups()) ? meeting : null;
    }

    private boolean canBeGroupMeeting(final int userId, final int companyId) {
        return context.usersThatAllowGroupMeetings().contains(userId) && context.usersThatAllowGroupMeetings().contains(companyId);
    }
}
