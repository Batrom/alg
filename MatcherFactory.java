import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

class MatcherFactory {

    static ParallelMatcher create(final List<Main.User> users, final List<Main.Company> companies, final List<Main.Timeslot> timeslots) {
        final var matcherContext = createMatcherContext(users, companies);
        final var snapshotsTracker = createSnapshotsTracker(users, companies, timeslots);
        final var bestMeetingsPicker = new BestMeetingsPicker(matcherContext, snapshotsTracker);

        return new ParallelMatcher(snapshotsTracker, matcherContext, bestMeetingsPicker);
    }

    private static SnapshotsTracker createSnapshotsTracker(final List<Main.User> users, final List<Main.Company> companies, final List<Main.Timeslot> timeslots) {
        final var usersTimeslots = users.stream().collect(toMap(Main.User::id, Main.User::timeslots));
        final var companiesTimeslots = companies.stream().collect(toMap(Main.Company::id, Main.Company::timeslots));
        final var timeslotsRoomsCount = timeslots.stream().collect(toMap(Main.Timeslot::id, Main.Timeslot::roomsCount));
        return SnapshotsTracker.initialize(usersTimeslots, companiesTimeslots, timeslotsRoomsCount);
    }

    private static MatcherContext createMatcherContext(final List<Main.User> users, final List<Main.Company> companies) {
        final var pairs = createPairs(users);
        final var usersThatAllowGroupMeetings = users.stream().filter(Main.User::allowGroupMeetings).map(Main.User::id).collect(toSet());
        final var companiesThatAllowGroupMeetings = companies.stream().filter(Main.Company::allowGroupMeetings).map(Main.Company::id).collect(toSet());
        return new MatcherContext(usersThatAllowGroupMeetings, companiesThatAllowGroupMeetings, pairs);
    }

    private static List<Pair> createPairs(final List<Main.User> users) {
        return users.stream()
                .sorted(Comparator.comparing(Main.User::order))
                .flatMap(user -> user.companies().stream().map(companyId -> new Pair(user.id(), companyId)))
                .toList();
    }
}
