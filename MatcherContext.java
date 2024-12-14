import java.util.List;
import java.util.Set;

record MatcherContext(Set<Integer> usersThatAllowGroupMeetings,
                      Set<Integer> companiesThatAllowGroupMeetings,
                      List<Pair> pairs) {
}
