package advanced;

import java.util.List;
import java.util.Map;
import java.util.Set;

record Context(Set<Long> usersThatAllowGroupMeetings,
               Set<Long> companiesThatAllowGroupMeetings,
               TimeslotsHolder timeslotsHolder,
               List<Pair> pairs) {
}
