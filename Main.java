import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
//        final var users = List.of(
//                new User(1, 1, Set.of(1), List.of(1), false),
//                new User(2, 2, Set.of(1, 2), List.of(1, 2), true));
//        final var companies = List.of(
//                new Company(1, Set.of(1), true),
//                new Company(2, Set.of(1, 2), true));
//        final var timeslots = List.of(
//                new Timeslot(1, 5),
//                new Timeslot(2, 5),
//                new Timeslot(3, 5));

//        List<User> users = new ArrayList<>();
//        List<Company> companies = new ArrayList<>();
//        List<Timeslot> timeslots = new ArrayList<>();
//
//        for (int i = 0; i < 100; i++) {
//            users.add(new User(i, i, Set.of(i), List.of(i), true));
//            companies.add(new Company(i, Set.of(i), true));
//            timeslots.add(new Timeslot(i, 100_000));
//        }



        Random rand = new Random();
        List<User> users = new ArrayList<>();
        final var usersCount = 1000;
        final var companiesCount = 100;
        final var timeslotsCount = 100;
        final var roomsCount = 10;
        for (int i = 1; i <= usersCount; i++) {
            long order = i;
            Set<Long> timeslots = new HashSet<>();
            for (int j = 1; j <= rand.nextInt(10) + 1; j++) {
                timeslots.add(rand.nextInt(timeslotsCount) + 1);
            }
            List<Long> companies = new ArrayList<>();
            for (int j = 1; j <= rand.nextInt(10) + 1; j++) {
                companies.add(rand.nextInt(companiesCount) + 1);
            }
            boolean allowGroupMeetings = rand.nextBoolean();
            users.add(new User(i, order, timeslots, companies, true));
        }

        // Generate 100 Companies
        List<Company> companies = new ArrayList<>();
        for (int i = 1; i <= companiesCount; i++) {
            Set<Long> timeslots = new HashSet<>();
            for (int j = 1; j <= rand.nextInt(10) + 1; j++) {
                timeslots.add(rand.nextInt(timeslotsCount) + 1); // Random timeslot between 1 and 5
            }
            boolean allowGroupMeetings = rand.nextBoolean();
            companies.add(new Company(i, timeslots, true));
        }

        // Generate Timeslots
        List<Timeslot> timeslots = new ArrayList<>();
        for (int i = 1; i <= timeslotsCount; i++) {
            timeslots.add(new Timeslot(i, roomsCount));
        }

        final var start = Instant.now();

        final var match = MatcherFactory.create(users, companies, timeslots).match();

        final var end = Instant.now();

        System.out.println(Duration.between(start, end).toSeconds());
        System.out.println(match);
    }
}
