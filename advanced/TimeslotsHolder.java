package advanced;

import java.util.List;
import java.util.Map;

record TimeslotsHolder(Map<Long, Map<Long, List<Long>>> timeslotsForGroupMeetings,
                       Map<Long, Map<Long, List<Long>>> timeslotsForSoloMeetings) {

    List<Long> groupMeetingsTimeslots(final Long userId, final Long companyId) {
        return timeslotsForGroupMeetings.get(userId).get(companyId);
    }

    List<Long> soloMeetingsTimeslots(final Long userId, final Long companyId) {
        return timeslotsForSoloMeetings.get(userId).get(companyId);
    }
}