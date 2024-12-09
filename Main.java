import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        final var users = List.of(
                new User(1, 1, Set.of(1), List.of(1), false),
                new User(2, 2, Set.of(1, 2), List.of(1, 2), true));
        final var companies = List.of(
                new Company(1, Set.of(1), true),
                new Company(2, Set.of(1, 2), true));
        final var timeslots = List.of(
                new Timeslot(1, 5),
                new Timeslot(2, 5),
                new Timeslot(3, 5));

        final var solve = Solver.solve(users, companies, timeslots);
        final var solve2 = ParallelSolver.solve(users, companies, timeslots);
        System.out.println(solve);
    }

    record User(int id, int order, Set<Integer> timeslots, List<Integer> companies, boolean allowGroupMeetings) {
    }

    record Company(int id, Set<Integer> timeslots, boolean allowGroupMeetings) {
    }

    record Timeslot(int id, int roomsCount) {
    }
}