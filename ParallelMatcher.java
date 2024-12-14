import java.util.List;
import java.util.Map;
import java.util.Set;

class ParallelMatcher {
    private final SnapshotsTracker snapshotsTracker;
    private final MatcherContext context;
    private final BestMeetingsPicker bestMeetingsPicker;

    ParallelMatcher(final SnapshotsTracker snapshotsTracker,
                    final MatcherContext context,
                    final BestMeetingsPicker bestMeetingsPicker) {
        this.snapshotsTracker = snapshotsTracker;
        this.bestMeetingsPicker = bestMeetingsPicker;
        this.context = context;
    }

    List<Meeting> match() {
        findAllPossibleMeetingsConfigurations();
        return bestMeetingsPicker.pickMeetings();
    }

    private void findAllPossibleMeetingsConfigurations() {
        System.out.println(context.pairs().size());
        while (snapshotsTracker.currentMaxIndex() < context.pairs().size()) {
            final var index = snapshotsTracker.currentMaxIndex();
            final var copyOfSnapshots = List.copyOf(snapshotsTracker.snapshots());
            final var executor = new ParallelExecutor(copyOfSnapshots.size());
            for (final var snapshot : copyOfSnapshots) {
                executor.submit(() -> matchRecursively(index, snapshot.meetings(), snapshot.usersTimeslots(), snapshot.companiesTimeslots(), snapshot.timeslotsRoomsCount()));
            }

            executor.waitForAll();
            snapshotsTracker.removeDuplicates();
            snapshotsTracker.incrementCurrentMaxIndex();
        }
    }

    private void matchRecursively(final int index,
                                  final Map<Integer, Map<Integer, Meeting>> meetings,
                                  final Map<Integer, Set<Integer>> usersTimeslots,
                                  final Map<Integer, Set<Integer>> companiesTimeslots,
                                  final Map<Integer, Integer> timeslotsRoomsCount) {

        if (index < context.pairs().size()) {
            final var pair = context.pairs().get(index);
            final var userId = pair.userId();
            final var companyId = pair.companyId();
            final var userTimeslots = usersTimeslots.get(userId);
            final var companyTimeslots = companiesTimeslots.get(companyId);

            for (final var timeslot : userTimeslots) {
                final var timeslotMeetings = meetings.get(timeslot);
                final var existingGroupMeeting = findExistingGroupMeeting(timeslotMeetings, companyId);
                final var canAttendGroupMeeting = canAttendGroupMeeting(userId, companyId);
                final int roomsCount = timeslotsRoomsCount.get(timeslot);
                final var nextIndex = index + 1;

                if (canAttendGroupMeeting && existingGroupMeeting != null) {
                    final var updatedUsersTimeslots = MatchingHelper.update(usersTimeslots, userId, MatchingHelper.remove(userTimeslots, timeslot));
                    final var updatedMeeting = existingGroupMeeting.addUser(userId);
                    final var updatedTimeslotMeetings = MatchingHelper.update(timeslotMeetings, companyId, updatedMeeting);
                    final var updatedMeetings = MatchingHelper.update(meetings, timeslot, updatedTimeslotMeetings);

                    matchRecursively(nextIndex, updatedMeetings, updatedUsersTimeslots, companiesTimeslots, timeslotsRoomsCount);
                }

                if (roomsCount > 0 && companyTimeslots.contains(timeslot)) {
                    final var updatedUsersTimeslots = MatchingHelper.update(usersTimeslots, userId, MatchingHelper.remove(userTimeslots, timeslot));
                    final var updatedCompaniesTimeslots = canAttendGroupMeeting ? companiesTimeslots : MatchingHelper.update(companiesTimeslots, companyId, MatchingHelper.remove(companyTimeslots, timeslot));
                    final var updatedTimeslotsRoomsCount = MatchingHelper.update(timeslotsRoomsCount, timeslot, roomsCount - 1);

                    final int[] users = {userId};
                    final var newMeeting = new Meeting(users, companyId, timeslot, canAttendGroupMeeting);
                    final var updatedTimeslotMeetings = timeslotMeetings != null ? MatchingHelper.update(timeslotMeetings, companyId, newMeeting) : Map.of(companyId, newMeeting);
                    final var updatedMeetings = MatchingHelper.update(meetings, timeslot, updatedTimeslotMeetings);

                    matchRecursively(nextIndex, updatedMeetings, updatedUsersTimeslots, updatedCompaniesTimeslots, updatedTimeslotsRoomsCount);
                }
            }
            snapshotsTracker.updateSnapshots(index, meetings, usersTimeslots, companiesTimeslots, timeslotsRoomsCount);
        }
        snapshotsTracker.updateSnapshots(index - 1, meetings, usersTimeslots, companiesTimeslots, timeslotsRoomsCount);
    }

    private static Meeting findExistingGroupMeeting(final Map<Integer, Meeting> timeslotMeetings, final int companyId) {
        if (timeslotMeetings != null) {
            final var meeting = timeslotMeetings.get(companyId);
            return (meeting != null && meeting.allowGroups()) ? meeting : null;
        }
        return null;
    }

    private boolean canAttendGroupMeeting(final int userId, final int companyId) {
        return context.usersThatAllowGroupMeetings().contains(userId) && context.usersThatAllowGroupMeetings().contains(companyId);
    }
}
