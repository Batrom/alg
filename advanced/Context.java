package advanced;

import java.util.List;
import java.util.Map;
import java.util.Set;

record Context(Set<Long> usersThatAllowGroupMeetings,
               Set<Long> companiesThatAllowGroupMeetings,
               Map<Long, Set<Long>> usersTimeslots,
               List<Timeslot> timeslots,
               List<Pair> pairs) {
}
