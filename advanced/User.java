package advanced;

import java.util.List;
import java.util.Set;

record User(long id, int order, Set<Long> timeslots, List<Long> companies, boolean allowGroupMeetings) {
}