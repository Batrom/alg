package advanced;

import java.util.Set;

record Company(long id, Set<Long> timeslots, boolean allowGroupMeetings) {
}