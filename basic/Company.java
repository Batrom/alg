package basic;

import java.util.Set;

record Company(long id, Set<Long> timeslots, boolean allowGroupMeetings) {
}