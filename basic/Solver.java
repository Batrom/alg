package basic;

import java.util.List;

class Solver {
    private final MeetingsHolder meetingsHolder;
    private final List<User> users;

    Solver(final MeetingsHolder meetingsHolder, final List<User> users) {
        this.meetingsHolder = meetingsHolder;
        this.users = users;
    }

    List<Meeting> solve() {
        for (final var user : users) {
            for (final var companyId : user.companies()) {
                final var added = meetingsHolder.addUserToExistingMeeting(user.id(), companyId);
                if (!added) meetingsHolder.createMeeting(user.id(), companyId);
            }
        }
        return meetingsHolder.meetings();
    }
}
