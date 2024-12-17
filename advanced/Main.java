package advanced;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {
        final var numberOfUsers = 200;
        final var numberOfTimeslotsPerUser = 24;
        final var numberOfCompaniesPerUser = 10;

        final var numberOfCompanies = 100;
        final var numberOfTimeslotsPerCompany = 24;

        final var numberOfTimeslots = 24;
        final var numberOfRoomsPerTimeslot = 10;
        final var roomsMaxCapacity = 100;

        final var users = IntStream.range(1, numberOfUsers + 1)
                .mapToObj(index ->
                        new User(index,
                                index,
                                randomSet(numberOfTimeslotsPerUser, numberOfTimeslots),
                                randomList(numberOfCompaniesPerUser, numberOfCompanies),
                                randomBoolean()))
                .toList();

        final var companies = IntStream.range(1, numberOfCompanies + 1)
                .mapToObj(index ->
                        new Company(index,
                                randomSet(numberOfTimeslotsPerCompany, numberOfTimeslots),
                                randomBoolean()))
                .toList();

        final var timeslots = IntStream.range(1, numberOfTimeslots + 1)
                .mapToObj(index -> new Timeslot(index, randomRooms(numberOfRoomsPerTimeslot, roomsMaxCapacity)))
                .toList();

//        final var users = List.of(
//                new User(1, 1, Set.of(1L, 2L), List.of(3L, 2L), false));
//        final var companies = List.of(
//                new Company(2, Set.of(1L), true),
//                new Company(3, Set.of(1L), true));
//        final var timeslots = List.of(
//                new Timeslot(1, List.of(new Room(2, 1))),
//                new Timeslot(2, List.of(new Room(3, 1))));

//        final var users = List.of(
//                new User(1, 1, Set.of(1L, 2L), List.of(3L, 2L, 1L), false),
//                new User(2, 2, Set.of(2L), List.of(1L, 3L, 2L), true),
//                new User(3, 3, Set.of(2L), List.of(1L, 3L), false));
//        final var companies = List.of(
//                new Company(1, Set.of(2L), false),
//                new Company(2, Set.of(1L), true),
//                new Company(3, Set.of(1L), true));
//        final var timeslots = List.of(
//                new Timeslot(1, List.of(new Room(1, 5), new Room(2, 1))),
//                new Timeslot(2, List.of(new Room(3, 1), new Room(4, 3))));

        final var meetings = SolverFactory.create(users, companies, timeslots).createMeetings();
        System.out.println("asd");

    }

    private static Set<Long> randomSet(final int size, final int max) {
        return new Random().longs(size, 1, max + 1).boxed().collect(Collectors.toSet());
    }

    private static List<Long> randomList(final int size, final int max) {
        return new Random().longs(size, 1, max + 1).boxed().distinct().toList();
    }

    private static boolean randomBoolean() {
        return new Random().nextBoolean();
    }

    private static List<Room> randomRooms(final int count, final int maxCapacity) {
        final var random = new Random();
        return random.ints(count, 1, maxCapacity + 1)
                .mapToObj(capacity -> new Room(random.nextInt(0, 1_000_000), capacity))
                .toList();
    }
}
