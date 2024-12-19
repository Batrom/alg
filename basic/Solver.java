package basic;

import java.util.List;

class Solver {
    private final MeetingsMatcher meetingsMatcher;
    private final List<User> users;

    Solver(final MeetingsMatcher meetingsMatcher, final List<User> users) {
        this.meetingsMatcher = meetingsMatcher;
        this.users = users;
    }

    List<Meeting> solve() {
        for (final var user : users) {
            for (final var companyId : user.companies()) {
                meetingsMatcher.match(user.id(), companyId);
            }
        }
        return meetingsMatcher.meetings();
    }
}
