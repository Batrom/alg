import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {
        final var numberOfUsers = 10;
        final var numberOfTimeslotsPerUser = 2;
        final var numberOfCompaniesPerUser = 10;

        final var numberOfCompanies = 10;
        final var numberOfTimeslotsPerCompany = 2;

        final var numberOfTimeslots = 2;
        final var numberOfRoomsPerTimeslot = 5;
        final var roomsMaxCapacity = 5;

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
//                new User(1, 1, Set.of(1L), List.of(1L), true),
//                new User(2, 2, Set.of(1L, 2L), List.of(1L, 2L), true));
//        final var companies = List.of(
//                new Company(1, Set.of(1L), true),
//                new Company(2, Set.of(1L, 2L), true));
//        final var timeslots = List.of(
//                new Timeslot(1, List.of(new Room(1, 5))),
//                new Timeslot(2, List.of(new Room(1, 5))),
//                new Timeslot(3, List.of(new Room(1, 5))));

        final var meetings = SolverFactory.create(users, companies, timeslots).createMeetings();
        System.out.println(meetings);

//        List<User> users = new ArrayList<>();
//        List<Company> companies = new ArrayList<>();
//        List<Timeslot> timeslots = new ArrayList<>();
//
//        for (int i = 0; i < 100; i++) {
//            users.add(new User(i, i, Set.of(i), List.of(i), true));
//            companies.add(new Company(i, Set.of(i), true));
//            timeslots.add(new Timeslot(i, 100_000));
//        }
//
//
//        Random rand = new Random();
//        List<User> users = new ArrayList<>();
//        final var usersCount = 1000;
//        final var companiesCount = 100;
//        final var timeslotsCount = 100;
//        final var roomsCount = 10;
//        for (int i = 1; i <= usersCount; i++) {
//            long order = i;
//            Set<Long> timeslots = new HashSet<>();
//            for (int j = 1; j <= rand.nextInt(10) + 1; j++) {
//                timeslots.add(rand.nextInt(timeslotsCount) + 1);
//            }
//            List<Long> companies = new ArrayList<>();
//            for (int j = 1; j <= rand.nextInt(10) + 1; j++) {
//                companies.add(rand.nextInt(companiesCount) + 1);
//            }
//            boolean allowGroupMeetings = rand.nextBoolean();
//            users.add(new User(i, order, timeslots, companies, true));
//        }
//
//        // Generate 100 Companies
//        List<Company> companies = new ArrayList<>();
//        for (int i = 1; i <= companiesCount; i++) {
//            Set<Long> timeslots = new HashSet<>();
//            for (int j = 1; j <= rand.nextInt(10) + 1; j++) {
//                timeslots.add(rand.nextInt(timeslotsCount) + 1); // Random timeslot between 1 and 5
//            }
//            boolean allowGroupMeetings = rand.nextBoolean();
//            companies.add(new Company(i, timeslots, true));
//        }
//
//        // Generate Timeslots
//        List<Timeslot> timeslots = new ArrayList<>();
//        for (int i = 1; i <= timeslotsCount; i++) {
//            timeslots.add(new Timeslot(i, roomsCount));
//        }
//
//        final var start = Instant.now();
//
//        final var match = MatcherFactory.create(users, companies, timeslots).match();
//
//        final var end = Instant.now();
//
//        System.out.println(Duration.between(start, end).toSeconds());
//        System.out.println(match);
    }

    private static Set<Long> randomSet(final int size, final int max) {
        return new Random().longs(size, 1, max + 1).boxed().collect(Collectors.toSet());
    }

    private static List<Long> randomList(final int size, final int max) {
        return new Random().longs(size, 1, max + 1).boxed().toList();
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
