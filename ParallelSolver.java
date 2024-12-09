import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

class ParallelSolver {

    static List<Meeting> solve(final List<Main.User> users, final List<Main.Company> companies, final List<Main.Timeslot> timeslots) {
        final var pairs = createPairs(users);
        final var usersThatAllowGroupMeetings = users.stream().filter(Main.User::allowGroupMeetings).map(Main.User::id).collect(toSet());
        final var companiesThatAllowGroupMeetings = companies.stream().filter(Main.Company::allowGroupMeetings).map(Main.Company::id).collect(toSet());
        final var usersTimeslots = users.stream().collect(toMap(Main.User::id, Main.User::timeslots));
        final var companiesTimeslots = companies.stream().collect(toMap(Main.Company::id, Main.Company::timeslots));
        final var timeslotsRoomsCount = timeslots.stream().collect(toMap(Main.Timeslot::id, Main.Timeslot::roomsCount));
        final var matcher = new Matcher(usersThatAllowGroupMeetings, companiesThatAllowGroupMeetings, pairs);

        return matcher.match(usersTimeslots, companiesTimeslots, timeslotsRoomsCount);
    }

    private static List<Pair> createPairs(List<Main.User> users) {
        return users.stream()
                .sorted(Comparator.comparing(Main.User::order))
                .flatMap(user -> user.companies().stream().map(companyId -> new Pair(user.id(), companyId)))
                .toList();
    }

    static class Matcher {
        private final Lock lock = new ReentrantLock();
        private final Set<Integer> usersThatAllowGroupMeetings;
        private final Set<Integer> companiesThatAllowGroupMeetings;
        private final List<Pair> pairs;

        private List<Snapshot> snapshots = new ArrayList<>();
        private int currentMaxIndex = 0;

        private Matcher(final Set<Integer> usersThatAllowGroupMeetings,
                        final Set<Integer> companiesThatAllowGroupMeetings,
                        final List<Pair> pairs) {
            this.usersThatAllowGroupMeetings = usersThatAllowGroupMeetings;
            this.companiesThatAllowGroupMeetings = companiesThatAllowGroupMeetings;
            this.pairs = pairs;
        }

        private List<Meeting> match(final Map<Integer, Set<Integer>> usersTimeslots,
                                    final Map<Integer, Set<Integer>> companiesTimeslots,
                                    final Map<Integer, Integer> timeslotsRoomsCount) {
            snapshots = initialSnapshots(usersTimeslots, companiesTimeslots, timeslotsRoomsCount);

            findAllPossibleMeetingsConfigurations();

            return BestMeetingsPicker.findBestMatchedMeetings(pairs, snapshots);
        }

        private void findAllPossibleMeetingsConfigurations() {
            while (currentMaxIndex < pairs.size()) {
                final int index = currentMaxIndex;
                List.copyOf(snapshots)
                        .parallelStream()
                        .forEach(snapshot -> matchRecursively(index, snapshot.meetings(), snapshot.usersTimeslots(), snapshot.companiesTimeslots(), snapshot.timeslotsRoomsCount()));

                snapshots = snapshotsWithoutDuplicates();
                currentMaxIndex++;
            }
        }

        private ArrayList<Snapshot> initialSnapshots(final Map<Integer, Set<Integer>> usersTimeslots,
                                                     final Map<Integer, Set<Integer>> companiesTimeslots,
                                                     final Map<Integer, Integer> timeslotsRoomsCount) {
            final var initialSnapshot = new Snapshot(Map.of(), usersTimeslots, companiesTimeslots, timeslotsRoomsCount);
            return new ArrayList<>(List.of(initialSnapshot));
        }

        private List<Snapshot> snapshotsWithoutDuplicates() {
            return snapshots.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
        }

        private void matchRecursively(final int index,
                                      final Map<Integer, Map<Integer, Meeting>> meetings,
                                      final Map<Integer, Set<Integer>> usersTimeslots,
                                      final Map<Integer, Set<Integer>> companiesTimeslots,
                                      final Map<Integer, Integer> timeslotsRoomsCount) {

            if (index < pairs.size()) {
                final var pair = pairs.get(index);
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
                updateSnapshot(index, meetings, usersTimeslots, companiesTimeslots, timeslotsRoomsCount);
            }
            updateSnapshot(index, meetings, usersTimeslots, companiesTimeslots, timeslotsRoomsCount);
        }

        private static Meeting findExistingGroupMeeting(Map<Integer, Meeting> timeslotMeetings, int companyId) {
            if (timeslotMeetings != null) {
                final var meeting = timeslotMeetings.get(companyId);
                return (meeting != null && meeting.allowGroups()) ? meeting : null;
            }
            return null;
        }

        private boolean canAttendGroupMeeting(final int userId, final int companyId) {
            return usersThatAllowGroupMeetings.contains(userId) && companiesThatAllowGroupMeetings.contains(companyId);
        }

        private void updateSnapshot(final int index,
                                    final Map<Integer, Map<Integer, Meeting>> meetings,
                                    final Map<Integer, Set<Integer>> usersTimeslots,
                                    final Map<Integer, Set<Integer>> companiesTimeslots,
                                    final Map<Integer, Integer> timeslotsRoomsCount) {
            lock.lock();

            try {
                if (currentMaxIndex < index) snapshots.clear();

                if (currentMaxIndex <= index) {
                    currentMaxIndex = index;
                    snapshots.add(new Snapshot(meetings, usersTimeslots, companiesTimeslots, timeslotsRoomsCount));
                }
            } finally {
                lock.unlock();
            }
        }
    }
}