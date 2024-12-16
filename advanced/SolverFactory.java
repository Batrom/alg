package advanced;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

class SolverFactory {

    static Solver create(final List<User> users, final List<Company> companies, final List<Timeslot> timeslots) {
        final var context = createContext(users, companies, timeslots);
        final var initialSnapshots = createInitialSnapshots(users, companies, timeslots);
        final var meetingsCreator = new MeetingsCreator(context, initialSnapshots);

        return new Solver(context, meetingsCreator, initialSnapshots);
    }

    private static Snapshots createInitialSnapshots(final List<User> users, final List<Company> companies, final List<Timeslot> timeslots) {
        final var usersTimeslots = mutableUsersTimeslots(users);
        final var companiesTimeslots = mutableCompaniesTimeslots(companies);
        final var timeslotsRooms = mutableTimeslotsRooms(timeslots);
        return Snapshots.initialize(usersTimeslots, companiesTimeslots, timeslotsRooms);
    }

    private static Map<Long, Map<Integer, Integer>> mutableTimeslotsRooms(final List<Timeslot> timeslots) {
        return timeslots.stream()
                .collect(toMap(Timeslot::id,
                        timeslot -> timeslot.rooms()
                                .stream()
                                .collect(groupingBy(Room::capacity, HashMap::new, countRooms())),
                        (prev, next) -> next,
                        HashMap::new));
    }

    private static Map<Long, Set<Long>> mutableCompaniesTimeslots(final List<Company> companies) {
        return companies
                .stream()
                .collect(toMap(Company::id,
                        company -> new HashSet<>(company.timeslots()),
                        (prev, next) -> next,
                        HashMap::new));
    }

    private static Context createContext(final List<User> users, final List<Company> companies, final List<Timeslot> timeslots) {
        final var pairs = createPairs(users);
        final var usersThatAllowGroupMeetings = usersThatAllowGroupMeetings(users);
        final var companiesThatAllowGroupMeetings = companiesThatAllowGroupMeetings(companies);
        final var usersTimeslots = usersTimeslots(users);
        return new Context(usersThatAllowGroupMeetings, companiesThatAllowGroupMeetings, usersTimeslots, timeslots, pairs);
    }

    private static Map<Long, Set<Long>> usersTimeslots(final List<User> users) {
        return users.stream().collect(toMap(User::id, User::timeslots));
    }

    private static Map<Long, Set<Long>> mutableUsersTimeslots(final List<User> users) {
        return users.stream().collect(toMap(User::id,
                user -> new HashSet<>(user.timeslots()),
                (prev, next) -> next,
                HashMap::new));
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
                .flatMap(SolverFactory::toPairs)
                .toList();
    }

    private static Stream<Pair> toPairs(final User user) {
        return user.companies().stream().map(companyId -> new Pair(user.id(), companyId));
    }

    private static Collector<Room, ?, Integer> countRooms() {
        return summingInt(room -> 1);
    }
}
