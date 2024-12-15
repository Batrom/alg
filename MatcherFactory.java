import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

class MatcherFactory {

    static Solver create(final List<User> users, final List<Company> companies, final List<Timeslot> timeslots) {
        final var context = createContext(users, companies);
        final var initialSnapshots = createInitialSnapshots(users, companies, timeslots);
        final var bestMeetingsPicker = new BestMeetingsPicker(context, initialSnapshots);

        return new Solver(context, bestMeetingsPicker, initialSnapshots);
    }

    private static Snapshots createInitialSnapshots(final List<User> users, final List<Company> companies, final List<Timeslot> timeslots) {
        final var usersTimeslots = usersTimeslots(users);
        final var companiesTimeslots = companies.stream().collect(toMap(Company::id, Company::timeslots));
        final var timeslotsRooms = timeslots.stream().collect(toMap(Timeslot::id, timeslot -> timeslot.rooms().stream().collect(groupingBy(Room::capacity, countRooms()))));
        return Snapshots.initialize(usersTimeslots, companiesTimeslots, timeslotsRooms);
    }

    private static Context createContext(final List<User> users, final List<Company> companies) {
        final var pairs = createPairs(users);
        final var usersThatAllowGroupMeetings = usersThatAllowGroupMeetings(users);
        final var companiesThatAllowGroupMeetings = companiesThatAllowGroupMeetings(companies);
        final var usersTimeslots = usersTimeslots(users);
        return new Context(usersThatAllowGroupMeetings, companiesThatAllowGroupMeetings, usersTimeslots, pairs);
    }

    private static Map<Long, Set<Long>> usersTimeslots(final List<User> users) {
        return users.stream().collect(toMap(User::id, User::timeslots));
    }

    private static Set<Long> companiesThatAllowGroupMeetings(final List<Company> companies) {
        return companies.stream().filter(Company::allowGroupMeetings).map(Company::id).collect(toSet());
    }

    private static Set<Long> usersThatAllowGroupMeetings(final List<User> users) {
        return users.stream().filter(User::allowGroupMeetings).map(User::id).collect(toSet());
    }

    private static List<Pair> createPairs(final List<User> users) {
        return users.stream()
                .sorted(Comparator.comparingInt(User::order))
                .flatMap(MatcherFactory::toPairs)
                .toList();
    }

    private static Stream<Pair> toPairs(final User user) {
        return user.companies().stream().map(companyId -> new Pair(user.id(), companyId));
    }

    private static Collector<Room, ?, Integer> countRooms() {
        return summingInt(room -> 1);
    }
}
