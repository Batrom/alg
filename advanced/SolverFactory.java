package advanced;

import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

class SolverFactory {

    static Solver create(final List<User> users, final List<Company> companies, final List<Timeslot> timeslots) {
        final var context = createContext(users, companies);
        final var initialSnapshots = createInitialSnapshots(users, companies, timeslots);
        final var meetingsCreator = new MeetingsCreator(context, initialSnapshots);

        return new Solver(context, meetingsCreator, initialSnapshots);
    }

    private static Snapshots createInitialSnapshots(final List<User> users, final List<Company> companies, final List<Timeslot> timeslots) {
        final var usersTimeslots = mutableUsersTimeslots(users);
        final var companiesTimeslots = mutableCompaniesTimeslots(companies);
        final var roomsHolder = new RoomsHolder(groupRooms(timeslots));
        return Snapshots.initialize(usersTimeslots, companiesTimeslots, roomsHolder);
    }

    private static Map<Long, Set<Long>> mutableCompaniesTimeslots(final List<Company> companies) {
        return companies
                .stream()
                .collect(toMap(Company::id,
                        company -> new HashSet<>(company.timeslots()),
                        (prev, next) -> next,
                        HashMap::new));
    }

    private static Context createContext(final List<User> users, final List<Company> companies) {
        final var pairs = createPairs(users);
        final var groupMeetingGateKeeper = groupMeetingGateKeeper(users, companies);
        final var timeslotsHolder = timeslotsHolder(users, companies);

        return new Context(groupMeetingGateKeeper, timeslotsHolder, pairs);
    }

    private static GroupMeetingGateKeeper groupMeetingGateKeeper(final List<User> users, final List<Company> companies) {
        final var usersThatAllowGroupMeetings = usersThatAllowGroupMeetings(users);
        final var companiesThatAllowGroupMeetings = companiesThatAllowGroupMeetings(companies);
        return new GroupMeetingGateKeeper(usersThatAllowGroupMeetings, companiesThatAllowGroupMeetings);
    }

    private static TimeslotsHolder timeslotsHolder(final List<User> users, final List<Company> companies) {
        final var timeslotsForGroupMeetings = timeslotsForGroupMeetings(users, companies);
        final var timeslotsForSoloMeetings = timeslotsForSoloMeetings(users, companies);

        return new TimeslotsHolder(timeslotsForGroupMeetings, timeslotsForSoloMeetings);
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

    private static Map<Long, Map<Long, List<Long>>> timeslotsForGroupMeetings(final List<User> users, final List<Company> companies) {
        final var companiesThatAllowGroupMeetings = companiesThatAllowGroupMeetings(companies);
        final var companiesTimeslots = companies.stream().collect(toMap(Company::id, Company::timeslots));

        final var map = users.stream()
                .filter(User::allowGroupMeetings)
                .flatMap(user ->
                        user.companies()
                                .stream()
                                .filter(companiesThatAllowGroupMeetings::contains)
                                .flatMap(companyId ->
                                        user.timeslots()
                                                .stream()
                                                .filter(timeslot -> companiesTimeslots.get(companyId).contains(timeslot))
                                                .map(timeslot -> Map.entry(companyId, timeslot))))
                .collect(groupingBy(Map.Entry::getKey, collectingAndThen(groupingBy(Map.Entry::getValue, counting()), Collections::unmodifiableMap)));


        return users.stream()
                .collect(toMap(User::id,
                        user ->
                                user.companies()
                                        .stream()
                                        .collect(toMap(Function.identity(),
                                                companyId -> map.getOrDefault(companyId, Map.of())
                                                        .entrySet()
                                                        .stream()
                                                        .filter(entry -> user.timeslots().contains(entry.getKey()))
                                                        .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                                                        .map(Map.Entry::getKey)
                                                        .toList()))
                ));
    }

    private static Map<Long, Map<Long, List<Long>>> timeslotsForSoloMeetings(final List<User> users, final List<Company> companies) {
        final var companiesThatAllowGroupMeetings = companiesThatAllowGroupMeetings(companies);
        final var companiesTimeslots = companies.stream().collect(toMap(Company::id, Company::timeslots));

        final var map = users.stream()
                .flatMap(user ->
                        user.companies()
                                .stream()
                                .filter(company -> !user.allowGroupMeetings() || !companiesThatAllowGroupMeetings.contains(company))
                                .flatMap(companyId ->
                                        user.timeslots()
                                                .stream()
                                                .filter(timeslot -> companiesTimeslots.get(companyId).contains(timeslot))
                                                .map(timeslot -> Map.entry(companyId, timeslot))))
                .collect(groupingBy(Map.Entry::getKey, collectingAndThen(groupingBy(Map.Entry::getValue, counting()), Collections::unmodifiableMap)));

        return users.stream()
                .collect(toMap(User::id,
                        user ->
                                user.companies()
                                        .stream()
                                        .collect(toMap(Function.identity(),
                                                companyId -> map.getOrDefault(companyId, Map.of())
                                                        .entrySet()
                                                        .stream()
                                                        .filter(entry -> user.timeslots().contains(entry.getKey()))
                                                        .sorted(Map.Entry.comparingByValue())
                                                        .map(Map.Entry::getKey)
                                                        .toList()))
                ));
    }

    private static Map<Long, Deque<Room>> groupRooms(final List<Timeslot> timeslots) {
        return timeslots.stream()
                .collect(toMap(Timeslot::id,
                        timeslot ->
                                timeslot.rooms()
                                        .stream()
                                        .sorted(comparingInt(Room::capacity))
                                        .collect(toCollection(LinkedList::new))));
    }
}
