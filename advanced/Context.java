package advanced;

import java.util.List;

record Context(GroupMeetingGateKeeper groupMeetingGateKeeper,
               TimeslotsHolder timeslotsHolder,
               List<Pair> pairs) {
}
