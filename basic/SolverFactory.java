package basic;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

class SolverFactory {

    static Solver create(final List<User> users, final List<Company> companies, final List<Timeslot> timeslots) {
        final var roomsHolder = new RoomsHolder(groupRooms(timeslots));

        final var usersThatAllowGroupMeetings = usersThatAllowGroupMeetings(users);
        final var companiesThatAllowGroupMeetings = companiesThatAllowGroupMeetings(companies);

        final var groupMeetings = emptyMeetings(timeslots);
        final var soloMeetings = emptyMeetings(timeslots);

        final var usersAvailableTimeslots = usersAvailableTimeslots(users);
        final var companiesAvailableTimeslots = companiesAvailableTimeslots(companies);

        final var timeslotsForGroupMeetings = timeslotsForGroupMeetings(users, companies);
        final var timeslotsForSoloMeetings = timeslotsForSoloMeetings(users, companies);

        final var timeslotsHolder = new TimeslotsHolder(timeslotsForGroupMeetings, timeslotsForSoloMeetings);

        final var meetingsHolder = new MeetingsHolder(roomsHolder, timeslotsHolder, soloMeetings, groupMeetings, usersAvailableTimeslots, companiesAvailableTimeslots, usersThatAllowGroupMeetings, companiesThatAllowGroupMeetings);

        return new Solver(meetingsHolder, users);
    }

    private static Map<Long, Set<Long>> companiesAvailableTimeslots(final List<Company> companies) {
        return companies.stream().collect(toMap(Company::id, company -> new HashSet<>(company.timeslots())));
    }

    private static Map<Long, Set<Long>> usersAvailableTimeslots(final List<User> users) {
        return users.stream().collect(toMap(User::id, user -> new HashSet<>(user.timeslots())));
    }

    private static Map<Long, Map<Long, Meeting>> emptyMeetings(final List<Timeslot> timeslots) {
        return timeslots.stream().collect(toMap(Timeslot::id, timeslot -> new HashMap<>()));
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

    private static Set<Long> companiesThatAllowGroupMeetings(final List<Company> companies) {
        return companies.stream().filter(Company::allowGroupMeetings).map(Company::id).collect(toSet());
    }

    private static Set<Long> usersThatAllowGroupMeetings(final List<User> users) {
        return users.stream().filter(User::allowGroupMeetings).map(User::id).collect(toSet());
    }
}


/*
* - solo meetings -> take the smallest free room
- create ranking of timeslots for user (given a timeslot and company pick one timeslot)


group meetings:


for each user timeslot and company return usersCount and sort by usersCount ascending -> take first one -> minimalize the chance of blocking





Map<timeslot, Map<company, usersCount>>
if group meeting then take the most occupied timeslot and company that is still free
if group meeting take the biggest free room


Map<timeslot, Map<company, usersCount>>
if solo meeting then take the least occupied timeslot and company that is still free
if solo meeting take the smallest free room


* */